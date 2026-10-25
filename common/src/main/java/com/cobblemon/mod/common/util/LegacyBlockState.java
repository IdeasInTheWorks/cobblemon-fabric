/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.util;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Legacy support policy for callbacks that now require an explicit block predicate. */
public final class LegacyBlockState {
    private LegacyBlockState() {}

    public static boolean blocksMotion(BlockState state) {
        // 1.21.1 excluded these two otherwise-solid blocks before checking solidity.
        return !state.is(Blocks.COBWEB) && !state.is(Blocks.BAMBOO_SAPLING) && state.isSolid();
    }
}
