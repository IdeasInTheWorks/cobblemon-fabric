/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.world.gamerules;

import com.mojang.serialization.Dynamic;

/** Called inside vanilla's old-world gamerule conversion, before registry decoding. */
public final class LegacyGameRules {
    public static <T> Dynamic<T> migrate(Dynamic<T> rules) {
        for (var entry : CobblemonGameRuleDefinitions.BY_LEGACY_NAME.entrySet()) {
            var legacy = rules.get(entry.getKey()).result();
            if (legacy.isEmpty()) continue;
            String target = entry.getValue().identifier().toString();
            // An explicit modern setting wins over a stale legacy copy.
            if (rules.get(target).result().isPresent()) {
                rules = rules.remove(entry.getKey());
                continue;
            }
            var value = legacy.get();
            var text = value.asString().result();
            boolean converted;
            if (text.isPresent() && (text.get().equalsIgnoreCase("true") || text.get().equalsIgnoreCase("false"))) {
                converted = Boolean.parseBoolean(text.get());
            } else {
                converted = value.asBoolean().result().orElseThrow(() ->
                        new IllegalArgumentException("Invalid legacy Cobblemon gamerule: " + entry.getKey()));
            }
            rules = rules.remove(entry.getKey()).set(target, rules.createBoolean(converted));
        }
        return rules;
    }

    private LegacyGameRules() {}
}
