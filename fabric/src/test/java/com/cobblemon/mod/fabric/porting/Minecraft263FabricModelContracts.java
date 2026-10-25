/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.fabric.porting;

import com.cobblemon.mod.common.client.model.StandaloneModelKey;
import com.cobblemon.mod.fabric.client.model.StandaloneModels;
import com.google.gson.JsonParser;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.client.model.loading.v1.*;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

/** Native Fabric plugin registration contract; no Fabric game bootstrap, reload, bake or GPU. */
public final class Minecraft263FabricModelContracts {
    private static int checks;
    private static void check(boolean condition,String message) {
        if(!condition)throw new AssertionError(message);
        checks++;
    }
    private static BlockStateModel marker() {
        return new BlockStateModel() {
            public void collectParts(RandomSource random,List<BlockStateModelPart> parts) {}
            public Material.Baked particleMaterial() { return null; }
            public int materialFlags() { return 0; }
        };
    }
    public static void main(String[] args) throws Exception {
        var active = new AtomicReference<>(marker());
        var lookupCalls = new AtomicInteger();
        var lookedUp = new AtomicReference<ExtraModelKey<BlockStateModel>>();
        var models = new StandaloneModels(key -> { lookupCalls.incrementAndGet();lookedUp.set(key);return active.get(); });
        var id = new StandaloneModelKey(Identifier.parse("contract:fluid"),"1");
        var source = Identifier.parse("contract:block/fluid_1");
        var supplier = models.declare(id,source);
        check(lookupCalls.get()==0,"declaration does not look up unapplied models");
        check(supplier.get()==active.get(),"supplier resolves currently applied model");
        var stableKey=lookedUp.get();
        active.set(marker());
        check(supplier.get()==active.get(),"supplier follows the replacement model after reload");
        check(lookedUp.get()==stableKey,"lookup key remains stable across applied reloads");
        var repeated=models.declare(new StandaloneModelKey(Identifier.parse("contract:fluid"),"1"),source);
        repeated.get();
        check(lookedUp.get()==stableKey,"repeated registration reuses key identity");
        models.declare(new StandaloneModelKey(Identifier.parse("contract:fluid"),"2"),Identifier.parse("contract:block/fluid_2"));
        var captured=new LinkedHashMap<ExtraModelKey<?>,UnbakedExtraModel<?>>();
        var context=(ModelLoadingPlugin.Context)Proxy.newProxyInstance(ModelLoadingPlugin.Context.class.getClassLoader(),new Class<?>[]{ModelLoadingPlugin.Context.class},(proxy,method,arguments)->{
            if(method.getName().equals("addModel"))captured.put((ExtraModelKey<?>)arguments[0],(UnbakedExtraModel<?>)arguments[1]);
            return null;
        });
        models.initialize(context);
        check(captured.size()==2,"both standalone variants registered with native Fabric context");
        check(captured.containsKey(stableKey),"native registration uses the supplier key");
        var dependencies=new LinkedHashSet<Identifier>();
        captured.values().forEach(unbaked->unbaked.resolveDependencies(dependencies::add));
        check(dependencies.equals(Set.of(source,Identifier.parse("contract:block/fluid_2"))),"Fabric helper retains both geometry dependencies");
        var priorKeys=Set.copyOf(captured.keySet());captured.clear();models.initialize(context);
        check(priorKeys.equals(captured.keySet()),"reload preparation does not replace extra-model keys");
        try { models.declare(id,Identifier.parse("contract:block/different"));throw new AssertionError("conflicting key source accepted"); }
        catch(IllegalArgumentException expected){ checks++; }
        var common=JsonParser.parseString(Files.readString(Path.of("common/src/main/resources/architectury.common.json"))).getAsJsonObject();
        var fabric=JsonParser.parseString(Files.readString(Path.of("fabric/src/main/resources/fabric.mod.json"))).getAsJsonObject();
        String modelPart="net/minecraft/client/model/geom/ModelPart";
        String bone="com/cobblemon/mod/common/client/render/models/blockbench/pose/Bone";
        check(common.getAsJsonObject("injected_interfaces").getAsJsonArray(modelPart).get(0).getAsString().equals(bone),"common injection names the actual official target class");
        check(fabric.getAsJsonObject("custom").getAsJsonObject("loom:injected_interfaces").getAsJsonArray(modelPart).get(0).getAsString().equals(bone),"Fabric/common compile-time Bone contracts agree");
        System.out.println("{\"passed\":true,\"checks\":"+checks+",\"scope\":\"production standalone registry with native Fabric API context; no bake/reload/GPU\"}");
    }
}
