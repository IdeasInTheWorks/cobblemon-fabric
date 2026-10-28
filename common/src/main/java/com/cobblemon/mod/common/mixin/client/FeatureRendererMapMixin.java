/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.mixin.client;
import com.cobblemon.mod.common.client.model.FrontLitFeatures;
import net.minecraft.client.renderer.feature.FeatureRendererMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(FeatureRendererMap.class)
public abstract class FeatureRendererMapMixin {
    @Inject(method = "<init>", at = @At("TAIL"), require = 1)
    private void cobblemon$frontLighting(CallbackInfo ci) { FrontLitFeatures.install((FeatureRendererMap)(Object)this); }
}
