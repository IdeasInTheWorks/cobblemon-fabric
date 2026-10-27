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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/** Native extraction/submission boundary for the mod's CPU geometry renderers. */
public abstract class CapturedBlockEntityRenderer<T extends BlockEntity>
        implements BlockEntityRenderer<T, CapturedBlockEntityRenderer.State> {
    public static final class State extends BlockEntityRenderState {
        private boolean improvedTransparency;
        private ModelBufferSource.Snapshot geometry = ModelBufferSource.Snapshot.EMPTY;
    }
    @Override public final State createRenderState() { return new State(); }
    @Override public final void extractRenderState(T entity, State state, float partialTick, Vec3 camera,
                                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(entity, state, crumbling);
        state.improvedTransparency = net.minecraft.client.Minecraft.getInstance().gameRenderer.useImprovedTransparency();
        state.geometry = ModelBufferSource.Snapshot.EMPTY;
        var buffers = new ModelBufferSource();
        capture(entity, partialTick, new PoseStack(), buffers, state.lightCoords, OverlayTexture.NO_OVERLAY);
        state.geometry = buffers.snapshot();
    }
    @Override public final void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        state.geometry.submit(pose, collector, state.breakProgress, null, state.improvedTransparency);
    }
    protected abstract void capture(T entity, float partialTick, PoseStack pose, ModelBufferSource buffers, int light, int overlay);
}
