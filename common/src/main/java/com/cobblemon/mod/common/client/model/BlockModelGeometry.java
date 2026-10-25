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
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.util.RandomSource;

/** Collects the unculled geometry used by the original standalone fossil/mulch models. */
public final class BlockModelGeometry {
    private BlockModelGeometry() { }

    public static void capture(BlockStateModel model, RandomSource random, PoseStack pose,
                               VertexConsumer target, int color, int light, int overlay) {
        var parts = new ArrayList<BlockStateModelPart>();
        model.collectParts(random, parts);
        var instance = new QuadInstance();
        instance.setColor(color);
        instance.setLightCoords(light);
        instance.setOverlayCoords(overlay);
        for (var part : parts) for (var quad : part.getQuads(null)) target.putBakedQuad(pose.last(), quad, instance);
    }
}
