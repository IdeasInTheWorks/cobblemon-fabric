/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.decor

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.CobblemonSounds
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.util.StringRepresentable
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.level.*
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

@Suppress("OVERRIDE_DEPRECATION")
class TerrainSeedBlock(private val terrainSeedType: TerrainSeedType, settings: Properties) : CropBlock(settings) {
    companion object {
        val CODEC: MapCodec<TerrainSeedBlock> = RecordCodecBuilder.mapCodec { it.group(
            TerrainSeedType.CODEC.fieldOf("terrainSeedType").forGetter(TerrainSeedBlock::terrainSeedType),
            propertiesCodec()
        ).apply(it, ::TerrainSeedBlock) }
        const val MATURE_AGE = 4
        val AGE: IntegerProperty = IntegerProperty.create("age", 0, MATURE_AGE)
        val HALF: EnumProperty<DoubleBlockHalf> = BlockStateProperties.DOUBLE_BLOCK_HALF

        val AGE_TO_SHAPE = arrayOf(
            box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0), // Stage 0
            box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0), // Stage 1
            box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0), // Stage 2
            box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0), // Stage 3
            box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0),  // Stage 4
        )

        val AGE_TO_SHAPE_TOP = arrayOf(
            box(2.0, 0.0, 2.0, 14.0, 1.0, 14.0), // Stage 0
            box(2.0, 0.0, 2.0, 14.0, 1.0, 14.0), // Stage 1
            box(2.0, 0.0, 2.0, 14.0, 8.0, 14.0), // Stage 2
            box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0), // Stage 3
            box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0),  // Stage 4
        )
    }

    override fun codec(): MapCodec<out CropBlock> {
        return CODEC
    }

    init {
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(HALF, DoubleBlockHalf.LOWER)
        )
    }

    override fun getAgeProperty(): IntegerProperty = AGE

    override fun getMaxAge(): Int = MATURE_AGE

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(AGE, HALF)
    }

    override fun getBaseSeedId(): ItemLike = this.terrainSeedType.getSeed()

    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape =
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) AGE_TO_SHAPE_TOP[getAge(state)] else AGE_TO_SHAPE[this.getAge(state)]

    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER || !canGrow(world, pos, state)) return

        if (hasSufficientLight(world, pos) && random.nextInt(32) == 0) {
            grow(world, pos, state, 1)
        }
    }

    private fun canGrow(world: LevelReader, pos: BlockPos, state: BlockState): Boolean {
        return !this.isMaxAge(state) && canSurvive(state, world, pos) && !(!canGrowInto(world, pos.above()) && state.getValue(AGE) >= 4)
    }

    private fun canGrowInto(world: LevelReader, pos: BlockPos): Boolean {
        val blockState = world.getBlockState(pos)
        return blockState.isAir || blockState.`is`(this.terrainSeedType.getCropBlock())
    }

    override fun updateShape(state: BlockState, direction: Direction, neighborState: BlockState, level: LevelAccessor, pos: BlockPos, neighborPos: BlockPos): BlockState {
        val doubleBlockHalf = state.getValue(DoublePlantBlock.HALF) as DoubleBlockHalf
        if (direction.getAxis() !== Direction.Axis.Y || doubleBlockHalf == DoubleBlockHalf.LOWER != (direction == Direction.UP) || neighborState.`is`(this) && neighborState.getValue(DoublePlantBlock.HALF) != doubleBlockHalf
        ) {
            return if (doubleBlockHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canSurvive(level, pos)) Blocks.AIR.defaultBlockState() else super.updateShape(state, direction, neighborState, level, pos, neighborPos)
        } else {
            return Blocks.AIR.defaultBlockState()
        }
    }

    override fun playerWillDestroy(world: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {
        if (!world.isClientSide && player?.isCreative == true) {
            var blockPos: BlockPos = BlockPos.ZERO
            var blockState: BlockState = state
            val part = state.getValue(DoublePlantBlock.HALF)
            if (part == DoubleBlockHalf.UPPER && world.getBlockState(pos.below().also { blockPos = it }).also { blockState = it }.`is`(state.block) && blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
                val blockState2 = if (blockState.fluidState.`is`(Fluids.WATER)) Blocks.WATER.defaultBlockState() else Blocks.AIR.defaultBlockState()
                world.setBlock(blockPos, blockState2, UPDATE_ALL or UPDATE_SUPPRESS_DROPS)
                world.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, blockPos, getId(blockState))
            }
        }
        return super.playerWillDestroy(world, pos, state, player)
    }

    override fun isValidBonemealTarget(level: LevelReader, pos: BlockPos, state: BlockState): Boolean {
        val posAndState = getLowerHalf(level, pos, state)
        return if (posAndState == null) false else this.canGrow(
            level,
            posAndState.first,
            posAndState.second
        )
    }

    override fun performBonemeal(level: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState) {
        val posAndState = getLowerHalf(level, pos, state)
        if (posAndState != null) {
            grow(level, posAndState.first, posAndState.second, getBonemealAgeIncrease(level))
        }
    }

    fun grow(world: Level, pos: BlockPos, state: BlockState, increment: Int) {
        if (!canGrow(world, pos, state)) return

        val newAge = (this.getAge(state) + increment).coerceAtMost(this.maxAge)
        world.setBlock(pos, state.setValue(this.ageProperty, newAge), UPDATE_CLIENTS)

        val abovePos = pos.above()
        val stateAbove = world.getBlockState(abovePos)
        // Set top half depending on age
        if (newAge >= (2)) {
            if (stateAbove.isAir) {
                world.setBlock(
                    abovePos,
                    this.defaultBlockState().setValue(AGE, newAge).setValue(HALF, DoubleBlockHalf.UPPER),
                    UPDATE_CLIENTS
                )
            }
            else if (stateAbove.hasProperty(HALF) && stateAbove.getValue(HALF) == DoubleBlockHalf.UPPER) {
                world.setBlock(
                    abovePos,
                    stateAbove.setValue(AGE, newAge),
                    UPDATE_CLIENTS
                )
            }
        }
        // Swaps blocks to the actual Flower when fully grown
        if (newAge == this.maxAge) {
            val nextStateLower = this.terrainSeedType.getFlowerBlock().defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER)
            val nextStateUpper = this.terrainSeedType.getFlowerBlock().defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER)
            world.setBlock(abovePos, nextStateUpper, UPDATE_CLIENTS)
            world.setBlock(pos, nextStateLower, UPDATE_CLIENTS)
        }
    }

    override fun getBonemealAgeIncrease(level: Level): Int {
        return 1
    }

    override fun canSurvive(state: BlockState, world: LevelReader, pos: BlockPos): Boolean {
        return when (state.getValue(HALF)) {
            DoubleBlockHalf.LOWER -> {
                val floorBlock = pos.below()
                val floor = world.getBlockState(floorBlock)

                floor.`is`(Blocks.FARMLAND)
            }
            DoubleBlockHalf.UPPER -> {
                getLowerHalf(world, pos, state) != null
                // Upper part survives only if the lower part is valid
            }
        }
    }

    private fun getLowerHalf(level: LevelReader, pos: BlockPos, state: BlockState): Pair<BlockPos, BlockState>? {
        if (isLower(state)) {
            return Pair(pos, state)
        } else {
            val blockPos = pos.below()
            val blockState = level.getBlockState(blockPos)
            return if (isLower(blockState)) Pair(
                blockPos,
                blockState
            ) else null
        }
    }

    private fun isLower(state: BlockState): Boolean =
        state.`is`(this.terrainSeedType.getCropBlock()) && state.getValue(HALF) == DoubleBlockHalf.LOWER

    override fun getSoundType(state: BlockState): SoundType {
        return CobblemonSounds.HEARTY_GRAINS_SOUNDS
    }

    @Suppress("unused")
    enum class TerrainSeedType : StringRepresentable {
        ELECTRIC,
        PSYCHIC,
        MISTY,
        GRASSY;
        fun getSeed(): Item = when (this) {
            ELECTRIC -> CobblemonItems.ELECTRIC_SEED
            PSYCHIC -> CobblemonItems.PSYCHIC_SEED
            MISTY -> CobblemonItems.MISTY_SEED
            GRASSY -> CobblemonItems.GRASSY_SEED
        }
        fun getCropBlock(): TerrainSeedBlock = when (this) {
            ELECTRIC -> CobblemonBlocks.ELECTRIC_SEED
            PSYCHIC -> CobblemonBlocks.PSYCHIC_SEED
            MISTY -> CobblemonBlocks.MISTY_SEED
            GRASSY -> CobblemonBlocks.GRASSY_SEED
        }
        fun getFlowerBlock(): Block = when (this) {
            ELECTRIC -> CobblemonBlocks.VOLTCONIA
            PSYCHIC -> CobblemonBlocks.LUCIBELL
            MISTY -> CobblemonBlocks.MISTLEAF
            GRASSY -> CobblemonBlocks.VERDROOT
        }

        override fun getSerializedName(): String = this.name.lowercase()

        companion object {
            val CODEC = StringRepresentable.fromEnum(::values)
        }
    }
}