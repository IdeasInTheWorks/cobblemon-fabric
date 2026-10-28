/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.mixin.client;
import com.cobblemon.mod.common.OrientationControllable;
import com.cobblemon.mod.common.client.model.*;
import com.cobblemon.mod.common.client.render.player.FirstPersonRecallBallRenderer;
import com.cobblemon.mod.common.item.PokedexItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(FirstPersonHandsAndItems.class)
public abstract class FirstPersonHandsAndItemsMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"), require = 1)
    private void cobblemon$capture(LocalPlayer player, float partialTick, FirstPersonHandsAndItemsRenderState state, CallbackInfo ci) {
        var output = new ModelBufferSource();
        boolean recalling = FirstPersonRecallBallRenderer.INSTANCE.render(new PoseStack(), output,
                net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(player, partialTick), partialTick);
        boolean hidePokedex = player.isUsingItem() && player.getUseItem().getItem() instanceof PokedexItem;
        boolean suppressBob = player.getVehicle() instanceof OrientationControllable controllable
                && controllable.getOrientationController().isActive();
        ((FirstPersonRenderStateAccess)state).cobblemon$setFirstPersonData(
                new FirstPersonRenderData(hidePokedex, suppressBob, recalling, output.snapshot()));
    }
}
