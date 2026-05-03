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
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.GrowingPlantHeadBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.VoxelShape


class WistensuFlowerBlock(properties: Properties?) : GrowingPlantHeadBlock(properties, Direction.DOWN, SHAPE, false, 0.1) {
    companion object {
        val CODEC: MapCodec<WistensuFlowerBlock> = RecordCodecBuilder.mapCodec {
            it.group(propertiesCodec()).apply(it, ::WistensuFlowerBlock)
        }
        val SHAPE: VoxelShape = box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0)
    }
    override fun codec(): MapCodec<WistensuFlowerBlock> = CODEC

    override fun getBlocksToGrowWhenBonemealed(random: RandomSource): Int {
        var d = 1.0
        var i = 0
        while (random.nextDouble() < d) {
            d *= 0.826
            ++i
        }
        return i
    }

    override fun canGrowInto(state: BlockState): Boolean {
        return state.isAir
    }

    override fun getBodyBlock(): Block {
        return CobblemonBlocks.WISTENSU_VINE
    }
}