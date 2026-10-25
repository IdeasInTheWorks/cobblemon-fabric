/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.util.CompoundTagExtensionsKt;
import com.cobblemon.mod.common.util.CompoundTagUtilities;
import com.cobblemon.mod.common.util.ShoulderData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.UUID;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerShoulderMixin {
    @Shadow public abstract CompoundTag getShoulderEntityLeft();
    @Shadow public abstract CompoundTag getShoulderEntityRight();
    @Shadow protected abstract void setShoulderEntityLeft(CompoundTag tag);
    @Shadow protected abstract void setShoulderEntityRight(CompoundTag tag);
    @Shadow public abstract void respawnEntityOnShoulder(CompoundTag tag);

    @Inject(method = "setShoulderEntityLeft", at = @At("TAIL"))
    private void cobblemon$syncLeft(CompoundTag tag, CallbackInfo ci) {
        ShoulderData.sync((ServerPlayer) (Object) this, true, tag);
    }
    @Inject(method = "setShoulderEntityRight", at = @At("TAIL"))
    private void cobblemon$syncRight(CompoundTag tag, CallbackInfo ci) {
        ShoulderData.sync((ServerPlayer) (Object) this, false, tag);
    }

    @Inject(method = "respawnEntityOnShoulder", at = @At("HEAD"), cancellable = true)
    private void cobblemon$recallPokemon(CompoundTag tag, CallbackInfo ci) {
        if (!CompoundTagExtensionsKt.isPokemonEntity(tag)) return;
        UUID uuid = CompoundTagUtilities.getPokemonID(tag);
        var player = (ServerPlayer) (Object) this;
        var party = Cobblemon.INSTANCE.getStorage().getParty(player.getUUID(), player.registryAccess());
        for (var pokemon : party) {
            if (pokemon.getUuid().equals(uuid)) pokemon.recall();
        }
        if (CompoundTagUtilities.isShoulderPokemon(getShoulderEntityLeft()) &&
                uuid.equals(CompoundTagUtilities.getPokemonID(getShoulderEntityLeft()))) {
            setShoulderEntityLeft(new CompoundTag());
        }
        if (CompoundTagUtilities.isShoulderPokemon(getShoulderEntityRight()) &&
                uuid.equals(CompoundTagUtilities.getPokemonID(getShoulderEntityRight()))) {
            setShoulderEntityRight(new CompoundTag());
        }
        ci.cancel();
    }

    // Preserve the native 20-tick mount grace period by injecting at its first respawn call.
    @Inject(method = "removeEntitiesOnShoulder", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;respawnEntityOnShoulder(Lnet/minecraft/nbt/CompoundTag;)V",
            ordinal = 0), cancellable = true)
    private void cobblemon$keepMountedPokemon(CallbackInfo ci) {
        var player = (ServerPlayer) (Object) this;
        if (player.isSpectator() || player.isDeadOrDying()) return;
        if (!CompoundTagUtilities.isShoulderPokemon(getShoulderEntityLeft())) {
            respawnEntityOnShoulder(getShoulderEntityLeft());
            setShoulderEntityLeft(new CompoundTag());
        }
        if (!CompoundTagUtilities.isShoulderPokemon(getShoulderEntityRight())) {
            respawnEntityOnShoulder(getShoulderEntityRight());
            setShoulderEntityRight(new CompoundTag());
        }
        ci.cancel();
    }
}
