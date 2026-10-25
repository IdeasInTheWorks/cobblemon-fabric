/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.fabric.client.model;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.rendering.v1.AtlasRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

/** Native atlas ownership and a post-model-application callback for coded berry geometry. */
public final class AtlasReloadListener implements IdentifiableResourceReloadListener {
    private final Identifier id;
    private final Consumer<ResourceManager> apply;

    public AtlasReloadListener(Identifier id, Consumer<ResourceManager> apply) {
        this.id = id;
        this.apply = apply;
    }

    public static void registerAtlas(Identifier texture, Identifier definition) {
        AtlasRegistry.register(configuration(texture, definition));
    }

    public static AtlasManager.AtlasConfig configuration(Identifier texture, Identifier definition) {
        return new AtlasManager.AtlasConfig(texture, definition, true);
    }

    @Override public Identifier getFabricId() { return id; }
    @Override public Collection<Identifier> getFabricDependencies() { return List.of(ResourceReloadListenerKeys.MODELS); }

    @Override public CompletableFuture<Void> reload(SharedState state, Executor prepare, PreparationBarrier barrier, Executor executor) {
        return barrier.wait(null).thenRunAsync(() -> apply.accept(state.resourceManager()), executor);
    }
}
