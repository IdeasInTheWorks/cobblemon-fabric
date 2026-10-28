/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.mixin.client;
import com.cobblemon.mod.common.client.model.*;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
@Mixin(FirstPersonHandsAndItemsRenderState.class)
public abstract class FirstPersonRenderStateMixin implements FirstPersonRenderStateAccess {
    @Unique private FirstPersonRenderData cobblemon$data = FirstPersonRenderData.EMPTY;
    public FirstPersonRenderData cobblemon$firstPersonData() { return cobblemon$data; }
    public void cobblemon$setFirstPersonData(FirstPersonRenderData data) { cobblemon$data = data; }
}
