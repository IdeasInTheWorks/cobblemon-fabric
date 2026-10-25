/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.entity.ai;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Legacy nearby-player queries test the player's position, not intersecting hitboxes. */
public final class LegacyEntityQueries {
    public static List<Player> nearbyPlayers(ServerLevel level, LivingEntity source, AABB bounds,
                                             TargetingConditions targeting) {
        return select(level.players(), bounds, Player::position, player -> targeting.test(level, source, player));
    }

    public static <T> List<T> select(List<? extends T> players, AABB bounds, Function<T, Vec3> position,
                                     Predicate<T> targeting) {
        return players.stream().filter(player -> bounds.contains(position.apply(player)) && targeting.test(player))
                .map(player -> (T) player).toList();
    }

    private LegacyEntityQueries() {}
}
