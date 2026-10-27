/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.player

import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.riding.RidingAnimation
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockAnimationRepository
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.model.AnimationUtils
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.phys.Vec3

/**
 * @author landonjw
 */
object MountedPlayerRenderer {
    val defaultAnimations = listOf(
        RidingAnimation(
            fileName = "player",
            animationName = "animation.player.default_ride_pose"
        )
    )

    fun capture(pokemonEntity: PokemonEntity, player: AbstractClientPlayer,
                renderState: net.minecraft.client.renderer.entity.state.AvatarRenderState): com.cobblemon.mod.common.client.model.RiderPose {
        val headYaw = renderState.yRot
        val headPitch = renderState.xRot
        val ageInTicks = renderState.ageInTicks
        val limbSwing = renderState.walkAnimationPos
        val limbSwingAmount = renderState.walkAnimationSpeed
        val relevantPartsByName = listOf("body", "arm_left", "arm_right", "leg_left", "leg_right", "head", "hat", "torso")
            .associateWith { ModelPart(emptyList(), emptyMap()) }
        val seat = pokemonEntity.getSeatForPassenger(player) ?: return com.cobblemon.mod.common.client.model.RiderPose.EMPTY
        val animations = seat.poseAnimations
            ?.firstOrNull { it.poseTypes.isEmpty() || it.poseTypes.contains(pokemonEntity.getCurrentPoseType()) }?.animations
            ?: defaultAnimations
        val state = pokemonEntity.delegate as PokemonClientDelegate

        relevantPartsByName.values.forEach {
            it.xRot = 0F
            it.yRot = 0F
            it.zRot = 0F
        }

        state.runtime.environment.setSimpleVariable("age_in_ticks", DoubleValue(ageInTicks.toDouble()))
        state.runtime.environment.setSimpleVariable("limb_swing", DoubleValue(limbSwing.toDouble()))
        state.runtime.environment.setSimpleVariable("limb_swing_amount", DoubleValue(limbSwingAmount.toDouble()))
        state.runtime.environment.setSimpleVariable("head_yaw", DoubleValue(headYaw.toDouble()))
        state.runtime.environment.setSimpleVariable("head_pitch", DoubleValue(headPitch.toDouble()))

        // TODO add the q.player reference, requires a cached thing or something on the client idk

        for (animation in animations) {
            val bedrockAnimation = BedrockAnimationRepository.getAnimationOrNull(animation.fileName, animation.animationName) ?: continue
            bedrockAnimation.run(
                context = null,
                relevantPartsByName = relevantPartsByName,
                rootPart = null,
                state = state,
                animationSeconds = state.animationSeconds,
                limbSwing = limbSwing,
                limbSwingAmount = limbSwingAmount,
                ageInTicks = ageInTicks,
                intensity = 1F
            )
        }

        return com.cobblemon.mod.common.client.model.RiderPose(relevantPartsByName.mapValues {
            com.cobblemon.mod.common.client.model.RiderPose.Part.capture(it.value)
        })
    }
}
