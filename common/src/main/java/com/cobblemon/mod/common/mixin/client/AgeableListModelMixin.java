/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.client.model.RiderModelAccess;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import org.spongepowered.asm.mixin.Mixin;

/** Apply only the pose associated with this model's extracted state; always balance the stack. */
@Mixin(Model.class)
public abstract class AgeableListModelMixin {
    @WrapMethod(method = "renderToBuffer")
    private void cobblemon$renderRoot(PoseStack pose, VertexConsumer vertices, int light, int overlay, int color, Operation<Void> original) {
        pose.pushPose();
        try {
            if ((Object)this instanceof RiderModelAccess rider) rider.cobblemon$getRiderPose().applyRoot(pose);
            original.call(pose, vertices, light, overlay, color);
        } finally { pose.popPose(); }
    }
}
