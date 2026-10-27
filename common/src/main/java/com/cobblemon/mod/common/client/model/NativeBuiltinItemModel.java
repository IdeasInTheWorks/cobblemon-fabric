/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.client.model;

import java.util.function.Consumer;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/** Native item-model entrypoint: capture mutable model/stack data during update, then submit a snapshot. */
public final class NativeBuiltinItemModel {
    @FunctionalInterface public interface Capture {
        ModelBufferSource.Snapshot capture(ItemStack stack, ItemDisplayContext display);
    }
    public static ItemModel model(Capture capture) {
        return model(capture, new org.joml.Matrix4f());
    }
    public static ItemModel model(Capture capture, org.joml.Matrix4fc transform) {
        var frozenTransform = new org.joml.Matrix4f(transform);
        return (output, stack, resolver, display, level, owner, seed) -> {
            var snapshot = capture.capture(stack.copy(), display);
            if (snapshot.batches().isEmpty() && snapshot.items().isEmpty()) return;
            output.appendModelIdentityElement(snapshot);
            output.setAnimated();
            var layer = output.newLayer();
            layer.setLocalTransform(frozenTransform);
            layer.setUsesBlockLight(display != ItemDisplayContext.GUI);
            layer.setExtents(() -> snapshot.batches().stream().flatMap(batch -> batch.vertices().stream())
                    .map(vertex -> (Vector3fc)new Vector3f(vertex.x(), vertex.y(), vertex.z())).toArray(Vector3fc[]::new));
            layer.setupSpecialModel(RENDERER, snapshot);
        };
    }
    private static final SpecialModelRenderer<ModelBufferSource.Snapshot> RENDERER = new SpecialModelRenderer<>() {
        public void submit(ModelBufferSource.Snapshot data, com.mojang.blaze3d.vertex.PoseStack pose,
                           net.minecraft.client.renderer.SubmitNodeCollector collector, int light, int overlay,
                           boolean foil, int outline) {
            data.submit(pose, collector, null, light);
        }
        public void getExtents(Consumer<Vector3fc> consumer) {
            consumer.accept(new Vector3f(-1, -1, -1)); consumer.accept(new Vector3f(1, 1, 1));
        }
        public ModelBufferSource.Snapshot extractArgument(ItemStack stack) {
            throw new UnsupportedOperationException("Use the display-aware item-model update entrypoint");
        }
    };
    private NativeBuiltinItemModel() { }
}
