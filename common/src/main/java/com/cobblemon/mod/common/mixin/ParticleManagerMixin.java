/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.api.snowstorm.ParticleMaterials;
import com.google.common.collect.ImmutableList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.cobblemon.mod.common.client.model.SnowstormParticleGroup;
import net.minecraft.client.particle.ParticleGroup;

import java.util.List;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;

@Mixin(ParticleEngine.class)
final class ParticleManagerMixin {
    @Mutable
    @Final
    @Shadow
    private static List<ParticleRenderType> RENDER_ORDER;

    @Inject(method = "createParticleGroup", at = @At("HEAD"), cancellable = true, require = 1)
    private void cobblemon$createGroup(ParticleRenderType type, CallbackInfoReturnable<ParticleGroup<?>> cir) {
        if (ParticleMaterials.INSTANCE.isSnowstorm(type))
            cir.setReturnValue(new SnowstormParticleGroup((ParticleEngine)(Object)this, type));
    }

    @Inject(at = @At("RETURN"), method = "<clinit>")
    private static void lodestone$addTypes(CallbackInfo ci) {
        RENDER_ORDER = ImmutableList.<ParticleRenderType>builder().addAll(RENDER_ORDER)
                .add(ParticleMaterials.INSTANCE.getADD(), ParticleMaterials.INSTANCE.getALPHA(), ParticleMaterials.INSTANCE.getBLEND(), ParticleMaterials.INSTANCE.getOPAQUE()).build();
    }
}