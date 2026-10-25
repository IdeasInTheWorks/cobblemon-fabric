/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.entity.ai;

import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Retains zero minimum radius and nullable failed candidates across the new Java signatures. */
public final class LegacyRandomPos {
    public static @Nullable BlockPos direction(RandomSource random, int radius, int yRange, int yOffset,
                                               double x, double z, double angle) {
        return RandomPos.generateRandomDirectionWithinRadians(random, 0.0, radius, yRange, yOffset, x, z, angle);
    }

    public static @Nullable Vec3 select(Supplier<@Nullable BlockPos> candidate,
                                        ToDoubleFunction<@Nullable BlockPos> score) {
        return RandomPos.generateRandomPos(candidate, score);
    }

    private LegacyRandomPos() {}
}
