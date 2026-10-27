/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.client.model;

import com.cobblemon.mod.common.api.snowstorm.ParticleMaterials;
import com.cobblemon.mod.common.client.render.SnowstormParticle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;

/** Evaluates particle expressions during extraction; submission only replays frozen vertices. */
public final class SnowstormParticleGroup extends ParticleGroup<SnowstormParticle> {
    private final ParticleRenderType group;
    public SnowstormParticleGroup(ParticleEngine engine, ParticleRenderType group) {
        super(engine); this.group = group;
    }
    @Override public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTick) {
        var buffers = new ModelBufferSource();
        var material = ParticleMaterials.INSTANCE.material(group);
        for (var particle : particles) {
            if (frustum.isVisible(particle.getBoundingBox())) particle.capture(buffers.getBuffer(material), camera, partialTick);
        }
        var snapshot = buffers.snapshot();
        // Particle capture already subtracts the camera position, as the original renderer did.
        return (collector, cameraState) -> snapshot.submit(new PoseStack(), collector);
    }
}
