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
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

/**
 * Keeps native living metadata/rotations/name and leash submission, while the complete animated body
 * is captured by the mod's CPU poser during extraction. The native model owns no geometry: submitting
 * it would duplicate the captured body. Neither a live entity nor a mutable poser enters the queue.
 */
public abstract class CapturedLivingEntityRenderer<T extends LivingEntity>
        extends LivingEntityRenderer<T, CapturedLivingEntityRenderer.State, EntityModel<CapturedLivingEntityRenderer.State>> {
    public static class State extends LivingEntityRenderState {
        public Identifier texture;
        public float modelScale = 1F;
        public CapturedLabel label;
        public float extrasScale = 1F;
        public float extrasOffsetY;
        private ModelBufferSource.Snapshot geometry = ModelBufferSource.Snapshot.EMPTY;
    }
    protected CapturedLivingEntityRenderer(EntityRendererProvider.Context context, float shadow) {
        super(context, new EntityModel<State>(new ModelPart(List.of(), Map.of())) {}, shadow);
    }
    @Override public final State createRenderState() { return new State(); }
    @Override public final Identifier getTextureLocation(State state) { return state.texture; }
    @Override protected final RenderType getRenderType(State state, boolean visible, boolean translucent, boolean glowing) {
        // All body geometry and layers are already frozen in state.geometry.
        return null;
    }
    @Override protected final boolean shouldRenderLayers(State state) { return false; }
    @Override protected final void scale(State state, PoseStack pose) {
        pose.scale(state.modelScale, state.modelScale, state.modelScale);
    }
    @Override public final void extractRenderState(T entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.geometry = ModelBufferSource.Snapshot.EMPTY;
        state.texture = null;
        state.label = null;
        state.modelScale = 1F;
        state.extrasScale = 1F;
        state.extrasOffsetY = 0F;
        var output = new ModelBufferSource();
        capture(entity, state, partialTick, new PoseStack(), output);
        state.geometry = output.snapshot();
    }
    @Override public final void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        state.geometry.submitWithOutline(pose, collector, state.outlineColor);
        pose.pushPose();
        try {
            pose.translate(0F, state.extrasOffsetY, 0F);
            pose.scale(state.extrasScale, state.extrasScale, state.extrasScale);
            super.submit(state, pose, collector, camera);
        } finally { pose.popPose(); }
        if (state.label != null) state.label.submit(pose, collector, camera);
    }
    protected abstract void capture(T entity, State state, float partialTick, PoseStack pose, ModelBufferSource output);

    /** Same transform order as native LivingEntityRenderer.submit; caller supplies its model-specific scale. */
    protected final void transformBody(State state, PoseStack pose) {
        Direction bed = state.bedOrientation;
        if (state.hasPose(Pose.SLEEPING) && bed != null) {
            float offset = state.eyeHeight - 0.1F;
            pose.translate(-bed.getStepX() * offset, 0F, -bed.getStepZ() * offset);
        }
        pose.scale(state.scale, state.scale, state.scale);
        setupRotations(state, pose, state.bodyRot, state.scale);
        pose.scale(-1F, -1F, 1F);
        scale(state, pose);
        pose.translate(0F, -1.501F, 0F);
    }
    protected final RenderType bodyRenderType(State state, RenderType normal) {
        if (!state.isInvisible) return normal;
        if (!state.isInvisibleToPlayer) return RenderTypes.entityTranslucentCull(state.texture);
        return state.appearsGlowing() ? RenderTypes.outline(state.texture) : null;
    }
    protected final int bodyColor(State state) {
        return state.isInvisible && !state.isInvisibleToPlayer ? 0x26FFFFFF : -1;
    }
}
