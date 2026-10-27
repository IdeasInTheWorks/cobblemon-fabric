/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.client.model;

import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.resources.Identifier;

/** Native descriptors for the old custom model layers; no immediate GPU state changes. */
public final class NativeRenderSetups {
    private static final List<RenderPipeline> PIPELINES = new ArrayList<>();
    private static final RenderPipeline[] PARTICLES = new RenderPipeline[4];
    private static final RenderPipeline[] ENTITY = new RenderPipeline[8];
    private static final OitPipelineSet[] OIT = new OitPipelineSet[8];
    private static final RenderPipeline BERRIES = copy(RenderPipelines.CUTOUT_BLOCK, "berries", false, null);

    static {
        for (int key = 0; key < 8; key++) {
            boolean emissive = (key & 4) != 0;
            boolean translucent = (key & 2) != 0;
            boolean translucentCull = (key & 1) != 0;
            RenderPipeline base = emissive ? RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE
                    : translucent ? (translucentCull ? RenderPipelines.ENTITY_TRANSLUCENT_CULL
                    : RenderPipelines.ENTITY_TRANSLUCENT) : RenderPipelines.ENTITY_CUTOUT_CULL;
            // The legacy custom path always enabled culling and color/depth writes.
            ENTITY[key] = copy(base, "entity_" + key, true, translucent);
            if (translucent) {
                OitPipelineSet source = emissive ? RenderPipelines.OIT_ENTITY_EMISSIVE
                        : translucentCull ? RenderPipelines.OIT_ENTITY_CULL : RenderPipelines.OIT_ENTITY;
                OIT[key] = new OitPipelineSet(
                        copy(source.depthBoundsPipeline(), "entity_" + key + "_depth", true, null),
                        copy(source.transmittancePipeline(), "entity_" + key + "_transmittance", true, null),
                        copy(source.accumulatePipeline(), "entity_" + key + "_accumulate", true, null));
            }
        }
        for (int key = 0; key < 4; key++) {
            var source = RenderPipelines.TRANSLUCENT_PARTICLE;
            // Preserve the old blendFunc for color AND alpha, including SRC_COLOR/ZERO for OPAQUE.
            var src = key == 3 ? com.mojang.renderpearl.api.pipeline.BlendFactor.SRC_COLOR
                    : com.mojang.renderpearl.api.pipeline.BlendFactor.SRC_ALPHA;
            var dst = key == 1 ? com.mojang.renderpearl.api.pipeline.BlendFactor.ONE
                    : key == 3 ? com.mojang.renderpearl.api.pipeline.BlendFactor.ZERO
                    : com.mojang.renderpearl.api.pipeline.BlendFactor.ONE_MINUS_SRC_ALPHA;
            PARTICLES[key] = RenderPipeline.builder(snippet(source, key == 0 || key == 3))
                    .withLocation(Identifier.fromNamespaceAndPath("cobblemon", "pipeline/snowstorm_material_" + key))
                    .withColorTargetState(new ColorTargetState(new com.mojang.renderpearl.api.pipeline.BlendFunction(src, dst)))
                    .withDepthStencilState(new DepthStencilState(source.getDepthStencilState().depthTest(), true,
                            source.getDepthStencilState().depthBiasScaleFactor(), source.getDepthStencilState().depthBiasConstant()))
                    .build();
            PIPELINES.add(PARTICLES[key]);
        }
    }

    private NativeRenderSetups() { }

    /** Called once during client initialization so native shader discovery includes our descriptors. */
    public static void register(Consumer<RenderPipeline> registrar) {
        PIPELINES.forEach(registrar);
    }

    public static RenderSetup berries(Identifier texture) {
        return RenderSetup.builder(BERRIES).withTexture("Sampler0", texture,
                () -> RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST, true))
                .useLightmap().affectsCrumbling()
                .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).createRenderSetup();
    }

    public static RenderSetup entity(Identifier texture, boolean emissive, boolean translucent, boolean translucentCull) {
        int key = (emissive ? 4 : 0) | (translucent ? 2 : 0) | (translucentCull ? 1 : 0);
        var builder = RenderSetup.builder(ENTITY[key]).withTexture("Sampler0", texture,
                () -> RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST, false))
                .useOverlay().affectsCrumbling().setOutline(RenderSetup.OutlineProperty.NONE);
        if (!emissive) builder.useLightmap();
        if (translucent) builder.sortOnUpload().setOitPipelines(OIT[key]);
        return builder.createRenderSetup();
    }

    public static RenderSetup particle(int material) {
        return RenderSetup.builder(PARTICLES[material])
                .withTexture("Sampler0", net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_PARTICLES,
                    () -> RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST, false))
                .useLightmap().createRenderSetup();
    }

    private static RenderPipeline.Snippet snippet(RenderPipeline source, boolean cull) {
        var colors = source.getColorTargetStates().toArray(ColorTargetState[]::new);
        return new RenderPipeline.Snippet(source.getShaders(), Optional.of(source.getShaderDefines()),
                Optional.of(source.getBindGroupLayouts()), colors, colors.length,
                Optional.ofNullable(source.getDepthStencilState()), Optional.of(source.getPolygonMode()),
                Optional.of(cull), source.getVertexFormatBindings().toArray(VertexFormat[]::new),
                Optional.of(source.getPrimitiveTopology()), source.pushConstantSize());
    }

    private static RenderPipeline copy(RenderPipeline source, String name, boolean cull, Boolean translucent) {
        var snippet = snippet(source, cull);
        var builder = RenderPipeline.builder(snippet).withLocation(Identifier.fromNamespaceAndPath("cobblemon", "pipeline/" + name));
        if (translucent != null) {
            if (!translucent) builder.withColorTargetState(ColorTargetState.DEFAULT);
            var depth = source.getDepthStencilState();
            builder.withDepthStencilState(new DepthStencilState(depth.depthTest(), true, depth.depthBiasScaleFactor(), depth.depthBiasConstant()));
        }
        var pipeline = builder.build();
        PIPELINES.add(pipeline);
        return pipeline;
    }
}
