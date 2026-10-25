/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

/** Consumption behavior kept separate from nutrition in the target component API. */
public final class CobblemonConsumables {
    // The custom finishUsingItem handlers own nutrition and stack consumption.
    public static final Consumable MANUAL_FOOD = Consumables.defaultFood().build();
    public static final Consumable MANUAL_DRINK = Consumables.defaultDrink().consumeSeconds(1.6F).build();
    public static final Consumable QUICK_FOOD = Consumables.defaultFood().consumeSeconds(0.8F).build();
    public static final Consumable VIVICHOKE_DIP = Consumables.defaultFood().onConsume(
            new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.ABSORPTION, 900, 0), 1F)).build();
    private CobblemonConsumables() {}
}
