/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.fabric.client.model;

import com.cobblemon.mod.common.client.model.StandaloneModelKey;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;

/** Stable keys survive reload preparation; suppliers always resolve the currently applied model manager. */
public final class StandaloneModels implements ModelLoadingPlugin {
    private record Entry(Identifier source, ExtraModelKey<BlockStateModel> key) {}
    private final Map<StandaloneModelKey, Entry> entries = new ConcurrentHashMap<>();
    private final Function<ExtraModelKey<BlockStateModel>, BlockStateModel> lookup;

    public StandaloneModels() {
        this(key -> ((FabricModelManager) Minecraft.getInstance().getModelManager()).getModel(key));
    }
    public StandaloneModels(Function<ExtraModelKey<BlockStateModel>, BlockStateModel> lookup) {
        this.lookup = Objects.requireNonNull(lookup);
    }
    public Supplier<BlockStateModel> declare(StandaloneModelKey id, Identifier source) {
        var entry = entries.compute(id, (key, previous) -> {
            if (previous != null) {
                if (!previous.source().equals(source)) throw new IllegalArgumentException("Conflicting standalone model: " + id);
                return previous;
            }
            return new Entry(source, ExtraModelKey.create(id::toString));
        });
        return () -> Objects.requireNonNull(lookup.apply(entry.key()), "Standalone model missing after reload: " + id);
    }
    @Override public void initialize(ModelLoadingPlugin.Context context) {
        entries.values().forEach(entry -> context.addModel(entry.key(), SimpleUnbakedExtraModel.blockStateModel(entry.source())));
    }
}
