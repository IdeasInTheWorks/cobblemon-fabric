package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.block.decor.TerrainFlowerBlock
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class TerrainFlowerBlockEntity(pos: BlockPos, state: BlockState): BlockEntity(CobblemonBlockEntities.TERRAIN_FLOWER, pos, state) {
    companion object {
        fun serverTick(level: Level, pos: BlockPos, state: BlockState, terrainFlowerBlockEntity: TerrainFlowerBlockEntity) {
            if (level.isClientSide) return
            if (state.block !is TerrainFlowerBlock) return

            val glowing = state.getValue(TerrainFlowerBlock.GLOWING)

            val wantedState = if (state.`is`(CobblemonBlocks.VOLTCONIA)) level.isThundering
                else if (state.`is`(CobblemonBlocks.VERDROOT)) level.isRaining
                else null // We dont control glowing here

            if (wantedState != null && glowing != wantedState) {
                level.setBlock(pos, state.setValue(TerrainFlowerBlock.GLOWING, wantedState), Block.UPDATE_CLIENTS)
            }
        }
    }
}