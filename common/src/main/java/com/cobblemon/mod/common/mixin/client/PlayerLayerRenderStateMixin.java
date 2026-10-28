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
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
@Mixin(AvatarRenderState.class)
public abstract class PlayerLayerRenderStateMixin implements PlayerLayerRenderStateAccess {
    @Unique private final Map<String, ModelBufferSource.Snapshot> cobblemon$layerSnapshots = new HashMap<>();
    public Map<String, ModelBufferSource.Snapshot> cobblemon$layerSnapshots() { return cobblemon$layerSnapshots; }
}
