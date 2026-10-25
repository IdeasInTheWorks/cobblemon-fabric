/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.util;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

/** Preserves held-stack transformations and legacy sided swing behavior with target results. */
public final class ItemUseResults {
    private ItemUseResults() {}
    public static InteractionResult success(ItemStack stack) {
        return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
    }
    public static InteractionResult consume(ItemStack stack) {
        return InteractionResult.CONSUME.heldItemTransformedTo(stack);
    }
    // Pass/fail callers retain their original in-hand stack; neither outcome replaces it.
    public static InteractionResult pass(ItemStack stack) { return InteractionResult.PASS; }
    public static InteractionResult fail(ItemStack stack) { return InteractionResult.FAIL; }
    public static InteractionResult sidedSuccess(ItemStack stack, boolean clientSide) {
        return withStack(clientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME, stack);
    }
    public static InteractionResult withStack(InteractionResult result, ItemStack stack) {
        return result instanceof InteractionResult.Success success ? success.heldItemTransformedTo(stack) : result;
    }
}
