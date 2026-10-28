/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.boat

import com.cobblemon.mod.common.client.model.NativeBoatRenderer
import com.cobblemon.mod.common.entity.boat.CobblemonBoatEntity
import com.cobblemon.mod.common.entity.boat.CobblemonBoatType
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.model.`object`.boat.BoatModel
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.Identifier

class CobblemonBoatRenderer(ctx: EntityRendererProvider.Context, hasChest: Boolean) : NativeBoatRenderer<CobblemonBoatEntity>(ctx) {
    init {
        CobblemonBoatType.values().forEach { type ->
            val subPath = if (hasChest) "chest_boat" else "boat"
            addModel(cobblemonResource(type.name.lowercase()),
                cobblemonResource("textures/entity/$subPath/${type.name.lowercase()}.png"),
                BoatModel(ctx.bakeLayer(createBoatModelLayer(type, hasChest))))
        }
    }
    override fun variant(boat: CobblemonBoatEntity): Identifier = cobblemonResource(boat.boatType.name.lowercase())
    companion object {
        internal fun createBoatModelLayer(type: CobblemonBoatType, hasChest: Boolean): ModelLayerLocation {
            val subPath = if (hasChest) "chest_boat" else "boat"
            return ModelLayerLocation(cobblemonResource("$subPath/${type.name.lowercase()}"), "main")
        }
    }
}
