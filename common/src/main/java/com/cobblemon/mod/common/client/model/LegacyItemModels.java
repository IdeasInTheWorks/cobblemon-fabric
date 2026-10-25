/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.client.model;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;

/** Adapts existing item geometry and ordered override predicates to native item-model updates. */
public final class LegacyItemModels {
    @FunctionalInterface public interface PredicateFunction {
        float get(ItemStack stack, ClientLevel level, LivingEntity entity, int seed);
    }
    @FunctionalInterface public interface ItemColor {
        int getColor(ItemStack stack, int tintIndex);
    }
    private static final Map<Item, Map<Identifier, PredicateFunction>> PREDICATES = new ConcurrentHashMap<>();
    private static final Map<Item, ItemColor> COLORS = new ConcurrentHashMap<>();
    private static final Identifier CUSTOM_DATA = Identifier.withDefaultNamespace("custom_model_data");

    public static void register(Item item, Identifier property, PredicateFunction function) {
        PREDICATES.computeIfAbsent(item, ignored -> new ConcurrentHashMap<>()).put(property, function);
    }
    public static void registerColors(ItemColor provider, Item... items) {
        for (Item item : items) COLORS.put(item, provider);
    }
    public static int color(ItemStack stack, int index) {
        var provider = COLORS.get(stack.getItem());
        // Both native render paths consume ARGB, including provider-supplied alpha.
        return provider == null ? -1 : provider.getColor(stack, index);
    }
    public static float value(Identifier property, ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
        var callback = PREDICATES.getOrDefault(stack.getItem(), Map.of()).get(property);
        if (callback != null) return Mth.clamp(callback.get(stack, level, owner == null ? null : owner.asLivingEntity(), seed), 0F, 1F);
        if (property.equals(CUSTOM_DATA)) {
            var data = stack.get(DataComponents.CUSTOM_MODEL_DATA);
            var number = data == null ? null : data.getFloat(0);
            return number == null ? 0F : number;
        }
        // Matches the old resolver when an addon property has no registered function.
        return Float.NEGATIVE_INFINITY;
    }
    public static Identifier itemId(Identifier modelId) {
        return modelId.withPath(path -> path.startsWith("item/") ? path.substring(5) : path);
    }
    public static boolean supportsResource(Identifier resource) {
        String path = resource.getPath();
        return path.startsWith("models/item/") && path.endsWith(".json");
    }
    public static Identifier modelId(Identifier resource) {
        if (!resource.getPath().startsWith("models/") || !resource.getPath().endsWith(".json"))
            throw new IllegalArgumentException("Not a model JSON resource: " + resource);
        return resource.withPath(path -> path.substring(7, path.length() - 5));
    }
    public record ModelOverride(Identifier model, Map<Identifier, Float> predicates) {
        public ModelOverride { Objects.requireNonNull(model); predicates = Collections.unmodifiableMap(new LinkedHashMap<>(predicates)); }
        boolean matches(Function<Identifier, Float> values) {
            for (var entry : predicates.entrySet()) if (!(values.apply(entry.getKey()) >= entry.getValue())) return false;
            return true;
        }
    }
    public record Definition(Identifier model, List<ModelOverride> overrides) {
        public Definition { Objects.requireNonNull(model); overrides = List.copyOf(overrides); }
        public Identifier select(Function<Identifier, Float> values) {
            for (int i = overrides.size() - 1; i >= 0; i--) if (overrides.get(i).matches(values)) return overrides.get(i).model();
            return model;
        }
        public Set<Identifier> dependencies() {
            var result = new LinkedHashSet<Identifier>(); result.add(model);
            overrides.forEach(value -> result.add(value.model())); return Collections.unmodifiableSet(result);
        }
        public ItemModel bind(Function<Identifier, ItemModel> models) {
            var baked = new LinkedHashMap<Identifier, ItemModel>();
            dependencies().forEach(id -> baked.put(id, Objects.requireNonNull(models.apply(id))));
            var properties = new LinkedHashSet<Identifier>();
            overrides.forEach(override -> properties.addAll(override.predicates().keySet()));
            return (state, stack, resolver, display, level, owner, seed) -> {
                var values = new HashMap<Identifier, Float>();
                properties.forEach(id -> values.put(id, value(id, stack, level, owner, seed)));
                baked.get(select(values::get)).update(state, stack, resolver, display, level, owner, seed);
            };
        }
    }
    public static Definition parse(Identifier model, JsonObject json) {
        var overrides = new ArrayList<ModelOverride>();
        if (json.has("overrides")) for (var element : json.getAsJsonArray("overrides")) {
            var entry = element.getAsJsonObject();
            var predicates = new LinkedHashMap<Identifier, Float>();
            for (var condition : entry.getAsJsonObject("predicate").entrySet()) {
                if (!condition.getValue().isJsonPrimitive() || !condition.getValue().getAsJsonPrimitive().isNumber())
                    throw new IllegalArgumentException("Non-numeric model predicate: " + condition.getKey());
                float threshold = condition.getValue().getAsFloat();
                if (!Float.isFinite(threshold)) throw new IllegalArgumentException("Non-finite model predicate");
                predicates.put(Identifier.parse(condition.getKey()), threshold);
            }
            overrides.add(new ModelOverride(Identifier.parse(entry.get("model").getAsString()), predicates));
        }
        return new Definition(model, overrides);
    }
    public static ClientItem clientItem(Definition definition) {
        return new ClientItem(new Unbaked(definition), ClientItem.Properties.DEFAULT);
    }
    public static Map<Identifier, ClientItem> merge(Map<Identifier, ClientItem> nativeItems, Map<Identifier, Definition> legacy) {
        var result = new LinkedHashMap<>(nativeItems);
        legacy.forEach((id, definition) -> result.putIfAbsent(id, clientItem(definition)));
        return Map.copyOf(result);
    }
    public record Unbaked(Definition definition) implements ItemModel.Unbaked {
        @Override public MapCodec<Unbaked> type() { return MapCodec.unit(this); }
        @Override public void resolveDependencies(ResolvableModel.Resolver resolver) {
            definition.dependencies().forEach(resolver::markDependency);
        }
        @Override public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transform) {
            return definition.bind(id -> {
                var baker = context.blockModelBaker(); var resolved = baker.getModel(id);
                var geometry = resolved.bakeTopGeometry(resolved.getTopTextureSlots(), baker, BlockModelRotation.IDENTITY);
                int maxTint = geometry.getAll().stream().mapToInt(quad -> quad.materialInfo().tintIndex()).max().orElse(-1);
                var tints = new ArrayList<ItemTintSource>();
                for (int i = 0; i <= maxTint; i++) tints.add(new Tint(i));
                return new CuboidItemModelWrapper.Unbaked(id, Optional.empty(), tints).bake(context, transform);
            });
        }
    }
    public record Tint(int index) implements ItemTintSource {
        @Override public int calculate(ItemStack stack, ClientLevel level, LivingEntity entity) { return color(stack, index); }
        @Override public MapCodec<Tint> type() { return MapCodec.unit(this); }
    }
    private LegacyItemModels() {}
}
