/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.util.NativeTrades;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.trading.TradeSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerTradeMixin {
    @WrapOperation(method = "addOffersFromTradeSet", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/Registry;getOptional(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;"), require = 1)
    private Optional<TradeSet> cobblemon$tradeSet(Registry<TradeSet> registry, ResourceKey<TradeSet> key,
                                                 Operation<Optional<TradeSet>> original) {
        return NativeTrades.resolve(key, original.call(registry, key));
    }
}
