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
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

/** CPU geometry capture for model code migrating from immediate buffers to native submission. */
public final class ModelBufferSource {
    private final List<NativeItemCapture.Submission> items = new ArrayList<>();

    void addItem(NativeItemCapture.Submission item) { items.add(item); }

    private final Map<RenderType, Recorder> buffers = new LinkedHashMap<>();

    public VertexConsumer getBuffer(RenderType type) {
        return buffers.computeIfAbsent(type, ignored -> new Recorder());
    }

    /** Copies all geometry; later writes, model resets and state reuse cannot change a submitted frame. */
    public Snapshot snapshot() {
        var batches = new ArrayList<Batch>();
        buffers.forEach((type, recorder) -> {
            var vertices = recorder.snapshot();
            if (!vertices.isEmpty()) batches.add(new Batch(type, vertices));
        });
        return new Snapshot(List.copyOf(batches), items);
    }

    public record Batch(RenderType type, List<Vertex> vertices) {
        public Batch { vertices = List.copyOf(vertices); }
    }

    public record Snapshot(List<Batch> batches, List<NativeItemCapture.Submission> items) {
        public Snapshot(List<Batch> batches) { this(batches, List.of()); }
        public static final Snapshot EMPTY = new Snapshot(List.of());
        public Snapshot { batches = List.copyOf(batches); items = List.copyOf(items); }

        public void submit(PoseStack pose, OrderedSubmitNodeCollector collector) {
            submit(pose, collector, null, null);
        }

        /** Custom geometry needs an explicit outline submission; native model submissions do this internally. */
        public void submitWithOutline(PoseStack pose, OrderedSubmitNodeCollector collector, int outlineColor) {
            for (var batch : batches) {
                if (!batch.type().isOutline() || outlineColor == 0) {
                    collector.submitCustomGeometry(pose, batch.type(), (transform, consumer) -> {
                        for (var vertex : batch.vertices()) vertex.replay(transform, consumer);
                    });
                }
                if (outlineColor != 0) {
                    var outline = batch.type().isOutline() ? java.util.Optional.of(batch.type()) : batch.type().outline();
                    outline.ifPresent(type -> collector.submitCustomGeometry(pose, type, (transform, consumer) -> {
                        for (var vertex : batch.vertices()) {
                            vertex.replay(transform, consumer);
                            consumer.setColor(outlineColor);
                        }
                    }));
                }
            }
            for (var item : items) item.submit(pose, collector, outlineColor);
        }

        public void submit(PoseStack pose, OrderedSubmitNodeCollector collector,
                           net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling,
                           Integer defaultLight) {
            submit(pose, collector, crumbling, defaultLight, false);
        }

        public void submit(PoseStack pose, OrderedSubmitNodeCollector collector,
                           net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling,
                           Integer defaultLight, boolean improvedTransparency) {
            for (var batch : batches) {
                collector.submitCustomGeometry(pose, batch.type(), (transform, consumer) -> {
                    for (var vertex : batch.vertices()) {
                        vertex.replay(transform, consumer);
                        if (defaultLight != null && vertex.lightU() == 0 && vertex.lightV() == 0)
                            consumer.setLight(defaultLight);
                    }
                });
                if (crumbling != null && batch.type().affectsCrumbling()) {
                    var decalPose = crumbling.cameraPose().copy();
                    var types = improvedTransparency && batch.type().hasBlending()
                            ? net.minecraft.client.resources.model.ModelBakery.DESTROY_TYPES_OIT
                            : net.minecraft.client.resources.model.ModelBakery.DESTROY_TYPES;
                    var type = types.get(crumbling.progress());
                    var ordered = collector instanceof net.minecraft.client.renderer.SubmitNodeCollector nativeCollector
                            ? nativeCollector.order(0) : collector;
                    if (!(ordered instanceof net.minecraft.client.renderer.SubmitNodeCollection collection))
                        throw new IllegalArgumentException("Crumbling requires a native phase collection");
                    var phase = batch.type().hasBlending() ? collection.breakingOverlay : collection.solid;
                    phase.submit(new net.minecraft.client.renderer.feature.CustomFeatureRenderer.Submit(pose.last().copy(), type,
                            (transform, consumer) -> {
                                var decal = new com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator(consumer, decalPose, 1F);
                                for (var vertex : batch.vertices()) vertex.replay(transform, decal);
                            }));
                }
            }
            for (var item : items) item.submit(pose, collector);
        }
    }

    public record Vertex(float x, float y, float z, int color, float u, float v, int overlayU, int overlayV,
                         int lightU, int lightV, float glintU, float glintV, float nx, float ny, float nz,
                         float lineWidth, int attributes) {
        public void replay(PoseStack.Pose pose, VertexConsumer target) {
            target.addVertex(pose, x, y, z);
            if ((attributes & 1) != 0) target.setColor(color);
            if ((attributes & 2) != 0) target.setUv(u, v);
            if ((attributes & 4) != 0) target.setUv1(overlayU, overlayV);
            if ((attributes & 8) != 0) target.setUv2(lightU, lightV);
            if ((attributes & 16) != 0) target.setUv3(glintU, glintV);
            if ((attributes & 32) != 0) target.setNormal(pose, nx, ny, nz);
            if ((attributes & 64) != 0) target.setLineWidth(lineWidth);
        }
    }

    /** Implements every native vertex attribute, including glint UV and line width. */
    public static final class Recorder implements VertexConsumer {
        private final List<Vertex> completed = new ArrayList<>();
        private boolean active;
        private float x, y, z, u, v, gu, gv, nx, ny, nz, width;
        private int color, ou, ov, lu, lv, attributes;

        private void requireVertex() {
            if (!active) throw new IllegalStateException("Add a vertex before assigning attributes");
        }

        private Vertex current() {
            return new Vertex(x, y, z, color, u, v, ou, ov, lu, lv, gu, gv, nx, ny, nz, width, attributes);
        }

        public List<Vertex> snapshot() {
            var copy = new ArrayList<>(completed);
            if (active) copy.add(current());
            return List.copyOf(copy);
        }

        @Override public VertexConsumer addVertex(float x, float y, float z) {
            if (active) completed.add(current());
            active = true;
            this.x = x; this.y = y; this.z = z;
            attributes = 0;
            return this;
        }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) {
            return setColor(a << 24 | r << 16 | g << 8 | b);
        }
        @Override public VertexConsumer setColor(int color) { requireVertex(); this.color = color; attributes |= 1; return this; }
        @Override public VertexConsumer setUv(float u, float v) { requireVertex(); this.u = u; this.v = v; attributes |= 2; return this; }
        @Override public VertexConsumer setUv1(int u, int v) { requireVertex(); ou = u; ov = v; attributes |= 4; return this; }
        @Override public VertexConsumer setUv2(int u, int v) { requireVertex(); lu = u; lv = v; attributes |= 8; return this; }
        @Override public VertexConsumer setUv3(float u, float v) { requireVertex(); gu = u; gv = v; attributes |= 16; return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { requireVertex(); nx = x; ny = y; nz = z; attributes |= 32; return this; }
        @Override public VertexConsumer setLineWidth(float width) { requireVertex(); this.width = width; attributes |= 64; return this; }
    }
}
