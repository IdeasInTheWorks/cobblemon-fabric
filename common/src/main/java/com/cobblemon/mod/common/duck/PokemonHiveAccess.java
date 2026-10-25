/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.duck;

import net.minecraft.world.entity.Entity;

/** The generic hive entry lifecycle retained after vanilla narrowed addOccupant to Bee. */
public interface PokemonHiveAccess {
    boolean cobblemon$addPokemon(Entity entity);
}
