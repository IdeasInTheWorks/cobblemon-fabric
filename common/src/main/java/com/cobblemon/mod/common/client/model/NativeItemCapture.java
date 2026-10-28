/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

/** Extract native item states now; deferred submission never looks up the live stack or level. */
public final class NativeItemCapture {
    private record Request(ItemStack stack, net.minecraft.resources.Identifier model) {}
    private static final ThreadLocal<Request> MODEL = new ThreadLocal<>();
    public static net.minecraft.resources.Identifier requestedModel(ItemStack stack) {
        var request = MODEL.get(); return request != null && request.stack() == stack ? request.model() : null;
    }
    public static void withModel(ItemStack stack, net.minecraft.resources.Identifier model, Runnable action) {
        var previous = MODEL.get(); MODEL.set(new Request(stack, model));
        try { action.run(); } finally { if (previous == null) MODEL.remove(); else MODEL.set(previous); }
    }
    public static void captureExplicit(ItemStack stack, net.minecraft.resources.Identifier model, ItemDisplayContext display,
            int light, int overlay, PoseStack pose, ModelBufferSource output, Level level, int seed) {
        var copy = stack.copy(); var state = new ItemStackRenderState();
        withModel(copy, LegacyItemModels.itemId(model), () -> Minecraft.getInstance().getItemModelResolver()
                .updateForTopItem(state, copy, display, level, null, seed));
        add(state, light, overlay, pose, output, false);
    }
    public static void captureLiving(ItemStack stack, ItemDisplayContext display, int light, int overlay,
            PoseStack pose, ModelBufferSource output, net.minecraft.world.entity.LivingEntity entity, int seed, boolean frontLight) {
        var state = new ItemStackRenderState();
        // Native living update reads the original slot identity for use/hand predicates during extraction only.
        // The queued payload is the fresh native item state; it never retains the owner or a lookup callback.
        if (entity != null) Minecraft.getInstance().getItemModelResolver().updateForLiving(state, stack, display, entity);
        else Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, stack.copy(), display,
                Minecraft.getInstance().level, null, seed);
        add(state, light, overlay, pose, output, frontLight);
    }
    private static void add(ItemStackRenderState state, int light, int overlay, PoseStack pose, ModelBufferSource output, boolean frontLight) {
        output.addItem(new Submission(state, new Matrix4f(pose.last().pose()), light, overlay, frontLight));
    }

    public static void capture(ItemStack stack, ItemDisplayContext display, int light, int overlay,
                               PoseStack pose, ModelBufferSource output, Level level, int seed) {
        var state = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, stack.copy(), display, level, null, seed);
        add(state, light, overlay, pose, output, false);
    }

    public static final class Submission {
        private final ItemStackRenderState state;
        private final Matrix4fc transform;
        private final int light, overlay;
        private final boolean frontLight;
        private Submission(ItemStackRenderState state, Matrix4fc transform, int light, int overlay, boolean frontLight) {
            this.state = state; this.transform = transform; this.light = light; this.overlay = overlay;
            this.frontLight = frontLight;
        }
        public void submit(PoseStack pose, OrderedSubmitNodeCollector collector) {
            submit(pose, collector, 0);
        }
        public void submit(PoseStack pose, OrderedSubmitNodeCollector collector, int outline) {
            pose.pushPose();
            try {
                pose.mulPose(transform);
                // ItemStackRenderState requires the native collector (all production callers supply it).
                if (frontLight) {
                    var storage = new net.minecraft.client.renderer.SubmitNodeStorage();
                    storage.setUseImprovedTransparency(FrontLitFeatures.improvedTransparency((SubmitNodeCollector)collector));
                    state.submit(pose, storage, light, overlay, outline);
                    FrontLitFeatures.transfer(storage, (SubmitNodeCollector)collector);
                } else state.submit(pose, (SubmitNodeCollector)collector, light, overlay, outline);
            } finally { pose.popPose(); }
        }
    }
    private NativeItemCapture() { }
}
