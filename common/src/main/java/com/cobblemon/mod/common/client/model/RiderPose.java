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
import com.mojang.math.Axis;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;

/** Immutable animation deltas, extracted without retaining a player, Pokemon or mutable model. */
public record RiderPose(Map<String, Part> parts) {
    public static final RiderPose EMPTY = new RiderPose(Map.of());
    public RiderPose { parts = Map.copyOf(parts); }
    public boolean mounted() { return !parts.isEmpty(); }
    public record Part(float x, float y, float z, float rx, float ry, float rz, float sx, float sy, float sz) {
        public static Part capture(ModelPart part) {
            return new Part(part.x, part.y, part.z, part.xRot, part.yRot, part.zRot, part.xScale, part.yScale, part.zScale);
        }
        public void apply(ModelPart target) {
            target.x += x; target.y += y; target.z += z;
            target.xRot = rx; target.yRot = ry; target.zRot = rz;
            target.xScale *= sx; target.yScale *= sy; target.zScale *= sz;
        }
    }
    public void apply(String name, ModelPart target) {
        var part = parts.get(name); if (part != null) part.apply(target);
    }
    public void applyRoot(PoseStack pose) {
        var root = parts.get("body");
        if (root == null) return;
        pose.translate(-root.x / 16F, (root.y - 10F) / 16F, root.z / 16F);
        pose.rotate(Axis.ZP.rotation(root.rz));
        pose.rotate(Axis.YP.rotation(root.ry));
        pose.rotate(Axis.XP.rotation(root.rx));
    }
}
