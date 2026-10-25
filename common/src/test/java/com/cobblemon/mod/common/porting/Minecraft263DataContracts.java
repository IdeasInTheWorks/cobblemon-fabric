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
        System.out.println("{\"passed\":true,\"checks\":" + checks + ",\"scope\":\"production migration helpers with real target classes and minimal inventory component fixtures; no game launch\"}");
    }
}
