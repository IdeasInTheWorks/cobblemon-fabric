/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Records at the native movement-emission site, preserving its movement/passenger guards. */
@Mixin(Entity.class)
public abstract class PokemonMovementEmissionMixin {
    @Inject(method = "applyMovementEmissionAndPlaySound", at = @At("HEAD"), require = 1)
    private void cobblemon$recordWalkDistance(Entity.MovementEmission emission, Vec3 movement,
                                              BlockPos pos, BlockState state, CallbackInfo ci) {
        if ((Object) this instanceof PokemonEntity pokemon) {
            pokemon.recordLegacyWalkMovement(movement);
        }
    }
}
