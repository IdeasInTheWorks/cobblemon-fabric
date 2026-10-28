/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.mixin.client;
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState;
import com.cobblemon.mod.common.entity.PosableEntity;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(EntityHitboxDebugRenderer.class)
public abstract class EntityHitboxDebugRendererMixin {
    @Inject(method = "showHitboxes", at = @At("TAIL"), require = 1)
    private void cobblemon$locators(Entity entity, float partialTick, boolean serverEntity, CallbackInfo ci) {
        if (!serverEntity && entity instanceof PosableEntity posable && posable.getDelegate() instanceof PosableState state) {
            var offset = entity.getPosition(partialTick).subtract(entity.position());
            state.getLocatorStates().forEach((name, locator) -> Gizmos.cuboid(
                    AABB.ofSize(locator.getOrigin().add(offset), 0.25, 0.25, 0.25), GizmoStyle.stroke(0xFF00FF00)));
        }
    }
}
