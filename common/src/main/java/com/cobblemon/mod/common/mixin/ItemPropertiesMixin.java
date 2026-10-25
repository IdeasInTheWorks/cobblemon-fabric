/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.FishingRodCast;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keep a PokeRod in one hand from making the vanilla rod in the other look cast. */
@Mixin(FishingRodCast.class)
public class ItemPropertiesMixin {
    @Inject(method = "get", at = @At("HEAD"), cancellable = true, require = 1)
    private void cobblemon$preventRodMixup(ItemStack stack, ClientLevel level, LivingEntity entity,
            int seed, ItemDisplayContext display, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof Player player) || player.fishing instanceof PokeRodFishingBobberEntity)
            cir.setReturnValue(false);
    }
}
