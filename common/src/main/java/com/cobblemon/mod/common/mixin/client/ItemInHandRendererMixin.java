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
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true, require = 1)
    private void cobblemon$submitArm(PlayerRenderState player, FirstPersonHandsAndItemsRenderState state,
            float partialTick, float xRot, InteractionHand hand, float attack, ItemStack stack, float height,
            PoseStack pose, SubmitNodeCollector collector, int light, CallbackInfo ci) {
        var data = ((FirstPersonRenderStateAccess)state).cobblemon$firstPersonData();
        if (data.hidePokedex()) ci.cancel();
        else if (hand == InteractionHand.MAIN_HAND && data.recalling()) {
            data.recall().submit(pose, collector);
            ci.cancel();
        }
    }
    @Redirect(method = "submitHandsWithItems", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;rotateDegrees(Lcom/mojang/math/Axis;F)V"), require = 2)
    private void cobblemon$viewBob(PoseStack pose, Axis axis, float degrees, float partialTick,
            PoseStack outerPose, SubmitNodeCollector collector, PlayerRenderState player,
            FirstPersonHandsAndItemsRenderState state) {
        if (!((FirstPersonRenderStateAccess)state).cobblemon$firstPersonData().suppressViewBob()) pose.rotateDegrees(axis, degrees);
    }
}
