/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Assign stable namespaced IDs before constructors derive descriptions, models and loot. */
public final class RegistrationProperties {
    private RegistrationProperties() {}

    public static Item.Properties item(String path) {
        return item(path, new Item.Properties());
    }

    public static Item.Properties item(String path, Item.Properties properties) {
        return properties.setId(ResourceKey.create(Registries.ITEM, id(path)));
    }

    public static BlockBehaviour.Properties block(String path, BlockBehaviour.Properties properties) {
        return properties.setId(ResourceKey.create(Registries.BLOCK, id(path)));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("cobblemon", path);
    }
}
