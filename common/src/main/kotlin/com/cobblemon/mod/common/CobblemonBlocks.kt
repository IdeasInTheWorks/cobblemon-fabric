/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.api.apricorn.Apricorn
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.block.*
import com.cobblemon.mod.common.block.LecternBlock
import com.cobblemon.mod.common.block.MintBlock.MintType
import com.cobblemon.mod.common.block.campfirepot.CampfireBlock
import com.cobblemon.mod.common.block.campfirepot.CampfirePotBlock
import com.cobblemon.mod.common.block.chest.GildedChestBlock
import com.cobblemon.mod.common.block.decor.*
import com.cobblemon.mod.common.block.general.BaleBlock
import com.cobblemon.mod.common.block.general.HorizontalRotationCarpetBlock
import com.cobblemon.mod.common.block.general.HorizontalRotationalBlock
import com.cobblemon.mod.common.block.grower.SaccharineTreeGrower
import com.cobblemon.mod.common.block.habitat.HabitatBlock
import com.cobblemon.mod.common.block.sign.CobblemonHangingSignBlock
import com.cobblemon.mod.common.block.sign.CobblemonSignBlock
import com.cobblemon.mod.common.block.sign.CobblemonWallHangingSignBlock
import com.cobblemon.mod.common.block.sign.CobblemonWallSignBlock
import com.cobblemon.mod.common.block.tmmachine.TMMachineBlock
import com.cobblemon.mod.common.block.general.MintLitterBlock
import com.cobblemon.mod.common.mixin.invoker.BlocksInvoker
import com.cobblemon.mod.common.mixin.invoker.DoorBlockInvoker
import com.cobblemon.mod.common.mixin.invoker.FireBlockInvoker
import com.cobblemon.mod.common.mixin.invoker.PressurePlateBlockInvoker
import com.cobblemon.mod.common.mixin.invoker.StairsBlockInvoker
import com.cobblemon.mod.common.mixin.invoker.TrapdoorBlockInvoker
import com.cobblemon.mod.common.platform.PlatformRegistry
import com.cobblemon.mod.common.util.RegistrationProperties
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.Direction
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.Identifier
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.block.state.properties.WoodType
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction

@Suppress("SameParameterValue", "HasPlatformType", "MemberVisibilityCanBePrivate", "unused")
object CobblemonBlocks : PlatformRegistry<Registry<Block>, ResourceKey<Registry<Block>>, Block>() {

    override val registry: Registry<Block> = BuiltInRegistries.BLOCK
    override val resourceKey: ResourceKey<Registry<Block>> = Registries.BLOCK

    val APRICORN_BLOCK_SET_TYPE = BlockSetType("apricorn")
    val APRICORN_WOOD_TYPE = WoodType.register(WoodType("apricorn", APRICORN_BLOCK_SET_TYPE))
    val SACCHARINE_BLOCK_SET_TYPE = BlockSetType("saccharine")
    val SACCHARINE_WOOD_TYPE = WoodType.register(WoodType("saccharine", SACCHARINE_BLOCK_SET_TYPE))

    // Evolution Ores
    @JvmField
    val DAWN_STONE_ORE = evolutionStoneOre("dawn_stone_ore", 7)
    @JvmField
    val DUSK_STONE_ORE = evolutionStoneOre("dusk_stone_ore")
    @JvmField
    val FIRE_STONE_ORE = evolutionStoneOre("fire_stone_ore", 10)
    @JvmField
    val NETHER_FIRE_STONE_ORE = this.create("nether_fire_stone_ore", DropExperienceBlock(UniformInt.of(1, 2), RegistrationProperties.block("nether_fire_stone_ore", BlockBehaviour.Properties.of()).mapColor(MapColor.NETHER).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F).sound(SoundType.NETHER_ORE).lightLevel{10}))
    @JvmField
    val ICE_STONE_ORE = evolutionStoneOre("ice_stone_ore")
    @JvmField
    val LEAF_STONE_ORE = evolutionStoneOre("leaf_stone_ore")
    @JvmField
    val MOON_STONE_ORE = evolutionStoneOre("moon_stone_ore")
    @JvmField
    val DRIPSTONE_MOON_STONE_ORE = this.create("dripstone_moon_stone_ore", DropExperienceBlock(UniformInt.of(1, 2), RegistrationProperties.block("dripstone_moon_stone_ore", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_BROWN).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F).sound(SoundType.DRIPSTONE_BLOCK)))
    @JvmField
    val SHINY_STONE_ORE = evolutionStoneOre("shiny_stone_ore", 12)
    @JvmField
    val SUN_STONE_ORE = evolutionStoneOre("sun_stone_ore", 2)
    @JvmField
    val TERRACOTTA_SUN_STONE_ORE = this.create("terracotta_sun_stone_ore", DropExperienceBlock(UniformInt.of(1, 2), RegistrationProperties.block("terracotta_sun_stone_ore", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F).lightLevel{2}))
    @JvmField
    val THUNDER_STONE_ORE = evolutionStoneOre("thunder_stone_ore", 6)
    @JvmField
    val WATER_STONE_ORE = evolutionStoneOre("water_stone_ore")

    @JvmField
    val DEEPSLATE_DAWN_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_dawn_stone_ore", 7)
    @JvmField
    val DEEPSLATE_DUSK_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_dusk_stone_ore")
    @JvmField
    val DEEPSLATE_FIRE_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_fire_stone_ore", 10)
    @JvmField
    val DEEPSLATE_ICE_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_ice_stone_ore")
    @JvmField
    val DEEPSLATE_LEAF_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_leaf_stone_ore")
    @JvmField
    val DEEPSLATE_MOON_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_moon_stone_ore")
    @JvmField
    val DEEPSLATE_SHINY_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_shiny_stone_ore", 12)
    @JvmField
    val DEEPSLATE_SUN_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_sun_stone_ore", 2)
    @JvmField
    val DEEPSLATE_THUNDER_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_thunder_stone_ore", 6)
    @JvmField
    val DEEPSLATE_WATER_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_water_stone_ore")

    // Apricorns
    @JvmField
    val APRICORN_LOG = log("apricorn_log", arg2 = MapColor.COLOR_BROWN)
    @JvmField
    val STRIPPED_APRICORN_LOG = log("stripped_apricorn_log")
    @JvmField
    val APRICORN_WOOD = log("apricorn_wood")
    @JvmField
    val STRIPPED_APRICORN_WOOD = log("stripped_apricorn_wood")
    @JvmField
    val APRICORN_PLANKS = this.create("apricorn_planks",
        Block(
            RegistrationProperties.block("apricorn_planks", BlockBehaviour.Properties.of()).mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASS)
                .strength(2.0F, 3.0F).sound(SoundType.WOOD)
        )
    )
    @JvmField
    val APRICORN_LEAVES = leaves("apricorn_leaves")
    @JvmField
    val APRICORN_FENCE = this.create("apricorn_fence", FenceBlock(RegistrationProperties.block("apricorn_fence", BlockBehaviour.Properties.of()).mapColor(APRICORN_PLANKS.defaultMapColor()).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD)))
    @JvmField
    val APRICORN_FENCE_GATE = this.create("apricorn_fence_gate", FenceGateBlock(APRICORN_WOOD_TYPE, RegistrationProperties.block("apricorn_fence_gate", BlockBehaviour.Properties.of()).mapColor(APRICORN_PLANKS.defaultMapColor()).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava().forceSolidOn()))
    @JvmField
    val APRICORN_BUTTON = this.create("apricorn_button", BlocksInvoker.createWoodenButtonBlock(APRICORN_BLOCK_SET_TYPE, 30, RegistrationProperties.block("apricorn_button", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON))))
    @JvmField
    val APRICORN_PRESSURE_PLATE = this.create("apricorn_pressure_plate", PressurePlateBlockInvoker.`cobblemon$create`(APRICORN_BLOCK_SET_TYPE, RegistrationProperties.block("apricorn_pressure_plate", BlockBehaviour.Properties.of()).mapColor(APRICORN_PLANKS.defaultMapColor()).noCollision().strength(0.5F).sound(SoundType.WOOD)))
    @JvmField
    val APRICORN_SIGN = this.create("apricorn_sign", CobblemonSignBlock(RegistrationProperties.block("apricorn_sign", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN)), APRICORN_WOOD_TYPE))

    @JvmField //if you wonder why we don't copy of OAK_WALL_SIGN, then I can tell you its because it uses Properties#dropsLike to point to the SIGN loot table, but we cant use that in our setup due to a deeper rooted issue, so falling back to this and writing dedicated (duplicated) loot tables
    val APRICORN_WALL_SIGN = this.create(
        "apricorn_wall_sign",
        CobblemonWallSignBlock(RegistrationProperties.block("apricorn_wall_sign", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN)), APRICORN_WOOD_TYPE)
    )
    @JvmField
    val APRICORN_HANGING_SIGN = this.create(
        "apricorn_hanging_sign",
        CobblemonHangingSignBlock(RegistrationProperties.block("apricorn_hanging_sign", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_HANGING_SIGN)), APRICORN_WOOD_TYPE)
    )
    @JvmField
    val APRICORN_WALL_HANGING_SIGN = this.create("apricorn_wall_hanging_sign", CobblemonWallHangingSignBlock(RegistrationProperties.block("apricorn_wall_hanging_sign", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_HANGING_SIGN)), APRICORN_WOOD_TYPE))
    @JvmField
    val APRICORN_SLAB = this.create("apricorn_slab", SlabBlock(RegistrationProperties.block("apricorn_slab", BlockBehaviour.Properties.of()).mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD)))
    @JvmField
    val APRICORN_STAIRS = this.create("apricorn_stairs", StairsBlockInvoker.`cobblemon$create`(APRICORN_PLANKS.defaultBlockState(), RegistrationProperties.block("apricorn_stairs", BlockBehaviour.Properties.ofFullCopy(APRICORN_PLANKS))))
    @JvmField
    val APRICORN_DOOR = this.create("apricorn_door", DoorBlockInvoker.`cobblemon$create`(APRICORN_BLOCK_SET_TYPE, RegistrationProperties.block("apricorn_door", BlockBehaviour.Properties.of()).mapColor(APRICORN_PLANKS.defaultMapColor()).instrument(NoteBlockInstrument.BASS).strength(3.0F).sound(SoundType.WOOD).noOcclusion()))
    @JvmField
    val APRICORN_TRAPDOOR = this.create("apricorn_trapdoor", TrapdoorBlockInvoker.`cobblemon$create`(APRICORN_BLOCK_SET_TYPE, RegistrationProperties.block("apricorn_trapdoor", BlockBehaviour.Properties.of()).mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0F).sound(SoundType.WOOD).noOcclusion().isValidSpawn { _, _, _, _ -> false }))

    private val PLANT_PROPERTIES get() = BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.POPPED)
    @JvmField
    val BLACK_APRICORN_SAPLING = this.create("black_apricorn_sapling", ApricornSaplingBlock(RegistrationProperties.block("black_apricorn_sapling", PLANT_PROPERTIES), Apricorn.BLACK))
    @JvmField
    val BLUE_APRICORN_SAPLING = this.create("blue_apricorn_sapling", ApricornSaplingBlock(RegistrationProperties.block("blue_apricorn_sapling", PLANT_PROPERTIES), Apricorn.BLUE))
    @JvmField
    val GREEN_APRICORN_SAPLING = this.create("green_apricorn_sapling", ApricornSaplingBlock(RegistrationProperties.block("green_apricorn_sapling", PLANT_PROPERTIES), Apricorn.GREEN))
    @JvmField
    val PINK_APRICORN_SAPLING = this.create("pink_apricorn_sapling", ApricornSaplingBlock(RegistrationProperties.block("pink_apricorn_sapling", PLANT_PROPERTIES), Apricorn.PINK))
    @JvmField
    val RED_APRICORN_SAPLING = this.create("red_apricorn_sapling", ApricornSaplingBlock(RegistrationProperties.block("red_apricorn_sapling", PLANT_PROPERTIES), Apricorn.RED))
    @JvmField
    val WHITE_APRICORN_SAPLING = this.create("white_apricorn_sapling", ApricornSaplingBlock(RegistrationProperties.block("white_apricorn_sapling", PLANT_PROPERTIES), Apricorn.WHITE))
    @JvmField
    val YELLOW_APRICORN_SAPLING = this.create("yellow_apricorn_sapling", ApricornSaplingBlock(RegistrationProperties.block("yellow_apricorn_sapling", PLANT_PROPERTIES), Apricorn.YELLOW))

    // Saccharines
    @JvmField
    val SACCHARINE_LOG = this.create("saccharine_log", SaccharineLogBlock(RegistrationProperties.block("saccharine_log", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_BLUE).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()))
    @JvmField
    val SACCHARINE_LOG_SLATHERED = this.create("saccharine_log_slathered", SaccharineLogSlatheredBlock(RegistrationProperties.block("saccharine_log_slathered", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_BLUE).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()))

    @JvmField
    val STRIPPED_SACCHARINE_LOG = this.create("stripped_saccharine_log", RotatedPillarBlock(RegistrationProperties.block("stripped_saccharine_log", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_LIGHT_BLUE).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava())) //log("stripped_saccharine_log")
    @JvmField
    val SACCHARINE_WOOD = log("saccharine_wood")
    @JvmField
    val STRIPPED_SACCHARINE_WOOD = this.create("stripped_saccharine_wood", RotatedPillarBlock(RegistrationProperties.block("stripped_saccharine_wood", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_LIGHT_BLUE).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava())) //log("stripped_saccharine_log")
    @JvmField
    val SACCHARINE_PLANKS = this.create("saccharine_planks", Block(RegistrationProperties.block("saccharine_planks", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_LIGHT_BLUE).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD)))
    @JvmField
    val SACCHARINE_LEAVES = this.create("saccharine_leaves", SaccharineLeafBlock(
        RegistrationProperties.block("saccharine_leaves", BlockBehaviour.Properties.of()).mapColor(
            MapColor.PLANT
        ).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion()
            .isValidSpawn { _: BlockState?, _: BlockGetter?, _: BlockPos?, entity: EntityType<*>? ->
                entity === net.minecraft.world.entity.EntityTypes.OCELOT || entity === net.minecraft.world.entity.EntityTypes.PARROT
            }.isSuffocating { _: BlockState?, _: BlockGetter?, _: BlockPos? ->
                false
            }.isViewBlocking { _: BlockState?, _: BlockGetter?, _: BlockPos?, _ ->
                false
            }.ignitedByLava().pushReaction(PushReaction.POPPED)
            .isRedstoneConductor { _: BlockState?, _: BlockGetter?, _: BlockPos? ->
                false
            }))
    @JvmField
    val SACCHARINE_FENCE = this.create("saccharine_fence", FenceBlock(RegistrationProperties.block("saccharine_fence", BlockBehaviour.Properties.of()).mapColor(SACCHARINE_PLANKS.defaultMapColor()).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD)))
    @JvmField
    val SACCHARINE_FENCE_GATE = this.create("saccharine_fence_gate", FenceGateBlock(SACCHARINE_WOOD_TYPE, RegistrationProperties.block("saccharine_fence_gate", BlockBehaviour.Properties.of()).mapColor(SACCHARINE_PLANKS.defaultMapColor()).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava().forceSolidOn()))
    @JvmField
    val SACCHARINE_BUTTON = this.create("saccharine_button", BlocksInvoker.createWoodenButtonBlock(BlockSetType.OAK, 30, RegistrationProperties.block("saccharine_button", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON))))
    @JvmField
    val SACCHARINE_PRESSURE_PLATE = this.create("saccharine_pressure_plate", PressurePlateBlockInvoker.`cobblemon$create`(SACCHARINE_BLOCK_SET_TYPE, RegistrationProperties.block("saccharine_pressure_plate", BlockBehaviour.Properties.of()).mapColor(SACCHARINE_PLANKS.defaultMapColor()).instrument(NoteBlockInstrument.BASS).noCollision().strength(0.5F).sound(SoundType.WOOD)))
    @JvmField
    val SACCHARINE_SIGN = this.create("saccharine_sign", CobblemonSignBlock(RegistrationProperties.block("saccharine_sign", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN)), SACCHARINE_WOOD_TYPE))
    @JvmField
    val SACCHARINE_WALL_SIGN = this.create("saccharine_wall_sign", CobblemonWallSignBlock(RegistrationProperties.block("saccharine_wall_sign", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN)), SACCHARINE_WOOD_TYPE))
    @JvmField
    val SACCHARINE_HANGING_SIGN = this.create("saccharine_hanging_sign", CobblemonHangingSignBlock(RegistrationProperties.block("saccharine_hanging_sign", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_HANGING_SIGN)), SACCHARINE_WOOD_TYPE))
    @JvmField
    val SACCHARINE_WALL_HANGING_SIGN = this.create("saccharine_wall_hanging_sign", CobblemonWallHangingSignBlock(RegistrationProperties.block("saccharine_wall_hanging_sign", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_HANGING_SIGN)), SACCHARINE_WOOD_TYPE))
    @JvmField
    val SACCHARINE_SLAB = this.create("saccharine_slab", SlabBlock(RegistrationProperties.block("saccharine_slab", BlockBehaviour.Properties.of()).mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD)))
    @JvmField
    val SACCHARINE_STAIRS = this.create("saccharine_stairs", StairsBlockInvoker.`cobblemon$create`(SACCHARINE_PLANKS.defaultBlockState(), RegistrationProperties.block("saccharine_stairs", BlockBehaviour.Properties.ofFullCopy(SACCHARINE_PLANKS))))
    @JvmField
    val SACCHARINE_DOOR = this.create("saccharine_door", DoorBlockInvoker.`cobblemon$create`(SACCHARINE_BLOCK_SET_TYPE, RegistrationProperties.block("saccharine_door", BlockBehaviour.Properties.of()).mapColor(SACCHARINE_PLANKS.defaultMapColor()).instrument(NoteBlockInstrument.BASS).strength(3.0F).sound(SoundType.WOOD).noOcclusion()))
    @JvmField
    val SACCHARINE_TRAPDOOR = this.create("saccharine_trapdoor", TrapdoorBlockInvoker.`cobblemon$create`(SACCHARINE_BLOCK_SET_TYPE, RegistrationProperties.block("saccharine_trapdoor", BlockBehaviour.Properties.of()).mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0F).sound(SoundType.WOOD).noOcclusion()))
    @JvmField
    val SACCHARINE_SAPLING = this.create("saccharine_sapling", SaplingBlock(SaccharineTreeGrower(), RegistrationProperties.block("saccharine_sapling", PLANT_PROPERTIES)))

    @JvmField
    val MEDICINAL_LEEK = this.create("medicinal_leek", MedicinalLeekBlock(RegistrationProperties.block("medicinal_leek", BlockBehaviour.Properties.of()).pushReaction(PushReaction.POPPED).ignitedByLava().mapColor(MapColor.CRIMSON_NYLIUM).noCollision().randomTicks().instabreak().sound(CobblemonSounds.MEDICINAL_LEEK_SOUNDS)))

    // TODO after 1.7
//    @JvmField
//    val BUGWORT = this.create("bugwort", BugwortBlock(RegistrationProperties.block("bugwort", BlockBehaviour.Properties.of()).pushReaction(PushReaction.POPPED).ignitedByLava().mapColor(MapColor.COLOR_PURPLE).noCollision().randomTicks().instabreak().sound(CobblemonSounds.MEDICINAL_LEEK_SOUNDS)))
    @JvmField
    val ENERGY_ROOT = this.create("energy_root", EnergyRootBlock(RegistrationProperties.block("energy_root", BlockBehaviour.Properties.of()).pushReaction(PushReaction.POPPED).ignitedByLava().mapColor(MapColor.DIRT).noCollision().randomTicks().instabreak().sound(CobblemonSounds.ENERGY_ROOT_SOUNDS)))
    @JvmField
    val BIG_ROOT = this.create("big_root", BigRootBlock(RegistrationProperties.block("big_root", BlockBehaviour.Properties.of()).pushReaction(PushReaction.POPPED).ignitedByLava().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(CobblemonSounds.BIG_ROOT_SOUNDS)))
    @JvmField
    val REVIVAL_HERB = this.create("revival_herb", RevivalHerbBlock(RegistrationProperties.block("revival_herb", BlockBehaviour.Properties.of()).pushReaction(PushReaction.POPPED).mapColor(MapColor.PLANT).ignitedByLava().noCollision().instabreak().sound(CobblemonSounds.REVIVAL_HERB_SOUNDS)))

    @JvmField
    val POKE_CAKE = this.create("poke_cake", PokeSnackBlock(RegistrationProperties.block("poke_cake", BlockBehaviour.Properties.of()).forceSolidOn().strength(0.5F).sound(SoundType.WOOL).pushReaction(PushReaction.POPPED).lightLevel { if (it.getValue(BlockStateProperties.LIT)) 3 else 0 }, false))
    @JvmField
    val POKE_SNACK = this.create("poke_snack", PokeSnackBlock(RegistrationProperties.block("poke_snack", BlockBehaviour.Properties.of()).forceSolidOn().strength(0.5F).sound(SoundType.WOOL).pushReaction(PushReaction.POPPED).lightLevel { if (it.getValue(BlockStateProperties.LIT)) 3 else 0 }, true))

    @JvmField
    val TUMBLESTONE_CLUSTER = tumblestoneBlock("tumblestone_cluster", TumblestoneBlock.STAGE_3, 7, 3, null, MapColor.TERRACOTTA_ORANGE)
    @JvmField
    val LARGE_BUDDING_TUMBLESTONE = tumblestoneBlock("large_budding_tumblestone", TumblestoneBlock.STAGE_2, 5, 3, TUMBLESTONE_CLUSTER, MapColor.TERRACOTTA_ORANGE)
    @JvmField
    val MEDIUM_BUDDING_TUMBLESTONE = tumblestoneBlock("medium_budding_tumblestone", TumblestoneBlock.STAGE_1, 4, 3, LARGE_BUDDING_TUMBLESTONE, MapColor.TERRACOTTA_ORANGE)
    @JvmField
    val SMALL_BUDDING_TUMBLESTONE = tumblestoneBlock("small_budding_tumblestone", TumblestoneBlock.STAGE_0, 3, 4, MEDIUM_BUDDING_TUMBLESTONE, MapColor.TERRACOTTA_ORANGE)

    @JvmField
    val SKY_TUMBLESTONE_CLUSTER = tumblestoneBlock("sky_tumblestone_cluster", TumblestoneBlock.STAGE_3, 7, 3, null, MapColor.COLOR_LIGHT_BLUE)
    @JvmField
    val LARGE_BUDDING_SKY_TUMBLESTONE = tumblestoneBlock("large_budding_sky_tumblestone", TumblestoneBlock.STAGE_2, 5, 3, SKY_TUMBLESTONE_CLUSTER, MapColor.COLOR_LIGHT_BLUE)
    @JvmField
    val MEDIUM_BUDDING_SKY_TUMBLESTONE = tumblestoneBlock("medium_budding_sky_tumblestone", TumblestoneBlock.STAGE_1, 4, 3, LARGE_BUDDING_SKY_TUMBLESTONE, MapColor.COLOR_LIGHT_BLUE)
    @JvmField
    val SMALL_BUDDING_SKY_TUMBLESTONE = tumblestoneBlock("small_budding_sky_tumblestone", TumblestoneBlock.STAGE_0, 3, 4, MEDIUM_BUDDING_SKY_TUMBLESTONE, MapColor.COLOR_LIGHT_BLUE)

    @JvmField
    val BLACK_TUMBLESTONE_CLUSTER = tumblestoneBlock("black_tumblestone_cluster", TumblestoneBlock.STAGE_3, 7, 3, null, MapColor.TERRACOTTA_BLACK)
    @JvmField
    val LARGE_BUDDING_BLACK_TUMBLESTONE = tumblestoneBlock("large_budding_black_tumblestone", TumblestoneBlock.STAGE_2, 5, 3, BLACK_TUMBLESTONE_CLUSTER, MapColor.TERRACOTTA_BLACK)
    @JvmField
    val MEDIUM_BUDDING_BLACK_TUMBLESTONE = tumblestoneBlock("medium_budding_black_tumblestone", TumblestoneBlock.STAGE_1, 4, 3, LARGE_BUDDING_BLACK_TUMBLESTONE, MapColor.TERRACOTTA_BLACK)
    @JvmField
    val SMALL_BUDDING_BLACK_TUMBLESTONE = tumblestoneBlock("small_budding_black_tumblestone", TumblestoneBlock.STAGE_0, 3, 4, MEDIUM_BUDDING_BLACK_TUMBLESTONE, MapColor.TERRACOTTA_BLACK)

    @JvmField
    val TUMBLESTONE_BLOCK = this.create("tumblestone_block", Block(RegistrationProperties.block("tumblestone_block", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_ORANGE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val SKY_TUMBLESTONE_BLOCK = this.create("sky_tumblestone_block", Block(RegistrationProperties.block("sky_tumblestone_block", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_LIGHT_BLUE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val BLACK_TUMBLESTONE_BLOCK = this.create("black_tumblestone_block", Block(RegistrationProperties.block("black_tumblestone_block", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_BLACK).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))

    @JvmField
    val TYPE_GEM_CORE = this.create("deepslate_crystal_core", TypeGemCoreBlock(RegistrationProperties.block("deepslate_crystal_core", BlockBehaviour.Properties.of()).strength(4.0F, 10.0F).requiresCorrectToolForDrops().mapColor(MapColor.DEEPSLATE)))

    private val typeGemBlocks = mutableMapOf<Identifier, Block>()

    @JvmField
    val TYPE_GEM_BLOCK_NORMAL = typeGemBlock("normal_gem_block", MapColor.QUARTZ)
    @JvmField
    val TYPE_GEM_BLOCK_FIRE = typeGemBlock("fire_gem_block", MapColor.FIRE)
    @JvmField
    val TYPE_GEM_BLOCK_WATER = typeGemBlock("water_gem_block", MapColor.WATER)
    @JvmField
    val TYPE_GEM_BLOCK_GRASS = typeGemBlock("grass_gem_block", MapColor.COLOR_LIGHT_GREEN)
    @JvmField
    val TYPE_GEM_BLOCK_ELECTRIC = typeGemBlock("electric_gem_block", MapColor.GOLD)
    @JvmField
    val TYPE_GEM_BLOCK_ICE = typeGemBlock("ice_gem_block", MapColor.ICE)
    @JvmField
    val TYPE_GEM_BLOCK_FIGHTING = typeGemBlock("fighting_gem_block", MapColor.COLOR_RED)
    @JvmField
    val TYPE_GEM_BLOCK_POISON = typeGemBlock("poison_gem_block", MapColor.COLOR_PURPLE)
    @JvmField
    val TYPE_GEM_BLOCK_GROUND = typeGemBlock("ground_gem_block", MapColor.PODZOL)
    @JvmField
    val TYPE_GEM_BLOCK_FLYING = typeGemBlock("flying_gem_block", MapColor.COLOR_LIGHT_BLUE)
    @JvmField
    val TYPE_GEM_BLOCK_PSYCHIC = typeGemBlock("psychic_gem_block", MapColor.COLOR_MAGENTA)
    @JvmField
    val TYPE_GEM_BLOCK_BUG = typeGemBlock("bug_gem_block", MapColor.TERRACOTTA_LIGHT_GREEN)
    @JvmField
    val TYPE_GEM_BLOCK_ROCK = typeGemBlock("rock_gem_block", MapColor.TERRACOTTA_LIGHT_GRAY)
    @JvmField
    val TYPE_GEM_BLOCK_GHOST = typeGemBlock("ghost_gem_block", MapColor.TERRACOTTA_MAGENTA)
    @JvmField
    val TYPE_GEM_BLOCK_DRAGON = typeGemBlock("dragon_gem_block", MapColor.COLOR_BLUE)
    @JvmField
    val TYPE_GEM_BLOCK_DARK = typeGemBlock("dark_gem_block", MapColor.TERRACOTTA_BLACK)
    @JvmField
    val TYPE_GEM_BLOCK_STEEL = typeGemBlock("steel_gem_block", MapColor.METAL)
    @JvmField
    val TYPE_GEM_BLOCK_FAIRY = typeGemBlock("fairy_gem_block", MapColor.COLOR_PINK)

    private val typeGemClusters = mutableMapOf<Identifier, TypeGemClusterBlock>()

    @JvmField
    val TYPE_GEM_CLUSTER_NORMAL = typeGemCluster("normal_gem_cluster", TYPE_GEM_BLOCK_NORMAL, cobblemonResource("normal_gem"), MapColor.QUARTZ)
    @JvmField
    val TYPE_GEM_CLUSTER_FIRE = typeGemCluster("fire_gem_cluster", TYPE_GEM_BLOCK_FIRE, cobblemonResource("fire_gem"), MapColor.FIRE)
    @JvmField
    val TYPE_GEM_CLUSTER_WATER = typeGemCluster("water_gem_cluster", TYPE_GEM_BLOCK_WATER, cobblemonResource("water_gem"), MapColor.WATER)
    @JvmField
    val TYPE_GEM_CLUSTER_GRASS = typeGemCluster("grass_gem_cluster", TYPE_GEM_BLOCK_GRASS, cobblemonResource("grass_gem"), MapColor.COLOR_LIGHT_GREEN)
    @JvmField
    val TYPE_GEM_CLUSTER_ELECTRIC = typeGemCluster("electric_gem_cluster", TYPE_GEM_BLOCK_ELECTRIC, cobblemonResource("electric_gem"), MapColor.GOLD)
    @JvmField
    val TYPE_GEM_CLUSTER_ICE = typeGemCluster("ice_gem_cluster", TYPE_GEM_BLOCK_ICE, cobblemonResource("ice_gem"), MapColor.ICE)
    @JvmField
    val TYPE_GEM_CLUSTER_FIGHTING = typeGemCluster("fighting_gem_cluster", TYPE_GEM_BLOCK_FIGHTING, cobblemonResource("fighting_gem"), MapColor.COLOR_RED)
    @JvmField
    val TYPE_GEM_CLUSTER_POISON = typeGemCluster("poison_gem_cluster", TYPE_GEM_BLOCK_POISON, cobblemonResource("poison_gem"), MapColor.COLOR_PURPLE)
    @JvmField
    val TYPE_GEM_CLUSTER_GROUND = typeGemCluster("ground_gem_cluster", TYPE_GEM_BLOCK_GROUND, cobblemonResource("ground_gem"), MapColor.PODZOL)
    @JvmField
    val TYPE_GEM_CLUSTER_FLYING = typeGemCluster("flying_gem_cluster", TYPE_GEM_BLOCK_FLYING, cobblemonResource("flying_gem"), MapColor.COLOR_LIGHT_BLUE)
    @JvmField
    val TYPE_GEM_CLUSTER_PSYCHIC = typeGemCluster("psychic_gem_cluster", TYPE_GEM_BLOCK_PSYCHIC, cobblemonResource("psychic_gem"), MapColor.COLOR_MAGENTA)
    @JvmField
    val TYPE_GEM_CLUSTER_BUG = typeGemCluster("bug_gem_cluster", TYPE_GEM_BLOCK_BUG, cobblemonResource("bug_gem"), MapColor.TERRACOTTA_LIGHT_GREEN)
    @JvmField
    val TYPE_GEM_CLUSTER_ROCK = typeGemCluster("rock_gem_cluster", TYPE_GEM_BLOCK_ROCK, cobblemonResource("rock_gem"), MapColor.TERRACOTTA_LIGHT_GRAY)
    @JvmField
    val TYPE_GEM_CLUSTER_GHOST = typeGemCluster("ghost_gem_cluster", TYPE_GEM_BLOCK_GHOST, cobblemonResource("ghost_gem"), MapColor.TERRACOTTA_MAGENTA)
    @JvmField
    val TYPE_GEM_CLUSTER_DRAGON = typeGemCluster("dragon_gem_cluster", TYPE_GEM_BLOCK_DRAGON, cobblemonResource("dragon_gem"), MapColor.COLOR_BLUE)
    @JvmField
    val TYPE_GEM_CLUSTER_DARK = typeGemCluster("dark_gem_cluster", TYPE_GEM_BLOCK_DARK, cobblemonResource("dark_gem"), MapColor.TERRACOTTA_BLACK)
    @JvmField
    val TYPE_GEM_CLUSTER_STEEL = typeGemCluster("steel_gem_cluster", TYPE_GEM_BLOCK_STEEL, cobblemonResource("steel_gem"), MapColor.METAL)
    @JvmField
    val TYPE_GEM_CLUSTER_FAIRY = typeGemCluster("fairy_gem_cluster", TYPE_GEM_BLOCK_FAIRY, cobblemonResource("fairy_gem"), MapColor.COLOR_PINK)

    @JvmField
    val POLISHED_TUMBLESTONE = this.create("polished_tumblestone", Block(RegistrationProperties.block("polished_tumblestone", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_ORANGE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val POLISHED_TUMBLESTONE_STAIRS = this.create("polished_tumblestone_stairs", StairsBlockInvoker.`cobblemon$create`(POLISHED_TUMBLESTONE.defaultBlockState(), RegistrationProperties.block("polished_tumblestone_stairs", BlockBehaviour.Properties.ofFullCopy(POLISHED_TUMBLESTONE))))
    @JvmField
    val POLISHED_TUMBLESTONE_SLAB = this.create("polished_tumblestone_slab", SlabBlock(RegistrationProperties.block("polished_tumblestone_slab", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_ORANGE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val POLISHED_TUMBLESTONE_WALL = this.create("polished_tumblestone_wall", WallBlock(RegistrationProperties.block("polished_tumblestone_wall", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_ORANGE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val CHISELED_POLISHED_TUMBLESTONE = this.create("chiseled_polished_tumblestone", Block(RegistrationProperties.block("chiseled_polished_tumblestone", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_ORANGE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val SMOOTH_TUMBLESTONE = this.create("smooth_tumblestone", Block(RegistrationProperties.block("smooth_tumblestone", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_ORANGE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val SMOOTH_TUMBLESTONE_STAIRS = this.create("smooth_tumblestone_stairs", StairsBlockInvoker.`cobblemon$create`(SMOOTH_TUMBLESTONE.defaultBlockState(), RegistrationProperties.block("smooth_tumblestone_stairs", BlockBehaviour.Properties.ofFullCopy(SMOOTH_TUMBLESTONE))))
    @JvmField
    val SMOOTH_TUMBLESTONE_SLAB = this.create("smooth_tumblestone_slab", SlabBlock(RegistrationProperties.block("smooth_tumblestone_slab", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_ORANGE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val TUMBLESTONE_BRICKS = this.create("tumblestone_bricks", Block(RegistrationProperties.block("tumblestone_bricks", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_ORANGE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val TUMBLESTONE_BRICK_STAIRS = this.create("tumblestone_brick_stairs", StairsBlockInvoker.`cobblemon$create`(TUMBLESTONE_BRICKS.defaultBlockState(), RegistrationProperties.block("tumblestone_brick_stairs", BlockBehaviour.Properties.ofFullCopy(TUMBLESTONE_BRICKS))))
    @JvmField
    val TUMBLESTONE_BRICK_SLAB = this.create("tumblestone_brick_slab", SlabBlock(RegistrationProperties.block("tumblestone_brick_slab", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_ORANGE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val TUMBLESTONE_BRICK_WALL = this.create("tumblestone_brick_wall", WallBlock(RegistrationProperties.block("tumblestone_brick_wall", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_ORANGE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val CHISELED_TUMBLESTONE_BRICKS = this.create("chiseled_tumblestone_bricks", Block(RegistrationProperties.block("chiseled_tumblestone_bricks", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_ORANGE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))

    @JvmField
    val POLISHED_SKY_TUMBLESTONE = this.create("polished_sky_tumblestone", Block(RegistrationProperties.block("polished_sky_tumblestone", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_LIGHT_BLUE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val POLISHED_SKY_TUMBLESTONE_STAIRS = this.create("polished_sky_tumblestone_stairs", StairsBlockInvoker.`cobblemon$create`(POLISHED_SKY_TUMBLESTONE.defaultBlockState(), RegistrationProperties.block("polished_sky_tumblestone_stairs", BlockBehaviour.Properties.ofFullCopy(POLISHED_SKY_TUMBLESTONE))))
    @JvmField
    val POLISHED_SKY_TUMBLESTONE_SLAB = this.create("polished_sky_tumblestone_slab", SlabBlock(RegistrationProperties.block("polished_sky_tumblestone_slab", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_LIGHT_BLUE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val POLISHED_SKY_TUMBLESTONE_WALL = this.create("polished_sky_tumblestone_wall", WallBlock(RegistrationProperties.block("polished_sky_tumblestone_wall", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_LIGHT_BLUE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val CHISELED_POLISHED_SKY_TUMBLESTONE = this.create("chiseled_polished_sky_tumblestone", Block(RegistrationProperties.block("chiseled_polished_sky_tumblestone", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_LIGHT_BLUE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val SMOOTH_SKY_TUMBLESTONE = this.create("smooth_sky_tumblestone", Block(RegistrationProperties.block("smooth_sky_tumblestone", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_LIGHT_BLUE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val SMOOTH_SKY_TUMBLESTONE_STAIRS = this.create("smooth_sky_tumblestone_stairs", StairsBlockInvoker.`cobblemon$create`(SMOOTH_SKY_TUMBLESTONE.defaultBlockState(), RegistrationProperties.block("smooth_sky_tumblestone_stairs", BlockBehaviour.Properties.ofFullCopy(SMOOTH_SKY_TUMBLESTONE))))
    @JvmField
    val SMOOTH_SKY_TUMBLESTONE_SLAB = this.create("smooth_sky_tumblestone_slab", SlabBlock(RegistrationProperties.block("smooth_sky_tumblestone_slab", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_LIGHT_BLUE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val SKY_TUMBLESTONE_BRICKS = this.create("sky_tumblestone_bricks", Block(RegistrationProperties.block("sky_tumblestone_bricks", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_LIGHT_BLUE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val SKY_TUMBLESTONE_BRICK_STAIRS = this.create("sky_tumblestone_brick_stairs", StairsBlockInvoker.`cobblemon$create`(SKY_TUMBLESTONE_BRICKS.defaultBlockState(), RegistrationProperties.block("sky_tumblestone_brick_stairs", BlockBehaviour.Properties.ofFullCopy(SKY_TUMBLESTONE_BRICKS))))
    @JvmField
    val SKY_TUMBLESTONE_BRICK_SLAB = this.create("sky_tumblestone_brick_slab", SlabBlock(RegistrationProperties.block("sky_tumblestone_brick_slab", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_LIGHT_BLUE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val SKY_TUMBLESTONE_BRICK_WALL = this.create("sky_tumblestone_brick_wall", WallBlock(RegistrationProperties.block("sky_tumblestone_brick_wall", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_LIGHT_BLUE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val CHISELED_SKY_TUMBLESTONE_BRICKS = this.create("chiseled_sky_tumblestone_bricks", Block(RegistrationProperties.block("chiseled_sky_tumblestone_bricks", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_LIGHT_BLUE).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))

    @JvmField
    val POLISHED_BLACK_TUMBLESTONE = this.create("polished_black_tumblestone", Block(RegistrationProperties.block("polished_black_tumblestone", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_BLACK).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val POLISHED_BLACK_TUMBLESTONE_STAIRS = this.create("polished_black_tumblestone_stairs", StairsBlockInvoker.`cobblemon$create`(POLISHED_BLACK_TUMBLESTONE.defaultBlockState(), RegistrationProperties.block("polished_black_tumblestone_stairs", BlockBehaviour.Properties.ofFullCopy(POLISHED_BLACK_TUMBLESTONE))))
    @JvmField
    val POLISHED_BLACK_TUMBLESTONE_SLAB = this.create("polished_black_tumblestone_slab", SlabBlock(RegistrationProperties.block("polished_black_tumblestone_slab", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_BLACK).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val POLISHED_BLACK_TUMBLESTONE_WALL = this.create("polished_black_tumblestone_wall", WallBlock(RegistrationProperties.block("polished_black_tumblestone_wall", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_BLACK).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val CHISELED_POLISHED_BLACK_TUMBLESTONE = this.create("chiseled_polished_black_tumblestone", Block(RegistrationProperties.block("chiseled_polished_black_tumblestone", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_BLACK).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val SMOOTH_BLACK_TUMBLESTONE = this.create("smooth_black_tumblestone", Block(RegistrationProperties.block("smooth_black_tumblestone", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_BLACK).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val SMOOTH_BLACK_TUMBLESTONE_STAIRS = this.create("smooth_black_tumblestone_stairs", StairsBlockInvoker.`cobblemon$create`(SMOOTH_BLACK_TUMBLESTONE.defaultBlockState(), RegistrationProperties.block("smooth_black_tumblestone_stairs", BlockBehaviour.Properties.ofFullCopy(SMOOTH_BLACK_TUMBLESTONE))))
    @JvmField
    val SMOOTH_BLACK_TUMBLESTONE_SLAB = this.create("smooth_black_tumblestone_slab", SlabBlock(RegistrationProperties.block("smooth_black_tumblestone_slab", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_BLACK).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val BLACK_TUMBLESTONE_BRICKS = this.create("black_tumblestone_bricks", Block(RegistrationProperties.block("black_tumblestone_bricks", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_BLACK).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val BLACK_TUMBLESTONE_BRICK_STAIRS = this.create("black_tumblestone_brick_stairs", StairsBlockInvoker.`cobblemon$create`(BLACK_TUMBLESTONE_BRICKS.defaultBlockState(), RegistrationProperties.block("black_tumblestone_brick_stairs", BlockBehaviour.Properties.ofFullCopy(BLACK_TUMBLESTONE_BRICKS))))
    @JvmField
    val BLACK_TUMBLESTONE_BRICK_SLAB = this.create("black_tumblestone_brick_slab", SlabBlock(RegistrationProperties.block("black_tumblestone_brick_slab", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_BLACK).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val BLACK_TUMBLESTONE_BRICK_WALL = this.create("black_tumblestone_brick_wall", WallBlock(RegistrationProperties.block("black_tumblestone_brick_wall", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_BLACK).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))
    @JvmField
    val CHISELED_BLACK_TUMBLESTONE_BRICKS = this.create("chiseled_black_tumblestone_bricks", Block(RegistrationProperties.block("chiseled_black_tumblestone_bricks", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_BLACK).strength(1.0F).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM)))

    @JvmField
    val FIRE_STONE_BLOCK = this.create("fire_stone_block", Block(RegistrationProperties.block("fire_stone_block", BlockBehaviour.Properties.of()).mapColor(MapColor.FIRE).strength(1.0F).sound(CobblemonSounds.EVOLUTION_STONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BIT).lightLevel{13}))
    @JvmField
    val WATER_STONE_BLOCK = this.create("water_stone_block", Block(RegistrationProperties.block("water_stone_block", BlockBehaviour.Properties.of()).mapColor(MapColor.WATER).strength(1.0F).sound(CobblemonSounds.EVOLUTION_STONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BIT)))
    @JvmField
    val THUNDER_STONE_BLOCK = this.create("thunder_stone_block", Block(RegistrationProperties.block("thunder_stone_block", BlockBehaviour.Properties.of()).mapColor(MapColor.GLOW_LICHEN).strength(1.0F).sound(CobblemonSounds.EVOLUTION_STONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BIT).lightLevel{8}))
    @JvmField
    val LEAF_STONE_BLOCK = this.create("leaf_stone_block", Block(RegistrationProperties.block("leaf_stone_block", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_GREEN).strength(1.0F).sound(CobblemonSounds.EVOLUTION_STONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BIT)))
    @JvmField
    val ICE_STONE_BLOCK = this.create("ice_stone_block", Block(RegistrationProperties.block("ice_stone_block", BlockBehaviour.Properties.of()).mapColor(MapColor.ICE).strength(1.0F).sound(CobblemonSounds.EVOLUTION_STONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BIT)))
    @JvmField
    val SUN_STONE_BLOCK = this.create("sun_stone_block", Block(RegistrationProperties.block("sun_stone_block", BlockBehaviour.Properties.of()).mapColor(MapColor.TERRACOTTA_ORANGE).strength(1.0F).sound(CobblemonSounds.EVOLUTION_STONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BIT).lightLevel{5}))
    @JvmField
    val MOON_STONE_BLOCK = this.create("moon_stone_block", Block(RegistrationProperties.block("moon_stone_block", BlockBehaviour.Properties.of()).mapColor(MapColor.DEEPSLATE).strength(1.0F).sound(CobblemonSounds.EVOLUTION_STONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BIT)))
    @JvmField
    val SHINY_STONE_BLOCK = this.create("shiny_stone_block", Block(RegistrationProperties.block("shiny_stone_block", BlockBehaviour.Properties.of()).mapColor(MapColor.QUARTZ).strength(1.0F).sound(CobblemonSounds.EVOLUTION_STONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BIT).lightLevel{15}))
    @JvmField
    val DAWN_STONE_BLOCK = this.create("dawn_stone_block", Block(RegistrationProperties.block("dawn_stone_block", BlockBehaviour.Properties.of()).mapColor(MapColor.DIAMOND).strength(1.0F).sound(CobblemonSounds.EVOLUTION_STONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BIT).lightLevel{15}))
    @JvmField
    val DUSK_STONE_BLOCK = this.create("dusk_stone_block", Block(RegistrationProperties.block("dusk_stone_block", BlockBehaviour.Properties.of()).mapColor(MapColor.WARPED_HYPHAE).strength(1.0F).sound(CobblemonSounds.EVOLUTION_STONE_BLOCK_SOUNDS).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BIT)))

    @JvmField
    val BLACK_APRICORN = apricornBlock("black_apricorn", Apricorn.BLACK)
    @JvmField
    val BLUE_APRICORN = apricornBlock("blue_apricorn", Apricorn.BLUE)
    @JvmField
    val GREEN_APRICORN = apricornBlock("green_apricorn", Apricorn.GREEN)
    @JvmField
    val PINK_APRICORN = apricornBlock("pink_apricorn", Apricorn.PINK)
    @JvmField
    val RED_APRICORN = apricornBlock("red_apricorn", Apricorn.RED)
    @JvmField
    val WHITE_APRICORN = apricornBlock("white_apricorn", Apricorn.WHITE)
    @JvmField
    val YELLOW_APRICORN = apricornBlock("yellow_apricorn", Apricorn.YELLOW)

    @JvmField
    val BLACK_CAMPFIRE_POT = create("campfire_pot_black",
        CampfirePotBlock(
            RegistrationProperties.block("campfire_pot_black", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_BLACK).sound(CobblemonSounds.CAMPFIRE_POT_SOUNDS)
                .strength(0.5F).pushReaction(PushReaction.IMMOVEABLE).noOcclusion()
        )
    )
    @JvmField
    val BLUE_CAMPFIRE_POT = create("campfire_pot_blue",
        CampfirePotBlock(
            RegistrationProperties.block("campfire_pot_blue", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_BLUE).sound(CobblemonSounds.CAMPFIRE_POT_SOUNDS)
                .strength(0.5F).pushReaction(PushReaction.IMMOVEABLE).noOcclusion()
        )
    )
    @JvmField
    val GREEN_CAMPFIRE_POT = create("campfire_pot_green",
        CampfirePotBlock(
            RegistrationProperties.block("campfire_pot_green", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_GREEN).sound(CobblemonSounds.CAMPFIRE_POT_SOUNDS)
                .strength(0.5F).pushReaction(PushReaction.IMMOVEABLE).noOcclusion()
        )
    )
    @JvmField
    val PINK_CAMPFIRE_POT = create("campfire_pot_pink",
        CampfirePotBlock(
            RegistrationProperties.block("campfire_pot_pink", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_PINK).sound(CobblemonSounds.CAMPFIRE_POT_SOUNDS)
                .strength(0.5F).pushReaction(PushReaction.IMMOVEABLE).noOcclusion()
        )
    )
    @JvmField
    val RED_CAMPFIRE_POT = create("campfire_pot_red",
        CampfirePotBlock(
            RegistrationProperties.block("campfire_pot_red", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_RED).sound(CobblemonSounds.CAMPFIRE_POT_SOUNDS)
                .strength(0.5F).pushReaction(PushReaction.IMMOVEABLE).noOcclusion()
        )
    )
    @JvmField
    val WHITE_CAMPFIRE_POT = create("campfire_pot_white",
        CampfirePotBlock(
            RegistrationProperties.block("campfire_pot_white", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_LIGHT_GRAY)
                .sound(CobblemonSounds.CAMPFIRE_POT_SOUNDS).strength(0.5F).pushReaction(PushReaction.IMMOVEABLE)
                .noOcclusion()
        )
    )
    @JvmField
    val YELLOW_CAMPFIRE_POT = create("campfire_pot_yellow",
        CampfirePotBlock(
            RegistrationProperties.block("campfire_pot_yellow", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_YELLOW).sound(CobblemonSounds.CAMPFIRE_POT_SOUNDS)
                .strength(0.5F).pushReaction(PushReaction.IMMOVEABLE).noOcclusion()
        )
    )

    @JvmField
    val HABITAT_BLOCK = this.create(
        name = "habitat_block",
        entry = HabitatBlock(
            properties = RegistrationProperties.block("habitat_block", BlockBehaviour.Properties.of())
                .mapColor(MapColor.STONE)
                .strength(1.5F, Blocks.OBSIDIAN.explosionResistance)
                .pushReaction(PushReaction.IMMOVEABLE)
        )
    )


    @JvmField
    val CAMPFIRE = create("campfire", CampfireBlock(RegistrationProperties.block("campfire", BlockBehaviour.Properties.of()).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.IMMOVEABLE).mapColor(MapColor.PODZOL).strength(2.0F).lightLevel{14}, false))

    @JvmField
    val SOUL_CAMPFIRE = create("soul_campfire", CampfireBlock(RegistrationProperties.block("soul_campfire", BlockBehaviour.Properties.of()).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.IMMOVEABLE).mapColor(MapColor.PODZOL).strength(2.0F).lightLevel{9}, true))

    @JvmField
    val RELIC_COIN_POUCH = create("relic_coin_pouch", CoinPouchBlock(RegistrationProperties.block("relic_coin_pouch", BlockBehaviour.Properties.of()).sound(CobblemonSounds.RELIC_COIN_POUCH_SOUNDS).pushReaction(PushReaction.POPPED).noOcclusion(), true))
    @JvmField
    val RELIC_COIN_SACK = create("relic_coin_sack", CoinPouchBlock(RegistrationProperties.block("relic_coin_sack", BlockBehaviour.Properties.of()).sound(CobblemonSounds.RELIC_COIN_SACK_SOUNDS).pushReaction(PushReaction.POPPED).strength(0.4F).mapColor(MapColor.GOLD), false))

    @JvmField
    val GILDED_CHEST = create("gilded_chest", GildedChestBlock(RegistrationProperties.block("gilded_chest", BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST)).noOcclusion().sound(CobblemonSounds.GILDED_CHEST_SOUNDS), GildedChestBlock.Type.RED))
    @JvmField
    val BLUE_GILDED_CHEST = create("blue_gilded_chest", GildedChestBlock(RegistrationProperties.block("blue_gilded_chest", BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST)).noOcclusion().sound(CobblemonSounds.GILDED_CHEST_SOUNDS), GildedChestBlock.Type.BLUE))
    @JvmField
    val BLACK_GILDED_CHEST = create("black_gilded_chest", GildedChestBlock(RegistrationProperties.block("black_gilded_chest", BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST)).noOcclusion().sound(CobblemonSounds.GILDED_CHEST_SOUNDS), GildedChestBlock.Type.BLACK))
    @JvmField
    val YELLOW_GILDED_CHEST = create("yellow_gilded_chest", GildedChestBlock(RegistrationProperties.block("yellow_gilded_chest", BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST)).noOcclusion().sound(CobblemonSounds.GILDED_CHEST_SOUNDS), GildedChestBlock.Type.YELLOW))
    @JvmField
    val WHITE_GILDED_CHEST = create("white_gilded_chest", GildedChestBlock(RegistrationProperties.block("white_gilded_chest", BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST)).noOcclusion().sound(CobblemonSounds.GILDED_CHEST_SOUNDS), GildedChestBlock.Type.WHITE))
    @JvmField
    val GREEN_GILDED_CHEST = create("green_gilded_chest", GildedChestBlock(RegistrationProperties.block("green_gilded_chest", BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST)).noOcclusion().sound(CobblemonSounds.GILDED_CHEST_SOUNDS), GildedChestBlock.Type.GREEN))
    @JvmField
    val PINK_GILDED_CHEST = create("pink_gilded_chest", GildedChestBlock(RegistrationProperties.block("pink_gilded_chest", BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST)).noOcclusion().sound(CobblemonSounds.GILDED_CHEST_SOUNDS), GildedChestBlock.Type.PINK))
    @JvmField
    val GIMMIGHOUL_CHEST = create("gimmighoul_chest", GildedChestBlock(RegistrationProperties.block("gimmighoul_chest", BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST)).noOcclusion().sound(CobblemonSounds.GILDED_CHEST_SOUNDS), GildedChestBlock.Type.FAKE))

    @JvmField
    val WHITE_PLAQUE = create("white_plaque", PlaqueBlock(RegistrationProperties.block("white_plaque", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().pushReaction(PushReaction.POPPED).strength(5.0F)))
    @JvmField
    val LIGHT_GRAY_PLAQUE = create("light_gray_plaque", PlaqueBlock(RegistrationProperties.block("light_gray_plaque", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().pushReaction(PushReaction.POPPED).strength(5.0F)))
    @JvmField
    val GRAY_PLAQUE = create("gray_plaque", PlaqueBlock(RegistrationProperties.block("gray_plaque", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().pushReaction(PushReaction.POPPED).strength(5.0F)))
    @JvmField
    val BLACK_PLAQUE = create("black_plaque", PlaqueBlock(RegistrationProperties.block("black_plaque", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().pushReaction(PushReaction.POPPED).strength(5.0F)))
    @JvmField
    val BROWN_PLAQUE = create("brown_plaque", PlaqueBlock(RegistrationProperties.block("brown_plaque", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().pushReaction(PushReaction.POPPED).strength(5.0F)))
    @JvmField
    val RED_PLAQUE = create("red_plaque", PlaqueBlock(RegistrationProperties.block("red_plaque", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().pushReaction(PushReaction.POPPED).strength(5.0F)))
    @JvmField
    val ORANGE_PLAQUE = create("orange_plaque", PlaqueBlock(RegistrationProperties.block("orange_plaque", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().pushReaction(PushReaction.POPPED).strength(5.0F)))
    @JvmField
    val YELLOW_PLAQUE = create("yellow_plaque", PlaqueBlock(RegistrationProperties.block("yellow_plaque", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().pushReaction(PushReaction.POPPED).strength(5.0F)))
    @JvmField
    val LIME_PLAQUE = create("lime_plaque", PlaqueBlock(RegistrationProperties.block("lime_plaque", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().pushReaction(PushReaction.POPPED).strength(5.0F)))
    @JvmField
    val GREEN_PLAQUE = create("green_plaque", PlaqueBlock(RegistrationProperties.block("green_plaque", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().pushReaction(PushReaction.POPPED).strength(5.0F)))
    @JvmField
    val CYAN_PLAQUE = create("cyan_plaque", PlaqueBlock(RegistrationProperties.block("cyan_plaque", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().pushReaction(PushReaction.POPPED).strength(5.0F)))
    @JvmField
    val LIGHT_BLUE_PLAQUE = create("light_blue_plaque", PlaqueBlock(RegistrationProperties.block("light_blue_plaque", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().pushReaction(PushReaction.POPPED).strength(5.0F)))
    @JvmField
    val BLUE_PLAQUE = create("blue_plaque", PlaqueBlock(RegistrationProperties.block("blue_plaque", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().pushReaction(PushReaction.POPPED).strength(5.0F)))
    @JvmField
    val PURPLE_PLAQUE = create("purple_plaque", PlaqueBlock(RegistrationProperties.block("purple_plaque", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().pushReaction(PushReaction.POPPED).strength(5.0F)))
    @JvmField
    val MAGENTA_PLAQUE = create("magenta_plaque", PlaqueBlock(RegistrationProperties.block("magenta_plaque", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().pushReaction(PushReaction.POPPED).strength(5.0F)))
    @JvmField
    val PINK_PLAQUE = create("pink_plaque", PlaqueBlock(RegistrationProperties.block("pink_plaque", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.METAL).noOcclusion().pushReaction(PushReaction.POPPED).strength(5.0F)))

    @JvmField
    val CLEANSE_TAG = create("cleanse_tag", WallAttachedStackableItemBlock(RegistrationProperties.block("cleanse_tag", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_PAPER_SMALL_SOUNDS).mapColor(MapColor.NONE).pushReaction(PushReaction.POPPED).noOcclusion().noCollision(), WallAttachedStackableItemBlock.WallStackableItemBlockType.TAG))
    @JvmField
    val SPELL_TAG = create("spell_tag", WallAttachedStackableItemBlock(RegistrationProperties.block("spell_tag", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_PAPER_SMALL_SOUNDS).mapColor(MapColor.NONE).pushReaction(PushReaction.POPPED).noOcclusion().noCollision(), WallAttachedStackableItemBlock.WallStackableItemBlockType.TAG))

    @JvmField
    val BLUNDER_POLICY = create("blunder_policy", WallAttachedDirectionalShapeBlock(RegistrationProperties.block("blunder_policy", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_PAPER_LARGE_SOUNDS).mapColor(MapColor.NONE).noOcclusion().noCollision(), 14, 12, 1))
    @JvmField
    val WEAKNESS_POLICY = create("weakness_policy", WallAttachedDirectionalShapeBlock(RegistrationProperties.block("weakness_policy", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_PAPER_LARGE_SOUNDS).mapColor(MapColor.NONE).noOcclusion().noCollision(), 14, 12, 1))

    @JvmField
    val RING_TARGET = create("ring_target", RingTargetBlock(RegistrationProperties.block("ring_target", BlockBehaviour.Properties.of()).sound(SoundType.WOOD).mapColor(MapColor.NONE).noOcclusion().strength(0.125f)))
    @JvmField
    val MAGNET = create("magnet", MagnetBlock(RegistrationProperties.block("magnet", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.NONE).noOcclusion().strength(0.075f)))
    @JvmField
    val GALARICA_WREATH = create("galarica_wreath", GalaricaWreathBlock(RegistrationProperties.block("galarica_wreath", BlockBehaviour.Properties.of()).sound(SoundType.AZALEA_LEAVES).mapColor(MapColor.NONE).noOcclusion().noCollision()))

    @JvmField
    val FLAME_ORB = create("flame_orb", OrbBlock(RegistrationProperties.block("flame_orb", BlockBehaviour.Properties.of()).sound(SoundType.GLASS).mapColor(MapColor.NONE).noOcclusion().lightLevel{15}))
    @JvmField
    val TOXIC_ORB = create("toxic_orb", OrbBlock(RegistrationProperties.block("toxic_orb", BlockBehaviour.Properties.of()).sound(SoundType.GLASS).mapColor(MapColor.NONE).noOcclusion()))
    @JvmField
    val LIFE_ORB = create("life_orb", OrbBlock(RegistrationProperties.block("life_orb", BlockBehaviour.Properties.of()).sound(SoundType.GLASS).mapColor(MapColor.NONE).noOcclusion().lightLevel{7}))
    @JvmField
    val LIGHT_BALL = create("light_ball", OrbBlock(RegistrationProperties.block("light_ball", BlockBehaviour.Properties.of()).sound(SoundType.GLASS).mapColor(MapColor.NONE).noOcclusion().lightLevel{13}))
    @JvmField
    val SMOKE_BALL = create("smoke_ball", OrbBlock(RegistrationProperties.block("smoke_ball", BlockBehaviour.Properties.of()).sound(SoundType.GLASS).mapColor(MapColor.NONE).noOcclusion()))

    @JvmField
    val KINGS_ROCK = create("kings_rock", DecorativeItemBlock(RegistrationProperties.block("kings_rock", BlockBehaviour.Properties.of()).sound(CobblemonSounds.TUMBLESTONE_BLOCK_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.CENTERED_6x))

    @JvmField
    val GALARICA_CUFF = create("galarica_cuff", DecorativeItemBlock(RegistrationProperties.block("galarica_cuff", BlockBehaviour.Properties.of()).sound(SoundType.AZALEA_LEAVES).mapColor(MapColor.NONE).noCollision().noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.FLATTENED_10x4))
    @JvmField
    val UNREMARKABLE_TEACUP = create("unremarkable_teacup", DecorativeItemBlock(RegistrationProperties.block("unremarkable_teacup", BlockBehaviour.Properties.of()).sound(SoundType.DECORATED_POT).mapColor(MapColor.NONE).noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.TEACUP))
    @JvmField
    val MASTERPIECE_TEACUP = create("masterpiece_teacup", DecorativeItemBlock(RegistrationProperties.block("masterpiece_teacup", BlockBehaviour.Properties.of()).sound(SoundType.DECORATED_POT).mapColor(MapColor.NONE).noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.TEACUP))
    @JvmField
    val CHIPPED_POT = create("chipped_pot", DecorativeItemBlock(RegistrationProperties.block("chipped_pot", BlockBehaviour.Properties.of()).sound(SoundType.DECORATED_POT).mapColor(MapColor.NONE).noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.TEAPOT))
    @JvmField
    val CRACKED_POT = create("cracked_pot", DecorativeItemBlock(RegistrationProperties.block("cracked_pot", BlockBehaviour.Properties.of()).sound(SoundType.DECORATED_POT).mapColor(MapColor.NONE).noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.TEAPOT))
    @JvmField
    val ELECTIRIZER = create("electirizer", DecorativeItemBlock(RegistrationProperties.block("electirizer", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.NONE).noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.IZER))
    @JvmField
    val MAGMARIZER = create("magmarizer", DecorativeItemBlock(RegistrationProperties.block("magmarizer", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.NONE).noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.IZER))

    @JvmField
    val SACHET = create("sachet", DecorativeItemBlock(RegistrationProperties.block("sachet", BlockBehaviour.Properties.of()).sound(SoundType.WOOL).mapColor(MapColor.NONE).noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.POUCH))
    @JvmField
    val METAL_POWDER = create("metal_powder", DecorativeItemBlock(RegistrationProperties.block("metal_powder", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_POWDER_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.POUCH))
    @JvmField
    val QUICK_POWDER = create("quick_powder", DecorativeItemBlock(RegistrationProperties.block("quick_powder", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_POWDER_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.POUCH))
    @JvmField
    val BRIGHT_POWDER = create("bright_powder", DecorativeItemBlock(RegistrationProperties.block("bright_powder", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_POWDER_SOUNDS).mapColor(MapColor.NONE).noOcclusion().lightLevel{3}, DecorativeItemBlock.DecorativeItemBlockType.POUCH))
    @JvmField
    val SOFT_SAND = create("soft_sand", DecorativeItemBlock(RegistrationProperties.block("soft_sand", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_POWDER_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.POUCH))

    @JvmField
    val NEVER_MELT_ICE = create("never_melt_ice", DecorativeItemBlock(RegistrationProperties.block("never_melt_ice", BlockBehaviour.Properties.of()).sound(SoundType.GLASS).mapColor(MapColor.NONE).noOcclusion().friction(0.989F), DecorativeItemBlock.DecorativeItemBlockType.NEVERMELT))
    @JvmField
    val SCOPE_LENS = create("scope_lens", DecorativeItemBlock(RegistrationProperties.block("scope_lens", BlockBehaviour.Properties.of()).sound(SoundType.COPPER).mapColor(MapColor.NONE).noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.SCOPE))
    @JvmField
    val WIDE_LENS = create("wide_lens", DecorativeItemBlock(RegistrationProperties.block("wide_lens", BlockBehaviour.Properties.of()).sound(SoundType.COPPER).mapColor(MapColor.NONE).noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.WIDE))
    @JvmField
    val ZOOM_LENS = create("zoom_lens", DecorativeItemBlock(RegistrationProperties.block("zoom_lens", BlockBehaviour.Properties.of()).sound(SoundType.COPPER).mapColor(MapColor.NONE).noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.ZOOM))

    @JvmField
    val ICY_ROCK = create("icy_rock", DecorativeItemBlock(RegistrationProperties.block("icy_rock", BlockBehaviour.Properties.of()).sound(SoundType.DEEPSLATE).mapColor(MapColor.NONE).noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.FLATTENED_8x4))
    @JvmField
    val HEAT_ROCK = create("heat_rock", DecorativeItemBlock(RegistrationProperties.block("heat_rock", BlockBehaviour.Properties.of()).sound(SoundType.DEEPSLATE).mapColor(MapColor.NONE).noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.FLATTENED_8x4))
    @JvmField
    val SMOOTH_ROCK = create("smooth_rock", DecorativeItemBlock(RegistrationProperties.block("smooth_rock", BlockBehaviour.Properties.of()).sound(SoundType.DEEPSLATE).mapColor(MapColor.NONE).noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.FLATTENED_8x4))
    @JvmField
    val DAMP_ROCK = create("damp_rock", DecorativeItemBlock(RegistrationProperties.block("damp_rock", BlockBehaviour.Properties.of()).sound(SoundType.DEEPSLATE).mapColor(MapColor.NONE).noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.FLATTENED_8x6))
    @JvmField
    val DESTINY_KNOT = create("destiny_knot", DecorativeItemBlock(RegistrationProperties.block("destiny_knot", BlockBehaviour.Properties.of()).sound(SoundType.WOOL).mapColor(MapColor.NONE).noOcclusion(), DecorativeItemBlock.DecorativeItemBlockType.KNOT))
    @JvmField
    val LUMINOUS_MOSS = create("luminous_moss", DecorativeItemBlock(RegistrationProperties.block("luminous_moss", BlockBehaviour.Properties.of()).sound(SoundType.MOSS).mapColor(MapColor.NONE).noOcclusion().lightLevel{5}, DecorativeItemBlock.DecorativeItemBlockType.CENTERED_8x))

    @JvmField
    val METRONOME = create("metronome", ActivatableDecorationBlock(RegistrationProperties.block("metronome", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.NONE).pushReaction(PushReaction.POPPED).noOcclusion(), ActivatableDecorationBlock.ActivatableDecorationBlockType.METRONOME))
    @JvmField
    val EJECT_BUTTON = create("eject_button", EjectButtonBlock(RegistrationProperties.block("eject_button", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.NONE).pushReaction(PushReaction.POPPED).noOcclusion().strength(0.125f), 40))

    @JvmField
    val LOADED_DICE = create("loaded_dice", StackableItemBlock(RegistrationProperties.block("loaded_dice", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_DICE_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.DICE))
    @JvmField
    val IRON_BALL = create("iron_ball", StackableItemBlock(RegistrationProperties.block("iron_ball", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.IRON_BALL))
    @JvmField
    val METAL_COAT = create("metal_coat", StackableItemBlock(RegistrationProperties.block("metal_coat", BlockBehaviour.Properties.of()).sound(SoundType.METAL).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.METAL_COAT))
    @JvmField
    val UPGRADE = create("upgrade", StackableItemBlock(RegistrationProperties.block("upgrade", BlockBehaviour.Properties.of()).sound(SoundType.CANDLE).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.UPGRADE))
    @JvmField
    val DUBIOUS_DISC = create("dubious_disc", StackableItemBlock(RegistrationProperties.block("dubious_disc", BlockBehaviour.Properties.of()).sound(SoundType.CANDLE).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.UPGRADE))

    @JvmField
    val THROAT_SPRAY = create("throat_spray", StackableItemBlock(RegistrationProperties.block("throat_spray", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_LIQUID_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.THROAT_SPRAY))
    @JvmField
    val TERRAIN_EXTENDER = create("terrain_extender", StackableItemBlock(RegistrationProperties.block("terrain_extender", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_METAL_CAN_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.EXTENDER))
    @JvmField
    val MOOMOO_MILK = create("moomoo_milk", StackableItemBlock(RegistrationProperties.block("moomoo_milk", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_JAR_GLASS_LIQUID_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.GLASS_BOTTLE))
    @JvmField
    val MEDICINAL_BREW = create("medicinal_brew", StackableItemBlock(RegistrationProperties.block("medicinal_brew", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_JAR_GLASS_LIQUID_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.GLASS_BOTTLE))

    @JvmField
    val FULL_HEAL = create("full_heal", StackableItemBlock(RegistrationProperties.block("full_heal", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_LIQUID_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.STATUS_RESTORE))
    @JvmField
    val ANTIDOTE = create("antidote", StackableItemBlock(RegistrationProperties.block("antidote", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_LIQUID_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.STATUS_RESTORE))
    @JvmField
    val AWAKENING = create("awakening", StackableItemBlock(RegistrationProperties.block("awakening", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_LIQUID_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.STATUS_RESTORE))
    @JvmField
    val BURN_HEAL = create("burn_heal", StackableItemBlock(RegistrationProperties.block("burn_heal", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_LIQUID_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.STATUS_RESTORE))
    @JvmField
    val ICE_HEAL = create("ice_heal", StackableItemBlock(RegistrationProperties.block("ice_heal", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_LIQUID_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.STATUS_RESTORE))
    @JvmField
    val PARALYZE_HEAL = create("paralyze_heal", StackableItemBlock(RegistrationProperties.block("paralyze_heal", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_LIQUID_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.STATUS_RESTORE))

    @JvmField
    val ETHER = create("ether", StackableItemBlock(RegistrationProperties.block("ether", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_LIQUID_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.ETHER))
    @JvmField
    val MAX_ETHER = create("max_ether", StackableItemBlock(RegistrationProperties.block("max_ether", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_LIQUID_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.ETHER))
    @JvmField
    val ELIXIR = create("elixir", StackableItemBlock(RegistrationProperties.block("elixir", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_LIQUID_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.ETHER))
    @JvmField
    val MAX_ELIXIR = create("max_elixir", StackableItemBlock(RegistrationProperties.block("max_elixir", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_LIQUID_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.ETHER))

    @JvmField
    val POTION = create("potion", StackableItemBlock(RegistrationProperties.block("potion", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_LIQUID_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.POTION))
    @JvmField
    val SUPER_POTION = create("super_potion", StackableItemBlock(RegistrationProperties.block("super_potion", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_LIQUID_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.POTION))
    @JvmField
    val HYPER_POTION = create("hyper_potion", StackableItemBlock(RegistrationProperties.block("hyper_potion", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_LIQUID_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.POTION))
    @JvmField
    val MAX_POTION = create("max_potion", StackableItemBlock(RegistrationProperties.block("max_potion", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_LIQUID_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.MAX_POTION))
    @JvmField
    val FULL_RESTORE = create("full_restore", StackableItemBlock(RegistrationProperties.block("full_restore", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_LIQUID_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.MAX_POTION))

    @JvmField
    val DIRE_HIT = create("dire_hit", StackableItemBlock(RegistrationProperties.block("dire_hit", BlockBehaviour.Properties.of()).sound(SoundType.CANDLE).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.BATTLE_ITEM))
    @JvmField
    val GUARD_SPEC = create("guard_spec", StackableItemBlock(RegistrationProperties.block("guard_spec", BlockBehaviour.Properties.of()).sound(SoundType.CANDLE).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.BATTLE_ITEM))
    @JvmField
    val X_ACCURACY = create("x_${Stats.ACCURACY.identifier.path}", StackableItemBlock(RegistrationProperties.block("x_${Stats.ACCURACY.identifier.path}", BlockBehaviour.Properties.of()).sound(SoundType.CANDLE).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.BATTLE_ITEM))
    @JvmField
    val X_ATTACK = create("x_${Stats.ATTACK.identifier.path}", StackableItemBlock(RegistrationProperties.block("x_${Stats.ATTACK.identifier.path}", BlockBehaviour.Properties.of()).sound(SoundType.CANDLE).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.BATTLE_ITEM))
    @JvmField
    val X_DEFENSE = create("x_${Stats.DEFENCE.identifier.path}", StackableItemBlock(RegistrationProperties.block("x_${Stats.DEFENCE.identifier.path}", BlockBehaviour.Properties.of()).sound(SoundType.CANDLE).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.BATTLE_ITEM))
    @JvmField
    val X_SP_ATK = create("x_${Stats.SPECIAL_ATTACK.identifier.path}", StackableItemBlock(RegistrationProperties.block("x_${Stats.SPECIAL_ATTACK.identifier.path}", BlockBehaviour.Properties.of()).sound(SoundType.CANDLE).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.BATTLE_ITEM))
    @JvmField
    val X_SP_DEF = create("x_${Stats.SPECIAL_DEFENCE.identifier.path}", StackableItemBlock(RegistrationProperties.block("x_${Stats.SPECIAL_DEFENCE.identifier.path}", BlockBehaviour.Properties.of()).sound(SoundType.CANDLE).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.BATTLE_ITEM))
    @JvmField
    val X_SPEED = create("x_${Stats.SPEED.identifier.path}", StackableItemBlock(RegistrationProperties.block("x_${Stats.SPEED.identifier.path}", BlockBehaviour.Properties.of()).sound(SoundType.CANDLE).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.BATTLE_ITEM))

    @JvmField
    val HP_UP = create("hp_up", StackableItemBlock(RegistrationProperties.block("hp_up", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_PILLS_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.VITAMIN))
    @JvmField
    val PROTEIN = create("protein", StackableItemBlock(RegistrationProperties.block("protein", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_PILLS_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.VITAMIN))
    @JvmField
    val IRON = create("iron", StackableItemBlock(RegistrationProperties.block("iron", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_PILLS_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.VITAMIN))
    @JvmField
    val CARBOS = create("carbos", StackableItemBlock(RegistrationProperties.block("carbos", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_PILLS_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.VITAMIN))
    @JvmField
    val CALCIUM = create("calcium", StackableItemBlock(RegistrationProperties.block("calcium", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_PILLS_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.VITAMIN))
    @JvmField
    val ZINC = create("zinc", StackableItemBlock(RegistrationProperties.block("zinc", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_PILLS_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.VITAMIN))
    @JvmField
    val PP_UP = create("pp_up", StackableItemBlock(RegistrationProperties.block("pp_up", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_PILLS_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.VITAMIN))
    @JvmField
    val PP_MAX = create("pp_max", StackableItemBlock(RegistrationProperties.block("pp_max", BlockBehaviour.Properties.of()).sound(CobblemonSounds.ITEM_BLOCK_BOTTLE_PILLS_SOUNDS).mapColor(MapColor.NONE).noOcclusion(), StackableItemBlock.StackableItemBlockType.VITAMIN))

    @JvmField
    val MONITOR = create("monitor", MonitorBlock(RegistrationProperties.block("monitor", BlockBehaviour.Properties.of()).mapColor(MapColor.METAL).sound(SoundType.METAL).pushReaction(PushReaction.IMMOVEABLE).requiresCorrectToolForDrops().strength(5.0F, 6.0F).lightLevel { if (it.getValue(MonitorBlock.SCREEN) != MonitorBlock.MonitorScreen.OFF) 13 else 0 }))
    @JvmField
    val DAMAGED_MONITOR = create("damaged_monitor", DamagedMonitorBlock(RegistrationProperties.block("damaged_monitor", BlockBehaviour.Properties.of()).mapColor(MapColor.METAL).sound(SoundType.METAL).pushReaction(PushReaction.IMMOVEABLE).requiresCorrectToolForDrops().strength(5.0F, 6.0F).lightLevel { when (it.getValue(DamagedMonitorBlock.SCREEN)) { DamagedMonitorBlock.BrokenMonitorScreen.GLITCHING -> 10; else -> 0 } }))
    @JvmField
    val FOSSIL_ANALYZER = create("fossil_analyzer", FossilAnalyzerBlock(RegistrationProperties.block("fossil_analyzer", BlockBehaviour.Properties.of()).mapColor(MapColor.METAL).sound(SoundType.METAL).pushReaction(PushReaction.IMMOVEABLE).requiresCorrectToolForDrops().strength(5.0F, 6.0F).noOcclusion()))
    @JvmField
    val RESTORATION_TANK = create("restoration_tank", RestorationTankBlock(RegistrationProperties.block("restoration_tank", BlockBehaviour.Properties.of()).mapColor(MapColor.METAL).sound(SoundType.GLASS).pushReaction(PushReaction.IMMOVEABLE).requiresCorrectToolForDrops().strength(5.0F, 6.0F).noOcclusion()))

    @JvmField
    val HEALING_MACHINE = create("healing_machine", HealingMachineBlock(RegistrationProperties.block("healing_machine", BlockBehaviour.Properties.of()).mapColor(MapColor.METAL).sound(SoundType.METAL).pushReaction(PushReaction.IMMOVEABLE).strength(2F).noOcclusion().lightLevel { if (it.getValue(HealingMachineBlock.CHARGE_LEVEL) >= HealingMachineBlock.MAX_CHARGE_LEVEL) 12 else 5 }))

    @JvmField
    val PC = create("pc", PCBlock(RegistrationProperties.block("pc", BlockBehaviour.Properties.of()).mapColor(MapColor.METAL).sound(SoundType.METAL).pushReaction(PushReaction.IMMOVEABLE).strength(2F).noOcclusion().lightLevel { if ((it.getValue(PCBlock.ON) as Boolean) && (it.getValue(PCBlock.PART) == PCBlock.PCPart.TOP)) 13 else 0 }))

    @JvmField
    val TM_MACHINE = create(
            "tm_machine",
        TMMachineBlock(
            RegistrationProperties.block("tm_machine", BlockBehaviour.Properties.of())
                .mapColor(MapColor.COLOR_GREEN)
                .sound(SoundType.METAL)
                .pushReaction(PushReaction.IMMOVEABLE)
                .strength(2F)
                .noOcclusion()
        )
    )


    @JvmField
    val LECTERN = create("lectern", LecternBlock(RegistrationProperties.block("lectern", BlockBehaviour.Properties.of()).mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).sound(SoundType.WOOD).strength(2.5F).ignitedByLava().lightLevel { if (it.getValue(LecternBlock.EMIT_LIGHT) as Boolean) 13 else 0 }))

    @JvmField
    val DISC_SHELF = create("disc_shelf", DiscShelfBlock(RegistrationProperties.block("disc_shelf", BlockBehaviour.Properties.of()).mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).sound(SoundType.METAL).strength(2.5F)))

    @JvmField
    val DISPLAY_CASE = create("display_case", DisplayCaseBlock(RegistrationProperties.block("display_case", BlockBehaviour.Properties.of()).sound(CobblemonSounds.DISPLAY_CASE_SOUNDS).noOcclusion().pushReaction(PushReaction.IMMOVEABLE).mapColor(MapColor.STONE).strength(0.3F).requiresCorrectToolForDrops()))

    // TODO after 1.7
//    @JvmField
//    val INCENSE_SWEET = create("incense_sweet", SweetIncenseBlock(RegistrationProperties.block("incense_sweet", BlockBehaviour.Properties.of()).sound(CobblemonSounds.DISPLAY_CASE_SOUNDS).noOcclusion().pushReaction(PushReaction.IMMOVEABLE).mapColor(MapColor.STONE).strength(0.3F)))

    @JvmField
    val PASTURE = create("pasture", PastureBlock(RegistrationProperties.block("pasture", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_BROWN).sound(SoundType.WOOD).strength(2F).noOcclusion().pushReaction(PushReaction.IMMOVEABLE).lightLevel { if ((it.getValue(PastureBlock.ON) as Boolean) && (it.getValue(PastureBlock.PART) == PastureBlock.PasturePart.TOP)) 13 else 0 }))
    @JvmField
    val RED_MINT = create("red_mint", MintBlock(MintType.RED, RegistrationProperties.block("red_mint", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_RED).noCollision().randomTicks().instabreak().sound(CobblemonSounds.MINT_SOUNDS).pushReaction(PushReaction.POPPED)))
    @JvmField
    val BLUE_MINT = create("blue_mint", MintBlock(MintType.BLUE, RegistrationProperties.block("blue_mint", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_BLUE).noCollision().randomTicks().instabreak().sound(CobblemonSounds.MINT_SOUNDS).pushReaction(PushReaction.POPPED)))
    @JvmField
    val CYAN_MINT = create("cyan_mint", MintBlock(MintType.CYAN, RegistrationProperties.block("cyan_mint", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_CYAN).noCollision().randomTicks().instabreak().sound(CobblemonSounds.MINT_SOUNDS).pushReaction(PushReaction.POPPED)))
    @JvmField
    val PINK_MINT = create("pink_mint", MintBlock(MintType.PINK, RegistrationProperties.block("pink_mint", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_PINK).noCollision().randomTicks().instabreak().sound(CobblemonSounds.MINT_SOUNDS).pushReaction(PushReaction.POPPED)))
    @JvmField
    val GREEN_MINT = create("green_mint", MintBlock(MintType.GREEN, RegistrationProperties.block("green_mint", BlockBehaviour.Properties.of()).mapColor(MapColor.COLOR_GREEN).noCollision().randomTicks().instabreak().sound(CobblemonSounds.MINT_SOUNDS).pushReaction(PushReaction.POPPED)))
    @JvmField
    val WHITE_MINT = create("white_mint", MintBlock(MintType.WHITE, RegistrationProperties.block("white_mint", BlockBehaviour.Properties.of()).mapColor(MapColor.SNOW).noCollision().randomTicks().instabreak().sound(CobblemonSounds.MINT_SOUNDS).pushReaction(PushReaction.POPPED)))

    @JvmField
    val VIVICHOKE_SEEDS = this.create("vivichoke_seeds", VivichokeBlock(RegistrationProperties.block("vivichoke_seeds", BlockBehaviour.Properties.of()).pushReaction(PushReaction.POPPED).ignitedByLava().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(CobblemonSounds.VIVICHOKE_SOUNDS)))
    @JvmField
    val PEP_UP_FLOWER = this.create("pep_up_flower", FlowerBlock(MobEffects.LEVITATION, 10F, RegistrationProperties.block("pep_up_flower", BlockBehaviour.Properties.of()).mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.POPPED)))
    @JvmField
    val POTTED_PEP_UP_FLOWER = this.create("potted_pep_up_flower", FlowerPotBlock(PEP_UP_FLOWER, RegistrationProperties.block("potted_pep_up_flower", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val HEARTY_GRAINS = this.create("hearty_grains", HeartyGrainsBlock(RegistrationProperties.block("hearty_grains", BlockBehaviour.Properties.of()).pushReaction(PushReaction.POPPED).ignitedByLava().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak()))

    @JvmField
    val GALARICA_NUT_BUSH = this.create("galarica_nut_bush", NutBushBlock(RegistrationProperties.block("galarica_nut_bush", BlockBehaviour.Properties.of()).mapColor(MapColor.PLANT).randomTicks().noCollision().sound(SoundType.SWEET_BERRY_BUSH).pushReaction(PushReaction.POPPED)))

    @JvmField
    val POTTED_RED_APRICORN_SAPLING = this.create("potted_red_apricorn_sapling", FlowerPotBlock(RED_APRICORN_SAPLING, RegistrationProperties.block("potted_red_apricorn_sapling", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_YELLOW_APRICORN_SAPLING = this.create("potted_yellow_apricorn_sapling", FlowerPotBlock(YELLOW_APRICORN_SAPLING, RegistrationProperties.block("potted_yellow_apricorn_sapling", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_GREEN_APRICORN_SAPLING = this.create("potted_green_apricorn_sapling", FlowerPotBlock(GREEN_APRICORN_SAPLING, RegistrationProperties.block("potted_green_apricorn_sapling", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_BLUE_APRICORN_SAPLING = this.create("potted_blue_apricorn_sapling", FlowerPotBlock(BLUE_APRICORN_SAPLING, RegistrationProperties.block("potted_blue_apricorn_sapling", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_PINK_APRICORN_SAPLING = this.create("potted_pink_apricorn_sapling", FlowerPotBlock(PINK_APRICORN_SAPLING, RegistrationProperties.block("potted_pink_apricorn_sapling", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_BLACK_APRICORN_SAPLING = this.create("potted_black_apricorn_sapling", FlowerPotBlock(BLACK_APRICORN_SAPLING, RegistrationProperties.block("potted_black_apricorn_sapling", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_WHITE_APRICORN_SAPLING = this.create("potted_white_apricorn_sapling", FlowerPotBlock(WHITE_APRICORN_SAPLING, RegistrationProperties.block("potted_white_apricorn_sapling", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_SACCHARINE_SAPLING = this.create("potted_saccharine_sapling", FlowerPotBlock(SACCHARINE_SAPLING, RegistrationProperties.block("potted_saccharine_sapling", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))

    @JvmField
    val RED_MINT_LEAF_LITTER = this.mintLitter("red_mint_leaf_litter", MapColor.COLOR_RED)
    @JvmField
    val POTTED_RED_MINT = this.pottedPlant("potted_red_mint", RED_MINT_LEAF_LITTER)
    @JvmField
    val BLUE_MINT_LEAF_LITTER = this.mintLitter("blue_mint_leaf_litter", MapColor.COLOR_BLUE)
    @JvmField
    val POTTED_BLUE_MINT = this.pottedPlant("potted_blue_mint", BLUE_MINT_LEAF_LITTER)
    @JvmField
    val CYAN_MINT_LEAF_LITTER = this.mintLitter("cyan_mint_leaf_litter", MapColor.COLOR_CYAN)
    @JvmField
    val POTTED_CYAN_MINT = this.pottedPlant("potted_cyan_mint", CYAN_MINT_LEAF_LITTER)
    @JvmField
    val PINK_MINT_LEAF_LITTER = this.mintLitter("pink_mint_leaf_litter", MapColor.COLOR_PINK)
    @JvmField
    val POTTED_PINK_MINT = this.pottedPlant("potted_pink_mint", PINK_MINT_LEAF_LITTER)
    @JvmField
    val GREEN_MINT_LEAF_LITTER = this.mintLitter("green_mint_leaf_litter", MapColor.COLOR_GREEN)
    @JvmField
    val POTTED_GREEN_MINT = this.pottedPlant("potted_green_mint", GREEN_MINT_LEAF_LITTER)
    @JvmField
    val WHITE_MINT_LEAF_LITTER = this.mintLitter("white_mint_leaf_litter", MapColor.SNOW)
    @JvmField
    val POTTED_WHITE_MINT = this.pottedPlant("potted_white_mint", WHITE_MINT_LEAF_LITTER)

    @JvmField
    val LOBAILA = this.create("lobaila", LobailaFlowerBlock(RegistrationProperties.block("lobaila", BlockBehaviour.Properties.of()).mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).ignitedByLava().pushReaction(PushReaction.POPPED)))
    @JvmField
    val POMOHANA = this.create("pomohana", PomohanaFlowerBlock(RegistrationProperties.block("pomohana", BlockBehaviour.Properties.of()).mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).ignitedByLava().pushReaction(PushReaction.POPPED)))
    @JvmField
    val PAUVILLEA = this.create("pauvillea",PauvilleaFlowerBlock(RegistrationProperties.block("pauvillea", BlockBehaviour.Properties.of()).mapColor(MapColor.PLANT).noCollision().sound(SoundType.GRASS).instabreak().ignitedByLava().pushReaction(PushReaction.POPPED)))
    @JvmField
    val WISTENSU = this.create("wistensu", WistensuFlowerHeadBlock(RegistrationProperties.block("wistensu", BlockBehaviour.Properties.of()).mapColor(MapColor.PLANT).noCollision().sound(SoundType.GRASS).instabreak().ignitedByLava().pushReaction(PushReaction.POPPED).lightLevel{11}))
    @JvmField
    val WISTENSU_VINE = this.create("wistensu_vine", WistensuFlowerVineBlock(RegistrationProperties.block("wistensu_vine", BlockBehaviour.Properties.of()).mapColor(MapColor.PLANT).noCollision().sound(SoundType.GRASS).instabreak().ignitedByLava().pushReaction(PushReaction.POPPED)))

    @JvmField
    val ELECTRIC_SEED = this.terrainSeed("electric_seed", TerrainSeedBlock.TerrainSeedType.ELECTRIC)
    @JvmField
    val PSYCHIC_SEED = this.terrainSeed("psychic_seed", TerrainSeedBlock.TerrainSeedType.PSYCHIC)
    @JvmField
    val MISTY_SEED = this.terrainSeed("misty_seed", TerrainSeedBlock.TerrainSeedType.MISTY)
    @JvmField
    val GRASSY_SEED = this.terrainSeed("grassy_seed", TerrainSeedBlock.TerrainSeedType.GRASSY)
    @JvmField
    val VOLTACONIA = this.terrainPlant("voltaconia") { state -> if (state.getValue(TerrainPlantBlock.GLOWING)) 8 else 0 }
    @JvmField
    val VERDROOT = this.terrainPlant("verdroot") { state -> if (state.getValue(TerrainPlantBlock.GLOWING)) 5 else 0 }
    @JvmField
    val LUCIBELL = this.terrainPlant("lucibell") { state -> if (state.getValue(TerrainPlantBlock.GLOWING)) 8 else 0 }
    @JvmField
    val MISTLEAF = this.create(
        "mistleaf",
        MistleafFlowerBlock(RegistrationProperties.block("mistleaf", BlockBehaviour.Properties.of())) {
            if ((it.getValue(MistleafFlowerBlock.GLOWING) >= 1)) it.getValue(MistleafFlowerBlock.GLOWING) + 2 else 0
        }
    )

    @JvmField
    val HEARTY_GRAIN_BALE = this.create("hearty_grain_bale", BaleBlock(RegistrationProperties.block("hearty_grain_bale", BlockBehaviour.Properties.of()).mapColor(MapColor.PLANT).instrument(NoteBlockInstrument.BANJO).strength(0.5F).sound(CobblemonSounds.HEARTY_GRAIN_BALE_SOUNDS)))
    @JvmField
    val TATAMI_BLOCK = this.create("tatami_block", HorizontalRotationalBlock(RegistrationProperties.block("tatami_block", BlockBehaviour.Properties.of()).ignitedByLava().mapColor(MapColor.PLANT).strength(0.1F).sound(CobblemonSounds.TATAMI_BLOCK_SOUNDS)))
    @JvmField
    val TATAMI_MAT = this.create("tatami_mat", HorizontalRotationCarpetBlock(RegistrationProperties.block("tatami_mat", BlockBehaviour.Properties.of()).ignitedByLava().mapColor(MapColor.PLANT).strength(0.1F).sound(CobblemonSounds.TATAMI_MAT_SOUNDS)))

    /**
     * Returns a map of all the blocks that can be stripped with an axe in the format of input - output.
     *
     * @return A map of all the blocks that can be stripped with an axe in the format of input - output.
     */
    fun strippedBlocks(): Map<Block, Block> = mapOf(
        APRICORN_WOOD to STRIPPED_APRICORN_WOOD,
        APRICORN_LOG to STRIPPED_APRICORN_LOG,
        SACCHARINE_WOOD to STRIPPED_SACCHARINE_WOOD,
        SACCHARINE_LOG to STRIPPED_SACCHARINE_LOG,
        SACCHARINE_LOG_SLATHERED to STRIPPED_SACCHARINE_LOG
    )

    private fun apricornBlock(name: String, apricorn: Apricorn): ApricornBlock = this.create(name, ApricornBlock(RegistrationProperties.block(name, BlockBehaviour.Properties.of()).mapColor(apricorn.mapColor()).randomTicks().strength(Blocks.OAK_LOG.defaultDestroyTime(), Blocks.OAK_LOG.explosionResistance).sound(SoundType.WOOD).noOcclusion(), apricorn))

    private fun tumblestoneBlock(name: String, stage: Int, height: Int, xzOffset: Int, nextStage: Block?, mapColor: MapColor) : Block {
        return this.create(name, TumblestoneBlock(RegistrationProperties.block(name, BlockBehaviour.Properties.of())
            .mapColor(mapColor)
            .pushReaction(PushReaction.POPPED)
            .noOcclusion()
            .strength(1.5F)
            .sound(CobblemonSounds.TUMBLESTONE_SOUNDS),
            stage, height, xzOffset, nextStage))
    }

    private val berries = mutableMapOf<Identifier, BerryBlock>()

    @JvmField
    val AGUAV_BERRY = this.berryBlock("aguav")
    @JvmField
    val APICOT_BERRY = this.berryBlock("apicot")
    @JvmField
    val ASPEAR_BERRY = this.berryBlock("aspear")
    @JvmField
    val BABIRI_BERRY = this.berryBlock("babiri")
    @JvmField
    val BELUE_BERRY = this.berryBlock("belue")
    @JvmField
    val BLUK_BERRY = this.berryBlock("bluk")
    @JvmField
    val CHARTI_BERRY = this.berryBlock("charti")
    @JvmField
    val CHERI_BERRY = this.berryBlock("cheri")
    @JvmField
    val CHESTO_BERRY = this.berryBlock("chesto")
    @JvmField
    val CHILAN_BERRY = this.berryBlock("chilan")
    @JvmField
    val CHOPLE_BERRY = this.berryBlock("chople")
    @JvmField
    val COBA_BERRY = this.berryBlock("coba")
    @JvmField
    val COLBUR_BERRY = this.berryBlock("colbur")
    @JvmField
    val CORNN_BERRY = this.berryBlock("cornn")
    @JvmField
    val CUSTAP_BERRY = this.berryBlock("custap")
    @JvmField
    val DURIN_BERRY = this.berryBlock("durin")
    @JvmField
    val EGGANT_BERRY = this.berryBlock("eggant")
    @JvmField
    val ENIGMA_BERRY = this.berryBlock("enigma")
    @JvmField
    val FIGY_BERRY = this.berryBlock("figy")
    @JvmField
    val GANLON_BERRY = this.berryBlock("ganlon")
    @JvmField
    val GREPA_BERRY = this.berryBlock("grepa")
    @JvmField
    val HABAN_BERRY = this.berryBlock("haban")
    @JvmField
    val HONDEW_BERRY = this.berryBlock("hondew")
    @JvmField
    val HOPO_BERRY = this.berryBlock("hopo")
    @JvmField
    val IAPAPA_BERRY = this.berryBlock("iapapa")
    @JvmField
    val JABOCA_BERRY = this.berryBlock("jaboca")
    @JvmField
    val KASIB_BERRY = this.berryBlock("kasib")
    @JvmField
    val KEBIA_BERRY = this.berryBlock("kebia")
    @JvmField
    val KEE_BERRY = this.berryBlock("kee")
    @JvmField
    val KELPSY_BERRY = this.berryBlock("kelpsy")
    @JvmField
    val LANSAT_BERRY = this.berryBlock("lansat")
    @JvmField
    val LEPPA_BERRY = this.berryBlock("leppa")
    @JvmField
    val LIECHI_BERRY = this.berryBlock("liechi")
    @JvmField
    val LUM_BERRY = this.berryBlock("lum")
    @JvmField
    val MAGO_BERRY = this.berryBlock("mago")
    @JvmField
    val MAGOST_BERRY = this.berryBlock("magost")
    @JvmField
    val MARANGA_BERRY = this.berryBlock("maranga")
    @JvmField
    val MICLE_BERRY = this.berryBlock("micle")
    @JvmField
    val NANAB_BERRY = this.berryBlock("nanab")
    @JvmField
    val NOMEL_BERRY = this.berryBlock("nomel")
    @JvmField
    val OCCA_BERRY = this.berryBlock("occa")
    @JvmField
    val ORAN_BERRY = this.berryBlock("oran")
    @JvmField
    val PAMTRE_BERRY = this.berryBlock("pamtre")
    @JvmField
    val PASSHO_BERRY = this.berryBlock("passho")
    @JvmField
    val PAYAPA_BERRY = this.berryBlock("payapa")
    @JvmField
    val PECHA_BERRY = this.berryBlock("pecha")
    @JvmField
    val PERSIM_BERRY = this.berryBlock("persim")
    @JvmField
    val PETAYA_BERRY = this.berryBlock("petaya")
    @JvmField
    val PINAP_BERRY = this.berryBlock("pinap")
    @JvmField
    val POMEG_BERRY = this.berryBlock("pomeg")
    @JvmField
    val QUALOT_BERRY = this.berryBlock("qualot")
    @JvmField
    val RABUTA_BERRY = this.berryBlock("rabuta")
    @JvmField
    val RAWST_BERRY = this.berryBlock("rawst")
    @JvmField
    val RAZZ_BERRY = this.berryBlock("razz")
    @JvmField
    val RINDO_BERRY = this.berryBlock("rindo")
    @JvmField
    val ROSELI_BERRY = this.berryBlock("roseli")
    @JvmField
    val ROWAP_BERRY = this.berryBlock("rowap")
    @JvmField
    val SALAC_BERRY = this.berryBlock("salac")
    @JvmField
    val SHUCA_BERRY = this.berryBlock("shuca")
    @JvmField
    val SITRUS_BERRY = this.berryBlock("sitrus")
    @JvmField
    val SPELON_BERRY = this.berryBlock("spelon")
    @JvmField
    val STARF_BERRY = this.berryBlock("starf")
    @JvmField
    val TAMATO_BERRY = this.berryBlock("tamato")
    @JvmField
    val TANGA_BERRY = this.berryBlock("tanga")
    @JvmField
    val TOUGA_BERRY = this.berryBlock("touga")
    @JvmField
    val WACAN_BERRY = this.berryBlock("wacan")
    @JvmField
    val WATMEL_BERRY = this.berryBlock("watmel")
    @JvmField
    val WEPEAR_BERRY = this.berryBlock("wepear")
    @JvmField
    val WIKI_BERRY = this.berryBlock("wiki")
    @JvmField
    val YACHE_BERRY = this.berryBlock("yache")
    //val BINDING_SOIL = this.create("binding_soil", BindingSoilBlock(RegistrationProperties.block("binding_soil", BlockBehaviour.Properties.of())))

    @JvmField
    val POTTED_AGUAV_BERRY = this.create("potted_aguav_berry", FlowerPotBlock(AGUAV_BERRY, RegistrationProperties.block("potted_aguav_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_APICOT_BERRY = this.create("potted_apicot_berry", FlowerPotBlock(APICOT_BERRY, RegistrationProperties.block("potted_apicot_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_ASPEAR_BERRY = this.create("potted_aspear_berry", FlowerPotBlock(ASPEAR_BERRY, RegistrationProperties.block("potted_aspear_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_BABIRI_BERRY = this.create("potted_babiri_berry", FlowerPotBlock(BABIRI_BERRY, RegistrationProperties.block("potted_babiri_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_BELUE_BERRY = this.create("potted_belue_berry", FlowerPotBlock(BELUE_BERRY, RegistrationProperties.block("potted_belue_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_BLUK_BERRY = this.create("potted_bluk_berry", FlowerPotBlock(BLUK_BERRY, RegistrationProperties.block("potted_bluk_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_CHARTI_BERRY = this.create("potted_charti_berry", FlowerPotBlock(CHARTI_BERRY, RegistrationProperties.block("potted_charti_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_CHERI_BERRY = this.create("potted_cheri_berry", FlowerPotBlock(CHERI_BERRY, RegistrationProperties.block("potted_cheri_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_CHESTO_BERRY = this.create("potted_chesto_berry", FlowerPotBlock(CHESTO_BERRY, RegistrationProperties.block("potted_chesto_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_CHILAN_BERRY = this.create("potted_chilan_berry", FlowerPotBlock(CHILAN_BERRY, RegistrationProperties.block("potted_chilan_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_CHOPLE_BERRY = this.create("potted_chople_berry", FlowerPotBlock(CHOPLE_BERRY, RegistrationProperties.block("potted_chople_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_COBA_BERRY = this.create("potted_coba_berry", FlowerPotBlock(COBA_BERRY, RegistrationProperties.block("potted_coba_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_COLBUR_BERRY = this.create("potted_colbur_berry", FlowerPotBlock(COLBUR_BERRY, RegistrationProperties.block("potted_colbur_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_CORNN_BERRY = this.create("potted_cornn_berry", FlowerPotBlock(CORNN_BERRY, RegistrationProperties.block("potted_cornn_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_CUSTAP_BERRY = this.create("potted_custap_berry", FlowerPotBlock(CUSTAP_BERRY, RegistrationProperties.block("potted_custap_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_DURIN_BERRY = this.create("potted_durin_berry", FlowerPotBlock(DURIN_BERRY, RegistrationProperties.block("potted_durin_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_EGGANT_BERRY = this.create("potted_eggant_berry", FlowerPotBlock(EGGANT_BERRY, RegistrationProperties.block("potted_eggant_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_ENIGMA_BERRY = this.create("potted_enigma_berry", FlowerPotBlock(ENIGMA_BERRY, RegistrationProperties.block("potted_enigma_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_FIGY_BERRY = this.create("potted_figy_berry", FlowerPotBlock(FIGY_BERRY, RegistrationProperties.block("potted_figy_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_GANLON_BERRY = this.create("potted_ganlon_berry", FlowerPotBlock(GANLON_BERRY, RegistrationProperties.block("potted_ganlon_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_GREPA_BERRY = this.create("potted_grepa_berry", FlowerPotBlock(GREPA_BERRY, RegistrationProperties.block("potted_grepa_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_HABAN_BERRY = this.create("potted_haban_berry", FlowerPotBlock(HABAN_BERRY, RegistrationProperties.block("potted_haban_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_HONDEW_BERRY = this.create("potted_hondew_berry", FlowerPotBlock(HONDEW_BERRY, RegistrationProperties.block("potted_hondew_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_HOPO_BERRY = this.create("potted_hopo_berry", FlowerPotBlock(HOPO_BERRY, RegistrationProperties.block("potted_hopo_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_IAPAPA_BERRY = this.create("potted_iapapa_berry", FlowerPotBlock(IAPAPA_BERRY, RegistrationProperties.block("potted_iapapa_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_JABOCA_BERRY = this.create("potted_jaboca_berry", FlowerPotBlock(JABOCA_BERRY, RegistrationProperties.block("potted_jaboca_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_KASIB_BERRY = this.create("potted_kasib_berry", FlowerPotBlock(KASIB_BERRY, RegistrationProperties.block("potted_kasib_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_KEBIA_BERRY = this.create("potted_kebia_berry", FlowerPotBlock(KEBIA_BERRY, RegistrationProperties.block("potted_kebia_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_KEE_BERRY = this.create("potted_kee_berry", FlowerPotBlock(KEE_BERRY, RegistrationProperties.block("potted_kee_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_KELPSY_BERRY = this.create("potted_kelpsy_berry", FlowerPotBlock(KELPSY_BERRY, RegistrationProperties.block("potted_kelpsy_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_LANSAT_BERRY = this.create("potted_lansat_berry", FlowerPotBlock(LANSAT_BERRY, RegistrationProperties.block("potted_lansat_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_LEPPA_BERRY = this.create("potted_leppa_berry", FlowerPotBlock(LEPPA_BERRY, RegistrationProperties.block("potted_leppa_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_LIECHI_BERRY = this.create("potted_liechi_berry", FlowerPotBlock(LIECHI_BERRY, RegistrationProperties.block("potted_liechi_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_LUM_BERRY = this.create("potted_lum_berry", FlowerPotBlock(LUM_BERRY, RegistrationProperties.block("potted_lum_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_MAGO_BERRY = this.create("potted_mago_berry", FlowerPotBlock(MAGO_BERRY, RegistrationProperties.block("potted_mago_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_MAGOST_BERRY = this.create("potted_magost_berry", FlowerPotBlock(MAGOST_BERRY, RegistrationProperties.block("potted_magost_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_MARANGA_BERRY = this.create("potted_maranga_berry", FlowerPotBlock(MARANGA_BERRY, RegistrationProperties.block("potted_maranga_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_MICLE_BERRY = this.create("potted_micle_berry", FlowerPotBlock(MICLE_BERRY, RegistrationProperties.block("potted_micle_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_NANAB_BERRY = this.create("potted_nanab_berry", FlowerPotBlock(NANAB_BERRY, RegistrationProperties.block("potted_nanab_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_NOMEL_BERRY = this.create("potted_nomel_berry", FlowerPotBlock(NOMEL_BERRY, RegistrationProperties.block("potted_nomel_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_OCCA_BERRY = this.create("potted_occa_berry", FlowerPotBlock(OCCA_BERRY, RegistrationProperties.block("potted_occa_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_ORAN_BERRY = this.create("potted_oran_berry", FlowerPotBlock(ORAN_BERRY, RegistrationProperties.block("potted_oran_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_PAMTRE_BERRY = this.create("potted_pamtre_berry", FlowerPotBlock(PAMTRE_BERRY, RegistrationProperties.block("potted_pamtre_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_PASSHO_BERRY = this.create("potted_passho_berry", FlowerPotBlock(PASSHO_BERRY, RegistrationProperties.block("potted_passho_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_PAYAPA_BERRY = this.create("potted_payapa_berry", FlowerPotBlock(PAYAPA_BERRY, RegistrationProperties.block("potted_payapa_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_PECHA_BERRY = this.create("potted_pecha_berry", FlowerPotBlock(PECHA_BERRY, RegistrationProperties.block("potted_pecha_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_PERSIM_BERRY = this.create("potted_persim_berry", FlowerPotBlock(PERSIM_BERRY, RegistrationProperties.block("potted_persim_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_PETAYA_BERRY = this.create("potted_petaya_berry", FlowerPotBlock(PETAYA_BERRY, RegistrationProperties.block("potted_petaya_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_PINAP_BERRY = this.create("potted_pinap_berry", FlowerPotBlock(PINAP_BERRY, RegistrationProperties.block("potted_pinap_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_POMEG_BERRY = this.create("potted_pomeg_berry", FlowerPotBlock(POMEG_BERRY, RegistrationProperties.block("potted_pomeg_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_QUALOT_BERRY = this.create("potted_qualot_berry", FlowerPotBlock(QUALOT_BERRY, RegistrationProperties.block("potted_qualot_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_RABUTA_BERRY = this.create("potted_rabuta_berry", FlowerPotBlock(RABUTA_BERRY, RegistrationProperties.block("potted_rabuta_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_RAWST_BERRY = this.create("potted_rawst_berry", FlowerPotBlock(RAWST_BERRY, RegistrationProperties.block("potted_rawst_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_RAZZ_BERRY = this.create("potted_razz_berry", FlowerPotBlock(RAZZ_BERRY, RegistrationProperties.block("potted_razz_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_RINDO_BERRY = this.create("potted_rindo_berry", FlowerPotBlock(RINDO_BERRY, RegistrationProperties.block("potted_rindo_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_ROSELI_BERRY = this.create("potted_roseli_berry", FlowerPotBlock(ROSELI_BERRY, RegistrationProperties.block("potted_roseli_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_ROWAP_BERRY = this.create("potted_rowap_berry", FlowerPotBlock(ROWAP_BERRY, RegistrationProperties.block("potted_rowap_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_SALAC_BERRY = this.create("potted_salac_berry", FlowerPotBlock(SALAC_BERRY, RegistrationProperties.block("potted_salac_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_SHUCA_BERRY = this.create("potted_shuca_berry", FlowerPotBlock(SHUCA_BERRY, RegistrationProperties.block("potted_shuca_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_SITRUS_BERRY = this.create("potted_sitrus_berry", FlowerPotBlock(SITRUS_BERRY, RegistrationProperties.block("potted_sitrus_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_SPELON_BERRY = this.create("potted_spelon_berry", FlowerPotBlock(SPELON_BERRY, RegistrationProperties.block("potted_spelon_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_STARF_BERRY = this.create("potted_starf_berry", FlowerPotBlock(STARF_BERRY, RegistrationProperties.block("potted_starf_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_TAMATO_BERRY = this.create("potted_tamato_berry", FlowerPotBlock(TAMATO_BERRY, RegistrationProperties.block("potted_tamato_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_TANGA_BERRY = this.create("potted_tanga_berry", FlowerPotBlock(TANGA_BERRY, RegistrationProperties.block("potted_tanga_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_TOUGA_BERRY = this.create("potted_touga_berry", FlowerPotBlock(TOUGA_BERRY, RegistrationProperties.block("potted_touga_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_WACAN_BERRY = this.create("potted_wacan_berry", FlowerPotBlock(WACAN_BERRY, RegistrationProperties.block("potted_wacan_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_WATMEL_BERRY = this.create("potted_watmel_berry", FlowerPotBlock(WATMEL_BERRY, RegistrationProperties.block("potted_watmel_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_WEPEAR_BERRY = this.create("potted_wepear_berry", FlowerPotBlock(WEPEAR_BERRY, RegistrationProperties.block("potted_wepear_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_WIKI_BERRY = this.create("potted_wiki_berry", FlowerPotBlock(WIKI_BERRY, RegistrationProperties.block("potted_wiki_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))
    @JvmField
    val POTTED_YACHE_BERRY = this.create("potted_yache_berry", FlowerPotBlock(YACHE_BERRY, RegistrationProperties.block("potted_yache_berry", BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))

    init {
        /**
         * Makes all blocks in array flammable by adding them to FireBlock's flammableRegistry.
         * second value is burn chance and third value is spread chance
         */
        arrayOf(
            Triple(APRICORN_LOG, 5, 5),
            Triple(STRIPPED_APRICORN_LOG, 5, 5),
            Triple(APRICORN_WOOD, 5, 5),
            Triple(STRIPPED_APRICORN_WOOD, 5, 5),
            Triple(APRICORN_PLANKS, 5, 20),
            Triple(APRICORN_LEAVES, 30, 60),
            Triple(APRICORN_FENCE, 5, 20),
            Triple(APRICORN_FENCE_GATE, 5, 20),
            Triple(APRICORN_SLAB, 5, 20),
            Triple(APRICORN_STAIRS, 5, 20),
            Triple(SACCHARINE_LOG, 5, 5),
            Triple(SACCHARINE_LOG_SLATHERED, 5, 5),
            Triple(STRIPPED_SACCHARINE_LOG, 5, 5),
            Triple(SACCHARINE_WOOD, 5, 5),
            Triple(STRIPPED_SACCHARINE_WOOD, 5, 5),
            Triple(SACCHARINE_PLANKS, 5, 20),
            Triple(SACCHARINE_LEAVES, 30, 60),
            Triple(SACCHARINE_FENCE, 5, 20),
            Triple(SACCHARINE_FENCE_GATE, 5, 20),
            Triple(SACCHARINE_SLAB, 5, 20),
            Triple(SACCHARINE_STAIRS, 5, 20)

        ).onEach{ data -> setFlammable(data.first, data.second, data.third) }
    }

    fun berries() = this.berries.toMap()

    private fun berryBlock(name: String): BerryBlock {
        val identifier = cobblemonResource("${name}_berry")
        val block = this.create(identifier.path, BerryBlock(identifier, RegistrationProperties.block(identifier.path, BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)).dynamicShape().sound(CobblemonSounds.BERRY_BUSH_SOUNDS).strength(0.2F)))
        this.berries[identifier] = block
        return block
    }

    /**
     * Calls helper method from Vanilla
     */
    private fun log(name: String, arg: MapColor = MapColor.DIRT, arg2: MapColor = MapColor.DIRT): Block {
        val block = RotatedPillarBlock(RegistrationProperties.block(name, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG))
            .mapColor { state -> if (state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y) arg else arg2 })
        return this.create(name, block)
    }

    /**
     * Method uses generic E in order to keep the block as the same return type.
     * If E is not a block then it will not be set as flammable.
     * Calls Vanilla implementation of registering a flammable block.
     * Mixins looks cursed but it is java's fault.
     */
    private fun <E> setFlammable(block: E, burnChance: Int, spreadChance: Int): E {
        if(block !is Block) return block

        var fireBlock: FireBlock =  Blocks.FIRE as FireBlock
        //Cursed Mixin stuff
        (fireBlock as FireBlockInvoker).registerNewFlammableBlock(block as Block, burnChance, spreadChance)
        return block
    }

    private fun evolutionStoneOre(name: String) = this.create(name, DropExperienceBlock(UniformInt.of(1, 2), RegistrationProperties.block(name, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE))))
    private fun evolutionStoneOre(name: String, lightLevel: Int) = this.create(name, DropExperienceBlock(UniformInt.of(1, 2), RegistrationProperties.block(name, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)).lightLevel { lightLevel }))

    private fun deepslateEvolutionStoneOre(name: String) = this.create(name, DropExperienceBlock(UniformInt.of(1, 2), RegistrationProperties.block(name, BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE))))
    private fun deepslateEvolutionStoneOre(name: String, lightLevel: Int) = this.create(name, DropExperienceBlock(UniformInt.of(1, 2), RegistrationProperties.block(name, BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)).lightLevel { lightLevel }))

    /**
     * Helper method for creating leaves
     * copied over from Vanilla
     */
    private fun leaves(name: String): Block {
        val block = TintedParticleLeavesBlock(0.0F, RegistrationProperties.block(name,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)).sound(SoundType.GRASS))
        return this.create(name, block)
    }

    private fun typeGemBlock(name: String, mapColor: MapColor): Block {
        val typeGemBlock = this.create(name, Block(RegistrationProperties.block(name, BlockBehaviour.Properties.of()).mapColor(mapColor).strength(3.0F, 6.0F).sound(CobblemonSounds.TYPE_GEM_BLOCK_SOUNDS)))

        val identifier = cobblemonResource(name)
        typeGemBlocks[identifier] = typeGemBlock

        return typeGemBlock
    }

    fun typeGemBlocks() = typeGemBlocks.toMap()

    private fun typeGemCluster(name: String, gemBlock: Block, dropItemId: Identifier, mapColor: MapColor): Block {
        val gemClusterBlock = this.create(name, TypeGemClusterBlock(RegistrationProperties.block(name, BlockBehaviour.Properties.of()).mapColor(mapColor).strength(2.0F, 3.0F).sound(CobblemonSounds.TYPE_GEM_CLUSTER_SOUNDS).pushReaction(PushReaction.POPPED), gemBlock, dropItemId))

        TypeGemClusterBlock.gemToClusterMap[gemBlock] = gemClusterBlock

        val identifier = cobblemonResource(name)
        typeGemClusters[identifier] = gemClusterBlock

        return gemClusterBlock
    }

    fun typeGemClusters() = typeGemClusters.toMap()

    private fun pottedPlant(name: String, flower: Block) = this.create(name, FlowerPotBlock(flower,
        RegistrationProperties.block(name, BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT))))

    private fun mintLitter(name: String, color: MapColor) = this.create(
        name,
        MintLitterBlock(
            RegistrationProperties.block(name, BlockBehaviour.Properties.of())
                .sound(CobblemonSounds.MINT_SOUNDS)
                .mapColor(color)
                .noOcclusion()
                .noCollision()
                .pushReaction(PushReaction.POPPED)
        )
    )

    private fun terrainSeed(name: String, type: TerrainSeedBlock.TerrainSeedType) = this.create(
        name,
        TerrainSeedBlock(
            type,
            RegistrationProperties.block(name, BlockBehaviour.Properties.of())
                .pushReaction(PushReaction.POPPED)
                .ignitedByLava()
                .mapColor(MapColor.PLANT)
                .offsetType(BlockBehaviour.OffsetType.XZ)
                .noCollision()
                .randomTicks()
                .instabreak()
        )
    )

    private fun terrainPlant(name: String, lightLevel: (BlockState) -> Int) = this.create(
        name,
        TerrainPlantBlock(
            RegistrationProperties.block(name, BlockBehaviour.Properties.of())
                .mapColor(MapColor.PLANT)
                .noCollision()
                .instabreak()
                .sound(SoundType.GRASS)
                .offsetType(BlockBehaviour.OffsetType.XZ)
                .ignitedByLava()
                .pushReaction(PushReaction.POPPED)
                .lightLevel(lightLevel)
        )
    )
}
