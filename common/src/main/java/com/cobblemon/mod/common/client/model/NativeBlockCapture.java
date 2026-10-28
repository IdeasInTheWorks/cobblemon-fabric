/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.client.model;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Resolve native random model parts, tint/AO/light and all uncullled faces now, never from a queued world lookup. */
public final class NativeBlockCapture {
    public static RenderType material(ChunkSectionLayer layer) {
        return switch (layer) {
            case SOLID -> RenderTypes.solidMovingBlock();
            case CUTOUT -> RenderTypes.cutoutMovingBlock();
            case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
        };
    }
    public static boolean hasModel(BlockState state) { return state.getRenderShape() == net.minecraft.world.level.block.RenderShape.MODEL; }
    public static void capture(BlockState state, ClientLevel level, BlockPos pos, PoseStack pose, ModelBufferSource output) {
        if (!hasModel(state)) return;
        var mc = Minecraft.getInstance();
        var model = mc.getModelManager().getBlockStateModelSet().get(state);
        var renderer = new ModelBlockRenderer(mc.options.ambientOcclusion().get(), false, mc.getBlockColors());
        renderer.tesselateBlock((x, y, z, quad, instance) -> {
            pose.pushPose();
            try {
                pose.translate(x, y, z);
                output.getBuffer(material(quad.materialInfo().layer())).putBakedQuad(pose.last(), quad, instance);
            } finally { pose.popPose(); }
        }, 0F, 0F, 0F, level, pos, state, model, state.getSeed(pos));
    }
    private NativeBlockCapture() {}
}
