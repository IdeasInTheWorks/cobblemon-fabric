/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.api.net.serializers;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;

/** NBT is mutable: entity-data snapshots must own a deep copy. */
public final class ShoulderTagDataSerializer implements EntityDataSerializer<CompoundTag> {
    public static final ShoulderTagDataSerializer INSTANCE = new ShoulderTagDataSerializer();
    @Override public StreamCodec<ByteBuf, CompoundTag> codec() { return ByteBufCodecs.COMPOUND_TAG; }
    @Override public CompoundTag copy(CompoundTag value) { return value.copy(); }
    private ShoulderTagDataSerializer() {}
}
