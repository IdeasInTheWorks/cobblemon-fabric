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
    public static void capture(ItemStack stack, ItemDisplayContext display, int light, int overlay,
                               PoseStack pose, ModelBufferSource output, Level level, int seed) {
        var state = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, stack.copy(), display, level, null, seed);
        output.addItem(new Submission(state, new Matrix4f(pose.last().pose()), light, overlay));
    }

    public static final class Submission {
        private final ItemStackRenderState state;
        private final Matrix4fc transform;
        private final int light, overlay;
        private Submission(ItemStackRenderState state, Matrix4fc transform, int light, int overlay) {
            this.state = state; this.transform = transform; this.light = light; this.overlay = overlay;
        }
        public void submit(PoseStack pose, OrderedSubmitNodeCollector collector) {
            pose.pushPose();
            try {
                pose.mulPose(transform);
                // ItemStackRenderState requires the native collector (all production callers supply it).
                state.submit(pose, (SubmitNodeCollector)collector, light, overlay, 0);
            } finally { pose.popPose(); }
        }
    }
    private NativeItemCapture() { }
}
