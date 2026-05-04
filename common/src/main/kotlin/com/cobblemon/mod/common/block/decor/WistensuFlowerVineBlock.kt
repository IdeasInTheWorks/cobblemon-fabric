/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.decor

import com.cobblemon.mod.common.CobblemonBlocks
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Direction
import net.minecraft.world.level.block.GrowingPlantBodyBlock
import net.minecraft.world.level.block.GrowingPlantHeadBlock
import net.minecraft.world.phys.shapes.VoxelShape

//TODO: Root/connection shit
class WistensuFlowerVineBlock(properties: Properties?) : GrowingPlantBodyBlock(properties, Direction.DOWN, SHAPE, false)
{
    companion object {
        val CODEC: MapCodec<WistensuFlowerVineBlock> = RecordCodecBuilder.mapCodec {
            it.group(propertiesCodec()).apply(it, ::WistensuFlowerVineBlock)
        }
        val SHAPE: VoxelShape = box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0)
    }
    override fun codec(): MapCodec<WistensuFlowerVineBlock> = CODEC

    override fun getHeadBlock(): GrowingPlantHeadBlock {
        return CobblemonBlocks.WISTENSU as GrowingPlantHeadBlock
    }
}