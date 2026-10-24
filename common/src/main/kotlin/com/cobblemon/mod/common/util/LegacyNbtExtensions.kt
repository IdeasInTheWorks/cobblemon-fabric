/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.util

import com.cobblemon.mod.common.util.getUUID
import com.cobblemon.mod.common.util.putUUID
import com.cobblemon.mod.common.util.hasUUID
import com.cobblemon.mod.common.util.getList
import com.cobblemon.mod.common.util.contains

import java.util.UUID
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag

fun CompoundTag.contains(key: String, type: Int): Boolean = LegacyNbt.contains(this, key, type)
fun CompoundTag.getList(key: String, elementType: Int): ListTag = LegacyNbt.getList(this, key, elementType)
fun CompoundTag.getUUID(key: String): UUID = LegacyNbt.getUUID(this, key)
fun CompoundTag.putUUID(key: String, value: UUID) = LegacyNbt.putUUID(this, key, value)
fun CompoundTag.hasUUID(key: String): Boolean = LegacyNbt.hasUUID(this, key)
