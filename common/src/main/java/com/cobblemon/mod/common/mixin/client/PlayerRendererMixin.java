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
import com.cobblemon.mod.common.client.render.ClientPlayerIcon;
import com.cobblemon.mod.common.client.render.player.MountedPlayerRenderer;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin {
    @Inject(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("TAIL"), require = 1)
    private void cobblemon$submitIcon(AvatarRenderState state, PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera, CallbackInfo ci) {
        ((PlayerLayerRenderStateAccess)state).cobblemon$layerSnapshots()
                .getOrDefault("cobblemon:icon", ModelBufferSource.Snapshot.EMPTY).submit(pose, collector);
    }

    @Shadow @Final protected List<RenderLayer<AvatarRenderState, PlayerModel>> layers;
    @Shadow protected abstract void setupRotations(AvatarRenderState state, PoseStack pose, float yaw, float scale);
    @Shadow protected abstract void scale(AvatarRenderState state, PoseStack pose);

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"), require = 1)
    private void cobblemon$extractPlayer(Avatar entity, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        ((PlayerLayerRenderStateAccess)state).cobblemon$layerSnapshots().clear();
        if (!(entity instanceof AbstractClientPlayer player)) return;
        var access = (RiderRenderStateAccess)state;
        var data = access.cobblemon$getRiderData();
        if (player.getVehicle() instanceof PokemonEntity pokemon) {
            boolean headRotation = pokemon.ifRidingAvailableSupply(false,
                    (behaviour, settings, rideState) -> behaviour.shouldRotateRiderHead(settings, rideState, pokemon));
            if (!headRotation) { state.yRot = 0F; state.xRot = 0F; }
            state.isCrouching = false;
            access.cobblemon$setRiderData(new RiderRenderData(MountedPlayerRenderer.INSTANCE.capture(pokemon, player, state), data.seat(), 0F, data.recalling()));
        }
        ((AvatarRenderer<?>)(Object)this).getModel().setupAnim(state);
        var worldBody = new PoseStack();
        worldBody.translate(state.x, state.y, state.z);
        worldBody.translate(((AvatarRenderer<?>)(Object)this).getRenderOffset(state));
        if (state.hasPose(net.minecraft.world.entity.Pose.SLEEPING) && state.bedOrientation != null) {
            float offset = state.eyeHeight - 0.1F;
            worldBody.translate(-state.bedOrientation.getStepX() * offset, 0F, -state.bedOrientation.getStepZ() * offset);
        }
        worldBody.scale(state.scale, state.scale, state.scale);
        setupRotations(state, worldBody, state.bodyRot, state.scale);
        worldBody.scale(-1F, -1F, 1F);
        scale(state, worldBody);
        worldBody.translate(0F, -1.501F, 0F);
        for (var layer : layers) if (layer instanceof CapturedPlayerLayer captured)
            captured.extract(player, state, partialTick, worldBody.last().pose());
        ((PlayerLayerRenderStateAccess)state).cobblemon$layerSnapshots().put("cobblemon:icon",
                ClientPlayerIcon.Companion.onRenderPlayer(player, partialTick, ((AvatarRenderer<?>)(Object)this).getRenderOffset(state)));
    }
}
