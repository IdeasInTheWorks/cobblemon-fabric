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
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;

/** World-label text and layout frozen on the extraction thread; submission only uses the camera state. */
public record CapturedLabel(double height, double scale, double sizeScale, double offsetScale,
                            int background, List<Line> lines) {
    public record Line(Component text, float x, float y) {
        public Line { text = text.copy(); }
    }
    public CapturedLabel { lines = List.copyOf(lines); }
    public void submit(PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        try {
            pose.translate(0D, height, 0D);
            pose.rotate(camera.orientation);
            pose.translate(0D, offsetScale / 2D, -(scale + offsetScale));
            pose.scale((float)(0.025D * sizeScale), (float)(-0.025D * sizeScale), (float)sizeScale);
            for (var line : lines) {
                var text = line.text().getVisualOrderText();
                collector.submitText(pose, line.x(), line.y(), text, false, Font.DisplayMode.SEE_THROUGH,
                        0xF000F0, 0x20FFFFFF, background, 0);
                collector.submitText(pose, line.x(), line.y(), text, false, Font.DisplayMode.NORMAL,
                        0xF000F0, -1, 0, 0);
            }
        } finally { pose.popPose(); }
    }
}
