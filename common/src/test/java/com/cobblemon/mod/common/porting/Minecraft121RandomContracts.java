/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.porting;

import com.cobblemon.mod.common.entity.ai.LegacyRandomPos;
import net.minecraft.util.RandomSource;

/** Runs the actual checksum-verified 1.21.1 random implementation alongside the target adapter. */
final class Minecraft121RandomContracts {
    public static void main(String[] args) throws Exception {
        // Official 1.21.1 mappings: RandomSource=ayw, RandomPos=cen, Vec3i=kh.
        // Reflection isolates their obfuscated types; no copies of the random algorithm are used.
        var oldRandom = Class.forName("ayw");
        var oldFactory = oldRandom.getMethod("a", long.class);
        var oldDirection = Class.forName("cen").getMethod("a", oldRandom, int.class, int.class, int.class,
                double.class, double.class, double.class);
        var nextInt = oldRandom.getMethod("a", int.class);
        var coordinates = Class.forName("kh");
        var getX = coordinates.getMethod("u");
        var getY = coordinates.getMethod("v");
        var getZ = coordinates.getMethod("w");
        int cases = 0;
        for (long seed : new long[] {0L, 1L, 42L, Long.MIN_VALUE, Long.MAX_VALUE}) {
            for (int radius : new int[] {0, 1, 2, 7, 16, 32}) {
                for (int height : new int[] {0, 2, 7}) {
                    for (double angle : new double[] {0, (Math.PI / 2F), Math.PI}) {
                        for (double[] vector : new double[][] {{1, 0}, {-3, 7}, {0, 0}}) {
                            var previous = oldFactory.invoke(null, seed);
                            RandomSource current = RandomSource.create(seed);
                            var oldPos = oldDirection.invoke(null, previous, radius, height, -2,
                                    vector[0], vector[1], angle);
                            var newPos = LegacyRandomPos.direction(current, radius, height, -2,
                                    vector[0], vector[1], angle);
                            if ((oldPos == null) != (newPos == null) || oldPos != null &&
                                    ((int) getX.invoke(oldPos) != newPos.getX() ||
                                     (int) getY.invoke(oldPos) != newPos.getY() ||
                                     (int) getZ.invoke(oldPos) != newPos.getZ())) {
                                throw new AssertionError("Legacy direction differs: seed=" + seed + " radius=" + radius +
                                        " height=" + height + " old=" + oldPos + " new=" + newPos);
                            }
                            if ((int) nextInt.invoke(previous, 10000) != current.nextInt(10000)) {
                                throw new AssertionError("Random consumption changed after direction sampling");
                            }
                            cases++;
                        }
                    }
                }
            }
        }
        System.out.println("{\"passed\":true,\"cases\":" + cases + ",\"checks\":" + cases * 2 +
                ",\"scope\":\"actual legacy versus target random positions and RNG consumption; no game launch\"}");
    }
}
