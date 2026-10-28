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
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;

/** Nested display entities become fresh native states with copied transforms, not live entity render callbacks. */
public final class NativeSceneCapture {
    private static final ThreadLocal<Integer> LIGHT = new ThreadLocal<>();
    public static int light(int normal) { var light = LIGHT.get(); return light == null ? normal : light; }
    public static <T> T withLight(int light, Supplier<T> extract) {
        var prior = LIGHT.get(); LIGHT.set(light);
        try { return extract.get(); } finally { if (prior == null) LIGHT.remove(); else LIGHT.set(prior); }
    }
    public static void capture(Entity entity, float partial, PoseStack pose, ModelBufferSource output, int light) {
        var mc = Minecraft.getInstance();
        var state = withLight(light, () -> mc.getEntityRenderDispatcher().extractEntity(entity, partial));
        state.lightCoords = light;
        state.shadowPieces.clear(); state.shadowRadius = 0F;
        var camera = new CameraRenderState();
        var actualCamera = mc.gameRenderer.mainCamera();
        camera.orientation = new Quaternionf(actualCamera.rotation());
        camera.pos = actualCamera.position();
        camera.yRot = actualCamera.yRot(); camera.xRot = actualCamera.xRot();
        output.addScene(new Submission(state, new Matrix4f(pose.last().pose()), camera));
    }
    public static final class Submission {
        private final EntityRenderState state;
        private final Matrix4fc transform;
        private final CameraRenderState camera;
        private Submission(EntityRenderState state, Matrix4fc transform, CameraRenderState camera) {
            this.state = state; this.transform = transform; this.camera = camera;
        }
        public void submit(PoseStack pose, OrderedSubmitNodeCollector collector) {
            pose.pushPose();
            try {
                pose.mulPose(transform);
                Minecraft.getInstance().getEntityRenderDispatcher().submit(state, camera, 0D, 0D, 0D,
                        pose, (SubmitNodeCollector)collector);
            } finally { pose.popPose(); }
        }
    }
    private NativeSceneCapture() {}
}
