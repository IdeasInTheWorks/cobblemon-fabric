/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.api.net.serializers;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;

/** A battle identifier is a UUID, not a reference to a living entity. */
public final class OptionalUuidDataSerializer {
    // Retain the legacy presence byte followed by two UUID longs. Optional and UUID are immutable.
    public static final EntityDataSerializer<Optional<UUID>> INSTANCE =
            EntityDataSerializer.forValueType(ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC));

    private OptionalUuidDataSerializer() {}
}
