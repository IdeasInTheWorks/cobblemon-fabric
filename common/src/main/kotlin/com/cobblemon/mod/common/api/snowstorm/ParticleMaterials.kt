/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.snowstorm

import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.renderer.rendertype.RenderType
import com.cobblemon.mod.common.client.model.NativeRenderSetups

/** Group identities are separate from immutable native pipeline/material descriptors. */
object ParticleMaterials {
    val ALPHA = ParticleRenderType("COBBLEMON_ALPHA", "CA")
    val ADD = ParticleRenderType("COBBLEMON_ADD", "CD")
    val BLEND = ParticleRenderType("COBBLEMON_BLEND", "CB")
    val OPAQUE = ParticleRenderType("COBBLEMON_OPAQUE", "CO")
    private val groups = listOf(ALPHA, ADD, BLEND, OPAQUE)
    private val materials by lazy { groups.indices.map { RenderType.create("cobblemon_snowstorm_$it", NativeRenderSetups.particle(it)) } }
    fun isSnowstorm(group: ParticleRenderType) = group in groups
    fun material(group: ParticleRenderType): RenderType = materials[groups.indexOf(group)]
}
