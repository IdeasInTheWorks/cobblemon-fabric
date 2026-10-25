/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.util;

import com.mojang.serialization.Dynamic;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryMap;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

/** A brain whose activities are installed by Cobblemon's behaviour configuration after construction. */
public final class ScriptedBrain<E extends LivingEntity> extends Brain<E> {
    public ScriptedBrain(Collection<? extends MemoryModuleType<?>> memories,
                         Collection<? extends SensorType<? extends Sensor<? super E>>> sensors,
                         Brain.Packed packed, RandomSource random) {
        // Same constructor contract as Provider.makeBrain, with no initial activities:
        // BehaviourConfigurationContext installs the complete configured activity set.
        super(memories, sensors, List.of(), packed.memories(), random);
    }

    public static Brain.Packed read(Dynamic<?> dynamic, HolderLookup.Provider lookup) {
        var ops = lookup.createSerializationContext(NbtOps.INSTANCE);
        return Brain.Packed.CODEC.parse(ops, dynamic.convert(NbtOps.INSTANCE).getValue()).getOrThrow();
    }

    public static Dynamic<Tag> dynamic(Brain.Packed packed, HolderLookup.Provider lookup) {
        var ops = lookup.createSerializationContext(NbtOps.INSTANCE);
        return new Dynamic<>(ops, Brain.Packed.CODEC.encodeStart(ops, packed).getOrThrow());
    }

    /** Live reconfiguration also retains runtime-only memories, unlike the disk-oriented Brain.pack(). */
    public static Brain.Packed snapshot(Brain<?> brain) {
        var memories = new MemoryMap.Builder();
        brain.forEach(new Brain.Visitor() {
            public <U> void acceptEmpty(MemoryModuleType<U> type) {}
            public <U> void accept(MemoryModuleType<U> type, U value) {
                memories.add(type, ExpirableValue.of(value));
            }
            public <U> void accept(MemoryModuleType<U> type, U value, long ttl) {
                memories.add(type, ExpirableValue.of(value, ttl));
            }
        });
        return new Brain.Packed(memories.build());
    }
}
