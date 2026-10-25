/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.world.gamerules;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Stable names/defaults shared by registration and the legacy world-data adapter. */
public final class CobblemonGameRuleDefinitions {
    public record Definition(Identifier identifier, GameRuleCategory category, boolean defaultValue) {
        public GameRule<Boolean> createRule() {
            return booleanRule(category, defaultValue);
        }
    }

    public static final Map<String, Definition> BY_LEGACY_NAME;
    static {
        var rules = new LinkedHashMap<String, Definition>();
        add(rules, "battleInvulnerability", "battle_invulnerability", GameRuleCategory.PLAYER, false);
        add(rules, "doPokemonSpawning", "do_pokemon_spawning", GameRuleCategory.SPAWNING, true);
        add(rules, "doPokemonLoot", "do_pokemon_loot", GameRuleCategory.DROPS, true);
        add(rules, "mobTargetInBattle", "mob_target_in_battle", GameRuleCategory.MOBS, true);
        add(rules, "doShinyStarters", "do_shiny_starters", GameRuleCategory.MISC, false);
        add(rules, "healersHealPC", "healers_heal_pc", GameRuleCategory.MISC, false);
        BY_LEGACY_NAME = Collections.unmodifiableMap(rules);
    }

    private static void add(Map<String, Definition> rules, String oldName, String path,
                            GameRuleCategory category, boolean defaultValue) {
        rules.put(oldName, new Definition(Identifier.fromNamespaceAndPath("cobblemon", path), category, defaultValue));
    }

    public static GameRule<Boolean> booleanRule(GameRuleCategory category, boolean defaultValue) {
        return new GameRule<>(category, GameRuleType.BOOL, BoolArgumentType.bool(),
                GameRuleTypeVisitor::visitBoolean, Codec.BOOL, value -> value ? 1 : 0,
                defaultValue, FeatureFlagSet.of());
    }

    private CobblemonGameRuleDefinitions() {}
}
