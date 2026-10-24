/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.util;

import java.util.UUID;
import java.util.Optional;
import java.util.function.Consumer;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Preserves the on-disk 1.21.1 contracts using the supported optional NBT API. */
public final class LegacyNbt {
    // CompoundTag is always a map. This keeps legacy root fields flat, without a wrapper key.
    private static final MapCodec<CompoundTag> ROOT_CODEC = MapCodec.assumeMapUnsafe(CompoundTag.CODEC);
    private LegacyNbt() {}

    public static CompoundTag read(ValueInput input) {
        return input.read(ROOT_CODEC).orElseThrow(
                () -> new IllegalArgumentException("Cannot decode legacy Cobblemon compound"));
    }

    public static void write(ValueOutput output, CompoundTag tag) {
        output.store(ROOT_CODEC, tag);
    }

    /** Adapts legacy NBT entry points without losing the caller's registry context. */
    public static CompoundTag toTag(HolderLookup.Provider lookup, Consumer<ValueOutput> writer) {
        var problems = new ProblemReporter.Collector();
        var output = TagValueOutput.createWithContext(problems, lookup);
        writer.accept(output);
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Cannot save Cobblemon data: " + problems.getReport());
        }
        return output.buildResult();
    }

    // NbtUtils in 1.21.1 used exactly three ints, not a packed long or an X/Y/Z map.
    public static IntArrayTag writeBlockPos(BlockPos pos) {
        return new IntArrayTag(new int[] {pos.getX(), pos.getY(), pos.getZ()});
    }

    public static Optional<BlockPos> readBlockPos(CompoundTag tag, String key) {
        return tag.getIntArray(key).filter(value -> value.length == 3)
                .map(value -> new BlockPos(value[0], value[1], value[2]));
    }

    public static boolean contains(CompoundTag tag, String key, int type) {
        Tag value = tag.get(key);
        return value != null && (type == 99 ? value instanceof NumericTag : value.getId() == type);
    }

    public static ListTag getList(CompoundTag tag, String key, int elementType) {
        ListTag list = tag.getListOrEmpty(key);
        // Modern lists may be heterogeneous. Old typed reads rejected a mismatched list.
        return list.isEmpty() || list.stream().allMatch(value -> value.getId() == elementType)
                ? list : new ListTag();
    }

    public static UUID getUUID(CompoundTag tag, String key) {
        int[] values = tag.getIntArray(key).orElseThrow(
                () -> new IllegalArgumentException("Expected UUID int array at " + key));
        if (values.length != 4) throw new IllegalArgumentException("Expected four UUID ints at " + key);
        return UUIDUtil.uuidFromIntArray(values);
    }

    public static void putUUID(CompoundTag tag, String key, UUID value) {
        tag.putIntArray(key, UUIDUtil.uuidToIntArray(value));
    }

    public static boolean hasUUID(CompoundTag tag, String key) {
        return tag.getIntArray(key).filter(value -> value.length == 4).isPresent();
    }
}
