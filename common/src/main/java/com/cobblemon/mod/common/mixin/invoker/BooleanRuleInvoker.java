/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.invoker;

import com.cobblemon.mod.common.world.gamerules.CobblemonGameRuleDefinitions;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

/** @deprecated Native rules are values; use CobblemonGameRuleDefinitions instead. */
@Deprecated
public interface BooleanRuleInvoker {
    static GameRule<Boolean> cobblemon$create(boolean initialValue) {
        return CobblemonGameRuleDefinitions.booleanRule(GameRuleCategory.MISC, initialValue);
    }
}
