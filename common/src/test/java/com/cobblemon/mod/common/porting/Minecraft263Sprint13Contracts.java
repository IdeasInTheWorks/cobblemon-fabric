/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.porting;

import com.cobblemon.mod.common.client.model.*;
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.Proxy;
import java.util.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.renderer.state.level.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

/** Native state/queue/descriptor contracts only. No mixin application, startup, atlas, world or GPU. */
final class Minecraft263Sprint13Contracts {
    private static int checks;
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        checks++;
    }
    private static final class BodyProbe extends CapturedLivingEntityRenderer<net.minecraft.world.entity.LivingEntity> {
        BodyProbe(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context) { super(context, .5F); }
        protected void capture(net.minecraft.world.entity.LivingEntity entity, State state, float partial,
                               PoseStack pose, ModelBufferSource output) { throw new AssertionError("not an extraction fixture"); }
        org.joml.Matrix4f transform(State state) {
            var pose = new PoseStack(); transformBody(state, pose); return new org.joml.Matrix4f(pose.last().pose());
        }
    }
    private static final class NativeBody extends net.minecraft.client.renderer.entity.LivingEntityRenderer<
            net.minecraft.world.entity.LivingEntity, CapturedLivingEntityRenderer.State,
            net.minecraft.client.model.EntityModel<CapturedLivingEntityRenderer.State>> {
        private final RenderType type;
        NativeBody(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context, RenderType type) {
            super(context, new net.minecraft.client.model.EntityModel<CapturedLivingEntityRenderer.State>(
                    new net.minecraft.client.model.geom.ModelPart(List.of(), Map.of())) {}, .5F); this.type = type;
        }
        public CapturedLivingEntityRenderer.State createRenderState() { return new CapturedLivingEntityRenderer.State(); }
        public net.minecraft.resources.Identifier getTextureLocation(CapturedLivingEntityRenderer.State state) { return state.texture; }
        protected RenderType getRenderType(CapturedLivingEntityRenderer.State state, boolean visible, boolean alpha, boolean glow) { return type; }
        protected void scale(CapturedLivingEntityRenderer.State state, PoseStack pose) { pose.scale(state.modelScale, state.modelScale, state.modelScale); }
    }
    static int run() throws Exception {
        checks = 0;
        var text = Component.literal("Eevee");
        var source = new ArrayList<CapturedLabel.Line>();
        source.add(new CapturedLabel.Line(text, -15F, 0F));
        var label = new CapturedLabel(2.5, 1.5, 1, 1, 0x40000000, source);
        text.append(" mutated"); source.clear();
        check(label.lines().size() == 1 && label.lines().getFirst().text().getString().equals("Eevee"), "label copies text and list before deferral");
        try { label.lines().clear(); throw new AssertionError("mutable label list"); }
        catch (UnsupportedOperationException expected) { checks++; }
        var calls = new ArrayList<Object[]>();
        var transforms = new ArrayList<org.joml.Matrix4f>();
        var collector = (SubmitNodeCollector)Proxy.newProxyInstance(Minecraft263Sprint13Contracts.class.getClassLoader(),
                new Class<?>[]{SubmitNodeCollector.class}, (proxy, method, args) -> {
                    if (method.getName().equals("submitText")) {
                        calls.add(args.clone()); transforms.add(new org.joml.Matrix4f(((PoseStack)args[0]).last().pose()));
                    }
                    return null;
                });
        var camera = new CameraRenderState(); camera.orientation = new org.joml.Quaternionf();
        var pose = new PoseStack(); pose.translate(10, 20, 30);
        var original = new org.joml.Matrix4f(pose.last().pose());
        label.submit(pose, collector, camera);
        check(calls.size() == 2, "both see-through and solid label passes submitted");
        check(calls.get(0)[5] == Font.DisplayMode.SEE_THROUGH && calls.get(1)[5] == Font.DisplayMode.NORMAL, "label pass modes retained");
        check((int)calls.get(0)[6] == 0xF000F0 && (int)calls.get(1)[6] == 0xF000F0, "label full-bright light retained");
        check((int)calls.get(0)[7] == 0x20FFFFFF && (int)calls.get(1)[7] == -1, "label alpha/solid ARGB retained");
        check((int)calls.get(0)[8] == 0x40000000 && (int)calls.get(1)[8] == 0, "label background only on first pass");
        check(transforms.getFirst().m30() == 10F && transforms.getFirst().m31() == 23F && transforms.getFirst().m32() == 27.5F, "label height, distance and camera offset retained");
        check(transforms.getFirst().m00() == 0.025F && transforms.getFirst().m11() == -0.025F, "label pixel axes retained");
        check(original.equals(pose.last().pose()) && pose.isEmpty(), "label restores caller pose");
        var recall = new RiderRenderData(RiderPose.EMPTY, new org.joml.Matrix4f(), 0F, true);
        check(recall.recalling() && recall.recall() == 0F, "active recall hides hand even at animation's zero-rise endpoint");
        check(!RiderRenderData.EMPTY.recalling(), "empty state resets recall suppression");
        check(!FirstPersonRenderData.EMPTY.recalling() && !FirstPersonRenderData.EMPTY.hidePokedex()
                && !FirstPersonRenderData.EMPTY.suppressViewBob(), "empty first-person state clears all suppression flags");

        var texture = net.minecraft.resources.Identifier.parse("fixture:textures/pokemon.png");
        // Use native outline material for the invisible/glowing case, with exactly one coloured pass.
        // Null rendering services are explicit fixture boundaries; constructor and CPU rotations are real native code.
        var context = new net.minecraft.client.renderer.entity.EntityRendererProvider.Context(null, null, null, null, null,
                null, null, null, null, null, null);
        var probe = new BodyProbe(context);
        var reference = new NativeBody(context, RenderTypes.entityCutout(texture));
        var bodyTransforms = new ArrayList<org.joml.Matrix4f>();
        var bodyCollector = (SubmitNodeCollector)Proxy.newProxyInstance(Minecraft263Sprint13Contracts.class.getClassLoader(),
                new Class<?>[]{SubmitNodeCollector.class}, (proxy, method, args) -> {
                    if (method.getName().equals("submitModel")) bodyTransforms.add(new org.joml.Matrix4f(((PoseStack)args[2]).last().pose()));
                    return null;
                });
        for (int scenario = 0; scenario < 6; scenario++) {
            var state = probe.createRenderState(); state.texture = texture;
            state.pose = net.minecraft.world.entity.Pose.STANDING;
            state.scale = 1.3F; state.modelScale = .7F; state.bodyRot = 32F;
            state.ageInTicks = 7F; state.eyeHeight = 1.5F; state.boundingBoxHeight = 2.3F;
            if (scenario == 1) { state.pose = net.minecraft.world.entity.Pose.SLEEPING; state.bedOrientation = net.minecraft.core.Direction.NORTH; }
            if (scenario == 2) state.deathTime = 7F;
            if (scenario == 3) { state.isAutoSpinAttack = true; state.xRot = 14F; }
            if (scenario == 4) state.isFullyFrozen = true;
            if (scenario == 5) state.isUpsideDown = true;
            bodyTransforms.clear(); reference.submit(state, new PoseStack(), bodyCollector, camera);
            check(bodyTransforms.size() == 1 && probe.transform(state).equals(bodyTransforms.getFirst(), 0.000001F),
                    "captured body matches actual native submit transform, scenario " + scenario);
        }
        var nativeOutline = RenderTypes.outline(texture);
        var buffers = new ModelBufferSource();
        buffers.getBuffer(nativeOutline).addVertex(1, 2, 3).setColor(-1).setUv(.25F, .75F);
        var submissions = new ArrayList<SubmitNodeCollector.CustomGeometryRenderer>();
        var geometryCollector = (OrderedSubmitNodeCollector)Proxy.newProxyInstance(Minecraft263Sprint13Contracts.class.getClassLoader(),
                new Class<?>[]{OrderedSubmitNodeCollector.class}, (proxy, method, args) -> {
                    if (method.getName().equals("submitCustomGeometry")) submissions.add((SubmitNodeCollector.CustomGeometryRenderer)args[2]);
                    return null;
                });
        buffers.snapshot().submitWithOutline(new PoseStack(), geometryCollector, 0xFF123456);
        check(submissions.size() == 1, "invisible glowing body queues one outline, without a white duplicate");
        var replay = new ModelBufferSource.Recorder(); submissions.getFirst().render(new PoseStack().last(), replay);
        check(replay.snapshot().getFirst().color() == 0xFF123456, "glowing custom geometry retains native outline colour");
        check(replay.snapshot().getFirst().u() == .25F && replay.snapshot().getFirst().z() == 3F, "outline retains copied UV/position");

        var hands = FirstPersonHandsAndItemsRenderer.class;
        check(hands.getDeclaredMethod("submitArmWithItem", PlayerRenderState.class, FirstPersonHandsAndItemsRenderState.class,
                float.class, float.class, InteractionHand.class, float.class, ItemStack.class, float.class,
                PoseStack.class, SubmitNodeCollector.class, int.class).getReturnType() == void.class, "first-person hand injection descriptor exists");
        check(hands.getDeclaredMethod("submitHandsWithItems", float.class, PoseStack.class, SubmitNodeCollector.class,
                PlayerRenderState.class, FirstPersonHandsAndItemsRenderState.class).getReturnType() == void.class, "first-person view-bob target exists");
        check(net.minecraft.client.player.FirstPersonHandsAndItems.class.getDeclaredMethod("extractRenderState",
                net.minecraft.client.player.LocalPlayer.class, float.class, FirstPersonHandsAndItemsRenderState.class).getReturnType() == void.class,
                "first-person extraction target exists");
        check(net.minecraft.client.renderer.entity.layers.ItemInHandLayer.class.getDeclaredMethod("submitArmWithItem",
                ArmedEntityRenderState.class, ItemStackRenderState.class, ItemStack.class, HumanoidArm.class,
                PoseStack.class, SubmitNodeCollector.class, int.class).getReturnType() == void.class, "third-person hand injection descriptor exists");
        check(net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer.class.getDeclaredMethod("showHitboxes",
                net.minecraft.world.entity.Entity.class, float.class, boolean.class).getReturnType() == void.class, "locator debug injection descriptor exists");
        check(net.minecraft.client.renderer.entity.EntityRenderDispatcher.class.getDeclaredField("playerRenderers").getGenericType().getTypeName()
                .contains("PlayerModelType"), "reload field now uses native player model type");
        check(CapturedLivingEntityRenderer.class.getSuperclass() == net.minecraft.client.renderer.entity.LivingEntityRenderer.class,
                "body adapter retains native living metadata extraction");
        check(CapturedPlayerLayer.class.getSuperclass() == net.minecraft.client.renderer.entity.layers.RenderLayer.class,
                "frozen shoulder/recall adapter retains native layer entrypoint");
        return checks;
    }
}
