/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.client.model.LegacyItemModels;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Supports explicit native cuboid definitions pointing at a registered legacy builtin model. */
@Mixin(CuboidItemModelWrapper.Unbaked.class)
public abstract class BuiltinModelItemRendererMixin {
    @Shadow public abstract Identifier model();
    @Shadow public abstract java.util.Optional<com.mojang.math.Transformation> transformation();
    @Inject(method = "bake", at = @At("HEAD"), cancellable = true, require = 1)
    private void cobblemon$nativeBuiltin(ItemModel.BakingContext context, Matrix4fc transform, CallbackInfoReturnable<ItemModel> cir) {
        var builtin = LegacyItemModels.builtin(model(), com.mojang.math.Transformation.compose(transform, transformation()));
        if (builtin != null) cir.setReturnValue(builtin);
    }
}
