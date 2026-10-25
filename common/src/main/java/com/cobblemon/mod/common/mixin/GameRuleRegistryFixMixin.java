/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.world.gamerules.LegacyGameRules;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.GameRuleRegistryFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRuleRegistryFix.class)
public abstract class GameRuleRegistryFixMixin {
    @ModifyReturnValue(method = "lambda$makeRule$2", at = @At("RETURN"), require = 1)
    private static Dynamic<?> cobblemon$migrateGameRules(Dynamic<?> rules) {
        return LegacyGameRules.migrate(rules);
    }
}
