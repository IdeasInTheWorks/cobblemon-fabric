/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.feature.*;
import net.minecraft.client.renderer.feature.phase.FeatureRenderPhase;
import net.minecraft.client.renderer.feature.submit.*;
import net.minecraft.client.renderer.oit.OitStage;

/** Per-draw front lighting for captured items; native geometry/material/tint/glint/OIT renderers are retained. */
public final class FrontLitFeatures {
    private record Binding(FeatureRendererType<LitSubmit> type, Supplier<FeatureRenderer<?>> factory) {}
    private static final Map<FeatureRendererType<?>, Binding> BINDINGS = new LinkedHashMap<>();
    static {
        register(ItemFeatureRenderer.TYPE, ItemFeatureRenderer::new);
        register(ModelFeatureRenderer.TYPE, ModelFeatureRenderer::new);
        register(CustomFeatureRenderer.TYPE, CustomFeatureRenderer::new);
        register(BlockModelFeatureRenderer.TYPE, BlockModelFeatureRenderer::new);
        register(MovingBlockFeatureRenderer.TYPE, MovingBlockFeatureRenderer::new);
        register(TextFeatureRenderer.TYPE, TextFeatureRenderer::new);
    }
    /** Custom native item features can opt in before dispatcher construction, without replacing their implementation. */
    public static void register(FeatureRendererType<?> original, Supplier<FeatureRenderer<?>> factory) {
        if (BINDINGS.containsKey(original)) throw new IllegalArgumentException("Duplicate front-light binding: " + original);
        BINDINGS.put(original, new Binding(FeatureRendererType.create("Cobblemon front " + original.name()), factory));
    }
    public record LitSubmit(SubmitNode original, FeatureRendererType<LitSubmit> featureType) implements TranslucentSubmit {
        public float distanceToCameraSq() { return original instanceof TranslucentSubmit t ? t.distanceToCameraSq() : 0F; }
    }
    public static LitSubmit wrap(SubmitNode node) {
        var binding = Objects.requireNonNull(BINDINGS.get(node.featureType()),
                "Custom front-lit item feature must register a FrontLitFeatures binding");
        return new LitSubmit(node, binding.type());
    }
    public static void install(FeatureRendererMap map) {
        BINDINGS.values().forEach(binding -> map.put(binding.type(), new Renderer(binding.factory().get())));
    }
    /** Same two unit vectors as the old GUI locator path, in the native std140 Lighting block. */
    public static ByteBuffer lightBytes() {
        var data = ByteBuffer.allocateDirect(com.mojang.blaze3d.platform.Lighting.UBO_SIZE).order(ByteOrder.nativeOrder());
        return com.mojang.blaze3d.buffers.Std140Builder.intoBuffer(data)
                .putVec3(0F, 0F, 1F).putVec3(0F, 0F, 1F).get();
    }
    public static ByteBuffer lightBytes(int alignment) {
        var data = ByteBuffer.allocateDirect(net.minecraft.util.Mth.roundToward(
                com.mojang.blaze3d.platform.Lighting.UBO_SIZE, alignment)).order(ByteOrder.nativeOrder());
        data.put(lightBytes()); data.clear(); return data;
    }
    public static RenderPass withLightBinding(RenderPass pass, GpuBufferSlice light) {
        return (RenderPass)Proxy.newProxyInstance(FrontLitFeatures.class.getClassLoader(), new Class<?>[]{RenderPass.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("setUniform") && args[0].equals("Lighting")) {
                        // Both native setUniform overloads must bind the correctly typed slice.
                        pass.setUniform("Lighting", light);
                        return null;
                    }
                    try { return method.invoke(pass, args); }
                    catch (InvocationTargetException failure) { throw failure.getCause(); }
                });
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final class Renderer implements FeatureRenderer<LitSubmit> {
        private final FeatureRenderer delegate;
        private GpuBuffer light;
        Renderer(FeatureRenderer<?> delegate) { this.delegate = delegate; }
        private List<SubmitNode> unwrap(List<LitSubmit> nodes) { return nodes.stream().map(LitSubmit::original).toList(); }
        public void beginPrepare(FeatureFrameContext c) { delegate.beginPrepare(c); }
        public void prepareGroup(FeatureFrameContext c, List<LitSubmit> nodes, boolean sorted) { delegate.prepareGroup(c, unwrap(nodes), sorted); }
        public void finishPrepare(FeatureFrameContext c) { delegate.finishPrepare(c); }
        public void executeGroup(FeatureFrameContext c, OitStage stage, RenderPass pass, int group, List<LitSubmit> nodes, boolean sorted) {
            if (light == null) {
                var device = RenderSystem.getDevice();
                light = device.createBuffer(() -> "Cobblemon locator front lights",
                        GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                        lightBytes(device.getDeviceInfo().limits().minUniformOffsetAlignment()));
            }
            var prior = RenderSystem.getShaderLights();
            pass.setUniform("Lighting", light.slice());
            try { delegate.executeGroup(c, stage, withLightBinding(pass, light.slice()), group, unwrap(nodes), sorted); }
            finally { if (prior != null) pass.setUniform("Lighting", prior); }
        }
        public void finishExecute(FeatureFrameContext c) { delegate.finishExecute(c); }
        public void close() { try { delegate.close(); } finally { if (light != null) { light.close(); light = null; } } }
    }
    /** GUI/PIP and world graphs can differ; use the destination graph rather than live world graphics options. */
    public static boolean improvedTransparency(SubmitNodeCollector collector) {
        if (!(collector.order(0) instanceof SubmitNodeCollection collection)) throw new IllegalArgumentException("Native phase collection required");
        return collection.translucentModels == collection.oitTranslucent;
    }
    /** Preserve order, native phases (including aliased OIT phases), and translucent distances. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void transfer(SubmitNodeStorage source, SubmitNodeCollector target) {
        var visited = Collections.newSetFromMap(new IdentityHashMap<FeatureRenderPhase<?>, Boolean>());
        for (var entry : source.getSubmitsPerOrder().int2ObjectEntrySet()) {
            var destination = target.order(entry.getIntKey());
            if (!(destination instanceof SubmitNodeCollection collection)) throw new IllegalArgumentException("Front lighting requires native phase collection");
            var from = entry.getValue().allPhases(); var to = collection.allPhases();
            if (from.size() != to.size()) throw new IllegalStateException("Native phase layout changed");
            for (int i = 0; i < from.size(); i++) {
                var phase = from.get(i); FeatureRenderPhase output = to.get(i);
                if (visited.add(phase)) phase.sortInto((node, sorted) -> output.submit(wrap(node)));
            }
        }
        // The shared see-through queue is owned by storage, not included in collection.allPhases().
        if (!source.seeThrough().isEmpty()) {
            if (!(target.order(0) instanceof SubmitNodeCollection collection)) throw new IllegalArgumentException("Native phase collection required");
            source.seeThrough().sortInto((node, sorted) -> collection.seeThrough.submit(wrap(node)));
        }
    }
    private FrontLitFeatures() {}
}
