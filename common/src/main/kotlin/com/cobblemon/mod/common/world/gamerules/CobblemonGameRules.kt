/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.gamerules

import com.cobblemon.mod.common.Cobblemon
import net.minecraft.world.level.gamerules.GameRule

object CobblemonGameRules {

    @JvmField
    val BATTLE_INVULNERABILITY: GameRule<Boolean> = register("battleInvulnerability")
    @JvmField
    val DO_POKEMON_SPAWNING: GameRule<Boolean> = register("doPokemonSpawning")
    @JvmField
    val DO_POKEMON_LOOT: GameRule<Boolean> = register("doPokemonLoot")
    @JvmField
    val MOB_TARGET_IN_BATTLE: GameRule<Boolean> = register("mobTargetInBattle")
    @JvmField
    val SHINY_STARTERS: GameRule<Boolean> = register("doShinyStarters")
    @JvmField
    val HEALERS_HEAL_PC: GameRule<Boolean> = register("healersHealPC")

    private fun register(legacyName: String): GameRule<Boolean> {
        val definition = CobblemonGameRuleDefinitions.BY_LEGACY_NAME.getValue(legacyName)
        return Cobblemon.implementation.registerGameRule(definition.identifier(), definition.createRule())
    }

}
