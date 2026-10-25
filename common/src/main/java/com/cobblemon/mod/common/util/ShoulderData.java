/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.util;

import com.cobblemon.mod.common.api.net.serializers.ShoulderTagDataSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Native server storage remains authoritative; custom metadata restores client shoulder NBT. */
public final class ShoulderData {
    public static final EntityDataAccessor<CompoundTag> LEFT =
            SynchedEntityData.defineId(Player.class, ShoulderTagDataSerializer.INSTANCE);
    public static final EntityDataAccessor<CompoundTag> RIGHT =
            SynchedEntityData.defineId(Player.class, ShoulderTagDataSerializer.INSTANCE);

    /** Force allocation during Player's static initialization, before any subclass data IDs. */
    public static void bootstrap() {}

    public static void define(SynchedEntityData.Builder builder) {
        builder.define(LEFT, new CompoundTag());
        builder.define(RIGHT, new CompoundTag());
    }

    public static CompoundTag get(Player player, boolean left) {
        if (player instanceof ServerPlayer serverPlayer) {
            return left ? serverPlayer.getShoulderEntityLeft() : serverPlayer.getShoulderEntityRight();
        }
        return player.getEntityData().get(left ? LEFT : RIGHT);
    }

    public static void sync(ServerPlayer player, boolean left, CompoundTag tag) {
        player.getEntityData().set(left ? LEFT : RIGHT, tag.copy());
    }

    private ShoulderData() {}
}
