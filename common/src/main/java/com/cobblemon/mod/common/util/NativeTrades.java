/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.trading.*;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

/** Adds legacy offers to native trade sets before native selection, never after random selection. */
public final class NativeTrades {
    private static final Map<ResourceKey<TradeSet>, List<VillagerTrade>> ADDITIONS = new ConcurrentHashMap<>();

    public static VillagerTrade offer(ItemCost cost, ItemStack result, int maxUses, int xp,
                                      Optional<ItemCost> secondary, float multiplier) {
        var builder = VillagerTrade.builder(cost(cost), ItemStackTemplate.fromNonEmptyStack(result), maxUses, xp, multiplier);
        secondary.ifPresent(value -> builder.additionalWants(cost(value)));
        return builder.build();
    }

    private static TradeCost cost(ItemCost value) {
        return new TradeCost(value.item(), ContextIntProviders.exactly(value.count()), value.components());
    }

    public static void register(ResourceKey<TradeSet> key, List<VillagerTrade> offers) {
        ADDITIONS.put(key, List.copyOf(offers));
    }

    public static Optional<TradeSet> resolve(ResourceKey<TradeSet> key, Optional<TradeSet> original) {
        return augment(original, ADDITIONS.getOrDefault(key, List.of()));
    }

    public static Optional<TradeSet> augment(Optional<TradeSet> original, List<VillagerTrade> additions) {
        if (additions.isEmpty()) return original;
        TradeSet base = original.orElseGet(() -> new TradeSet(HolderSet.direct(List.of()),
                ContextIntProviders.exactly(2), false, Optional.empty()));
        var entries = new ArrayList<Holder<VillagerTrade>>();
        base.trades().forEach(entries::add);
        additions.forEach(trade -> entries.add(Holder.direct(trade)));
        return Optional.of(new TradeSet(HolderSet.direct(entries), base.amount(), base.allowDuplicates(), base.randomSequence()));
    }
    private NativeTrades() { }
}
