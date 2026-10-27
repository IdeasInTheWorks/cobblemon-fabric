/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.util;

import java.util.function.Predicate;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** Uses native container slot IDs, including equipment, with one budget for the whole inventory. */
public final class InventoryRemoval {
    public static int removeMatching(Container inventory, int amount, Predicate<ItemStack> rule) {
        if (amount <= 0) return 0;
        int remaining = amount;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && rule.test(stack)) {
                remaining -= inventory.removeItem(slot, remaining).getCount();
            }
        }
        if (remaining != amount) inventory.setChanged();
        return amount - remaining;
    }
    private InventoryRemoval() { }
}
