/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.porting;

import com.cobblemon.mod.common.client.model.FrameResources;
import com.cobblemon.mod.common.client.model.NativeRenderSetups;
import com.cobblemon.mod.common.client.model.TextureInterpolation;
import com.cobblemon.mod.common.client.model.ModelBufferSource;
import com.cobblemon.mod.common.client.model.BlockModelGeometry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

/** Executes actual native material construction and CPU lifetime contracts; no GPU or mixin bootstrap. */
final class Minecraft263RenderContracts {
    private static int checks;
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        checks++;
    }
    private static Object field(Object value, String name) throws Exception {
        var field = value.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(value);
    }
    static int run() throws Exception {
        checks = 0;
        var id = Identifier.parse("addon:textures/pokemon/custom.png");
        var factory = RenderType.class.getDeclaredMethod("create", String.class, RenderSetup.class);
        factory.setAccessible(true); // Test only: production uses the validated access widener.
        var pipelines = new ArrayList<RenderPipeline>();
        NativeRenderSetups.register(pipelines::add);
        check(pipelines.size() == 25, "model, OIT, berry and four Snowstorm pipelines are registered");
        var names = new HashSet<Identifier>();
        for (var pipeline : pipelines) check(names.add(pipeline.getLocation()), "pipeline locations are unique");
        for (int key = 0; key < 8; key++) {
            boolean emissive = (key & 4) != 0, translucent = (key & 2) != 0, cull = (key & 1) != 0;
            var setup = NativeRenderSetups.entity(id, emissive, translucent, cull);
            var render = (RenderType) factory.invoke(null, "fixture", setup);
            check(render.pipeline().isCull(), "legacy custom model layers cull");
            check(render.hasBlending() == translucent, "blending follows model flag, including opaque emissive");
            check(render.pipeline().getDepthStencilState().writeDepth(), "base pass retains depth writes");
            check(render.sortOnUpload() == translucent, "only translucent layers sort");
            check(render.affectsCrumbling(), "custom model layers retain crumbling");
            check(render.outline().isEmpty(), "custom path does not contribute an outline");
            check(field(setup, "useLightmap").equals(!emissive), "emissive layers omit lightmap");
            check(field(setup, "useOverlay").equals(true), "overlay remains enabled");
            var oit = (OitPipelineSet) field(setup, "oitPipelineSet");
            check((oit != null) == translucent, "native transparency passes accompany translucent models");
            if (oit != null) {
                check(oit.depthBoundsPipeline().isCull(), "OIT depth geometry uses same culling");
                check(oit.transmittancePipeline().isCull(), "OIT transmittance uses same culling");
                check(oit.accumulatePipeline().isCull(), "OIT accumulation uses same culling");
            }
        }
        var berry = NativeRenderSetups.berries(id);
        var berryType = (RenderType) factory.invoke(null, "berries", berry);
        check(!berryType.pipeline().isCull(), "berries retain double sided geometry");
        check(!berryType.hasBlending(), "berries retain alpha cutout rather than blending");
        check(berryType.outline().isPresent(), "berries retain outline participation");
        check(field(berry, "useLightmap").equals(true), "berries use lightmap");
        check(field(berry, "useOverlay").equals(false), "berries do not use entity overlay");

        for (int material = 0; material < 4; material++) {
            var setup = NativeRenderSetups.particle(material);
            var type = (RenderType)factory.invoke(null, "snowstorm", setup);
            check(type.pipeline().isCull() == (material == 0 || material == 3), "Snowstorm material culling retained");
            check(type.pipeline().getDepthStencilState().writeDepth(), "Snowstorm retains depth writes");
            check(type.format() == net.minecraft.client.renderer.RenderPipelines.TRANSLUCENT_PARTICLE.getVertexFormatBindings().getFirst(), "native particle vertex format retained");
            var blend = type.pipeline().getColorTargetStates().getFirst().blendFunction().orElseThrow();
            var expectedSource = material == 3 ? com.mojang.renderpearl.api.pipeline.BlendFactor.SRC_COLOR : com.mojang.renderpearl.api.pipeline.BlendFactor.SRC_ALPHA;
            var expectedDest = material == 1 ? com.mojang.renderpearl.api.pipeline.BlendFactor.ONE : material == 3 ? com.mojang.renderpearl.api.pipeline.BlendFactor.ZERO : com.mojang.renderpearl.api.pipeline.BlendFactor.ONE_MINUS_SRC_ALPHA;
            check(blend.equals(new com.mojang.renderpearl.api.pipeline.BlendFunction(expectedSource, expectedDest)), "Snowstorm color/alpha blending retained");
            check(field(setup, "useLightmap").equals(true), "Snowstorm uses environment lightmap");
        }

        var buffers = new ModelBufferSource();
        var input = buffers.getBuffer(berryType);
        input.addVertex(1, 2, 3).setColor(11, 22, 33, 44).setUv(0.25F, 0.75F)
                .setUv1(2, 3).setUv2(4, 5).setUv3(0.125F, 0.5F).setNormal(0, 1, 0).setLineWidth(2);
        var snapshot = buffers.snapshot();
        input.setColor(0).addVertex(4, 5, 6).setUv(0, 1);
        var originalVertex = snapshot.batches().getFirst().vertices().getFirst();
        check(originalVertex.color() == 0x2C0B1621, "snapshot copies color before later model writes");
        check(originalVertex.attributes() == 127, "all seven native attribute groups captured");
        check(snapshot.batches().getFirst().vertices().size() == 1, "later vertices do not enter prior snapshots");
        check(buffers.snapshot().batches().getFirst().vertices().size() == 2, "next snapshot includes later vertices without duplication");
        try { snapshot.batches().clear(); throw new AssertionError("mutable batches"); }
        catch (UnsupportedOperationException expected) { checks++; }
        var submissions = new ArrayList<net.minecraft.client.renderer.SubmitNodeCollector.CustomGeometryRenderer>();
        var collector = (net.minecraft.client.renderer.OrderedSubmitNodeCollector) java.lang.reflect.Proxy.newProxyInstance(
                Minecraft263RenderContracts.class.getClassLoader(), new Class<?>[] {net.minecraft.client.renderer.OrderedSubmitNodeCollector.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("submitCustomGeometry")) {
                        check(args[1] == berryType, "native submission retains the material");
                        submissions.add((net.minecraft.client.renderer.SubmitNodeCollector.CustomGeometryRenderer) args[2]);
                    }
                    return null;
                });
        var pose = new PoseStack();
        pose.translate(10, 20, 30);
        snapshot.submit(pose, collector);
        check(submissions.size() == 1, "snapshot submits geometry through the native collector");
        var output = new ModelBufferSource.Recorder();
        submissions.getFirst().render(pose.last(), output);
        var replayed = output.snapshot().getFirst();
        check(replayed.x() == 11 && replayed.y() == 22 && replayed.z() == 33, "draw pose applied to copied local positions exactly once");
        check(replayed.color() == originalVertex.color(), "ARGB survives deferred replay");
        check(replayed.u() == 0.25F && replayed.v() == 0.75F, "texture coordinates survive replay");
        check(replayed.overlayU() == 2 && replayed.overlayV() == 3, "overlay coordinates survive replay");
        check(replayed.lightU() == 4 && replayed.lightV() == 5, "packed light coordinates survive replay");
        check(replayed.glintU() == 0.125F && replayed.glintV() == 0.5F, "glint coordinates survive replay");
        check(replayed.ny() == 1 && replayed.lineWidth() == 2, "normal and line width survive replay");
        check(buffers.snapshot().batches().getFirst().vertices().get(1).attributes() == 2, "omitted attributes do not leak from the prior vertex");
        try { new ModelBufferSource.Recorder().setUv(0, 0); throw new AssertionError("attribute before vertex accepted"); }
        catch (IllegalStateException expected) { checks++; }
        var phases = new net.minecraft.client.renderer.SubmitNodeCollection(false,
                new net.minecraft.client.renderer.feature.phase.TranslucentFeatureRenderPhase());
        var decalPose = new PoseStack().last();
        snapshot.submit(new PoseStack(), phases,
                new net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay(3, decalPose), null);
        var phaseNodes = new ArrayList<net.minecraft.client.renderer.feature.submit.SubmitNode>();
        phases.solid.sortInto((node, flag) -> phaseNodes.add(node));
        check(phaseNodes.size() == 2, "solid geometry and fossil breaking overlay use the native solid phase");
        var breaking = phaseNodes.stream().map(node -> (net.minecraft.client.renderer.feature.CustomFeatureRenderer.Submit)node)
                .filter(node -> node.renderType() == net.minecraft.client.resources.model.ModelBakery.DESTROY_TYPES.get(3)).findFirst().orElseThrow();
        check(breaking.renderType() == net.minecraft.client.resources.model.ModelBakery.DESTROY_TYPES.get(3), "native break progress selects correct decal material");
        var beforeMutation = new ModelBufferSource.Recorder(); breaking.customGeometryRenderer().render(new PoseStack().last(), beforeMutation);
        decalPose.translate(50, 60, 70);
        var afterMutation = new ModelBufferSource.Recorder(); breaking.customGeometryRenderer().render(new PoseStack().last(), afterMutation);
        check(beforeMutation.snapshot().equals(afterMutation.snapshot()), "breaking decal camera pose is captured rather than retained live");
        var translucentType = (RenderType)factory.invoke(null, "translucent-fixture", NativeRenderSetups.entity(id, false, true, false));
        var translucentBuffers = new ModelBufferSource();
        translucentBuffers.getBuffer(translucentType).addVertex(0, 0, 0).setColor(-1).setUv(0, 0).setNormal(0, 1, 0);
        var transparentPhases = new net.minecraft.client.renderer.SubmitNodeCollection(true,
                new net.minecraft.client.renderer.feature.phase.TranslucentFeatureRenderPhase());
        translucentBuffers.snapshot().submit(new PoseStack(), transparentPhases,
                new net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay(6, new PoseStack().last()), null, true);
        var overlayNodes = new ArrayList<net.minecraft.client.renderer.feature.submit.SubmitNode>();
        transparentPhases.breakingOverlay.sortInto((node, flag) -> overlayNodes.add(node));
        check(overlayNodes.size() == 2, "native improved-transparency phase contains both base geometry and decal");
        check(overlayNodes.stream().map(node -> (net.minecraft.client.renderer.feature.CustomFeatureRenderer.Submit)node)
                .anyMatch(node -> node.renderType() == net.minecraft.client.resources.model.ModelBakery.DESTROY_TYPES_OIT.get(6)), "improved transparency retains native OIT decal companion");

        var unlitBuffers = new ModelBufferSource();
        unlitBuffers.getBuffer(berryType).addVertex(2, 3, 4).setColor(-1).setUv(0.5F, 0.25F).setUv2(0, 0);
        var unlit = unlitBuffers.snapshot();
        submissions.clear();
        unlit.submit(new PoseStack(), collector, null, net.minecraft.util.LightCoordsUtil.pack(11, 7));
        var relit = new ModelBufferSource.Recorder(); submissions.getFirst().render(new PoseStack().last(), relit);
        check(relit.snapshot().getFirst().lightU() == (11 << 4) && relit.snapshot().getFirst().lightV() == (7 << 4), "special-item submission resolves native draw light after CPU capture");
        var builtin = com.cobblemon.mod.common.client.model.NativeBuiltinItemModel.model((stack, display) -> unlit);
        var itemState = new net.minecraft.client.renderer.item.ItemStackRenderState();
        builtin.update(itemState, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE), null,
                net.minecraft.world.item.ItemDisplayContext.GUI, null, null, 0);
        check(!itemState.isEmpty() && itemState.isAnimated(), "special item update creates a native animated layer");
        check(!itemState.usesBlockLight(), "GUI special item requests native flat item lighting");

        var fullMaterial = new net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo(null, null, berryType, null, null, 0, null, 0);
        var fullQuad = new net.minecraft.client.resources.model.geometry.BakedQuad(new org.joml.Vector3f(0, 0, 0),
                new org.joml.Vector3f(1, 0, 0), new org.joml.Vector3f(1, 1, 0), new org.joml.Vector3f(0, 1, 0),
                0, 0, 0, 0, net.minecraft.core.Direction.SOUTH, fullMaterial);
        var faceQueries = new HashSet<net.minecraft.core.Direction>();
        var allFacesModel = new net.minecraft.client.renderer.block.dispatch.BlockStateModel() {
            public void collectParts(net.minecraft.util.RandomSource random, List<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart> parts) {
                parts.add(new net.minecraft.client.renderer.block.dispatch.BlockStateModelPart() {
                    public List<net.minecraft.client.resources.model.geometry.BakedQuad> getQuads(net.minecraft.core.Direction face) {
                        faceQueries.add(face); return face == null || face == net.minecraft.core.Direction.NORTH ? List.of(fullQuad) : List.of();
                    }
                    public boolean useAmbientOcclusion() { return false; }
                    public net.minecraft.client.resources.model.sprite.Material.Baked particleMaterial() { return null; }
                    public int materialFlags() { return 0; }
                });
            }
            public net.minecraft.client.resources.model.sprite.Material.Baked particleMaterial() { return null; }
            public int materialFlags() { return 0; }
        };
        var fullBuffers = new ModelBufferSource();
        BlockModelGeometry.captureAll(allFacesModel, net.minecraft.util.RandomSource.create(42), new PoseStack(), fullBuffers, 0xFF112233, 7, 9);
        check(faceQueries.size() == 7, "pot/candle capture includes all six face lists and unculled geometry");
        check(fullBuffers.snapshot().batches().getFirst().vertices().size() == 8, "culled and unculled quads are both retained");
        check(fullBuffers.snapshot().batches().getFirst().vertices().getFirst().color() == 0xFF112233, "broth tint retained for tint-indexed geometry");

        var material = new net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo(null, null, null, null, null, -1, null, 0);
        var quad = new net.minecraft.client.resources.model.geometry.BakedQuad(new org.joml.Vector3f(0, 0, 0),
                new org.joml.Vector3f(1, 0, 0), new org.joml.Vector3f(1, 1, 0), new org.joml.Vector3f(0, 1, 0),
                0, 0, 0, 0, net.minecraft.core.Direction.SOUTH, material);
        var model = new net.minecraft.client.renderer.block.dispatch.BlockStateModel() {
            public void collectParts(net.minecraft.util.RandomSource random, List<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart> parts) {
                parts.add(new net.minecraft.client.renderer.block.dispatch.BlockStateModelPart() {
                    public List<net.minecraft.client.resources.model.geometry.BakedQuad> getQuads(net.minecraft.core.Direction face) {
                        check(face == null, "standalone capture retains original unculled-face query");
                        return List.of(quad);
                    }
                    public boolean useAmbientOcclusion() { return false; }
                    public net.minecraft.client.resources.model.sprite.Material.Baked particleMaterial() { return null; }
                    public int materialFlags() { return 0; }
                });
            }
            public net.minecraft.client.resources.model.sprite.Material.Baked particleMaterial() { return null; }
            public int materialFlags() { return 0; }
        };
        var quadOutput = new ModelBufferSource.Recorder();
        BlockModelGeometry.capture(model, net.minecraft.util.RandomSource.create(123), new PoseStack(), quadOutput,
                0xFFBFBFBF, 0x00F000F0, 0x000A000B);
        check(quadOutput.snapshot().size() == 4, "native baked quad emits four captured vertices");
        check(quadOutput.snapshot().getFirst().color() == 0xFFBFBFBF, "fossil connector/fluid tint retained");
        check(quadOutput.snapshot().getFirst().overlayU() == 11 && quadOutput.snapshot().getFirst().overlayV() == 10, "native quad overlay serialization retained");

        var released = new ArrayList<String>();
        var fences = new ArrayList<Runnable>();
        FrameResources.retain(() -> released.add("first"));
        FrameResources.retain(() -> released.add("second"));
        check(released.isEmpty(), "retaining does not close a texture during submission");
        FrameResources.finishFrame(fences::add);
        check(released.isEmpty() && fences.size() == 1, "cleanup waits for the GPU fence");
        FrameResources.retain(() -> released.add("next"));
        fences.removeFirst().run();
        check(released.equals(List.of("first", "second")), "only completed-frame resources released in order");
        FrameResources.finishFrame(fences::add);
        fences.removeFirst().run();
        check(released.equals(List.of("first", "second", "next")), "next frame owns its own resources");
        FrameResources.finishFrame(fences::add);
        check(fences.isEmpty(), "empty frames allocate no fence");
        FrameResources.retain(() -> { throw new IllegalStateException("fixture"); });
        FrameResources.retain(() -> released.add("after failure"));
        FrameResources.finishFrame(fences::add);
        try { fences.removeFirst().run(); throw new AssertionError("cleanup failure not propagated"); }
        catch (IllegalStateException expected) { checks++; }
        check(released.contains("after failure"), "one failed cleanup does not leak remaining textures");

        var random = new Random(0x263);
        for (int i = 0; i < 1024; i++) {
            int first = random.nextInt(), second = random.nextInt();
            float fraction = random.nextFloat();
            check(TextureInterpolation.blend(first, second, fraction) == legacyBlend(first, second, fraction), "legacy float interpolation and floor rounding retained");
        }
        check(TextureInterpolation.blend(0x00000000, 0xFFFF4010, 0.5F) == 0x7FFF4010, "transparent edge retains opaque color");
        check(TextureInterpolation.blend(0xFFFF4010, 0x00000000, 0.5F) == 0x7FFF4010, "reverse transparent edge retains opaque color");
        return checks;
    }

    private static int legacyBlend(int first, int second, float fraction) {
        float a = (first >>> 24) / 255F, b = (second >>> 24) / 255F;
        int result = (int) Math.floor((a + (b - a) * fraction) * 255) << 24;
        for (int channel = 0; channel < 3; channel++) {
            int shift = channel * 8;
            float x = (first >>> shift & 255) / 255F, y = (second >>> shift & 255) / 255F;
            float value = a == 0F && b == 1F ? y : a == 1F && b == 0F ? x : x + (y - x) * fraction;
            result |= (int) Math.floor(value * 255) << shift;
        }
        return result;
    }
}
