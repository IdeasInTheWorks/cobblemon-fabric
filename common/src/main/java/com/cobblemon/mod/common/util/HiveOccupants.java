/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;

/** Typed hive data retains the original Pokemon payload and legacy placeholder properties. */
public final class HiveOccupants {
    public static final String LEGACY_PLACEHOLDER = "cobblemon_pokemon_entity_placeholder";
    public static final String POKEMON_ID = "cobblemon:pokemon";
    // Retain the upstream root-only exclusions; Pokemon ownership/UUIDs nested below Pokemon stay intact.
    public static final List<String> IGNORED_TAGS = List.of("Air", "ArmorDropChances", "ArmorItems",
            "CanPickUpLoot", "DeathTime", "FallDistance", "FallFlying", "Fire", "HandDropChances",
            "HandItems", "HurtByTimestamp", "HurtTime", "LeftHanded", "Motion", "NoGravity", "OnGround",
            "PortalCooldown", "Pos", "Rotation", "Passengers", "leash", "UUID");

    public static BeehiveBlockEntity.Occupant snapshot(Entity entity, boolean hasNectar) {
        var saved = LegacyNbt.toTag(entity.registryAccess(), entity::save);
        return snapshot(entity.getType(), saved, hasNectar);
    }

    public static BeehiveBlockEntity.Occupant snapshot(EntityType<?> type, CompoundTag saved, boolean hasNectar) {
        var cleaned = saved.copy();
        IGNORED_TAGS.forEach(cleaned::remove);
        return new BeehiveBlockEntity.Occupant(TypedEntityData.of(type, cleaned), 0, hasNectar ? 2400 : 600);
    }

    public static BeehiveBlockEntity.Occupant placeholder(EntityType<?> pokemonType, String properties, int ticks) {
        var data = new CompoundTag();
        data.putString("pokemonProperties", properties);
        return new BeehiveBlockEntity.Occupant(TypedEntityData.of(pokemonType, data), ticks, 600);
    }

    public static @Nullable Entity restore(TypedEntityData<EntityType<?>> saved, Level level) {
        return EntityType.loadEntityRecursive(saved.type(), saved.copyTagWithoutId(), level,
                EntitySpawnReason.LOAD, EntityProcessor.NOP);
    }

    /** Wrap the native typed-data codec before its registry-ID decoder sees the old unregistered ID. */
    public static <I> Codec<TypedEntityData<I>> legacyCodec(Codec<TypedEntityData<I>> delegate) {
        return new Codec<>() {
            @Override public <T> DataResult<Pair<TypedEntityData<I>, T>> decode(DynamicOps<T> ops, T input) {
                var data = new Dynamic<>(ops, input);
                String id = data.get("id").asString("");
                if (id.equals(LEGACY_PLACEHOLDER) || id.equals("minecraft:" + LEGACY_PLACEHOLDER)) {
                    var properties = data.get("pokemonProperties").asString().result();
                    if (properties.isEmpty() || properties.get().isBlank()) {
                        return DataResult.error(() -> "Legacy Pokemon hive placeholder has no valid pokemonProperties");
                    }
                    data = data.set("id", data.createString(POKEMON_ID));
                }
                return delegate.decode(ops, data.getValue());
            }

            @Override public <T> DataResult<T> encode(TypedEntityData<I> value, DynamicOps<T> ops, T prefix) {
                return delegate.encode(value, ops, prefix);
            }
        };
    }

    private HiveOccupants() {}
}
