/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.atlas

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.resources.Identifier

/** A live handle to an atlas owned and reloaded by Minecraft's AtlasManager. */
class CobblemonAtlas(
    @Suppress("UNUSED_PARAMETER") textureManager: TextureManager,
    val atlasId: Identifier,
    val sourcePath: Identifier
) {
    val textureAtlas: TextureAtlas
        get() = Minecraft.getInstance().atlasManager.getAtlasOrThrow(atlasId)

    fun getSprite(id: Identifier): TextureAtlasSprite = textureAtlas.getSprite(id)
}
