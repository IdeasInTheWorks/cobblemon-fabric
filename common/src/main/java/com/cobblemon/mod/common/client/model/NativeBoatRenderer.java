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
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.joml.Quaternionf;

/** Variant and scalar boat state are frozen before native model submission; no boat is retained. */
public abstract class NativeBoatRenderer<T extends AbstractBoat> extends EntityRenderer<T, NativeBoatRenderer.State> {
    public static final class State extends BoatRenderState { public Identifier variant; }
    private record Entry(Identifier texture, BoatModel model) {}
    private final Map<Identifier, Entry> models = new HashMap<>();
    private final Model.Simple waterPatch = new Model.Simple(BoatModel.createWaterPatch().bakeRoot(), ignored -> RenderTypes.waterMask());
    protected NativeBoatRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius = .8F; }
    protected final void addModel(Identifier variant, Identifier texture, BoatModel model) { models.put(variant, new Entry(texture, model)); }
    protected abstract Identifier variant(T boat);
    @Override public final State createRenderState() { return new State(); }
    @Override public final void extractRenderState(T entity, State state, float partial) {
        super.extractRenderState(entity, state, partial);
        state.variant = variant(entity);
        state.yRot = entity.getYRot(partial);
        state.hurtTime = entity.getHurtTime() - partial;
        state.hurtDir = entity.getHurtDir();
        state.damageTime = Math.max(entity.getDamage() - partial, 0F);
        state.bubbleAngle = entity.getBubbleAngle(partial);
        state.isUnderWater = entity.isUnderWater();
        state.rowingTimeLeft = entity.getRowingTime(0, partial);
        state.rowingTimeRight = entity.getRowingTime(1, partial);
    }
    public static void transform(BoatRenderState state, PoseStack pose) {
        pose.translate(0F, .375F, 0F);
        pose.rotateDegrees(Axis.YP, 180F - state.yRot);
        if (state.hurtTime > 0F) pose.rotateDegrees(Axis.XP,
                Mth.sin(state.hurtTime) * state.hurtTime * state.damageTime / 10F * state.hurtDir);
        // Preserve the port's original bubble tilt, including while submerged, and its float constant.
        if (!Mth.equal(state.bubbleAngle, 0F)) pose.rotate(new Quaternionf().setAngleAxis(state.bubbleAngle * .017453292F, 1F, 0F, 1F));
        pose.scale(-1F, -1F, 1F);
        pose.rotateDegrees(Axis.YP, 90F);
    }
    protected net.minecraft.client.renderer.rendertype.RenderType waterMask() { return RenderTypes.waterMask(); }
    @Override public final void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        var entry = java.util.Objects.requireNonNull(models.get(state.variant), "Unregistered Cobblemon boat variant");
        pose.pushPose();
        try {
            transform(state, pose);
            collector.submitModel(entry.model(), state, pose, entry.texture(), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            if (!state.isUnderWater) collector.submitModel(waterPatch, net.minecraft.util.Unit.INSTANCE, pose,
                    waterMask(), state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, 0);
        } finally { pose.popPose(); }
        super.submit(state, pose, collector, camera);
    }
}
