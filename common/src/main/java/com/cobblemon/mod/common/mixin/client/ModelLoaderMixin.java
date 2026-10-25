/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.client.model.LegacyItemModels;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.client.resources.model.ClientItemInfoLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

@Mixin(ClientItemInfoLoader.class)
public abstract class ModelLoaderMixin {
    @ModifyReturnValue(method = "scheduleLoad", at = @At("RETURN"), require = 1)
    private static CompletableFuture<ClientItemInfoLoader.LoadedClientInfos> cobblemon$legacyItemInfos(
            CompletableFuture<ClientItemInfoLoader.LoadedClientInfos> original, ResourceManager resources, Executor executor) {
        return original.thenApplyAsync(loaded -> {
            var definitions = new LinkedHashMap<Identifier, LegacyItemModels.Definition>();
            resources.listResources("models/item", LegacyItemModels::supportsResource).forEach((resourceId, resource) -> {
                var model = LegacyItemModels.modelId(resourceId);
                var item = LegacyItemModels.itemId(model);
                if (loaded.contents().containsKey(item) || resources.getResource(item.withPath(path -> "items/" + path + ".json")).isPresent()) return;
                try (var reader = resource.openAsReader()) {
                    definitions.put(item, LegacyItemModels.parse(model, JsonParser.parseReader(reader).getAsJsonObject()));
                } catch (Exception error) {
                    LogUtils.getLogger().error("Cannot load legacy Cobblemon item model {}", resourceId, error);
                }
            });
            return new ClientItemInfoLoader.LoadedClientInfos(LegacyItemModels.merge(loaded.contents(), definitions));
        }, executor);
    }
}
