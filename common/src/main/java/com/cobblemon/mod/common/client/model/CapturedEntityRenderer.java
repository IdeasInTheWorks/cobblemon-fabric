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
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/** Native entity entrypoint, retaining native name/leash/shadow extraction and frozen model geometry. */
public abstract class CapturedEntityRenderer<T extends Entity> extends EntityRenderer<T, CapturedEntityRenderer.State> {
    public static final class State extends EntityRenderState {
        private ModelBufferSource.Snapshot geometry = ModelBufferSource.Snapshot.EMPTY;
    }
    protected CapturedEntityRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public final State createRenderState() { return new State(); }
    @Override public final void extractRenderState(T entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.lightCoords = NativeSceneCapture.light(state.lightCoords);
        state.geometry = ModelBufferSource.Snapshot.EMPTY;
        var buffers = new ModelBufferSource();
        capture(entity, Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot()), partialTick,
                new PoseStack(), buffers, state.lightCoords);
        state.geometry = buffers.snapshot();
    }
    @Override public final void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        state.geometry.submit(pose, collector);
        super.submit(state, pose, collector, camera);
    }
    protected abstract void capture(T entity, float yaw, float partialTick, PoseStack pose, ModelBufferSource buffers, int light);
}
