/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.client.model.*;
import com.cobblemon.mod.common.client.render.entity.MountedLivingEntityRenderer;
import com.cobblemon.mod.common.client.render.player.RecallArmAnimationTracker;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"), require = 1)
    private void cobblemon$extractMount(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        var seat = entity.getVehicle() instanceof PokemonEntity pokemon
                ? MountedLivingEntityRenderer.INSTANCE.capture(entity, pokemon, partialTick, state.bodyRot) : new Matrix4f();
        float recall = entity instanceof AbstractClientPlayer && !(entity.getVehicle() instanceof PokemonEntity)
                ? RecallArmAnimationTracker.INSTANCE.raiseFactor(entity.getUUID()) : 0F;
        ((RiderRenderStateAccess)state).cobblemon$setRiderData(new RiderRenderData(RiderPose.EMPTY, seat, recall));
    }

    @Inject(method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V", at = @At("HEAD"), require = 1)
    private void cobblemon$mountTransform(LivingEntityRenderState state, PoseStack pose, float bodyRot, float scale, CallbackInfo ci) {
        pose.mulPose(((RiderRenderStateAccess)state).cobblemon$getRiderData().seat());
    }
}
