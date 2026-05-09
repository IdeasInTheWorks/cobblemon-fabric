/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.decor

import com.cobblemon.mod.common.CobblemonBlocks
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.TallFlowerBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class TerrainFlowerBlock(properties: Properties?) : TallFlowerBlock(properties) {

    init {
        registerDefaultState(stateDefinition.any()
            .setValue(HALF, DoubleBlockHalf.LOWER)
            .setValue(GLOWING, false))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(HALF, GLOWING)
    }

    override fun tick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource?) {
        //TODO: Figure out why this isn't working
        if (state.`is`(CobblemonBlocks.LUCIBELL)) {this.checkInside(level, pos, state)}
        if (state.`is`(CobblemonBlocks.VERDROOT)) {this.checkRaining(level, pos, state)}
        if (state.`is`(CobblemonBlocks.VOLTCONIA)) {this.checkThundering(level, pos, state)}
    }

    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity?) {
        if (!level.isClientSide) {
            if (!state.getValue(GLOWING) && state.`is`(CobblemonBlocks.LUCIBELL)) {
                this.checkInside(level, pos, state)
            }
        }
    }
    private fun checkInside(level: Level, pos: BlockPos, state: BlockState) {
        var blockState = level.getBlockState(pos)
        val activeState = blockState.getValue(GLOWING) as Boolean
        var entityInsideFlower = false
        val list = level.getEntities(null as Entity?, blockState.getShape(level, pos).bounds().move(pos))
        if (!list.isEmpty()) {for (entity in list) { entityInsideFlower = true; break} }
        if (entityInsideFlower != activeState && state.`is`(CobblemonBlocks.LUCIBELL)) {
            blockState = blockState.setValue(GLOWING, entityInsideFlower) as BlockState
            level.setBlock(pos, blockState, UPDATE_CLIENTS)
            //TODO: Figure out way to apply blockstate to both halves of the flower if only one half is being intersected (problem is the detection needs to exist regardless of which half of the flower is interacted)
        }
        if (entityInsideFlower) {
            level.scheduleTick(BlockPos(pos), this, 30)
        }
    }

    fun checkRaining(world: Level, pos: BlockPos, state: BlockState) {
        val activeState = state.getValue(GLOWING) as Boolean
        //Verdroot On
        if (world.isRaining && !world.isThundering && !activeState && state.`is`(CobblemonBlocks.VERDROOT)) {
            world.setBlock(pos,state.setValue(GLOWING, true), UPDATE_CLIENTS)
        }
        //Verdroot off
        if (!world.isRaining && !world.isThundering && activeState && state.`is`(CobblemonBlocks.VERDROOT)) {
            world.setBlock(pos,state.setValue(GLOWING, false), UPDATE_CLIENTS)
        }
    }
    fun checkThundering(world: Level, pos: BlockPos, state: BlockState) {
        val activeState = state.getValue(GLOWING) as Boolean
        //Voltconia On
        if (world.isRaining && world.isThundering && !activeState && state.`is`(CobblemonBlocks.VOLTCONIA)) {
            world.setBlock(pos,state.setValue(GLOWING, true), UPDATE_CLIENTS)
        }
        //Voltconia off
        if (!world.isRaining && !world.isThundering && activeState && state.`is`(CobblemonBlocks.VOLTCONIA)) {
            world.setBlock(pos,state.setValue(GLOWING, false), UPDATE_CLIENTS)
        }
    }

    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = AABB
    companion object {
        private val AABB = Shapes.box(0.125, 0.0, 0.125, 0.875, 1.0, 0.875)
        val GLOWING = BooleanProperty.create("glowing")

    }

}