/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.item;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** Retains the custom upgrade heading removed from vanilla's template constructor. */
public class CobblemonSmithingTemplateItem extends SmithingTemplateItem {
    private final Component upgradeHeading;

    public CobblemonSmithingTemplateItem(Component appliesTo, Component ingredients, Component upgradeHeading,
                                        Component baseSlotDescription, Component additionsSlotDescription,
                                        List<Identifier> baseIcons, List<Identifier> additionIcons, Properties properties) {
        super(appliesTo, ingredients, baseSlotDescription, additionsSlotDescription, baseIcons, additionIcons, properties);
        this.upgradeHeading = upgradeHeading;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(upgradeHeading);
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
