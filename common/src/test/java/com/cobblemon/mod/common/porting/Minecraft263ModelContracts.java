/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.porting;

import com.cobblemon.mod.common.client.model.LegacyItemModels;
import com.cobblemon.mod.common.client.model.StandaloneModelKey;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;

/** Executes production discovery/override/tint contracts without a GPU, resource reload or model bake. */
final class Minecraft263ModelContracts {
    private static int checks;
    private static Identifier id(String value) { return Identifier.parse(value); }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
    static int run() throws Exception {
        checks = 0;
        var resource = id("addon:models/item/egg/regional/form.json");
        check(LegacyItemModels.supportsResource(resource), "addon egg model discovery retained");
        check(LegacyItemModels.supportsResource(id("addon:models/item/custom_ball.json")), "addon ball/wearable model discovery retained");
        check(!LegacyItemModels.supportsResource(id("addon:models/item/custom.png")), "non-JSON resources ignored");
        check(!LegacyItemModels.supportsResource(id("addon:models/block/custom.json")), "block files are not synthetic item definitions");
        check(LegacyItemModels.modelId(resource).equals(id("addon:item/egg/regional/form")), "geometry ID retains namespace and subfolders");
        check(LegacyItemModels.itemId(LegacyItemModels.modelId(resource)).equals(id("addon:egg/regional/form")), "native item definition ID strips exactly one item prefix");
        check(LegacyItemModels.itemId(id("addon:tm/fire_tm")).equals(id("addon:tm/fire_tm")), "already normalized dynamic IDs remain unchanged");
        check(!new StandaloneModelKey(id("cobblemon:fluid"),"1").equals(new StandaloneModelKey(id("cobblemon:fluid"),"2")), "standalone variant keys remain distinct");
        check(new StandaloneModelKey(id("cobblemon:fluid"),"1").equals(new StandaloneModelKey(id("cobblemon:fluid"),"1")), "standalone keys retain stable value identity across reload registration");
        var json = JsonParser.parseString("""
                {"overrides":[
                  {"predicate":{"custom_model_data":2},"model":"addon:item/a"},
                  {"predicate":{"custom_model_data":1},"model":"addon:item/b"},
                  {"predicate":{"custom_model_data":3,"addon:rare":0.4},"model":"addon:item/c"}
                ]}
                """).getAsJsonObject();
        var definition = LegacyItemModels.parse(id("addon:item/base"),json);
        var values = new HashMap<Identifier,Float>();
        var custom = id("minecraft:custom_model_data");var rare = id("addon:rare");
        java.util.function.Function<Identifier,Float> read = key -> values.getOrDefault(key,Float.NEGATIVE_INFINITY);
        values.put(custom,0F);
        check(definition.select(read).equals(id("addon:item/base")), "base used below all thresholds");
        values.put(custom,2F);
        check(definition.select(read).equals(id("addon:item/b")), "last matching override wins even when thresholds are unsorted");
        values.put(custom,3F);values.put(rare,0.39F);
        check(definition.select(read).equals(id("addon:item/b")), "all predicates must match");
        values.put(rare,0.4F);
        check(definition.select(read).equals(id("addon:item/c")), "threshold equality matches");
        values.put(rare,Float.NaN);
        check(definition.select(read).equals(id("addon:item/b")), "NaN does not satisfy a predicate");
        var dependencies = new LinkedHashSet<Identifier>();
        new LegacyItemModels.Unbaked(definition).resolveDependencies(dependencies::add);
        check(dependencies.equals(Set.of(id("addon:item/base"),id("addon:item/a"),id("addon:item/b"),id("addon:item/c"))), "all base and override geometry dependencies marked for native baking");
        var nativeItem = LegacyItemModels.clientItem(new LegacyItemModels.Definition(id("addon:item/native"),List.of()));
        var merged = LegacyItemModels.merge(Map.of(id("addon:base"),nativeItem),Map.of(id("addon:base"),definition,id("addon:second"),definition));
        check(merged.get(id("addon:base")) == nativeItem, "explicit native item definition wins over legacy adapter");
        check(merged.containsKey(id("addon:second")), "legacy-only item receives a native definition");
        var stack = new ItemStack(Items.DIAMOND,2);
        stack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(List.of(3F),List.of(),List.of(),List.of()));
        check(LegacyItemModels.value(custom,stack,null,null,77)==3F,"legacy numeric model data uses first native float without clamping");
        check(LegacyItemModels.value(rare,stack,null,null,77)==Float.NEGATIVE_INFINITY,"unregistered addon property remains unavailable");
        var property = id("contract:clamped");
        LegacyItemModels.register(Items.DIAMOND,property,(item,world,entity,seed)->seed==77?2F:-2F);
        check(LegacyItemModels.value(property,stack,null,null,77)==1F,"legacy per-item callback keeps upper clamp");
        check(LegacyItemModels.value(property,stack,null,null,0)==0F,"legacy per-item callback keeps lower clamp");
        LegacyItemModels.registerColors((item,index)->index==11?0x80123456:index==12?0x00123456:-1,Items.DIAMOND);
        check(new LegacyItemModels.Tint(11).calculate(stack,null,null)==0x80123456,"native tint source preserves ARGB alpha and forwards arbitrary tint index");
        check(new LegacyItemModels.Tint(12).calculate(stack,null,null)==0x00123456,"transparent provider alpha is retained");
        check(new LegacyItemModels.Tint(0).calculate(stack,null,null)==-1,"white/uncolored tint preserved");
        var predicateCalls = new java.util.concurrent.atomic.AtomicInteger();
        LegacyItemModels.register(Items.DIAMOND,rare,(item,world,entity,seed)->{ predicateCalls.incrementAndGet();return 0F; });
        var selected = new AtomicReference<Identifier>();
        var wrapper = definition.bind(model -> (state,item,resolver,display,world,owner,seed)->{
            if(item!=stack||display!=ItemDisplayContext.GUI||seed!=77||world!=null||owner!=null)
                throw new AssertionError("native item update context was altered");
            selected.set(model);
        });
        wrapper.update(null,stack,null,ItemDisplayContext.GUI,null,null,77);
        check(selected.get().equals(id("addon:item/b")),"native item update selects current override and forwards context");
        stack.set(DataComponents.CUSTOM_MODEL_DATA,CustomModelData.EMPTY);
        wrapper.update(null,stack,null,ItemDisplayContext.GUI,null,null,77);
        check(selected.get().equals(id("addon:item/base")),"changing stack data selects again rather than caching a previous model");
        check(predicateCalls.get()==2,"legacy property functions run once each per update, including when no override matches");
        try {
            LegacyItemModels.parse(id("addon:item/bad"),JsonParser.parseString("{\"overrides\":[{\"predicate\":{\"cast\":\"bad\"},\"model\":\"addon:item/a\"}]}").getAsJsonObject());
            throw new AssertionError("malformed model predicate accepted");
        } catch (IllegalArgumentException expected) { checks++; }
        net.minecraft.client.resources.model.ClientItemInfoLoader.class.getDeclaredMethod("scheduleLoad",net.minecraft.server.packs.resources.ResourceManager.class,java.util.concurrent.Executor.class);
        net.minecraft.client.renderer.item.ItemModelResolver.class.getDeclaredMethod("appendItemLayers",net.minecraft.client.renderer.item.ItemStackRenderState.class,ItemStack.class,ItemDisplayContext.class,net.minecraft.world.level.Level.class,net.minecraft.world.entity.ItemOwner.class,int.class);
        net.minecraft.client.renderer.item.properties.conditional.FishingRodCast.class.getDeclaredMethod("get",ItemStack.class,net.minecraft.client.multiplayer.ClientLevel.class,net.minecraft.world.entity.LivingEntity.class,int.class,ItemDisplayContext.class);
        checks+=3;
        Path assets=Path.of("common/src/main/resources/assets");
        if(!Files.isDirectory(assets))assets=Path.of("src/main/resources/assets");
        if(!Files.isDirectory(assets))throw new AssertionError("model fixtures require repository assets");
        int models=0,overrides=0,builtin=0;
        try(var files=Files.walk(assets)) {
            for(var file:files.filter(path->path.toString().replace('\\','/').contains("/models/item/")&&path.toString().endsWith(".json")).sorted().toList()) {
                var relative=assets.relativize(file).toString().replace('\\','/');
                int slash=relative.indexOf('/');
                var resourceId=Identifier.fromNamespaceAndPath(relative.substring(0,slash),relative.substring(slash+1));
                var modelId=LegacyItemModels.modelId(resourceId);
                var asset=JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                var parsed=LegacyItemModels.parse(modelId,asset);
                check(parsed.dependencies().contains(modelId),"asset base dependency retained: "+file);
                models++;overrides+=parsed.overrides().size();
                if(asset.has("parent")&&asset.get("parent").getAsString().endsWith("builtin/entity"))builtin++;
            }
        }
        System.out.println("{\"model_assets_parsed\":"+models+",\"legacy_overrides_parsed\":"+overrides+",\"builtin_entity_roots_pending_special_renderer\":"+builtin+",\"scope\":\"production model discovery, selection and tint adapters; no bake/GPU/reload\"}");
        return checks;
    }
}
