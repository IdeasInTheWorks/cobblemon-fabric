/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.entity.boat;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;

/** Binds the native final drop getter to synchronized custom variants after construction. */
public final class BoatDropItem implements Supplier<Item> {
    private Supplier<Item> delegate;

    public void bind(Supplier<Item> supplier) {
        if (delegate != null) throw new IllegalStateException("Boat drop supplier already bound");
        delegate = Objects.requireNonNull(supplier);
    }

    @Override public Item get() {
        return Objects.requireNonNull(Objects.requireNonNull(delegate, "Boat drop supplier not bound").get());
    }
}
