/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.api.tags.CobblemonBlockTags
import com.cobblemon.mod.common.block.TypeGemClusterBlock.Companion.FACING
import com.cobblemon.mod.common.block.TypeGemClusterBlock.Companion.GROWTH_TYPE
import com.cobblemon.mod.common.block.TypeGemClusterBlock.Companion.STAGE
import com.cobblemon.mod.common.block.TypeGemClusterBlock.GrowthType
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.DirectionalBlock
import net.minecraft.world.level.block.state.BlockState
import java.util.*

class TypeGemCoreBlock(properties: Properties) : Block(properties) {

    companion object {
        val BLOCK_TO_CLUSTER: Map<ResourceLocation, Block> by lazy {
            mapOf(
                ResourceLocation.parse("cobblemon:normal_gem_block") to CobblemonBlocks.TYPE_GEM_CLUSTER_NORMAL,
                ResourceLocation.parse("cobblemon:fire_gem_block") to CobblemonBlocks.TYPE_GEM_CLUSTER_FIRE,
                ResourceLocation.parse("cobblemon:water_gem_block") to CobblemonBlocks.TYPE_GEM_CLUSTER_WATER,
                ResourceLocation.parse("cobblemon:electric_gem_block") to CobblemonBlocks.TYPE_GEM_CLUSTER_ELECTRIC,
                ResourceLocation.parse("cobblemon:grass_gem_block") to CobblemonBlocks.TYPE_GEM_CLUSTER_GRASS,
                ResourceLocation.parse("cobblemon:ice_gem_block") to CobblemonBlocks.TYPE_GEM_CLUSTER_ICE,
                ResourceLocation.parse("cobblemon:fighting_gem_block") to CobblemonBlocks.TYPE_GEM_CLUSTER_FIGHTING,
                ResourceLocation.parse("cobblemon:poison_gem_block") to CobblemonBlocks.TYPE_GEM_CLUSTER_POISON,
                ResourceLocation.parse("cobblemon:ground_gem_block") to CobblemonBlocks.TYPE_GEM_CLUSTER_GROUND,
                ResourceLocation.parse("cobblemon:flying_gem_block") to CobblemonBlocks.TYPE_GEM_CLUSTER_FLYING,
                ResourceLocation.parse("cobblemon:psychic_gem_block") to CobblemonBlocks.TYPE_GEM_CLUSTER_PSYCHIC,
                ResourceLocation.parse("cobblemon:bug_gem_block") to CobblemonBlocks.TYPE_GEM_CLUSTER_BUG,
                ResourceLocation.parse("cobblemon:rock_gem_block") to CobblemonBlocks.TYPE_GEM_CLUSTER_ROCK,
                ResourceLocation.parse("cobblemon:ghost_gem_block") to CobblemonBlocks.TYPE_GEM_CLUSTER_GHOST,
                ResourceLocation.parse("cobblemon:dragon_gem_block") to CobblemonBlocks.TYPE_GEM_CLUSTER_DRAGON,
                ResourceLocation.parse("cobblemon:dark_gem_block") to CobblemonBlocks.TYPE_GEM_CLUSTER_DARK,
                ResourceLocation.parse("cobblemon:steel_gem_block") to CobblemonBlocks.TYPE_GEM_CLUSTER_STEEL,
                ResourceLocation.parse("cobblemon:fairy_gem_block") to CobblemonBlocks.TYPE_GEM_CLUSTER_FAIRY
            )
        }
        /*
        fun setStuntState(level: LevelAccessor, gemBlocks: List<Pair<BlockState, BlockPos>>, stunted: Boolean) {
            for ((_, gemPos) in gemBlocks) {
                for (dir in Direction.entries) {
                    val neighborPos = gemPos.relative(dir)
                    val neighborState = level.getBlockState(neighborPos)

                    if (neighborState.block is TypeGemClusterBlock) {
                        if (neighborState.hasProperty(STUNTED) && neighborState.getValue(STUNTED) != stunted) {

                            var updatedState = neighborState.setValue(STUNTED, stunted)

                            if (updatedState.hasProperty(SHOULD_GROW)) {
                                updatedState = updatedState.setValue(SHOULD_GROW, !stunted)
                            }

                            level.setBlock(neighborPos, updatedState, 3)
                        }
                    }
                }
            }
        }
        */

        fun getConnectedGemBlocks(level: BlockGetter, pos: BlockPos): List<Pair<BlockState, BlockPos>> {
            val connectedGems = mutableListOf<Pair<BlockState, BlockPos>>()
            val visited = mutableSetOf<BlockPos>()
            val queue: Queue<BlockPos> = LinkedList()

            visited.add(pos)
            queue.add(pos)
            connectedGems.add(level.getBlockState(pos) to pos)

            while (queue.isNotEmpty()) {
                val current = queue.poll()

                for (direction in Direction.entries) {
                    val neighbor = current.relative(direction)
                    if (visited.add(neighbor)) {
                        val state = level.getBlockState(neighbor)
                        if (state.`is`(CobblemonBlockTags.TYPE_GEM_BLOCKS)) {
                            connectedGems.add(state to neighbor)
                            queue.add(neighbor)
                        }
                    }
                }
            }
            return connectedGems
        }

        fun addClustersToBlock(state: BlockState, pos: BlockPos, level: LevelAccessor, random: RandomSource) {
            Direction.entries.forEach {
                if (random.nextFloat() < Cobblemon.config.typeGemGrowthChance) {
                    val faceState = level.getBlockState(pos.relative(it))
                    val randomGrowthType = decideClusterGrowthType(level, pos)
                    val newState = BLOCK_TO_CLUSTER[BuiltInRegistries.BLOCK.getKey(state.block)]?.defaultBlockState()
                        ?.setValue(DirectionalBlock.FACING, it)
                        ?.setValue(GROWTH_TYPE, randomGrowthType)
                        ?: return

//                // if min growth hasnt been hit, force more growth
//                val size = getConnectedGemBlocks(level, pos).size
//                if (size < MIN_GROWTH && randomGrowthType != GrowthType.FULL) {
//                    println("Forced extra growth at size $size")
//                    newState.setValue(GROWTH_TYPE, GrowthType.FULL)
//                }

                    if (faceState.`is`(Blocks.AIR)) {
                        level.setBlock(pos.relative(it), newState, 3)
                    }
                }

            }
        }

        private fun decideClusterGrowthType(level: LevelAccessor, pos: BlockPos, random: RandomSource = RandomSource.create()): GrowthType {
            val growthTypes = if (getConnectedGemBlocks(level, pos).size > Cobblemon.config.typeGemMaxConnected) {
                listOf(GrowthType.DECORATION, GrowthType.STUNTED)
            } else {
                GrowthType.entries
            }
            val totalChance = growthTypes.sumOf { it.chance.toDouble() }
            var roll = random.nextDouble() * totalChance

            for (type in growthTypes) {
                roll -= type.chance.toDouble()
                if (roll <= 0.0) {
                    return type
                }
            }

            return GrowthType.DECORATION
        }
    }

    private fun getConnectedClusters(level: LevelAccessor, pos: BlockPos): List<Pair<BlockState, BlockPos>> {
        val gemBlocks = getConnectedGemBlocks(level, pos)
        val clusters = mutableListOf<Pair<BlockState, BlockPos>>()
        val visited = mutableSetOf<BlockPos>()

        for ((gemState, gemPos) in gemBlocks) {
            val expectedCluster = BLOCK_TO_CLUSTER[BuiltInRegistries.BLOCK.getKey(gemState.block)] ?: continue

            for (direction in Direction.entries) {
                val clusterPos = gemPos.relative(direction)
                if (!visited.add(clusterPos)) continue

                val clusterState = level.getBlockState(clusterPos)
                if (clusterState.block == expectedCluster) {
                    clusters += clusterState to clusterPos
                }
            }
        }
        return clusters
    }

    override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        val gemCount = getConnectedGemBlocks(level, pos).size
//        if (gemCount > Cobblemon.config.typeGemMaxConnected) {
//            getConnectedGemBlocks(level, pos).forEach {
//                if(!it.first.`is`(CobblemonBlocks.TYPE_GEM_CORE)) attachDecorativeClusters(level, it.second, random)
//            }
//            return
//        }

        // if growth has stopped fully but min growth hasn't been reached, randomly set a stopped bud to GROWTH_TYPE full
        val clusters = getConnectedClusters(level, pos)
        val fullClusterCount = clusters.count { (clusterState, _) ->
            clusterState.getValue(GROWTH_TYPE) == GrowthType.FULL
        }

        if (fullClusterCount == 0) {
            if (gemCount < Cobblemon.config.typeGemMinGrowth && clusters.isNotEmpty()) {
                println("Force set a cluster to FULL at size $gemCount")
                val randomCluster = clusters.random()
                level.setBlockAndUpdate(randomCluster.second, randomCluster.first.setValue(GROWTH_TYPE, GrowthType.FULL))
            }
        }


        expand(state, level, pos, random)
    }

    fun expand(state: BlockState, level: LevelAccessor, pos: BlockPos, random: RandomSource) {
        getConnectedGemBlocks(level, pos).forEach {
            val state = it.first
            val pos = it.second
            addClustersToBlock(state, pos, level, random)
            growClusters(state, pos, level, random)
        }
    }

    private fun growClusters(state: BlockState, pos: BlockPos, level: LevelAccessor, random: RandomSource) {
        Direction.entries.forEach { direction ->
            val relativePos = pos.relative(direction)
            val relativeState = level.getBlockState(relativePos)
            val clusterBlock = relativeState.block as? TypeGemClusterBlock ?: return@forEach

            if (relativeState.getValue(DirectionalBlock.FACING) == direction &&
                random.nextFloat() < Cobblemon.config.typeGemGrowthChance
            ) {
                clusterBlock.advanceGrowth(relativeState, level, relativePos, random)
            }
        }
    }

    private fun addInitialClusters(pos: BlockPos, level: ServerLevel, clusterType: TypeGemClusterBlock) {
        Direction.entries.forEach {
            val faceState = level.getBlockState(pos.relative(it))

            if (faceState.`is`(Blocks.AIR)) {
                level.setBlockAndUpdate(pos.relative(it), clusterType.defaultBlockState().setValue(
                    DirectionalBlock.FACING, it)
                    .setValue(GROWTH_TYPE, GrowthType.FULL)
                )
            }
        }
    }

    override fun isRandomlyTicking(state: BlockState): Boolean = true

    private fun attachDecorativeClusters(level: LevelAccessor, gemPos: BlockPos, random: RandomSource) {
        val openDirections = Direction.entries.filter { direction ->
            level.getBlockState(gemPos.relative(direction)).isAir
        }.toMutableList()

        if (openDirections.isEmpty()) {
            return
        }

        val gemState = level.getBlockState(gemPos)
        val clusterBlock = BLOCK_TO_CLUSTER[BuiltInRegistries.BLOCK.getKey(gemState.block)] ?: return

        val minClusters = maxOf(1, (openDirections.size * 0.75f).toInt())
        val clusterCount = random.nextInt(minClusters, openDirections.size + 1)

        repeat(clusterCount) {
            if (openDirections.isEmpty()) return@repeat

            val index = random.nextInt(openDirections.size)
            val direction = openDirections.removeAt(index)
            val clusterPos = gemPos.relative(direction)
            val stage = random.nextInt(0, 4)

            val clusterState = clusterBlock.defaultBlockState()
                .setValue(FACING, direction)
                .setValue(STAGE, stage)
                .setValue(GROWTH_TYPE, GrowthType.DECORATION)

            level.setBlock(clusterPos, clusterState, UPDATE_ALL)
        }
    }
}
    /*
    fun forceGrow(level: WorldGenLevel, pos: BlockPos, random: RandomSource, percentage: Float) {
        val desiredLength = MAX_CONNECTED_GEMS * percentage

        repeat(MAX_FORCE_GROW_ATTEMPTS) {
            val (grown, clusterSize) = grow(level, pos, random, true)
            if (!grown || clusterSize >= desiredLength) return
        }

        Cobblemon.LOGGER.warn(
            "Forced type gem growth at {} stopped after {} attempts without reaching its target size; keeping the partial formation.",
            pos,
            MAX_FORCE_GROW_ATTEMPTS
        )
        */

    /*
    private fun grow(
        level: WorldGenLevel,
        pos: BlockPos,
        random: RandomSource,
        forced: Boolean = false
    ): Pair<Boolean, Int> {
        val connectedGems = getConnectedGemBlocks(level, pos) // maybe we can store this and access it for better performance? check for changes in list rather than checking each time grow is called? idk
        val gemCount = connectedGems.size
        val overLimit = gemCount >= MAX_CONNECTED_GEMS

        if (overLimit) {
            // if there is no open air pockets to grow any new clusters then exit early
            if (!hasBreathingRoom(level, connectedGems)) {
                return Pair(false, gemCount)
            }
            // max type gem block count met, tell clusters to grow but not turn into blocks
            setStuntState(level, connectedGems, true)
            //return Pair(false, gemCount)
        } else {
            // make sure connected clusters know they can grow and turn into blocks
            setStuntState(level, connectedGems, false)
        }

        val javaRandom = java.util.Random(random.nextLong())

        for ((gemState, gemPos) in connectedGems.shuffled(javaRandom)) {
            val registryKey = BuiltInRegistries.BLOCK.getKey(gemState.block)
            val clusterBlock = BLOCK_TO_CLUSTER[registryKey] ?: continue // If there is no block to grow, skip the block

            for (dir in Direction.entries.shuffled(javaRandom)) {
                val targetPos = gemPos.relative(dir)
                val targetState = level.getBlockState(targetPos)

                if (!targetState.isAir) continue
                if (!isPositionValidForGrowth(level, targetPos)) continue

                // Place gem cluster
                val placeState = clusterBlock.defaultBlockState()
                    .setValue(DirectionalBlock.FACING, dir)
                    .setValue(TypeGemClusterBlock.STAGE, 0)
                    .setValue(SHOULD_GROW, true)

                level.setBlock(targetPos, placeState, UPDATE_ALL)

                // During world gen, the region silently discards writes into neighboring chunks that are already fully generated.
                // Through ImposterProtoChunk with writes set to disabled. Checks if the block is still air after the write and skips if so as it can't be filled.
                // Preventing infinite loops.
                if (!level.getBlockState(targetPos).`is`(clusterBlock)) continue

                if (forced) {
                    forceAdvanceClusterGrowth(level, targetPos, random)
                }

                val updatedGemCount = if (forced) getConnectedGemBlocks(level, pos).size else connectedGems.size
                return Pair(true, updatedGemCount)
            }
        }

        // println("[TypeGemCoreBlock] No valid spot found for gem placement.")
        return Pair(false, connectedGems.size)
    }

    private fun forceAdvanceClusterGrowth(level: WorldGenLevel, clusterPos: BlockPos, random: RandomSource) {
        repeat(5) {
            val clusterState = level.getBlockState(clusterPos)
            val clusterBlock = clusterState.block as? TypeGemClusterBlock ?: return
            clusterBlock.advanceGrowth(clusterState, level, clusterPos, random)
        }
    }

    private fun hasBreathingRoom(level: WorldGenLevel, gems: List<Pair<BlockState, BlockPos>>): Boolean {
        for ((_, gemPos) in gems) {
            for (dir in Direction.entries) {
                if (level.getBlockState(gemPos.relative(dir)).isAir) {
                    return true
                }
            }
        }
        return false
    }

    private fun isPositionValidForGrowth(level: WorldGenLevel, pos: BlockPos): Boolean {
        return Direction.entries.none { dir ->
            val nearby = pos.relative(dir)
            isGem(level.getBlockState(nearby)) &&
                    pos.distManhattan(nearby) < MIN_DISTANCE_BETWEEN_GEMS
        }
    }

    private fun isGem(state: BlockState): Boolean {
        return state.`is`(CobblemonBlockTags.TYPE_GEM_BLOCKS)
    }

    */

