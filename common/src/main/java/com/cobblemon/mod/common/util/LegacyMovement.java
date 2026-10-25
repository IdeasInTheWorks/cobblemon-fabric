/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.util;

import net.minecraft.world.phys.Vec3;

/** The 1.21.1 walking counter uses horizontal distance and this exact float ordering. */
public final class LegacyMovement {
    public static float walkIncrement(Vec3 movement) {
        return (float) movement.horizontalDistance() * 0.6F;
    }
    private LegacyMovement() {}
}
