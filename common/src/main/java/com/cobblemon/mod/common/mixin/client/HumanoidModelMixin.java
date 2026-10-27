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
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin implements RiderModelAccess {
    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart hat;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightLeg;
    @Shadow @Final public ModelPart leftLeg;
    @Shadow @Final public ModelPart body;
    @Unique private RiderPose cobblemon$currentPose = RiderPose.EMPTY;
    public RiderPose cobblemon$getRiderPose() { return cobblemon$currentPose; }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("HEAD"), require = 1)
    private void cobblemon$resetPose(HumanoidRenderState state, CallbackInfo ci) {
        cobblemon$currentPose = ((RiderRenderStateAccess)state).cobblemon$getRiderData().pose();
    }

    // Native arm poses, attack animation and bobbing run once, after the riding animation.
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;isUsingItem:Z"), require = 1)
    private void cobblemon$animateRiders(HumanoidRenderState state, CallbackInfo ci) {
        var pose = cobblemon$currentPose;
        if (!pose.mounted()) return;
        pose.apply("arm_left", leftArm); pose.apply("arm_right", rightArm);
        pose.apply("leg_left", leftLeg); pose.apply("leg_right", rightLeg);
        pose.apply("head", head); pose.apply("hat", hat); pose.apply("torso", body);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"), require = 1)
    private void cobblemon$poseRecallArm(HumanoidRenderState state, CallbackInfo ci) {
        float raise = ((RiderRenderStateAccess)state).cobblemon$getRiderData().recall();
        if (raise <= 0F) return;
        rightArm.xRot = Mth.lerp(raise, rightArm.xRot, (float)(-Math.PI / 2));
        rightArm.yRot = Mth.lerp(raise, rightArm.yRot, 0F);
        rightArm.zRot = Mth.lerp(raise, rightArm.zRot, 0F);
    }
}
