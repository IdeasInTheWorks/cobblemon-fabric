/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.porting;

import com.cobblemon.mod.common.util.LegacyNbt;
import com.cobblemon.mod.common.util.RegistrationProperties;
import java.io.*;
import java.util.UUID;
import net.minecraft.nbt.*;

/** Executes production migration helpers with real Minecraft NBT classes. */
final class Minecraft263DataContracts {
    private static int checks;
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }

    @SuppressWarnings("unchecked")
    private static net.minecraft.core.component.DataComponentMap itemComponents(
            net.minecraft.world.item.Item.Properties properties, net.minecraft.core.HolderLookup.Provider lookup)
            throws ReflectiveOperationException {
        var field = net.minecraft.world.item.Item.Properties.class.getDeclaredField("componentInitializer");
        field.setAccessible(true);
        var initializer = (net.minecraft.core.component.DataComponentInitializers.Initializer<net.minecraft.world.item.Item>) field.get(properties);
        var builder = net.minecraft.core.component.DataComponentMap.builder();
        initializer.run(builder, lookup, net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM,
                net.minecraft.resources.Identifier.parse("cobblemon:contract_food")));
        return builder.build();
    }

    public static void main(String[] args) throws Exception {
        checks = 0;
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        var properties = RegistrationProperties.item("contract_item");
        var itemDescription = properties.getClass().getDeclaredMethod("effectiveDescriptionId");
        itemDescription.setAccessible(true);
        check(itemDescription.invoke(properties).equals("item.cobblemon.contract_item"), "item ID before constructor");
        check(properties.effectiveModel().toString().equals("cobblemon:contract_item"), "item model identity");
        var blockProperties = RegistrationProperties.block("contract_block",
                net.minecraft.world.level.block.state.BlockBehaviour.Properties.of());
        var blockDescription = blockProperties.getClass().getDeclaredMethod("effectiveDescriptionId");
        blockDescription.setAccessible(true);
        check(blockDescription.invoke(blockProperties).equals("block.cobblemon.contract_block"), "block ID before constructor");
        check(RegistrationProperties.item("contract_item") != properties, "properties are not shared");
        var blockItemProperties = RegistrationProperties.item("contract_item").useBlockDescriptionPrefix();
        check(itemDescription.invoke(blockItemProperties).equals("block.cobblemon.contract_item"), "block item translation prefix");
        blockItemProperties.useItemDescriptionPrefix();
        check(itemDescription.invoke(blockItemProperties).equals("item.cobblemon.contract_item"), "item-name block translation prefix");
        var copied = RegistrationProperties.block("copied_block",
                net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.STONE));
        check(blockDescription.invoke(copied).equals("block.cobblemon.copied_block"), "copied properties receive custom ID");
        CompoundTag root = new CompoundTag();
        UUID owner = UUID.fromString("89abcdef-0123-4567-89ab-cdef01234567");
        UUID pokemon = UUID.fromString("fedcba98-7654-3210-ffff-000012345678");
        LegacyNbt.putUUID(root, "owner", owner);
        LegacyNbt.putUUID(root, "uuid", pokemon);
        check(LegacyNbt.getUUID(root, "owner").equals(owner), "owner UUID");
        check(LegacyNbt.getUUID(root, "uuid").equals(pokemon), "Pokemon UUID");
        check(root.getIntArray("owner").orElseThrow().length == 4, "legacy UUID int-array shape");
        check(LegacyNbt.hasUUID(root, "owner"), "valid UUID detection");
        root.putIntArray("bad_uuid", new int[] {1, 2, 3});
        check(!LegacyNbt.hasUUID(root, "bad_uuid"), "malformed UUID detection");
        try {
            LegacyNbt.getUUID(root, "bad_uuid");
            throw new AssertionError("Malformed UUID was accepted");
        } catch (IllegalArgumentException expected) { checks++; }
        root.putString("species", "cobblemon:bulbasaur");
        root.putInt("level", 23);
        root.putByte("slot", (byte) 2);
        root.putBoolean("shiny", true);
        check(root.getStringOr("species", "").equals("cobblemon:bulbasaur"), "species identifier");
        check(root.getIntOr("level", 0) == 23, "level");
        check(root.getByteOr("slot", (byte) 0) == 2, "slot");
        check(root.getBooleanOr("shiny", false), "boolean");
        check(root.getIntOr("missing", 0) == 0, "missing numeric default");
        check(root.getStringOr("missing", "").isEmpty(), "missing string default");
        check(root.getIntOr("species", 0) == 0, "wrong primitive type default");
        check(LegacyNbt.contains(root, "level", 99), "numeric wildcard");
        check(!LegacyNbt.contains(root, "species", 99), "numeric wildcard rejects string");
        ListTag party = new ListTag();
        CompoundTag member = new CompoundTag();
        LegacyNbt.putUUID(member, "uuid", pokemon);
        member.putString("move", "tackle");
        party.add(member);
        root.put("party", party);
        check(LegacyNbt.getList(root, "party", Tag.TAG_COMPOUND).size() == 1, "typed party list");
        check(LegacyNbt.getList(root, "party", Tag.TAG_STRING).isEmpty(), "wrong list element type");
        check(LegacyNbt.getList(root, "missing", Tag.TAG_COMPOUND).isEmpty(), "missing list default");
        ListTag mixed = new ListTag();
        mixed.add(new CompoundTag());
        mixed.add(StringTag.valueOf("wrong"));
        root.put("mixed", mixed);
        check(LegacyNbt.getList(root, "mixed", Tag.TAG_COMPOUND).isEmpty(), "mixed list rejected");
        check(root.getCompoundOrEmpty("missing").isEmpty(), "missing compound default");
        check(root.getCompoundOrEmpty("level").isEmpty(), "wrong compound type default");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        NbtIo.write(root, new DataOutputStream(bytes));
        CompoundTag loaded = NbtIo.read(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));
        check(root.equals(loaded), "binary NBT round trip including unknown keys");
        check(LegacyNbt.getUUID(loaded, "owner").equals(owner), "ownership after reload");
        check(LegacyNbt.getList(loaded, "party", Tag.TAG_COMPOUND)
                .getCompoundOrEmpty(0).getStringOr("move", "").equals("tackle"), "move after reload");
        var registryKey = net.minecraft.resources.ResourceKey.<String>createRegistryKey(
                net.minecraft.resources.Identifier.parse("cobblemon:contract_registry"));
        var registry = new net.minecraft.core.MappedRegistry<String>(registryKey, com.mojang.serialization.Lifecycle.stable());
        var entryId = net.minecraft.resources.Identifier.parse("cobblemon:contract_entry");
        net.minecraft.core.Registry.register(registry, entryId, "retained value");
        registry.freeze();
        check(registry.getValue(entryId).equals("retained value"), "registry value lookup");
        check(registry.get(entryId).orElseThrow().value().equals("retained value"), "registry holder lookup");
        check(registry.getOptional(net.minecraft.resources.Identifier.parse("cobblemon:unknown")).isEmpty(), "missing registry entry");
        var encodedId = registry.byNameCodec().encodeStart(NbtOps.INSTANCE, "retained value").getOrThrow();
        check(encodedId.asString().orElseThrow().equals(entryId.toString()), "codec retains namespaced ID");
        check(registry.byNameCodec().parse(NbtOps.INSTANCE, encodedId).getOrThrow().equals("retained value"), "registry codec round trip");
        var provider = net.minecraft.core.HolderLookup.Provider.create(java.util.stream.Stream.of(registry));
        var input = net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, provider, root);
        check(LegacyNbt.read(input).equals(root), "ValueInput reads legacy root fields");
        var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, provider);
        output.putInt("vanilla_sentinel", 7);
        LegacyNbt.write(output, loaded);
        CompoundTag merged = output.buildResult();
        check(merged.getIntOr("vanilla_sentinel", 0) == 7, "ValueOutput preserves existing superclass fields");
        check(!merged.contains("legacy") && !merged.contains("data"), "no wrapper key or schema relocation");
        merged.remove("vanilla_sentinel");
        check(merged.equals(root), "ValueOutput preserves all legacy/unknown fields");
        check(LegacyNbt.getUUID(merged, "owner").equals(owner), "ValueOutput ownership retained");
        var vanilla = net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
        // A standalone bootstrap does not load datapack item components. Supply minimal
        // unit-fixture components only when unbound; this does not test vanilla gameplay defaults.
        for (var fixtureItem : new net.minecraft.world.item.Item[] {
                net.minecraft.world.item.Items.STONE, net.minecraft.world.item.Items.DIAMOND}) {
            var holder = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOrThrow(
                    net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM,
                            net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(fixtureItem)));
            if (!holder.areComponentsBound()) {
                holder.bindComponents(net.minecraft.core.component.DataComponentMap.builder()
                        .set(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE, 64).build());
            }
        }
        var inventory = net.minecraft.core.NonNullList.withSize(3, net.minecraft.world.item.ItemStack.EMPTY);
        inventory.set(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE, 12));
        inventory.set(2, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND, 1));
        var inventoryOutput = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, vanilla);
        net.minecraft.world.ContainerHelper.saveAllItems(inventoryOutput, inventory, true);
        LegacyNbt.write(inventoryOutput, root);
        CompoundTag savedInventory = inventoryOutput.buildResult();
        check(LegacyNbt.getUUID(savedInventory, "owner").equals(owner), "inventory writer retains custom ownership fields");
        var loadedInventory = net.minecraft.core.NonNullList.withSize(3, net.minecraft.world.item.ItemStack.EMPTY);
        net.minecraft.world.ContainerHelper.loadAllItems(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, vanilla, savedInventory), loadedInventory);
        check(loadedInventory.get(0).getItem() == net.minecraft.world.item.Items.STONE, "inventory item identity");
        check(loadedInventory.get(0).getCount() == 12, "inventory stack size");
        check(loadedInventory.get(1).isEmpty(), "empty inventory slot");
        check(loadedInventory.get(2).getItem() == net.minecraft.world.item.Items.DIAMOND, "non-contiguous inventory slot identity");
        check(loadedInventory.get(2).getCount() == 1, "non-contiguous inventory stack size");
        check(LegacyNbt.read(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, vanilla, savedInventory)).equals(savedInventory), "inventory and custom NBT merge round trip");
        var position = new net.minecraft.core.BlockPos(-30000001, -64, 29999999);
        root.put("pasture", LegacyNbt.writeBlockPos(position));
        check(java.util.Arrays.equals(root.getIntArray("pasture").orElseThrow(),
                new int[] {-30000001, -64, 29999999}), "legacy position is three untruncated ints");
        check(LegacyNbt.readBlockPos(root, "pasture").orElseThrow().equals(position), "legacy position round trip");
        root.putIntArray("bad_position", new int[] {1, 2});
        check(LegacyNbt.readBlockPos(root, "bad_position").isEmpty(), "malformed position rejected");
        check(LegacyNbt.readBlockPos(root, "missing").isEmpty(), "missing position rejected");
        check(LegacyNbt.readBlockPos(root, "species").isEmpty(), "wrong position type rejected");
        var metadata = new CompoundTag();
        metadata.putString("addon_origin", "contract:fossil");
        metadata.putInt("scan_progress", 73);
        inventory.get(0).set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(metadata));
        var fossilTag = LegacyNbt.toTag(vanilla, fossilOutput -> {
            fossilOutput.putString("superclass_sentinel", "retained");
            var structure = fossilOutput.child("MultiblockStore");
            var scalars = new CompoundTag();
            LegacyNbt.putUUID(scalars, "FossilOwner", owner);
            scalars.put("MonitorPos", LegacyNbt.writeBlockPos(position));
            scalars.putInt("TimeLeft", 14400);
            scalars.putInt("ProtectedTimeLeft", -1);
            scalars.putBoolean("HasCreatedPokemon", true);
            LegacyNbt.write(structure, scalars);
            var stacks = structure.list("InsertedFossilStacks", net.minecraft.world.item.ItemStack.CODEC);
            stacks.add(inventory.get(0));
            stacks.add(inventory.get(2));
            fossilOutput.store("MonitorDisk", net.minecraft.world.item.ItemStack.CODEC, inventory.get(2));
        });
        var fossilInput = net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, vanilla, fossilTag);
        var fossilState = fossilInput.childOrEmpty("MultiblockStore");
        var fossilRoot = LegacyNbt.read(fossilState);
        check(fossilTag.getStringOr("superclass_sentinel", "").equals("retained"), "nested serialization keeps superclass fields");
        check(LegacyNbt.getUUID(fossilRoot, "FossilOwner").equals(owner), "nested ownership retained");
        check(fossilState.read("MonitorPos", net.minecraft.core.BlockPos.CODEC).orElseThrow().equals(position), "target codec reads legacy position shape");
        check(fossilRoot.getIntOr("TimeLeft", 0) == 14400, "fossil progress retained");
        check(fossilRoot.getIntOr("ProtectedTimeLeft", 0) == -1, "unprotected sentinel retained");
        check(fossilRoot.getBooleanOr("HasCreatedPokemon", false), "created-Pokemon marker retained");
        var fossilStacks = fossilState.listOrEmpty("InsertedFossilStacks", net.minecraft.world.item.ItemStack.CODEC).stream().toList();
        check(fossilStacks.size() == 2, "context-aware nested list length");
        check(fossilStacks.get(0).getItem() == net.minecraft.world.item.Items.STONE && fossilStacks.get(0).getCount() == 12, "nested first item identity and count");
        check(fossilStacks.get(1).getItem() == net.minecraft.world.item.Items.DIAMOND, "nested item order");
        check(fossilStacks.get(0).get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag().equals(metadata), "item component payload survives nested save");
        check(fossilInput.read("MonitorDisk", net.minecraft.world.item.ItemStack.CODEC).orElseThrow().getItem()
                == net.minecraft.world.item.Items.DIAMOND, "monitor item uses parent registry context");
        check(fossilInput.read("MissingDisk", net.minecraft.world.item.ItemStack.CODEC)
                .orElse(net.minecraft.world.item.ItemStack.EMPTY).isEmpty(), "missing monitor disk defaults empty");
        var emptyListTag = LegacyNbt.toTag(vanilla, emptyOutput -> emptyOutput.list("InsertedFossilStacks", net.minecraft.world.item.ItemStack.CODEC));
        check(emptyListTag.getListOrEmpty("InsertedFossilStacks").isEmpty(), "empty fossil inventory round trip");
        try {
            LegacyNbt.toTag(vanilla, invalidOutput -> invalidOutput.store("invalid_stack", net.minecraft.world.item.ItemStack.CODEC,
                    net.minecraft.world.item.ItemStack.EMPTY));
            throw new AssertionError("Failed legacy save silently returned partial data");
        } catch (IllegalStateException expected) { checks++; }
        var optionalOps = vanilla.createSerializationContext(NbtOps.INSTANCE);
        var emptyStackTag = net.minecraft.world.item.ItemStack.OPTIONAL_CODEC.encodeStart(optionalOps,
                net.minecraft.world.item.ItemStack.EMPTY).getOrThrow();
        check(emptyStackTag.equals(new CompoundTag()), "empty shoulder item keeps legacy empty compound");
        check(net.minecraft.world.item.ItemStack.OPTIONAL_CODEC.parse(optionalOps, emptyStackTag)
                .getOrThrow().isEmpty(), "empty shoulder item decodes");
        var legacyBrain = new CompoundTag();
        var legacyMemories = new CompoundTag();
        var legacyAngryAt = new CompoundTag();
        LegacyNbt.putUUID(legacyAngryAt, "value", owner);
        legacyAngryAt.putLong("ttl", 123L);
        legacyMemories.put("minecraft:angry_at", legacyAngryAt);
        legacyBrain.put("memories", legacyMemories);
        var packedBrain = net.minecraft.world.entity.ai.Brain.Packed.CODEC.parse(optionalOps, legacyBrain).getOrThrow();
        var memory = packedBrain.memories().get(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT);
        check(memory.value().equals(owner), "packed brain reads legacy UUID memory");
        check(memory.timeToLive().orElseThrow() == 123L, "packed brain reads legacy memory expiry");
        var savedBrain = net.minecraft.world.entity.ai.Brain.Packed.CODEC.encodeStart(optionalOps, packedBrain).getOrThrow();
        check(savedBrain.equals(legacyBrain), "packed brain preserves legacy memory NBT shape");
        check(net.minecraft.world.entity.ai.Brain.Packed.CODEC.parse(optionalOps, savedBrain).getOrThrow()
                .memories().equals(packedBrain.memories()), "packed brain codec round trip");
        check(net.minecraft.world.level.block.FlowerBedBlock.AMOUNT.getName().equals("flower_amount"), "flower amount property name retained");
        check(new java.util.HashSet<>(net.minecraft.world.level.block.FlowerBedBlock.AMOUNT.getPossibleValues())
                .equals(java.util.Set.of(1, 2, 3, 4)), "flower amount range retained");
        check(net.minecraft.world.level.block.FlowerBedBlock.FACING.getName().equals("facing"), "flower facing property name retained");
        var scripted = new com.cobblemon.mod.common.util.ScriptedBrain<net.minecraft.world.entity.LivingEntity>(
                java.util.List.of(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT,
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.LOOK_TARGET),
                java.util.List.of(net.minecraft.world.entity.ai.sensing.SensorType.HURT_BY), packedBrain,
                net.minecraft.util.RandomSource.create(123));
        check(scripted.getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT).orElseThrow().equals(owner), "constructed brain restores UUID memory");
        check(scripted.getTimeUntilExpiry(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT) == 123L, "constructed brain restores expiry");
        check(scripted.checkMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.LOOK_TARGET,
                net.minecraft.world.entity.ai.memory.MemoryStatus.REGISTERED), "configured memory registered");
        check(scripted.checkMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.HURT_BY,
                net.minecraft.world.entity.ai.memory.MemoryStatus.REGISTERED), "sensor-required memory registered");
        scripted.setMemoryWithExpiry(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT, pokemon, 50L);
        var currentDynamic = com.cobblemon.mod.common.util.ScriptedBrain.dynamic(scripted.pack(), vanilla);
        var rebuilt = new com.cobblemon.mod.common.util.ScriptedBrain<net.minecraft.world.entity.LivingEntity>(
                java.util.List.of(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT), java.util.List.of(),
                com.cobblemon.mod.common.util.ScriptedBrain.read(currentDynamic, vanilla), net.minecraft.util.RandomSource.create(456));
        check(rebuilt.getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT).orElseThrow().equals(pokemon), "brain rebuild preserves current memory rather than initial snapshot");
        check(rebuilt.getTimeUntilExpiry(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT) == 50L, "brain rebuild preserves current expiry");
        var runtimeLookTarget = new net.minecraft.world.entity.ai.behavior.BlockPosTracker(new net.minecraft.core.BlockPos(1, 2, 3));
        scripted.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.LOOK_TARGET, runtimeLookTarget);
        check(scripted.pack().memories().get(net.minecraft.world.entity.ai.memory.MemoryModuleType.LOOK_TARGET) == null,
                "disk-oriented pack omits runtime-only memories");
        var liveRebuild = new com.cobblemon.mod.common.util.ScriptedBrain<net.minecraft.world.entity.LivingEntity>(
                java.util.List.of(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT, net.minecraft.world.entity.ai.memory.MemoryModuleType.LOOK_TARGET),
                java.util.List.of(), com.cobblemon.mod.common.util.ScriptedBrain.snapshot(scripted), net.minecraft.util.RandomSource.create(789));
        check(liveRebuild.getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.LOOK_TARGET).orElseThrow() == runtimeLookTarget,
                "live rebuild retains runtime-only memory identity");
        check(liveRebuild.getTimeUntilExpiry(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT) == 50L,
                "live rebuild retains persisted memory expiry too");
        scripted.eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.LOOK_TARGET);
        scripted.addActivity(net.minecraft.world.entity.schedule.Activity.CORE, com.google.common.collect.ImmutableList.of(), java.util.Set.of(), java.util.Set.of());
        scripted.addActivity(net.minecraft.world.entity.schedule.Activity.IDLE, com.google.common.collect.ImmutableList.of(),
                java.util.Set.of(com.mojang.datafixers.util.Pair.of(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT,
                        net.minecraft.world.entity.ai.memory.MemoryStatus.VALUE_PRESENT)), java.util.Set.of());
        scripted.addActivity(net.minecraft.world.entity.schedule.Activity.FIGHT, com.google.common.collect.ImmutableList.of(),
                java.util.Set.of(com.mojang.datafixers.util.Pair.of(net.minecraft.world.entity.ai.memory.MemoryModuleType.LOOK_TARGET,
                        net.minecraft.world.entity.ai.memory.MemoryStatus.VALUE_PRESENT)), java.util.Set.of());
        scripted.setCoreActivities(java.util.Set.of(net.minecraft.world.entity.schedule.Activity.CORE));
        scripted.setDefaultActivity(net.minecraft.world.entity.schedule.Activity.IDLE);
        scripted.setActiveActivityIfPossible(net.minecraft.world.entity.schedule.Activity.IDLE);
        check(scripted.isActive(net.minecraft.world.entity.schedule.Activity.CORE), "core activity retained");
        check(scripted.isActive(net.minecraft.world.entity.schedule.Activity.IDLE), "satisfied activity condition permits activation");
        scripted.setActiveActivityIfPossible(net.minecraft.world.entity.schedule.Activity.FIGHT);
        check(!scripted.isActive(net.minecraft.world.entity.schedule.Activity.FIGHT), "unsatisfied activity condition blocks activation");
        scripted.addActivity(net.minecraft.world.entity.schedule.Activity.REST, com.google.common.collect.ImmutableList.of(), java.util.Set.of(),
                java.util.Set.of(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT));
        scripted.setActiveActivityIfPossible(net.minecraft.world.entity.schedule.Activity.REST);
        check(scripted.isActive(net.minecraft.world.entity.schedule.Activity.REST), "rest activity activated before stop transition");
        scripted.addActivity(net.minecraft.world.entity.schedule.Activity.WORK, com.google.common.collect.ImmutableList.of(), java.util.Set.of(), java.util.Set.of());
        scripted.setActiveActivityIfPossible(net.minecraft.world.entity.schedule.Activity.WORK);
        check(!scripted.hasMemoryValue(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT), "activity stop erases only configured memory");
        try {
            com.cobblemon.mod.common.util.ScriptedBrain.read(new com.mojang.serialization.Dynamic<>(NbtOps.INSTANCE, StringTag.valueOf("invalid")), vanilla);
            throw new AssertionError("Malformed brain data silently discarded");
        } catch (IllegalStateException expected) { checks++; }
        var quickFood = com.cobblemon.mod.common.item.CobblemonConsumables.QUICK_FOOD;
        check(quickFood.consumeTicks() == 16, "quick food retains legacy 0.8 second consumption");
        check(quickFood.animation() == net.minecraft.world.item.ItemUseAnimation.EAT, "quick food keeps eating animation");
        var manualFood = com.cobblemon.mod.common.item.CobblemonConsumables.MANUAL_FOOD;
        var manualDrink = com.cobblemon.mod.common.item.CobblemonConsumables.MANUAL_DRINK;
        check(manualFood.consumeTicks() == 32 && manualFood.animation() == net.minecraft.world.item.ItemUseAnimation.EAT, "custom food retains eating duration and animation");
        check(manualFood.sound().equals(net.minecraft.sounds.SoundEvents.GENERIC_EAT), "custom food retains eating sound");
        check(manualDrink.consumeTicks() == 32 && manualDrink.animation() == net.minecraft.world.item.ItemUseAnimation.DRINK, "custom drink retains duration and animation");
        check(manualDrink.sound().equals(net.minecraft.sounds.SoundEvents.GENERIC_DRINK), "custom drink retains drinking sound");
        check(manualFood.onConsumeEffects().isEmpty() && manualDrink.onConsumeEffects().isEmpty(), "custom handlers have no duplicate component effects");
        var manualComponents = itemComponents(RegistrationProperties.item("contract_manual_drink")
                .component(net.minecraft.core.component.DataComponents.CONSUMABLE, manualDrink), vanilla);
        check(manualComponents.get(net.minecraft.core.component.DataComponents.FOOD) == null, "manual nutrition is not duplicated by a food component");
        check(manualComponents.get(net.minecraft.core.component.DataComponents.CONSUMABLE).equals(manualDrink), "manual drink sound metadata installed");
        var teaComponents = itemComponents(RegistrationProperties.item("contract_tea").stacksTo(16)
                .usingConvertsTo(net.minecraft.world.item.Items.BOWL)
                .food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(0).saturationModifier(0F).alwaysEdible().build(), manualDrink), vanilla);
        check(teaComponents.get(net.minecraft.core.component.DataComponents.FOOD).canAlwaysEat(), "tea remains usable at full hunger");
        check(teaComponents.get(net.minecraft.core.component.DataComponents.USE_REMAINDER).convertInto().item().value() == net.minecraft.world.item.Items.BOWL, "tea bowl remainder retained");
        check(teaComponents.get(net.minecraft.core.component.DataComponents.CONSUMABLE).equals(manualDrink), "tea uses native drinking component");
        var dip = com.cobblemon.mod.common.item.CobblemonConsumables.VIVICHOKE_DIP;
        var dipEffect = (net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect) dip.onConsumeEffects().getFirst();
        check(dipEffect.probability() == 1F, "dip absorption probability retained");
        check(dipEffect.effects().getFirst().getEffect().equals(net.minecraft.world.effect.MobEffects.ABSORPTION), "dip absorption effect retained");
        check(dipEffect.effects().getFirst().getDuration() == 900 && dipEffect.effects().getFirst().getAmplifier() == 0, "dip effect duration and amplifier retained");
        var dipData = net.minecraft.world.item.component.Consumable.CODEC.encodeStart(optionalOps, dip).getOrThrow();
        check(net.minecraft.world.item.component.Consumable.CODEC.parse(optionalOps, dipData).getOrThrow().equals(dip), "consumable effect codec round trip");
        var modelData = new net.minecraft.world.item.component.CustomModelData(java.util.List.of(5F), java.util.List.of(), java.util.List.of(), java.util.List.of());
        check(modelData.getFloat(0) == 5F && modelData.flags().isEmpty() && modelData.strings().isEmpty() && modelData.colors().isEmpty(), "legacy model number occupies first float channel only");
        check(net.minecraft.world.item.component.CustomModelData.CODEC.parse(optionalOps,
                net.minecraft.world.item.component.CustomModelData.CODEC.encodeStart(optionalOps, modelData).getOrThrow()).getOrThrow().equals(modelData), "model data component codec round trip");
        var changedStack = inventory.get(2);
        var useSuccess = (net.minecraft.world.InteractionResult.Success) com.cobblemon.mod.common.util.ItemUseResults.success(changedStack);
        check(useSuccess.consumesAction() && useSuccess.shouldSwing(), "item success consumes action and swings");
        check(useSuccess.heldItemTransformedTo() == changedStack, "success retains replacement stack identity");
        var consumeResult = (net.minecraft.world.InteractionResult.Success) com.cobblemon.mod.common.util.ItemUseResults.consume(changedStack);
        check(consumeResult.consumesAction() && !consumeResult.shouldSwing(), "consume preserves no-swing action");
        check(consumeResult.heldItemTransformedTo() == changedStack, "consume retains replacement stack identity");
        check(com.cobblemon.mod.common.util.ItemUseResults.pass(changedStack) == net.minecraft.world.InteractionResult.PASS, "item pass remains pass");
        check(com.cobblemon.mod.common.util.ItemUseResults.fail(changedStack) == net.minecraft.world.InteractionResult.FAIL, "item failure remains failure");
        var clientUse = (net.minecraft.world.InteractionResult.Success) com.cobblemon.mod.common.util.ItemUseResults.sidedSuccess(changedStack, true);
        var serverUse = (net.minecraft.world.InteractionResult.Success) com.cobblemon.mod.common.util.ItemUseResults.sidedSuccess(changedStack, false);
        check(clientUse.shouldSwing() && clientUse.heldItemTransformedTo() == changedStack, "client sided result retains swing and replacement");
        check(!serverUse.shouldSwing() && serverUse.heldItemTransformedTo() == changedStack, "server sided result retains consume and replacement");
        check(com.cobblemon.mod.common.util.ItemUseResults.sidedSuccess(true) == net.minecraft.world.InteractionResult.SUCCESS, "entity sided success retains client swing without stack replacement");
        check(com.cobblemon.mod.common.util.ItemUseResults.sidedSuccess(false) == net.minecraft.world.InteractionResult.CONSUME, "entity sided success retains server consume without stack replacement");
        check(com.cobblemon.mod.common.util.ItemUseResults.withStack(net.minecraft.world.InteractionResult.PASS, changedStack)
                == net.minecraft.world.InteractionResult.PASS, "placement pass stays pass");
        check(com.cobblemon.mod.common.util.ItemUseResults.withStack(net.minecraft.world.InteractionResult.FAIL, changedStack)
                == net.minecraft.world.InteractionResult.FAIL, "placement failure stays failure");
        var serverSuccess = (net.minecraft.world.InteractionResult.Success) com.cobblemon.mod.common.util.ItemUseResults.withStack(
                net.minecraft.world.InteractionResult.SUCCESS_SERVER, changedStack);
        check(serverSuccess.swingSource() == net.minecraft.world.InteractionResult.SUCCESS_SERVER.swingSource(), "placement keeps server swing source");
        var candyComponents = itemComponents(RegistrationProperties.item("contract_candy")
                .usingConvertsTo(net.minecraft.world.item.Items.STICK)
                .food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(6).saturationModifier(0.2F).build()), vanilla);
        check(candyComponents.get(net.minecraft.core.component.DataComponents.USE_REMAINDER).convertInto().item().value()
                == net.minecraft.world.item.Items.STICK, "food setup retains stick remainder");
        check(candyComponents.get(net.minecraft.core.component.DataComponents.FOOD).nutrition() == 6, "food nutrition retained");
        check(Math.abs(candyComponents.get(net.minecraft.core.component.DataComponents.FOOD).saturation() - 2.4F) < 0.00001F, "food saturation modifier retained");
        var dipComponents = itemComponents(RegistrationProperties.item("contract_dip").stacksTo(1)
                .usingConvertsTo(net.minecraft.world.item.Items.BOWL)
                .food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(10).saturationModifier(0.6F).alwaysEdible().build(), dip), vanilla);
        check(dipComponents.get(net.minecraft.core.component.DataComponents.USE_REMAINDER).convertInto().item().value()
                == net.minecraft.world.item.Items.BOWL, "dip setup retains bowl remainder");
        check(dipComponents.get(net.minecraft.core.component.DataComponents.FOOD).canAlwaysEat(), "dip always-edible flag retained");
        check(dipComponents.get(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE) == 1, "dip stack limit retained");
        check(dipComponents.get(net.minecraft.core.component.DataComponents.CONSUMABLE).equals(dip), "dip consumption and nutrition coexist");
        check(com.cobblemon.mod.common.util.LegacyBlockState.blocksMotion(net.minecraft.world.level.block.Blocks.STONE.defaultBlockState()), "solid stone supports legacy pasture teleport");
        check(com.cobblemon.mod.common.util.LegacyBlockState.blocksMotion(net.minecraft.world.level.block.Blocks.GLASS.defaultBlockState()), "transparent solid glass supports legacy pasture teleport");
        check(!com.cobblemon.mod.common.util.LegacyBlockState.blocksMotion(net.minecraft.world.level.block.Blocks.COBWEB.defaultBlockState()), "legacy teleport excludes cobweb even when solid");
        check(!com.cobblemon.mod.common.util.LegacyBlockState.blocksMotion(net.minecraft.world.level.block.Blocks.BAMBOO_SAPLING.defaultBlockState()), "legacy teleport excludes bamboo sapling");
        check(!com.cobblemon.mod.common.util.LegacyBlockState.blocksMotion(net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()), "air cannot support legacy pasture teleport");
        check(!com.cobblemon.mod.common.util.LegacyBlockState.blocksMotion(net.minecraft.world.level.block.Blocks.WATER.defaultBlockState()), "water cannot support legacy pasture teleport");
        for (var color : net.minecraft.world.item.DyeColor.values()) {
            var wool = net.minecraft.world.item.Items.WOOL.pick(color);
            check(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(wool).toString().equals("minecraft:" + color.getName() + "_wool"), "native wool collection retains legacy registry ID for " + color.getName());
        }
        var battleSerializer = com.cobblemon.mod.common.api.net.serializers.OptionalUuidDataSerializer.INSTANCE;
        net.minecraft.network.syncher.EntityDataSerializers.registerSerializer(battleSerializer);
        check(net.minecraft.network.syncher.EntityDataSerializers.getSerializedId(battleSerializer) >= 0, "battle serializer registered with native registry");
        var battleBuffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), vanilla);
        try {
            battleSerializer.codec().encode(battleBuffer, java.util.Optional.empty());
            check(battleBuffer.readableBytes() == 1 && battleBuffer.getByte(0) == 0, "absent battle keeps one false presence byte");
            check(battleSerializer.codec().decode(battleBuffer).isEmpty() && !battleBuffer.isReadable(), "empty battle clears without trailing bytes");
            var battleUuid = UUID.fromString("ffffffff-0000-ffff-8000-0123456789ab");
            battleBuffer.clear();
            battleSerializer.codec().encode(battleBuffer, java.util.Optional.of(battleUuid));
            byte[] expectedWire = java.nio.ByteBuffer.allocate(17).put((byte) 1)
                    .putLong(battleUuid.getMostSignificantBits()).putLong(battleUuid.getLeastSignificantBits()).array();
            byte[] actualWire = new byte[battleBuffer.readableBytes()];
            battleBuffer.getBytes(0, actualWire);
            check(java.util.Arrays.equals(expectedWire, actualWire), "present battle keeps presence plus full UUID longs in legacy wire order");
            check(battleSerializer.codec().decode(battleBuffer).orElseThrow().equals(battleUuid), "signed UUID bits survive battle synchronization");
            check(!battleBuffer.isReadable(), "battle decode consumes exactly one value");
            check(battleSerializer.copy(java.util.Optional.of(battleUuid)).orElseThrow().equals(battleUuid), "immutable battle UUID copy retains value");
            battleBuffer.clear();
            var dataValue = new net.minecraft.network.syncher.SynchedEntityData.DataValue<>(7, battleSerializer, java.util.Optional.of(battleUuid));
            dataValue.write(battleBuffer);
            int fieldId = battleBuffer.readUnsignedByte();
            var receivedValue = net.minecraft.network.syncher.SynchedEntityData.DataValue.read(battleBuffer, fieldId);
            check(receivedValue.id() == 7, "native entity data frame retains field ID");
            check(receivedValue.serializer() == battleSerializer, "native entity data frame resolves registered custom serializer");
            check(receivedValue.value().equals(java.util.Optional.of(battleUuid)) && !battleBuffer.isReadable(), "native entity data frame round trip retains battle UUID");
            battleBuffer.clear();
            new net.minecraft.network.syncher.SynchedEntityData.DataValue<>(7, battleSerializer, java.util.Optional.empty()).write(battleBuffer);
            fieldId = battleBuffer.readUnsignedByte();
            check(net.minecraft.network.syncher.SynchedEntityData.DataValue.read(battleBuffer, fieldId).value().equals(java.util.Optional.empty()), "native entity data frame clears battle UUID");
            battleBuffer.clear();
            battleBuffer.writeBoolean(true).writeLong(battleUuid.getMostSignificantBits());
            try {
                battleSerializer.codec().decode(battleBuffer);
                throw new AssertionError("Truncated UUID accepted");
            } catch (IndexOutOfBoundsException expected) { checks++; }
        } finally {
            battleBuffer.release();
        }
        var ownerReference = net.minecraft.world.entity.EntityReference.<net.minecraft.world.entity.LivingEntity>of(owner);
        check(ownerReference.getUUID().equals(owner), "unresolved vanilla owner reference retains UUID");
        var ownerReferenceCodec = net.minecraft.world.entity.EntityReference.<net.minecraft.world.entity.LivingEntity>codec();
        var referenceTag = ownerReferenceCodec.encodeStart(optionalOps, ownerReference).getOrThrow();
        check(referenceTag instanceof IntArrayTag && java.util.Arrays.equals(((IntArrayTag) referenceTag).getAsIntArray(), net.minecraft.core.UUIDUtil.uuidToIntArray(owner)), "native owner reference retains legacy four-int UUID shape");
        check(ownerReferenceCodec.parse(optionalOps, referenceTag).getOrThrow().getUUID().equals(owner), "native owner reference codec round trip");
        var savedOwner = LegacyNbt.toTag(vanilla, out -> net.minecraft.world.entity.EntityReference.store(ownerReference, out, "Owner"));
        check(LegacyNbt.getUUID(savedOwner, "Owner").equals(owner), "native owner save is readable by legacy UUID adapter");
        var loadedOwner = net.minecraft.world.entity.EntityReference.<net.minecraft.world.entity.LivingEntity>read(
                net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, vanilla, savedOwner), "Owner");
        check(loadedOwner != null && loadedOwner.getUUID().equals(owner), "native structured owner reload retains UUID");
        var noOwner = LegacyNbt.toTag(vanilla, out -> net.minecraft.world.entity.EntityReference.store(null, out, "Owner"));
        check(!noOwner.contains("Owner"), "clearing ownership omits saved reference");
        check(net.minecraft.world.entity.EntityReference.read(
                net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, vanilla, noOwner), "Owner") == null, "missing owner reloads as unowned");
        // Walking uses only horizontal displacement, including on climbable blocks.
        check(com.cobblemon.mod.common.util.LegacyMovement.walkIncrement(new net.minecraft.world.phys.Vec3(0, 4, 0)) == 0F, "vertical movement does not trigger walking");
        check(com.cobblemon.mod.common.util.LegacyMovement.walkIncrement(new net.minecraft.world.phys.Vec3(3, 100, 4)) == 3F, "walking uses horizontal 3-4-5 distance");
        check(com.cobblemon.mod.common.util.LegacyMovement.walkIncrement(new net.minecraft.world.phys.Vec3(-3, -100, -4)) == 3F, "walking handles negative directions");
        check(com.cobblemon.mod.common.util.LegacyMovement.walkIncrement(net.minecraft.world.phys.Vec3.ZERO) == 0F, "stationary walking counter remains still");
        check(com.cobblemon.mod.common.util.LegacyMovement.walkIncrement(new net.minecraft.world.phys.Vec3(0.001, 1, 0)) <= 0.005F, "legacy tiny-step threshold retained");
        net.minecraft.world.entity.Entity.class.getDeclaredMethod("applyMovementEmissionAndPlaySound", net.minecraft.world.entity.Entity.MovementEmission.class, net.minecraft.world.phys.Vec3.class, net.minecraft.core.BlockPos.class, net.minecraft.world.level.block.state.BlockState.class);
        checks++;
        var definitions = com.cobblemon.mod.common.world.gamerules.CobblemonGameRuleDefinitions.BY_LEGACY_NAME;
        check(definitions.size() == 6, "all six custom gamerules retained");
        boolean[] defaults = {false, true, true, true, false, false};
        int ruleIndex = 0;
        for (var entry : definitions.entrySet()) {
            var definition = entry.getValue();
            var rule = definition.createRule();
            check(definition.identifier().getNamespace().equals("cobblemon"), "custom gamerules use own namespace");
            check(rule.defaultValue() == defaults[ruleIndex++], "legacy gamerule default retained for " + entry.getKey());
            check(rule.category().equals(definition.category()), "gamerule category retained");
            check(rule.gameRuleType() == net.minecraft.world.level.gamerules.GameRuleType.BOOL, "boolean gamerule type");
            var gameRules = new net.minecraft.world.level.gamerules.GameRules(java.util.List.of(rule));
            check(gameRules.get(rule).equals(rule.defaultValue()), "native typed gamerule default lookup");
            gameRules.set(rule, !rule.defaultValue(), null);
            check(gameRules.get(rule) != rule.defaultValue(), "native typed gamerule update");
            check(rule.getCommandResult(true) == 1 && rule.getCommandResult(false) == 0, "boolean command result semantics");
            var legacyRules = new CompoundTag();
            legacyRules.putString(entry.getKey(), "TrUe");
            legacyRules.putString("unrelated:addon_rule", "preserve");
            var migrated = (CompoundTag) com.cobblemon.mod.common.world.gamerules.LegacyGameRules.migrate(new com.mojang.serialization.Dynamic<>(NbtOps.INSTANCE, legacyRules)).getValue();
            check(migrated.getBooleanOr(definition.identifier().toString(), false), "legacy true converts to boolean at stable ID");
            check(!migrated.contains(entry.getKey()), "successful conversion removes old name");
            check(migrated.getStringOr("unrelated:addon_rule", "").equals("preserve"), "other addon settings retained");
            check(legacyRules.contains(entry.getKey()), "conversion does not mutate original NBT input");
            legacyRules.putString(entry.getKey(), "false");
            migrated = (CompoundTag) com.cobblemon.mod.common.world.gamerules.LegacyGameRules.migrate(new com.mojang.serialization.Dynamic<>(NbtOps.INSTANCE, legacyRules)).getValue();
            check(!migrated.getBooleanOr(definition.identifier().toString(), true), "legacy false is retained");
            legacyRules.putBoolean(definition.identifier().toString(), true);
            migrated = (CompoundTag) com.cobblemon.mod.common.world.gamerules.LegacyGameRules.migrate(new com.mojang.serialization.Dynamic<>(NbtOps.INSTANCE, legacyRules)).getValue();
            check(migrated.getBooleanOr(definition.identifier().toString(), false), "explicit modern value wins conflict");
            check(!migrated.contains(entry.getKey()), "conflicting legacy copy is removed");
            check(com.cobblemon.mod.common.world.gamerules.LegacyGameRules.migrate(new com.mojang.serialization.Dynamic<>(NbtOps.INSTANCE, migrated)).getValue().equals(migrated), "gamerule conversion is idempotent");
        }
        var invalidRules = new CompoundTag();
        invalidRules.putString("doPokemonLoot", "yes");
        try {
            com.cobblemon.mod.common.world.gamerules.LegacyGameRules.migrate(new com.mojang.serialization.Dynamic<>(NbtOps.INSTANCE, invalidRules));
            throw new AssertionError("Malformed gamerule silently defaulted");
        } catch (IllegalArgumentException expected) { checks++; }
        var jsonRules = new com.google.gson.JsonObject();
        jsonRules.addProperty("doPokemonSpawning", "false");
        var jsonMigrated = com.cobblemon.mod.common.world.gamerules.LegacyGameRules.migrate(new com.mojang.serialization.Dynamic<>(com.mojang.serialization.JsonOps.INSTANCE, jsonRules)).getValue();
        check(!jsonMigrated.getAsJsonObject().get("cobblemon:do_pokemon_spawning").getAsBoolean(), "conversion supports JSON ops too");
        // Exercise actual vanilla conversion followed by the production return-value adapter.
        var vanillaFix = net.minecraft.util.datafix.fixes.GameRuleRegistryFix.class.getDeclaredMethod("lambda$makeRule$2", com.mojang.serialization.Dynamic.class);
        vanillaFix.setAccessible(true);
        var oldRuleSet = new CompoundTag();
        oldRuleSet.putString("doMobLoot", "false");
        oldRuleSet.putString("doPokemonLoot", "false");
        var vanillaConverted = (com.mojang.serialization.Dynamic<?>) vanillaFix.invoke(null, new com.mojang.serialization.Dynamic<>(NbtOps.INSTANCE, oldRuleSet));
        var allConverted = (CompoundTag) com.cobblemon.mod.common.world.gamerules.LegacyGameRules.migrate(vanillaConverted).getValue();
        check(!allConverted.getBooleanOr("minecraft:mob_drops", true), "vanilla gamerule conversion remains effective");
        check(!allConverted.getBooleanOr("cobblemon:do_pokemon_loot", true), "custom gamerule survives vanilla conversion chain");
        var shoulderSerializer = com.cobblemon.mod.common.api.net.serializers.ShoulderTagDataSerializer.INSTANCE;
        net.minecraft.network.syncher.EntityDataSerializers.registerSerializer(shoulderSerializer);
        check(net.minecraft.network.syncher.EntityDataSerializers.getSerializedId(shoulderSerializer) > net.minecraft.network.syncher.EntityDataSerializers.getSerializedId(battleSerializer), "shoulder serializer appends after existing battle registration");
        var shoulder = new CompoundTag();
        shoulder.putString("id", "cobblemon:pokemon");
        shoulder.put("Pokemon", loaded.copy());
        var shoulderCopy = shoulderSerializer.copy(shoulder);
        check(shoulderCopy.equals(shoulder) && shoulderCopy != shoulder, "shoulder snapshot owns tag copy");
        shoulder.getCompoundOrEmpty("Pokemon").putString("species", "cobblemon:eevee");
        check(shoulderCopy.getCompoundOrEmpty("Pokemon").getStringOr("species", "").equals("cobblemon:bulbasaur"), "nested shoulder snapshot cannot be mutated by caller");
        var shoulderBuffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), vanilla);
        try {
            for (var accessor : java.util.List.of(com.cobblemon.mod.common.util.ShoulderData.LEFT, com.cobblemon.mod.common.util.ShoulderData.RIGHT)) {
                shoulderBuffer.clear();
                new net.minecraft.network.syncher.SynchedEntityData.DataValue<>(accessor.id(), shoulderSerializer, shoulderCopy).write(shoulderBuffer);
                int index = shoulderBuffer.readUnsignedByte();
                var received = net.minecraft.network.syncher.SynchedEntityData.DataValue.read(shoulderBuffer, index);
                check(received.id() == accessor.id(), "native shoulder frame retains left/right field ID");
                check(received.value().equals(shoulderCopy) && !shoulderBuffer.isReadable(), "shoulder frame preserves full legacy Pokemon NBT");
                check(LegacyNbt.getUUID(((CompoundTag) received.value()).getCompoundOrEmpty("Pokemon"), "owner").equals(owner), "shoulder frame retains owner UUID");
                shoulderBuffer.clear();
                new net.minecraft.network.syncher.SynchedEntityData.DataValue<>(accessor.id(), shoulderSerializer, new CompoundTag()).write(shoulderBuffer);
                index = shoulderBuffer.readUnsignedByte();
                check(((CompoundTag) net.minecraft.network.syncher.SynchedEntityData.DataValue.read(shoulderBuffer, index).value()).isEmpty(), "empty shoulder frame clears client representation");
            }
        } finally { shoulderBuffer.release(); }
        check(com.cobblemon.mod.common.util.ShoulderData.LEFT.id() != com.cobblemon.mod.common.util.ShoulderData.RIGHT.id(), "left and right shoulder metadata are independent");
        net.minecraft.server.level.ServerPlayer.class.getDeclaredMethod("setShoulderEntityLeft", CompoundTag.class);
        net.minecraft.server.level.ServerPlayer.class.getDeclaredMethod("setShoulderEntityRight", CompoundTag.class);
        net.minecraft.server.level.ServerPlayer.class.getDeclaredMethod("respawnEntityOnShoulder", CompoundTag.class);
        checks += 3;
        var failedCandidates = new java.util.concurrent.atomic.AtomicInteger();
        var nullScoreCalls = new java.util.concurrent.atomic.AtomicInteger();
        var noRandomPosition = com.cobblemon.mod.common.entity.ai.LegacyRandomPos.select(
                () -> { failedCandidates.incrementAndGet(); return null; },
                pos -> { nullScoreCalls.incrementAndGet(); return 1.0; });
        check(noRandomPosition == null, "all failed random candidates retain nullable failure");
        check(failedCandidates.get() == 10 && nullScoreCalls.get() == 0, "native random selection makes ten attempts without scoring null");
        var candidates = new java.util.concurrent.atomic.AtomicInteger();
        var selectedPosition = com.cobblemon.mod.common.entity.ai.LegacyRandomPos.select(
                () -> { int i = candidates.getAndIncrement(); return i == 4 ? new net.minecraft.core.BlockPos(-8, 63, 7) : new net.minecraft.core.BlockPos(2, 64, 3); },
                pos -> pos.getX() == -8 ? 10.0 : -1.0);
        check(selectedPosition.equals(new net.minecraft.world.phys.Vec3(-7.5, 63, 7.5)), "native random selector keeps best scored bottom-center including negative coordinates");
        check(candidates.get() == 10, "successful random search still compares all ten candidates");
        record Positioned(String name, net.minecraft.world.phys.Vec3 pos, boolean accepted) {}
        var nearPlayers = java.util.List.of(
                new Positioned("lower boundary", new net.minecraft.world.phys.Vec3(-8, -8, -8), true),
                new Positioned("upper boundary", new net.minecraft.world.phys.Vec3(8, 0, 0), true),
                new Positioned("rejected", net.minecraft.world.phys.Vec3.ZERO, false),
                new Positioned("near edge outside", new net.minecraft.world.phys.Vec3(8.001, 0, 0), true),
                new Positioned("inside", new net.minecraft.world.phys.Vec3(0, 7.999, 0), true));
        var testedTargets = new java.util.ArrayList<String>();
        var foundPlayers = com.cobblemon.mod.common.entity.ai.LegacyEntityQueries.select(nearPlayers,
                net.minecraft.world.phys.AABB.ofSize(net.minecraft.world.phys.Vec3.ZERO, 16, 16, 16), Positioned::pos,
                player -> { testedTargets.add(player.name()); return player.accepted(); });
        check(foundPlayers.stream().map(Positioned::name).toList().equals(java.util.List.of("lower boundary", "inside")), "nearby query keeps old half-open position bounds and player order");
        check(testedTargets.equals(java.util.List.of("lower boundary", "rejected", "inside")), "out-of-bounds positions are rejected before targeting tests");
        check(nearPlayers.size() == 5, "nearby query does not mutate source players");
        check(com.cobblemon.mod.common.entity.ai.LegacyEntityQueries.select(java.util.List.<Positioned>of(),
                new net.minecraft.world.phys.AABB(0, 0, 0, 1, 1, 1), Positioned::pos, player -> true).isEmpty(), "empty nearby query stays empty");
        var hiveSaved = new CompoundTag();
        hiveSaved.putString("id", "minecraft:bee");
        for (String key : com.cobblemon.mod.common.util.HiveOccupants.IGNORED_TAGS) hiveSaved.putInt(key, 99);
        var pokemonPayload = new CompoundTag();
        LegacyNbt.putUUID(pokemonPayload, "UUID", pokemon);
        LegacyNbt.putUUID(pokemonPayload, "owner", owner);
        pokemonPayload.putString("species", "cobblemon:combee");
        hiveSaved.put("Pokemon", pokemonPayload);
        hiveSaved.putString("addon_payload", "retain");
        var hiveNectar = com.cobblemon.mod.common.util.HiveOccupants.snapshot(net.minecraft.world.entity.EntityTypes.BEE, hiveSaved, true);
        check(hiveNectar.entityData().type() == net.minecraft.world.entity.EntityTypes.BEE, "hive stores explicit native entity type");
        check(hiveNectar.ticksInHive() == 0 && hiveNectar.minTicksInHive() == 2400, "nectar retains 2400 tick residence");
        var hiveTag = hiveNectar.entityData().copyTagWithoutId();
        for (String key : com.cobblemon.mod.common.util.HiveOccupants.IGNORED_TAGS) {
            check(!hiveTag.contains(key), "root hive exclusion retained: " + key);
        }
        check(!hiveTag.contains("id"), "native typed data separates the entity ID from payload");
        check(LegacyNbt.getUUID(hiveTag.getCompoundOrEmpty("Pokemon"), "UUID").equals(pokemon), "hive filtering retains nested Pokemon identity");
        check(LegacyNbt.getUUID(hiveTag.getCompoundOrEmpty("Pokemon"), "owner").equals(owner), "hive filtering retains nested Pokemon ownership");
        check(hiveTag.getStringOr("addon_payload", "").equals("retain"), "hive retains unknown addon data");
        check(hiveSaved.contains("UUID"), "hive snapshot does not mutate source save");
        check(com.cobblemon.mod.common.util.HiveOccupants.snapshot(net.minecraft.world.entity.EntityTypes.BEE, hiveSaved, false).minTicksInHive() == 600, "non-nectar residence remains 600 ticks");
        var hiveEncoded = net.minecraft.world.level.block.entity.BeehiveBlockEntity.Occupant.CODEC.encodeStart(optionalOps, hiveNectar).getOrThrow();
        var hiveDecoded = net.minecraft.world.level.block.entity.BeehiveBlockEntity.Occupant.CODEC.parse(optionalOps, hiveEncoded).getOrThrow();
        check(hiveDecoded.equals(hiveNectar), "native occupant codec round trip with full custom payload");
        var hiveBuffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), vanilla);
        try {
            net.minecraft.world.level.block.entity.BeehiveBlockEntity.Occupant.STREAM_CODEC.encode(hiveBuffer, hiveNectar);
            check(net.minecraft.world.level.block.entity.BeehiveBlockEntity.Occupant.STREAM_CODEC.decode(hiveBuffer).equals(hiveNectar), "native occupant network codec retains type and custom payload");
            check(!hiveBuffer.isReadable(), "native hive frame has no unconsumed bytes");
        } finally { hiveBuffer.release(); }
        var placeholder = com.cobblemon.mod.common.util.HiveOccupants.placeholder(net.minecraft.world.entity.EntityTypes.BEE, "combee lvl=12", 598);
        check(placeholder.ticksInHive() == 598 && placeholder.minTicksInHive() == 600, "worldgen placeholder keeps original timer values");
        check(placeholder.entityData().copyTagWithoutId().getStringOr("pokemonProperties", "").equals("combee lvl=12"), "worldgen placeholder retains property string without creating an entity");
        // String IDs exercise native TypedEntityData without inventing a Pokemon registration in the fixture registry.
        var nativeStringHive = net.minecraft.world.item.component.TypedEntityData.codec(com.mojang.serialization.Codec.STRING);
        var migratedHive = com.cobblemon.mod.common.util.HiveOccupants.legacyCodec(nativeStringHive);
        for (String placeholderId : java.util.List.of("cobblemon_pokemon_entity_placeholder", "minecraft:cobblemon_pokemon_entity_placeholder")) {
            var legacyHive = new CompoundTag();
            legacyHive.putString("id", placeholderId);
            legacyHive.putString("pokemonProperties", "combee lvl=12");
            legacyHive.put("nested", pokemonPayload.copy());
            var restoredHive = migratedHive.parse(NbtOps.INSTANCE, legacyHive).getOrThrow();
            check(restoredHive.type().equals("cobblemon:pokemon"), "legacy placeholder ID becomes registered Pokemon ID before typed decoding");
            check(restoredHive.copyTagWithoutId().getStringOr("pokemonProperties", "").equals("combee lvl=12"), "legacy placeholder properties survive typed decoding");
            check(LegacyNbt.getUUID(restoredHive.copyTagWithoutId().getCompoundOrEmpty("nested"), "owner").equals(owner), "legacy placeholder retains unknown nested ownership data");
            check(legacyHive.getStringOr("id", "").equals(placeholderId), "legacy placeholder input remains unchanged");
            var modernHive = migratedHive.encodeStart(NbtOps.INSTANCE, restoredHive).getOrThrow();
            check(((CompoundTag) modernHive).getStringOr("id", "").equals("cobblemon:pokemon"), "upgraded placeholder writes the modern ID");
            check(migratedHive.parse(NbtOps.INSTANCE, modernHive).getOrThrow().equals(restoredHive), "placeholder migration is idempotent");
        }
        for (String badProperties : new String[] {"", "   "}) {
            var invalidHive = new CompoundTag();
            invalidHive.putString("id", "cobblemon_pokemon_entity_placeholder");
            invalidHive.putString("pokemonProperties", badProperties);
            check(migratedHive.parse(NbtOps.INSTANCE, invalidHive).error().isPresent(), "malformed legacy placeholder is refused instead of creating a random Pokemon");
        }
        var ordinaryHive = new CompoundTag();
        ordinaryHive.putString("id", "another_mod:occupant");
        ordinaryHive.putString("pokemonProperties", "preserve");
        check(migratedHive.parse(NbtOps.INSTANCE, ordinaryHive).getOrThrow().type().equals("another_mod:occupant"), "unrelated occupant IDs are unchanged");
        var jsonPlaceholder = new com.google.gson.JsonObject();
        jsonPlaceholder.addProperty("id", "cobblemon_pokemon_entity_placeholder");
        jsonPlaceholder.addProperty("pokemonProperties", "combee lvl=12");
        check(migratedHive.parse(com.mojang.serialization.JsonOps.INSTANCE, jsonPlaceholder).getOrThrow().type().equals("cobblemon:pokemon"), "placeholder conversion retains ops for JSON as well as NBT");
        var emptyScheduleBrain = new com.cobblemon.mod.common.util.ScriptedBrain<net.minecraft.world.entity.LivingEntity>(
                java.util.List.of(), java.util.List.of(), new net.minecraft.world.entity.ai.Brain.Packed(new net.minecraft.world.entity.ai.memory.MemoryMap.Builder().build()), net.minecraft.util.RandomSource.create(17));
        for (var activity : java.util.List.of(net.minecraft.world.entity.schedule.Activity.IDLE, net.minecraft.world.entity.schedule.Activity.FIGHT)) {
            emptyScheduleBrain.addActivity(activity, com.google.common.collect.ImmutableList.of(), java.util.Set.of(), java.util.Set.of());
        }
        emptyScheduleBrain.setDefaultActivity(net.minecraft.world.entity.schedule.Activity.IDLE);
        emptyScheduleBrain.setActiveActivityIfPossible(net.minecraft.world.entity.schedule.Activity.FIGHT);
        var environment = net.minecraft.world.attribute.EnvironmentAttributeSystem.builder().build();
        emptyScheduleBrain.updateActivityFromSchedule(environment, 0L, net.minecraft.world.phys.Vec3.ZERO);
        check(emptyScheduleBrain.isActive(net.minecraft.world.entity.schedule.Activity.IDLE), "native unset environmental schedule retains old empty-schedule IDLE fallback");
        emptyScheduleBrain.setActiveActivityIfPossible(net.minecraft.world.entity.schedule.Activity.FIGHT);
        emptyScheduleBrain.updateActivityFromSchedule(environment, 20L, net.minecraft.world.phys.Vec3.ZERO);
        check(emptyScheduleBrain.isActive(net.minecraft.world.entity.schedule.Activity.FIGHT), "schedule retains twenty-tick update throttle");
        emptyScheduleBrain.updateActivityFromSchedule(environment, 21L, new net.minecraft.world.phys.Vec3(123, 70, -45));
        check(emptyScheduleBrain.isActive(net.minecraft.world.entity.schedule.Activity.IDLE), "schedule can exit custom activity after throttle at an actual position");
        var particleStack = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND, 2);
        particleStack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("held particle"));
        var foodParticle = new net.minecraft.core.particles.ItemParticleOption(net.minecraft.core.particles.ParticleTypes.ITEM,
                net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(particleStack));
        check(foodParticle.getItem().count() == 2, "held-item particle template retains stack count");
        var particleBuffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), vanilla);
        try {
            var particleCodec = net.minecraft.core.particles.ItemParticleOption.streamCodec(net.minecraft.core.particles.ParticleTypes.ITEM);
            particleCodec.encode(particleBuffer, foodParticle);
            var decodedParticle = particleCodec.decode(particleBuffer);
            check(decodedParticle.getItem().create().getItem() == net.minecraft.world.item.Items.DIAMOND, "held-item particle codec retains item identity");
            check(decodedParticle.getItem().create().get(net.minecraft.core.component.DataComponents.CUSTOM_NAME).getString().equals("held particle"), "held-item particle codec retains custom components");
            check(!particleBuffer.isReadable(), "held-item particle frame consumes its complete payload");
        } finally { particleBuffer.release(); }
        checks += Minecraft263VehicleContracts.run(vanilla);
        checks += Minecraft263ModelContracts.run();
        checks += Minecraft263RenderContracts.run();
        checks += Minecraft263Sprint12Contracts.run(vanilla);
        checks += Minecraft263Sprint13Contracts.run();
        checks += Minecraft263Sprint14Contracts.run();
        System.out.println("{\"passed\":true,\"checks\":" + checks + ",\"scope\":\"production migration helpers with real target classes and minimal inventory component fixtures; no game launch\"}");
    }
}
