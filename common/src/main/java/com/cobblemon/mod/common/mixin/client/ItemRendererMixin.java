/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonItemComponents;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.client.model.LegacyItemModels;
import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.item.PokedexItem;
import com.cobblemon.mod.common.item.WearableItem;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemModelResolver.class)
public abstract class ItemRendererMixin {
    @ModifyExpressionValue(method = "appendItemLayers", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"), require = 1)
    private Object cobblemon$selectItemModel(Object original, ItemStackRenderState state, ItemStack stack,
            ItemDisplayContext display, Level level, ItemOwner owner, int seed) {
        var requested = com.cobblemon.mod.common.client.model.NativeItemCapture.requestedModel(stack);
        if (requested != null) return requested;
        Identifier model = null;
        var entity = owner == null ? null : owner.asLivingEntity();
        boolean flat = display == ItemDisplayContext.GUI || display == ItemDisplayContext.FIXED;
        if (stack.getItem() instanceof PokeBallItem ball) {
            model = flat ? ball.getPokeBall().getModel2d() : ball.getPokeBall().getModel3d();
        } else if (stack.getItem() instanceof PokedexItem pokedex) {
            if (flat) model = pokedex.getType().getItemSpritePath();
            else {
                boolean scanning = entity instanceof Player && entity.getUseItem() == stack && entity.isUsingItem();
                boolean canOpen = entity != null && ((entity.getOffhandItem() == stack && !(entity.getMainHandItem().getItem() instanceof PokedexItem)) || entity.getMainHandItem() == stack);
                model = pokedex.getType().getItemModelPath(scanning ? "scanning" : (canOpen ? null : "off"));
            }
        } else if (stack.getItem() == CobblemonItems.TECHNICAL_MACHINE) {
            var move = stack.get(CobblemonItemComponents.TM_MOVE);
            if (move != null) model = Identifier.fromNamespaceAndPath(Cobblemon.MODID, "tm/" + move.getMove().getElementalType().showdownId() + "_tm");
        } else if (stack.getItem() instanceof WearableItem wearable) {
            model = display == ItemDisplayContext.HEAD ? wearable.getModel3d() : wearable.getModel2d();
        }
        return model == null ? original : LegacyItemModels.itemId(model);
    }
}
