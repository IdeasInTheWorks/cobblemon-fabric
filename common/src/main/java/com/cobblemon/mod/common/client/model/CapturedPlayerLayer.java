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
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.joml.Matrix4fc;

/** Live player/model reads occur only in extract; native layer submission consumes frozen geometry. */
public abstract class CapturedPlayerLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final String key;
    protected CapturedPlayerLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent, String key) {
        super(parent); this.key = key;
    }
    public final void extract(AbstractClientPlayer player, AvatarRenderState state, float partialTick, Matrix4fc worldBody) {
        var buffers = new ModelBufferSource();
        capture(player, state, partialTick, new PoseStack(), buffers, worldBody);
        ((PlayerLayerRenderStateAccess)state).cobblemon$layerSnapshots().put(key, buffers.snapshot());
    }
    protected abstract void capture(AbstractClientPlayer player, AvatarRenderState state, float partialTick,
                                    PoseStack pose, ModelBufferSource buffers, Matrix4fc worldBody);
    @Override public final void submit(PoseStack pose, SubmitNodeCollector collector, int light,
                                        AvatarRenderState state, float yaw, float pitch) {
        ((PlayerLayerRenderStateAccess)state).cobblemon$layerSnapshots()
                .getOrDefault(key, ModelBufferSource.Snapshot.EMPTY).submitWithOutline(pose, collector, state.outlineColor);
    }
}
