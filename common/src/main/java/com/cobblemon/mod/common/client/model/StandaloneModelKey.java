/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.client.model;

import net.minecraft.resources.Identifier;
import java.util.Objects;

/** Retains the old identifier/variant identity independently of removed vanilla model keys. */
public record StandaloneModelKey(Identifier id, String variant) {
    public StandaloneModelKey { Objects.requireNonNull(id); Objects.requireNonNull(variant); }
    @Override public String toString() { return id + "#" + variant; }
}
