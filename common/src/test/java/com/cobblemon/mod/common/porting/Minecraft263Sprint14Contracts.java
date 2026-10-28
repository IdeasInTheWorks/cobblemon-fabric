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
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.buffers.*;
import com.mojang.renderpearl.api.commands.RenderPass;
import java.lang.reflect.Proxy;
import java.util.*;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.feature.*;
import net.minecraft.client.renderer.feature.phase.*;
import net.minecraft.client.renderer.feature.submit.*;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.*;
import org.joml.*;

/** Actual native state/phase/CPU contracts, without mixin application, live worlds, GPU or game startup. */
final class Minecraft263Sprint14Contracts {
    private static int checks;
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); checks++; }
    private static final class BoatProbe extends NativeBoatRenderer<AbstractBoat> {
        BoatProbe(EntityRendererProvider.Context ctx, Identifier first, Identifier second) {
            super(ctx);
            addModel(first, first, new BoatModel(BoatModel.createBoatModel().bakeRoot()));
            addModel(second, second, new BoatModel(BoatModel.createChestBoatModel().bakeRoot()));
        }
        // Native waterMask() reads Minecraft graphics options; use a fixture material only for queue/cardinality contracts.
        protected RenderType waterMask() { return RenderTypes.solidMovingBlock(); }
        protected Identifier variant(AbstractBoat boat) { throw new AssertionError("fixture has no live boat"); }
    }
    private static List<SubmitNode> nodes(FeatureRenderPhase<?> phase) {
        var result = new ArrayList<SubmitNode>(); phase.sortInto((node, sorted) -> result.add(node)); return result;
    }
    static int run() throws Exception {
        checks = 0;
        var first = Identifier.parse("fixture:apricorn_boat"); var second = Identifier.parse("fixture:chest_boat");
        var context = new EntityRendererProvider.Context(null, null, null, null, null, null, null, null, null, null, null);
        var renderer = new BoatProbe(context, first, second);
        var calls = new ArrayList<Object[]>();
        var collector = (SubmitNodeCollector)Proxy.newProxyInstance(Minecraft263Sprint14Contracts.class.getClassLoader(),
                new Class<?>[]{SubmitNodeCollector.class}, (proxy, method, args) -> { if (method.getName().equals("submitModel")) calls.add(args.clone()); return null; });
        for (int scenario = 0; scenario < 8; scenario++) {
            var state = renderer.createRenderState(); state.variant = (scenario & 1) == 0 ? first : second;
            state.yRot = scenario * 31F; state.hurtTime = scenario > 1 ? 4.5F : -1F;
            state.damageTime = 7F; state.hurtDir = scenario > 3 ? -1 : 1;
            state.bubbleAngle = scenario > 4 ? 21F : 0F; state.isUnderWater = (scenario & 2) != 0;
            var reference = new PoseStack(); reference.translate(0F,.375F,0F); reference.rotateDegrees(Axis.YP,180F-state.yRot);
            if (state.hurtTime>0F) reference.rotateDegrees(Axis.XP, net.minecraft.util.Mth.sin(state.hurtTime)*state.hurtTime*state.damageTime/10F*state.hurtDir);
            if (!net.minecraft.util.Mth.equal(state.bubbleAngle,0F)) reference.rotate(new Quaternionf().setAngleAxis(state.bubbleAngle*.017453292F,1F,0F,1F));
            reference.scale(-1F,-1F,1F); reference.rotateDegrees(Axis.YP,90F);
            var actual = new PoseStack(); NativeBoatRenderer.transform(state,actual);
            check(reference.last().pose().equals(actual.last().pose(),0.000001F),"legacy boat transform preserved scenario"+scenario);
            calls.clear(); var outer = new PoseStack(); renderer.submit(state,outer,collector,new CameraRenderState());
            check(calls.size()==(state.isUnderWater?1:2),"native water mask only above water, for both hull types");
            check(calls.getFirst()[3].equals(state.variant),"variant texture frozen in native state");
            check(outer.isEmpty()&&outer.last().pose().equals(new Matrix4f()),"boat submission balances pose");
        }
        var stack = new ItemStack(Items.STONE); var other = stack.copy();
        check(NativeItemCapture.requestedModel(stack)==null,"no explicit item override by default");
        NativeItemCapture.withModel(stack,first,()-> {
            check(first.equals(NativeItemCapture.requestedModel(stack)),"lectern explicit model bound to exact extracted stack");
            check(NativeItemCapture.requestedModel(other)==null,"nested/other stacks keep contextual model selection");
            NativeItemCapture.withModel(other,second,()->check(second.equals(NativeItemCapture.requestedModel(other)),"nested override selected"));
            check(first.equals(NativeItemCapture.requestedModel(stack)),"outer override restored after nested extraction");
        });
        try { NativeItemCapture.withModel(stack,first,()-> { throw new IllegalStateException("fixture"); }); } catch (IllegalStateException expected) {}
        check(NativeItemCapture.requestedModel(stack)==null,"explicit model scope restored on exception");
        check(NativeSceneCapture.light(17)==17,"scene light uses native value outside override");
        NativeSceneCapture.withLight(23,()-> {
            check(NativeSceneCapture.light(17)==23,"display Pokemon receives requested packed light before geometry capture");
            NativeSceneCapture.withLight(42,()-> { check(NativeSceneCapture.light(17)==42,"nested light scope"); return null; });
            check(NativeSceneCapture.light(17)==23,"nested light scope restored"); return null;
        });
        try { NativeSceneCapture.withLight(23,()-> { throw new IllegalStateException("fixture"); }); } catch (IllegalStateException expected) {}
        check(NativeSceneCapture.light(17)==17,"scene light scope restored on exception");
        var otherThread = new int[1]; NativeSceneCapture.withLight(23,()-> {
            var thread = new Thread(()->otherThread[0]=NativeSceneCapture.light(17)); thread.start();
            try { thread.join(); } catch (InterruptedException e) { throw new AssertionError(e); } return null;
        });
        check(otherThread[0]==17,"light override never leaks across extraction threads");
        var bytes = FrontLitFeatures.lightBytes();
        check(bytes.remaining()==com.mojang.blaze3d.platform.Lighting.UBO_SIZE,"native std140 Lighting block size");
        check(bytes.getFloat(0)==0F&&bytes.getFloat(4)==0F&&bytes.getFloat(8)==1F&&bytes.getFloat(16)==0F&&bytes.getFloat(20)==0F&&bytes.getFloat(24)==1F,"both original front vectors retained");
        var fakeBuffer=(GpuBuffer)Proxy.newProxyInstance(Minecraft263Sprint14Contracts.class.getClassLoader(),new Class<?>[]{GpuBuffer.class},(proxy,method,args)-> method.getName().equals("size")?64L:method.getName().equals("usage")?GpuBuffer.USAGE_UNIFORM:method.getName().equals("isClosed")?false:null);
        var front=new GpuBufferSlice(fakeBuffer,0,32); var ordinary=new GpuBufferSlice(fakeBuffer,32,32);
        var uniforms=new HashMap<String,Object>();
        var pass=(RenderPass)Proxy.newProxyInstance(Minecraft263Sprint14Contracts.class.getClassLoader(),new Class<?>[]{RenderPass.class},(proxy,method,args)-> {if(method.getName().equals("setUniform"))uniforms.put((String)args[0],args[1]); return null;});
        var scoped=FrontLitFeatures.withLightBinding(pass,front); scoped.setUniform("Lighting",ordinary); scoped.setUniform("Fog",ordinary);
        check(uniforms.get("Lighting")==front,"native prepared draw's default lighting rebind is overridden for this pass only");
        check(uniforms.get("Fog")==ordinary,"other uniform bindings pass through untouched");
        pass.setUniform("Lighting",ordinary);check(uniforms.get("Lighting")==ordinary,"unwrapped pass retains original lighting policy");
        for (boolean oit : List.of(false,true)) {
            var storage=new SubmitNodeStorage();storage.setUseImprovedTransparency(oit);
            var target=new SubmitNodeStorage();target.setUseImprovedTransparency(oit);
            check(FrontLitFeatures.improvedTransparency(target)==oit,"front lighting follows destination GUI/world transparency graph");
            storage.order(-2).submitCustomGeometry(new PoseStack(),RenderTypes.entityCutout(first),(p,v)->{});
            storage.order(7).submitCustomGeometry(new PoseStack(),RenderTypes.entityTranslucent(first),(p,v)->{});
            storage.submitText(new PoseStack(),1F,2F,net.minecraft.network.chat.Component.literal("native special text").getVisualOrderText(),false,net.minecraft.client.gui.Font.DisplayMode.SEE_THROUGH,0xF000F0,-1,0,0);
            var expected = new IdentityHashMap<FeatureRenderPhase<?>,List<SubmitNode>>();
            for (var collection : storage.getSubmitsPerOrder().values()) for (var phase : collection.allPhases()) if (!expected.containsKey(phase)) {
                var copy = nodes(phase); expected.put(phase,copy);
                for (var node : copy) ((FeatureRenderPhase)phase).submit(node);
            }
            var seeThroughBefore=nodes(storage.seeThrough()); for(var node:seeThroughBefore)storage.seeThrough().submit((TranslucentSubmit)node);
            FrontLitFeatures.transfer(storage,target);
            var seeThroughAfter=nodes(target.seeThrough());
            check(seeThroughBefore.size()==seeThroughAfter.size(),"shared see-through queue transferred exactly once");
            check(((FrontLitFeatures.LitSubmit)seeThroughAfter.getFirst()).original()==seeThroughBefore.getFirst(),"native see-through text retained");
            check(storage.getSubmitsPerOrder().keySet().equals(target.getSubmitsPerOrder().keySet()),"front-lit native order keys retained OIT="+oit);
            var seen=Collections.newSetFromMap(new IdentityHashMap<FeatureRenderPhase<?>,Boolean>());
            for (var entry:storage.getSubmitsPerOrder().int2ObjectEntrySet()) {
                var sourcePhases=entry.getValue().allPhases();var targetPhases=target.order(entry.getIntKey()).allPhases();
                for(int i=0;i<sourcePhases.size();i++) if(seen.add(sourcePhases.get(i))) {
                    var before=expected.get(sourcePhases.get(i));var after=nodes(targetPhases.get(i));
                    check(before.size()==after.size(),"front lighting keeps native phase cardinality including shared OIT queues");
                    for(int n=0;n<before.size();n++) {
                        var wrapped=(FrontLitFeatures.LitSubmit)after.get(n);
                        check(before.contains(wrapped.original()),"original native geometry/tint/glint payload retained");
                        check(wrapped.distanceToCameraSq()==(wrapped.original() instanceof TranslucentSubmit t ? t.distanceToCameraSq() : 0F),"native translucent distance retained");
                    }
                }
            }
        }
        var map=new FeatureRendererMap();FrontLitFeatures.install(map);
        var wrapped=FrontLitFeatures.wrap(new CustomFeatureRenderer.Submit(new PoseStack().last().copy(),RenderTypes.entityCutout(first),(p,v)->{}));
        check(map.getOrThrow(wrapped.featureType())!=null,"front features installed in native renderer map");map.close();
        check(FeatureRendererMap.class.getDeclaredConstructor()!=null,"feature-map mixin constructor target exists");
        check(NativeBlockCapture.material(net.minecraft.client.renderer.chunk.ChunkSectionLayer.SOLID)==RenderTypes.solidMovingBlock(),"habitat opaque native material");
        check(NativeBlockCapture.material(net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT)==RenderTypes.cutoutMovingBlock(),"habitat cutout native material");
        check(NativeBlockCapture.material(net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT)==RenderTypes.translucentMovingBlock(),"habitat translucent native material");
        check(net.minecraft.client.Camera.class.getDeclaredMethod("alignWithEntity",float.class).getReturnType()==void.class,"mounted camera position hook exact native descriptor");
        check(net.minecraft.client.Camera.class.getDeclaredField("level").getType()==net.minecraft.world.level.Level.class,"camera level shadow exact native descriptor");
        check(net.minecraft.client.player.LocalPlayer.class.getDeclaredMethod("pick",net.minecraft.world.entity.Entity.class,double.class,double.class,float.class).getReturnType()==net.minecraft.world.phys.HitResult.class,"rider picking hook moved to LocalPlayer");
        check(net.minecraft.client.renderer.GameRenderer.class.getDeclaredMethod("render").getReturnType()==void.class,"scheduled animation hook native parameterless frame render");
        var emptyCapture = new ModelBufferSource();
        NativeBlockCapture.capture(net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),null,net.minecraft.core.BlockPos.ZERO,new PoseStack(),emptyCapture);
        check(emptyCapture.snapshot().batches().isEmpty(),"invisible habitat mimic never resolves a missing-model cube or live client services");
        check(NativeBlockCapture.hasModel(net.minecraft.world.level.block.Blocks.STONE.defaultBlockState()),"visible habitat mimic follows native model shape");
        var viewFlag=net.minecraft.client.Camera.class.getDeclaredField("DIRTY_VIEW_ROT");viewFlag.setAccessible(true);
        var projectionFlag=net.minecraft.client.Camera.class.getDeclaredField("DIRTY_VIEW_ROT_PROJ");projectionFlag.setAccessible(true);
        check((viewFlag.getInt(null)|projectionFlag.getInt(null))==3,"mounted quaternion setter cache mask matches actual native view/projection flags");
        check(net.minecraft.client.player.AbstractClientPlayer.class.getDeclaredMethod("getFieldOfViewModifier",boolean.class,float.class).getReturnType()==float.class,"scanner/rider FOV hook native parameters");
        check(net.minecraft.client.MouseHandler.class.getDeclaredMethod("onScroll",long.class,double.class,double.class).getReturnType()==void.class,"native scroll hook SDL/window parameters");
        check(net.minecraft.world.entity.player.Inventory.class.getDeclaredMethod("setSelectedSlot",int.class).getReturnType()==void.class,"party/scanner hotbar interception target exists");
        var wheel=new net.minecraft.client.ScrollWheelHandler();
        check(wheel.onMouseScroll(0,.4).y==0&&wheel.onMouseScroll(0,.4).y==0&&wheel.onMouseScroll(0,.4).y==1,"native fractional scroll accumulates before party slot interception");
        check(wheel.onMouseScroll(0,-.4).y==0&&wheel.onMouseScroll(0,-.7).y==-1,"native scroll direction change resets fractional carry");
        var padded=FrontLitFeatures.lightBytes(256);
        check(padded.remaining()==256&&padded.getFloat(8)==1F&&padded.getFloat(24)==1F,"front light allocation respects native GPU uniform alignment");
        check(FrontLitFeatures.lightBytes(16).remaining()==32,"std140 light data retains final binding padding");
        scoped.setUniform("Lighting",fakeBuffer);
        check(uniforms.get("Lighting")==front,"full-buffer Lighting rebind also maps to scoped slice without overload type mismatch");
        return checks;
    }
}
