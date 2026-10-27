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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"), require = 1)
    private void cobblemon$extractPlayer(Avatar entity, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        if (!(entity instanceof AbstractClientPlayer player)) return;
        var access = (RiderRenderStateAccess)state;
        var data = access.cobblemon$getRiderData();
        if (player.getVehicle() instanceof PokemonEntity pokemon) {
            boolean headRotation = pokemon.ifRidingAvailableSupply(false,
                    (behaviour, settings, rideState) -> behaviour.shouldRotateRiderHead(settings, rideState, pokemon));
            if (!headRotation) { state.yRot = 0F; state.xRot = 0F; }
            state.isCrouching = false;
            access.cobblemon$setRiderData(new RiderRenderData(MountedPlayerRenderer.INSTANCE.capture(pokemon, player, state), data.seat(), 0F));
        }
        ClientPlayerIcon.Companion.onRenderPlayer(player);
    }
}
