/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.mixin;

import com.cobblemon.mod.common.CobblemonRecipeBookTypes;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.inventory.RecipeBookType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps the cooking UI setting separate from the fixed four-type vanilla wire codec. */
@Mixin(RecipeBookSettings.class)
public class RecipeBookSettingsMixin {
    @Unique private RecipeBookSettings.TypeSettings cobblemon$cookingPot = new RecipeBookSettings.TypeSettings(true, true);

    @Inject(method = "getSettings", at = @At("HEAD"), cancellable = true)
    private void cobblemon$getSettings(RecipeBookType type, CallbackInfoReturnable<RecipeBookSettings.TypeSettings> cir) {
        if (type == CobblemonRecipeBookTypes.INSTANCE.getCOOKING_POT()) cir.setReturnValue(cobblemon$cookingPot);
    }

    @Inject(method = "setOpen", at = @At("HEAD"), cancellable = true)
    private void cobblemon$setOpen(RecipeBookType type, boolean open, CallbackInfo ci) {
        if (type == CobblemonRecipeBookTypes.INSTANCE.getCOOKING_POT()) {
            cobblemon$cookingPot = cobblemon$cookingPot.setOpen(open);
            ci.cancel();
        }
    }

    @Inject(method = "setFiltering", at = @At("HEAD"), cancellable = true)
    private void cobblemon$setFiltering(RecipeBookType type, boolean filtering, CallbackInfo ci) {
        if (type == CobblemonRecipeBookTypes.INSTANCE.getCOOKING_POT()) {
            cobblemon$cookingPot = cobblemon$cookingPot.setFiltering(filtering);
            ci.cancel();
        }
    }

    @Inject(method = "copy", at = @At("RETURN"))
    private void cobblemon$copy(CallbackInfoReturnable<RecipeBookSettings> cir) {
        ((RecipeBookSettingsMixin)(Object)cir.getReturnValue()).cobblemon$cookingPot = cobblemon$cookingPot;
    }

    @Inject(method = "replaceFrom", at = @At("TAIL"))
    private void cobblemon$replaceFrom(RecipeBookSettings other, CallbackInfo ci) {
        cobblemon$cookingPot = ((RecipeBookSettingsMixin)(Object)other).cobblemon$cookingPot;
    }
}
