/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.npc

import com.cobblemon.mod.common.client.entity.NPCClientDelegate
import com.cobblemon.mod.common.client.render.item.HeldItemRenderer
import com.cobblemon.mod.common.client.render.models.blockbench.PosableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.npc.PosableNPCModel
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.mojang.blaze3d.vertex.PoseStack
import com.cobblemon.mod.common.client.model.ModelBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import com.cobblemon.mod.common.client.model.CapturedLivingEntityRenderer
import net.minecraft.resources.Identifier
import kotlin.math.min

class NPCRenderer(context: Context) : CapturedLivingEntityRenderer<NPCEntity>(context, 0.5f) {
    private val poserModel = PosableNPCModel()

    private fun textureOf(entity: NPCEntity): Identifier {
        return VaryingModelRepository.getTexture(entity.resourceIdentifier, (entity.delegate as NPCClientDelegate))
    }

    private val heldItemRenderer = HeldItemRenderer()

    override fun capture(entity: NPCEntity, state: State, partialTicks: Float, poseMatrix: PoseStack, buffer: ModelBufferSource) {
        val packedLight = state.lightCoords
        state.texture = textureOf(entity)
        state.modelScale = entity.renderScale
        state.extrasScale = entity.npc.modelScale
        val aspects = entity.aspects
        val clientDelegate = entity.delegate as NPCClientDelegate
        clientDelegate.currentAspects = aspects
        shadowRadius = min((entity.boundingBox.maxX - entity.boundingBox.minX), (entity.boundingBox.maxZ) - (entity.boundingBox.minZ)).toFloat() / 1.5F
        val model = VaryingModelRepository.getPoser(entity.resourceIdentifier, clientDelegate)
        poserModel.posableModel = model
        model.context = poserModel.context
        poserModel.setupEntityTypeContext(entity)
        poserModel.context.put(RenderContext.TEXTURE, state.texture)
        clientDelegate.updatePartialTicks(partialTicks)

        model.setLayerContext(buffer, clientDelegate, VaryingModelRepository.getLayers(entity.resourceIdentifier, clientDelegate))

        poseMatrix.pushPose()
        poseMatrix.scale(entity.npc.modelScale, entity.npc.modelScale, entity.npc.modelScale)
        poserModel.young = state.isBaby
        poserModel.riding = entity.isPassenger
        poserModel.attackTime = entity.getSwingAnimation(partialTicks)
        poserModel.setupAnim(entity, state.walkAnimationPos, state.walkAnimationSpeed, state.ageInTicks, state.yRot, state.xRot)
        val type = bodyRenderType(state, poserModel.renderType(state.texture))
        if (type != null) {
            transformBody(state, poseMatrix)
            poserModel.renderToBuffer(poseMatrix, buffer.getBuffer(type), packedLight,
                LivingEntityRenderer.getOverlayCoords(state, 0F), bodyColor(state))
        }
        poseMatrix.popPose()
        model.red = 1F
        model.green = 1F
        model.blue = 1F
        model.resetLayerContext()

        if (entity.deathTime < 1) {
            //Render Held Item
            heldItemRenderer.renderOnModel(
                entity.mainHandItem,
                clientDelegate,
                poseMatrix,
                buffer,
                packedLight,
                false,
                entity
            )
        }

//        if (this.shouldRenderLabel(entity)) {
//            this.renderLabelIfPresent(entity, entity.displayName, poseMatrix, buffer, packedLight)
//        }
    }

    override fun shouldShowName(entity: NPCEntity, distance: Double): Boolean {
        if (entity.hideNameTag) {
            return false
        }
        return super.shouldShowName(entity, distance)
    }
}