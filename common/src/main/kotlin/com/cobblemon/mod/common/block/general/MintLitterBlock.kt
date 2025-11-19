/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.general

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.PinkPetalsBlock
import net.minecraft.world.level.block.state.BlockState


class MintLitterBlock(properties: Properties?) : PinkPetalsBlock(properties) {
// TODO When updated to a version with actual Leaf Litter, switch the source block to that to get better hitbox values, I'm not fighting the Pink Petal hitboxes
// TODO Find some way to change the blockstate name from `flower_amount` to just `amount` without breaking anything
    override fun canSurvive(state: BlockState?, level: LevelReader, pos: BlockPos): Boolean {
        val posBelow = pos.below()
        return level.getBlockState(posBelow).isFaceSturdy(level, posBelow, Direction.UP)
    }

    override fun isValidBonemealTarget(level: LevelReader?, pos: BlockPos?, state: BlockState?): Boolean {
        return false
    }

    override fun isBonemealSuccess(level: Level?, random: RandomSource?, pos: BlockPos?, state: BlockState?): Boolean {
        return false
    }

}