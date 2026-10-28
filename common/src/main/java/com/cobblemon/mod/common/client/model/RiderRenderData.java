/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.client.model;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class RiderRenderData {
    public static final RiderRenderData EMPTY = new RiderRenderData(RiderPose.EMPTY, new Matrix4f(), 0F);
    private final RiderPose pose;
    private final Matrix4f seat;
    private final float recall;
    private final boolean recalling;
    public RiderRenderData(RiderPose pose, Matrix4fc seat, float recall) {
        this(pose, seat, recall, recall > 0F);
    }
    public RiderRenderData(RiderPose pose, Matrix4fc seat, float recall, boolean recalling) {
        this.pose = pose; this.seat = new Matrix4f(seat); this.recall = recall; this.recalling = recalling;
    }
    public RiderPose pose() { return pose; }
    public Matrix4f seat() { return new Matrix4f(seat); }
    public boolean recalling() { return recalling; }
    public float recall() { return recall; }
}
