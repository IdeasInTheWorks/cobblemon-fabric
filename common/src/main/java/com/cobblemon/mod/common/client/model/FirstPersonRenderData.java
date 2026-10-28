/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.client.model;
/** No player, stack, clock or animator references survive first-person extraction. */
public record FirstPersonRenderData(boolean hidePokedex, boolean suppressViewBob, boolean recalling,
                                    ModelBufferSource.Snapshot recall) {
    public static final FirstPersonRenderData EMPTY = new FirstPersonRenderData(false, false, false, ModelBufferSource.Snapshot.EMPTY);
}
