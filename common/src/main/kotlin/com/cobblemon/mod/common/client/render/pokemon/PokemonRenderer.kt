/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.OrientationControllable
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.battle.ClientBallDisplay
import com.cobblemon.mod.common.client.entity.NPCClientDelegate
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate.Companion.BEAM_EXTEND_TIME
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate.Companion.BEAM_SHRINK_TIME
import com.cobblemon.mod.common.client.keybind.boundKey
import com.cobblemon.mod.common.client.keybind.keybinds.PartySendBinding
import com.cobblemon.mod.common.client.render.item.HeldItemRenderer
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PosablePokemonEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.repository.MiscModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository
import com.cobblemon.mod.common.client.render.player.RecallArmAnimationTracker
import com.cobblemon.mod.common.client.render.pokeball.PokeBallPosableState
import com.cobblemon.mod.common.client.render.renderBeaconBeam
import com.cobblemon.mod.common.client.settings.ServerSettings
import com.cobblemon.mod.common.entity.PlatformType
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity.Companion.SPAWN_DIRECTION
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.util.effectiveName
import com.cobblemon.mod.common.util.isLookingAt
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.math.DoubleRange
import com.cobblemon.mod.common.util.math.geometry.toRadians
import com.cobblemon.mod.common.util.math.remap
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font.DisplayMode
import com.cobblemon.mod.common.client.model.ModelBufferSource
import com.cobblemon.mod.common.client.model.CapturedLivingEntityRenderer
import com.cobblemon.mod.common.client.model.CapturedLabel
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import org.joml.*
import kotlin.math.*

class PokemonRenderer(
    context: EntityRendererProvider.Context
) : CapturedLivingEntityRenderer<PokemonEntity>(context, 0.5f) {
    companion object {
        val recallBeamColour = Vector4f(1F, 0.1F, 0.1F, 1F)
        fun ease(x: Double): Double {
            return 1 - (1 - x).pow(3)
        }
        private val LEVEL_LABEL_STYLE = Style.EMPTY.withColor(ChatFormatting.WHITE)
            .withBold(false)
            .withItalic(false)
            .withUnderlined(false)
            .withStrikethrough(false)
            .withObfuscated(false)

        private const val HIDDEN_NAME = "???"

        private const val SPACE = " "
    }

    val ballContext = RenderContext().also {
        it.put(RenderContext.RENDER_STATE, RenderContext.RenderState.WORLD)
    }

    private val heldItemRenderer = HeldItemRenderer()

    private val poserModel = PosablePokemonEntityModel()

    private fun textureOf(entity: PokemonEntity): Identifier {
        return VaryingModelRepository.getTexture(entity.pokemon.species.resourceIdentifier, entity.delegate as PokemonClientDelegate)
    }

    override fun capture(
        entity: PokemonEntity,
        state: State,
        partialTicks: Float,
        poseMatrix: PoseStack,
        buffer: ModelBufferSource
    ) {
        val packedLight = state.lightCoords
        state.texture = textureOf(entity)
        state.modelScale = entity.pokemon.form.baseScale * entity.pokemon.effectiveScale *
            (entity.delegate as PokemonClientDelegate).activeSendoutScale
        val clientDelegate = entity.delegate as PokemonClientDelegate
        shadowRadius = (min((entity.boundingBox.maxX - entity.boundingBox.minX), (entity.boundingBox.maxZ) - (entity.boundingBox.minZ)).toFloat() / 1.5F * (entity.delegate as PokemonClientDelegate).activeSendoutScale)/entity.scale
        poserModel.posableModel = VaryingModelRepository.getPoser(entity.pokemon.species.resourceIdentifier, clientDelegate)
        poserModel.posableModel.context = poserModel.context
        poserModel.setupEntityTypeContext(entity)
        val modelNow = poserModel.posableModel

        val freezeFrame = entity.entityData.get(PokemonEntity.FREEZE_FRAME)
        if (freezeFrame != -1F) {
            clientDelegate.updateAge(0)
            clientDelegate.updatePartialTicks(freezeFrame * 20F)
        } else {
            clientDelegate.updatePartialTicks(partialTicks)
        }

        if (entity.beamMode != 0 && !Minecraft.getInstance().isPaused) {
            renderTransition(
                modelNow,
                entity.beamMode,
                entity,
                partialTicks,
                poseMatrix,
                buffer,
                packedLight,
                clientDelegate
            )
        }
        if (entity.platform != PlatformType.NONE) {
            drawPlatform(
                poseMatrix,
                entity,
                (entity.delegate as PokemonClientDelegate).activeSendoutScale,
                buffer,
                packedLight,
            )
            // keeps the pokemon's root on the raft
            state.extrasOffsetY = 0.25F * clientDelegate.activeSendoutScale
            poseMatrix.translate(0F, state.extrasOffsetY, 0F)
        }

        modelNow.setLayerContext(buffer, clientDelegate, VaryingModelRepository.getLayers(entity.pokemon.species.resourceIdentifier, clientDelegate))


        if (entity.passengers.isNotEmpty()) {
            renderRiding(entity, state, partialTicks, poseMatrix, buffer, packedLight)
        } else {
            captureBody(entity, state, poseMatrix, buffer, packedLight)
        }

        // Call rendering for alpha eye trail
        doAlphaEyeRendering(
            entity = entity,
            partialTicks = partialTicks,
            poseStack = poseMatrix,
            bufferSource = buffer
        )

        modelNow.green = 1F
        modelNow.blue = 1F
        modelNow.resetLayerContext()
        if (this.shouldRenderLabel(entity)) {
            state.label = captureLabel(entity, state.distanceToCameraSq, state.extrasOffsetY)
        }
//        Minecraft.getInstance().bufferBuilders.entityVertexConsumers.draw()

        //Render Held Item
        heldItemRenderer.renderOnModel(
            entity.shownItem,
            clientDelegate,
            poseMatrix,
            buffer,
            packedLight,
            false,
            entity
        )
    }

    private fun renderRiding(
        entity: PokemonEntity,
        state: State,
        partialTicks: Float,
        poseMatrix: PoseStack,
        buffer: ModelBufferSource,
        packedLight: Int
    ) {
        val rollable = entity as? OrientationControllable ?: return
        val controller = rollable.orientationController
        poseMatrix.pushPose()

        if (controller.active) {
            // Allow the ride controller to modify its rotations using partialTick
            entity.delegate.applyRenderRotation(partialTicks)

            val matrix = poseMatrix.last().pose()
            val yaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot)
            val center = Vector3f(0f, entity.bbHeight/2, 0f)
            val transformationMatrix = Matrix4f()
            //Move origin to center of Pokemon
            transformationMatrix.translate(center)

            transformationMatrix.rotate(controller.getRenderOrientation(partialTicks))
            //Move origin to base of the entity
            transformationMatrix.translate(center.negate(Vector3f()))

            //Pre-undo yaw rotation
            transformationMatrix.rotate(Axis.YP.rotationDegrees(yaw+180f))
            matrix.mul(transformationMatrix)
        }

        captureBody(entity, state, poseMatrix, buffer, packedLight)
        poseMatrix.popPose()
    }

    fun renderTransition(
        modelNow: PosableModel,
        beamMode: Int,
        entity: PokemonEntity,
        partialTicks: Float,
        poseMatrix: PoseStack,
        buffer: ModelBufferSource,
        packedLight: Int,
        clientDelegate: PokemonClientDelegate
    ) {
        val s = clientDelegate.secondsSinceBeamEffectStarted
        if (beamMode == 3) {
            if (s > BEAM_EXTEND_TIME) {
                val value = (s - BEAM_EXTEND_TIME) /  BEAM_SHRINK_TIME
                val colourValue = 1F - min(0.6F, value)
                modelNow.green = colourValue
                modelNow.blue = colourValue
            }
        }

        val phaseTarget = clientDelegate.phaseTarget ?: return
        poseMatrix.pushPose()
        var beamSourcePosition = if (phaseTarget is NPCEntity) {
                val npcDelegate = phaseTarget.delegate as NPCClientDelegate
                val baseScale = phaseTarget.renderScale.toDouble()

                (npcDelegate.locatorStates["beam"]?.getOrigin()?.scale(baseScale))
                    ?: phaseTarget.position().add(0.0, (phaseTarget.bbHeight / 2.0) * baseScale, 0.0)
                } else if (phaseTarget is PosableEntity) {
                    (phaseTarget.delegate as PosableState).locatorStates["beam"]?.getOrigin() ?: phaseTarget.position()
        } else {
            if (phaseTarget.uuid == Minecraft.getInstance().player?.uuid) {
                val lookVec = phaseTarget.lookAngle.yRot((PI / 2).toFloat()).multiply(1.0, 0.0, 1.0).normalize()
                phaseTarget.getEyePosition(partialTicks).subtract(0.0, 0.4, 0.0).subtract(lookVec.scale(0.3))
            } else {
                val lookVec = phaseTarget.lookAngle.yRot((PI / 2 - (phaseTarget.visualRotationYInDegrees - phaseTarget.xRot).toRadians()).toFloat()).multiply(1.0, 0.0, 1.0).normalize()
                phaseTarget.getEyePosition(partialTicks).subtract(0.0, 0.7, 0.0).subtract(lookVec.scale(0.4))
            }
        }

        if (clientDelegate.sendOutPosition == null && beamMode == 1) {
            clientDelegate.sendOutPosition = beamSourcePosition
        } else if (beamMode == 1) {
            clientDelegate.sendOutPosition = clientDelegate.sendOutPosition!!.add(0.0, 0.04, 0.0)
            beamSourcePosition = clientDelegate.sendOutPosition!!
        }
        val offsetDirection = beamSourcePosition.subtract(entity.position()).normalize().scale(-clientDelegate.ballOffset.toDouble())
        val facingDir: Vec3
        with(beamSourcePosition.subtract(entity.position())) {
            var newOffset = offsetDirection.scale(2.0)
            val distance = beamSourcePosition.distanceTo(entity.position())
            newOffset = newOffset.scale((distance / 10.0) * 5)
            facingDir = newOffset.normalize()
            clientDelegate.sendOutOffset = newOffset
            poseMatrix.translate(x+newOffset.x, y+newOffset.y, z+newOffset.z)
        }
        val dir = beamSourcePosition.subtract(entity.position()).normalize()
        val angle = Mth.atan2(dir.z, dir.x) - PI / 2
        poseMatrix.rotate(Axis.YP.rotation(-angle.toFloat() + (180 * Math.PI / 180).toFloat()))

        if (beamMode == 1 && !clientDelegate.ballDone){
            if(entity.pokemon.caughtBall.name.toString().contains("beast")){
                // get rotation angle on x-axis for facingDir
                val xAngleFacingDir = Mth.atan2(facingDir.y, sqrt(facingDir.x * facingDir.x + facingDir.z * facingDir.z))
                poseMatrix.rotate(Axis.XP.rotation(-xAngleFacingDir.toFloat()))
            }
            drawPokeBall(
                ClientBallDisplay(entity.pokemon.caughtBall, setOf()),
                poseMatrix,
                scale = clientDelegate.ballOffset,
                partialTicks = partialTicks,
                buff = buffer,
                packedLight = packedLight,
                ball = CobblemonClient.storage.party.firstOrNull { it?.uuid == entity.pokemon.uuid }?.caughtBall
                    ?: clientDelegate.currentEntity.pokemon.caughtBall,
                distance = ceil(beamSourcePosition.distanceTo(entity.position())/4f).toInt()
            )
        }
        poseMatrix.popPose()
        if (beamMode == 3) {
            renderBeam(poseMatrix, partialTicks, entity, phaseTarget, buffer, offsetDirection)
        }
    }

    private fun captureBody(entity: PokemonEntity, state: State, pose: PoseStack, buffers: ModelBufferSource, light: Int) {
        poserModel.young = state.isBaby
        poserModel.riding = entity.isPassenger
        poserModel.attackTime = entity.getSwingAnimation(state.ageInTicks - entity.tickCount)
        poserModel.setupAnim(entity, state.walkAnimationPos, state.walkAnimationSpeed, state.ageInTicks, state.yRot, state.xRot)
        val type = bodyRenderType(state, poserModel.renderType(state.texture)) ?: return
        pose.pushPose()
        try {
            transformBody(state, pose)
            poserModel.renderToBuffer(pose, buffers.getBuffer(type), light,
                LivingEntityRenderer.getOverlayCoords(state, 0F), bodyColor(state))
        } finally {
            pose.popPose()
        }
    }

    override fun getShadowRadius(state: State): Float = super.getShadowRadius(state) * state.ageScale

    /**
     * Renders a beam between the Cobblemon and the target.
     *
     * @param matrixStack The matrix stack to render with.
     * @param partialTicks The partial ticks.
     * @param entity The Cobblemon.
     * @param beamTarget The target.
     * @param colour The colour of the beam.
     * @param buffer The vertex consumer provider.
     */
    fun renderBeam(matrixStack: PoseStack, partialTicks: Float, entity: PokemonEntity, beamTarget: Entity, buffer: ModelBufferSource, offset: Vec3) {
        val clientDelegate = entity.delegate as PokemonClientDelegate
        val pokemonPosition = entity.position().add(0.0, entity.bbHeight / 2.0 * clientDelegate.activeSendoutScale.toDouble(), 0.0)
        val recallBallOrigin = (beamTarget as? Player)?.let { RecallArmAnimationTracker.get(it.uuid)?.beamOrigin }

        val beamSourcePosition = when {
            // cover the recall ball origin presence
            recallBallOrigin != null -> recallBallOrigin
            // cover the pokeball catching a specimen
            beamTarget is EmptyPokeBallEntity -> (beamTarget.delegate as PokeBallPosableState)
                .locatorStates["beam"]
                ?.transformPosition(Vec3(0.0, 0.2, 0.45)) ?: beamTarget.position()
            // player recall
            beamTarget.uuid == Minecraft.getInstance().player?.uuid -> {
                val lookVec = beamTarget.lookAngle.yRot((PI / 2).toFloat()).multiply(1.0, 0.0, 1.0).normalize()
                beamTarget.getEyePosition(partialTicks).subtract(0.0, 0.4, 0.0).subtract(lookVec.scale(0.3))
            }
            // npc recall
            beamTarget is NPCEntity -> {
                val npcDelegate = beamTarget.delegate as NPCClientDelegate
                val baseScale = beamTarget.renderScale * beamTarget.hitboxScale

                (npcDelegate.locatorStates["beam"]?.getOrigin()?.scale(baseScale.toDouble()))
                    ?: beamTarget.position().add(0.0, (beamTarget.bbHeight / 2.0) * baseScale, 0.0)
            }
            clientDelegate.sendOutPosition != null -> clientDelegate.sendOutPosition!!
            // ???
            else -> {
                val lookVec = beamTarget.lookAngle.yRot((PI / 2 - (beamTarget.visualRotationYInDegrees - beamTarget.xRot).toRadians()).toFloat()).multiply(1.0, 0.0, 1.0).normalize()
                beamTarget.getEyePosition(partialTicks).subtract(0.0, 0.7, 0.0).subtract(lookVec.scale(0.4))
            }
        }

        if (beamSourcePosition.distanceTo(pokemonPosition) > 20) return

        var newOffset = offset.scale(2.0)
        // the further away the source position is, the smaller the newOffset should be. Max distance is 20 blocks
        val distance = beamSourcePosition.distanceTo(entity.position())
        newOffset = newOffset.scale((distance / 10.0) * 5)
        newOffset = newOffset.multiply(0.0, 1+ease(clientDelegate.ballOffset.toDouble()), 0.0)
        val direction = pokemonPosition.subtract(beamSourcePosition.add(newOffset)).let { Vector3f(it.x.toFloat(), it.y.toFloat(), it.z.toFloat()) }

        matrixStack.pushPose()
        with(beamSourcePosition.subtract(entity.position())) {
            matrixStack.translate(x + newOffset.x, y + newOffset.y, z + newOffset.z)
        }

        val s = clientDelegate.secondsSinceBeamEffectStarted
        val ratio = if (s < BEAM_EXTEND_TIME) {
            s / BEAM_EXTEND_TIME
        } else if (s > BEAM_EXTEND_TIME + BEAM_SHRINK_TIME) {
            1 - min((s - BEAM_EXTEND_TIME - BEAM_SHRINK_TIME) / BEAM_EXTEND_TIME, 1F)
        } else {
            1F
        }

        direction.normalize()

        val yAxis = Vector3f(0F, 1F, 0F)
        val dot = direction.dot(yAxis)
        val cross = yAxis.cross(direction)
        val q = Quaternionf(cross.x, cross.y, cross.z, 1 + dot).normalize()
        matrixStack.rotate(q)

        renderBeaconBeam(
            matrixStack = matrixStack,
            buffer = buffer,
            partialTicks = partialTicks,
            totalLevelTime = entity.level().gameTime,
            height = pokemonPosition.distanceTo(beamSourcePosition.add(offset)).toFloat() * ratio,
            red = recallBeamColour.x,
            green = recallBeamColour.y,
            blue = recallBeamColour.z,
            alpha = recallBeamColour.w,
            beamRadius = 0.03F,
            glowRadius = 0.07F,
            glowAlpha = 0.4F
        )

        matrixStack.popPose()
    }

    override fun getFlipDegrees(): Float = 0F

    // At some point vanilla does something to tha matrix.
    // We want to prevent it from rendering there and instead do it ourselves here.
    override fun shouldShowName(entity: PokemonEntity, distance: Double): Boolean = false

    private fun shouldRenderLabel(entity: PokemonEntity): Boolean {
        if (!super.shouldShowName(entity, entityRenderDispatcher.distanceToSqr(entity)) ||
            !(entity.shouldShowName() || (entity.hasCustomName() && entity == entityRenderDispatcher.crosshairPickEntity))) {
            return false
        }
        if (entity.entityData.get(PokemonEntity.HIDE_LABEL)) {
            return false
        }
        val player = Minecraft.getInstance().player ?: return false
        val delegate = entity.delegate as? PokemonClientDelegate ?: return false
        return (!Cobblemon.config.displayEntityLabelsWhenCrouchingOnly || player.isCrouching) &&
                player.isLookingAt(entity) &&
                delegate.phaseTarget == null &&
                !CobblemonClient.pokedexUsageContext.scanningGuiOpen
    }

    private fun captureLabel(entity: PokemonEntity, distance: Double, platformOffset: Float): CapturedLabel? {
        if (entity.isInvisible || distance > 4096.0) return null
        val player = Minecraft.getInstance().player ?: return null
        val scale = min(1.5, max(0.65, distance.remap(DoubleRange(-16.0, 96.0), DoubleRange(0.0, 1.0))))
        val sizeScale = Mth.lerp(scale.remap(DoubleRange(0.65, 1.5), DoubleRange(0.0, 1.0)), 0.5, 1.0)
        val offsetScale = Mth.lerp(scale.remap(DoubleRange(0.65, 1.5), DoubleRange(0.0, 1.0)), 0.0, 1.0)
        val label = resolveBaseLabel(entity)
        if (ServerSettings.displayEntityLevelLabel && entity.labelLevel() > 0) {
            if (ServerSettings.displayEntityNameLabel) label.append(SPACE)
            label.append(lang("label.lv", entity.labelLevel()).setStyle(LEVEL_LABEL_STYLE))
        }
        val lines = mutableListOf(CapturedLabel.Line(label, (-font.width(label) / 2).toFloat(), 0F))
        if (CobblemonClient.clientPlayerData.showChallengeLabel && entity.canBattle(player)) {
            val prompt = lang("challenge_label", PartySendBinding.boundKey().displayName)
            lines += CapturedLabel.Line(prompt, (-font.width(prompt) / 2).toFloat(), 10F)
        }
        val opacity = (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255F).toInt() shl 24
        return CapturedLabel(entity.boundingBox.ysize + 0.5 + platformOffset, scale, sizeScale, offsetScale, opacity, lines)
    }

    private fun resolveBaseLabel(entity: PokemonEntity): MutableComponent {
        return when {
            !ServerSettings.displayEntityNameLabel -> Component.empty()
            Cobblemon.config.displayNameForUnknownPokemon || CobblemonClient.clientPokedexData.getKnowledgeForSpecies(entity.pokemon.species.resourceIdentifier) != PokedexEntryProgress.UNREGISTERED -> entity.getTitledName()
            else -> Component.literal(HIDDEN_NAME)
        }
    }

    private fun drawPokeBall(
        state: ClientBallDisplay,
        matrixStack: PoseStack,
        scale: Float = 5F,
        partialTicks: Float,
        reversed: Boolean = false,
        buff: ModelBufferSource,
        packedLight: Int,
        ball: PokeBall,
        distance: Int
    ) {
        matrixStack.pushPose()
        matrixStack.scale(0.7F, -0.7F, -0.7F)
        val model = VaryingModelRepository.getPoser(ball.name, state)
        val texture = VaryingModelRepository.getTexture(ball.name, state)
        if (scale == 1.0f) {
            model.moveToPose(state, model.poses["open"]!!)
        } else {
            matrixStack.translate(0.0, -0.2, 0.0)
            val rot = 360F * distance
            if(ball.name.toString().contains("beast")){
                matrixStack.rotate(Axis.ZN.rotationDegrees(Math.lerp(0F, rot, scale)))
            } else {
                matrixStack.rotate(Axis.XN.rotationDegrees(Math.lerp(0F, rot, scale)))
            }
            matrixStack.translate(0.0, 0.2, 0.0)
        }
        state.updatePartialTicks(partialTicks)
        model.context = ballContext
        ballContext.put(RenderContext.ASPECTS, state.aspects)
        ballContext.put(RenderContext.POSABLE_STATE, state)
        model.applyAnimations(null, state, 0F, 0F, 0F, 0F, 0F)
//        model.animateModel(null, 0f, 0F, 0F)
        val buffer = buff.getBuffer(RenderTypes.entityCutoutCull(texture))
//        matrixStack.scale(scale, scale, scale)
        model.render(ballContext, matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, -0x1)
        model.green = 1f
        model.blue = 1f
        model.red = 1f
        model.resetLayerContext()
        matrixStack.popPose()
    }

    private fun drawPlatform(
            matrixStack: PoseStack,
            entity: PokemonEntity,
            scale: Float = 1F,
            buff: ModelBufferSource,
            packedLight: Int,
            ) {
        val (modelResource, textureResource) = PlatformType.getModelWithTexture(entity.platform)
        val model = MiscModelRepository.modelOf(modelResource) ?: return
        matrixStack.pushPose()
        matrixStack.rotate(Axis.ZP.rotationDegrees(180f))
        matrixStack.rotateAround(Axis.YP.rotationDegrees(entity.entityData.get(SPAWN_DIRECTION)), 0.0f, 0f, 0.0f)
        matrixStack.scale(scale, scale, scale)
        val buffer = buff.getBuffer(RenderTypes.entityCutoutCull(textureResource))
        model.render(matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, -0x1)

        matrixStack.popPose()
    }
}
