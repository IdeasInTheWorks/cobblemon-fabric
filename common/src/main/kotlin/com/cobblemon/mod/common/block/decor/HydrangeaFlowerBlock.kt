/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.decor

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.BonemealableBlock
import net.minecraft.world.level.block.MultifaceBlock
import net.minecraft.world.level.block.MultifaceSpreader
import net.minecraft.world.level.block.state.BlockState

//TODO: Prevent it from being replaced if you interact on the block (like regular flowers, unlike lichen)
//TODO: Allow it to dangle like VineBlock.class
class HydrangeaFlowerBlock(properties: Properties?) : MultifaceBlock(properties), BonemealableBlock {
    val CODEC: MapCodec<HydrangeaFlowerBlock?> =
        simpleCodec<HydrangeaFlowerBlock?>({ properties: Properties? -> HydrangeaFlowerBlock(properties) })
    override fun codec(): MapCodec<HydrangeaFlowerBlock?> {
        return CODEC
    }

    private val spreader = MultifaceSpreader(this)

    override fun isValidBonemealTarget(level: LevelReader?, pos: BlockPos?, state: BlockState?): Boolean {
        return Direction.stream().anyMatch { arg4: Direction? ->
            this.spreader.canSpreadInAnyDirection(
                state,
                level,
                pos,
                arg4!!.getOpposite()
            )
        }
    }

    override fun isBonemealSuccess(level: Level?, random: RandomSource?, pos: BlockPos?, state: BlockState?): Boolean {
        return true
    }

    override fun performBonemeal(level: ServerLevel?, random: RandomSource?, pos: BlockPos?, state: BlockState?) {
        this.spreader.spreadFromRandomFaceTowardRandomDirection(state, level, pos, random)
    }

    override fun getSpreader(): MultifaceSpreader {
        return this.spreader
    }

}