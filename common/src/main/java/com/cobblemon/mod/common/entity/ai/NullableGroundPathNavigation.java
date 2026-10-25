/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.jspecify.annotations.Nullable;

/** Exposes vanilla's actual nullable path result to Kotlin under the target's null-marked package. */
public abstract class NullableGroundPathNavigation extends GroundPathNavigation {
    protected NullableGroundPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    // In 26.3 GroundPathNavigation.createPath explicitly returns null for an unloaded chunk.
    // Preserve that outcome and Cobblemon's unreachable-path handling instead of forcing a path.
    @Override
    public @Nullable Path createPath(BlockPos target, int reachRange) {
        return super.createPath(target, reachRange);
    }

    @Override
    public @Nullable Path createPath(Entity target, int reachRange) {
        return super.createPath(target, reachRange);
    }
}
