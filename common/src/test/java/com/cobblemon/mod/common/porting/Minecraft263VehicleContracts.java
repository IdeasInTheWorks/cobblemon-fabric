/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.porting;

import com.cobblemon.mod.common.entity.boat.BoatDropItem;
import com.cobblemon.mod.common.util.LegacyNbt;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.UniquelyIdentifyable;
import net.minecraft.world.level.entity.UUIDLookup;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;

/** Native owner/vehicle data contracts, without constructing a Minecraft world or player. */
final class Minecraft263VehicleContracts {
    private static int checks;
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        checks++;
    }

    static int run(HolderLookup.Provider registries) throws Exception {
        checks = 0;
        var drop = new BoatDropItem();
        try { drop.get(); throw new AssertionError("unbound drop silently resolved"); }
        catch (NullPointerException expected) { checks++; }
        var variant = new AtomicReference<Item>(Items.DIAMOND);
        drop.bind(variant::get);
        check(drop.get() == Items.DIAMOND, "drop supplier resolves its current variant");
        variant.set(Items.APPLE);
        check(drop.get() == Items.APPLE, "drop supplier follows later loaded/synchronized variant instead of caching first item");
        try { drop.bind(() -> Items.DIAMOND); throw new AssertionError("drop supplier rebound"); }
        catch (IllegalStateException expected) { checks++; }
        check(java.lang.reflect.Modifier.isFinal(net.minecraft.world.entity.vehicle.boat.AbstractBoat.class.getDeclaredMethod("getDropItem").getModifiers()), "target drop getter is final; dynamic provider remains required");

        UUID owner = UUID.fromString("61d07eb7-a810-407c-9a9d-c296cab675d4");
        EntityReference<UniquelyIdentifyable> reference = EntityReference.of(owner);
        var savedOwner = LegacyNbt.toTag(registries, output -> reference.store(output, "Owner"));
        check(LegacyNbt.getUUID(savedOwner, "Owner").equals(owner), "projectile owner retains legacy four-int UUID save form");
        var restored = EntityReference.<UniquelyIdentifyable>read(TagValueInput.create(ProblemReporter.DISCARDING, registries, savedOwner), "Owner");
        check(restored != null && restored.getUUID().equals(owner), "owner reference round trip preserves identity");
        UUIDLookup<UniquelyIdentifyable> absent = id -> null;
        check(restored.getEntity(absent, UniquelyIdentifyable.class) == null, "unloaded owner resolves nullable without losing UUID");
        check(restored.getUUID().equals(owner), "unresolved owner UUID stays available");
        class Identified implements UniquelyIdentifyable {
            boolean removed;
            public UUID getUUID() { return owner; }
            public boolean isRemoved() { return removed; }
        }
        var first = new Identified();
        check(restored.getEntity(id -> first, UniquelyIdentifyable.class) == first, "owner resolves through native lookup");
        first.removed = true;
        check(restored.getEntity(absent, UniquelyIdentifyable.class) == null, "removed cached owner is rejected");
        var replacement = new Identified();
        check(restored.getEntity(id -> replacement, UniquelyIdentifyable.class) == replacement, "owner re-resolves same UUID after unload/reload");
        check(EntityReference.read(TagValueInput.create(ProblemReporter.DISCARDING, registries, new net.minecraft.nbt.CompoundTag()), "Owner") == null, "absent owner stays absent");
        var malformedOwner = new net.minecraft.nbt.CompoundTag();
        malformedOwner.putIntArray("Owner", new int[] {1,2});
        check(EntityReference.read(TagValueInput.create(ProblemReporter.DISCARDING, registries, malformedOwner), "Owner") == null, "malformed UUID is not treated as an owner");

        var chest = new ChestFixture();
        var items = new ItemStack(Items.DIAMOND, 2);
        items.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("stored catch"));
        chest.items.set(26, items);
        var savedChest = LegacyNbt.toTag(registries, output -> {
            output.putString("type", "SACCHARINE");
            chest.addChestVehicleSaveData(output);
        });
        check(savedChest.getStringOr("type", "").equals("SACCHARINE"), "custom variant survives native inventory save");
        var loadedChest = new ChestFixture();
        loadedChest.readChestVehicleSaveData(TagValueInput.create(ProblemReporter.DISCARDING, registries, savedChest));
        check(loadedChest.items.size() == 27, "chest boat retains27 slots");
        check(loadedChest.items.get(0).isEmpty(), "sparse chest inventory retains empty slot");
        check(loadedChest.items.get(26).getItem() == Items.DIAMOND, "last chest slot identity preserved");
        check(loadedChest.items.get(26).getCount() == 2, "last chest slot count preserved");
        check(loadedChest.items.get(26).get(net.minecraft.core.component.DataComponents.CUSTOM_NAME).getString().equals("stored catch"), "chest inventory retains custom components");
        var key = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("cobblemon", "contracts/chest"));
        chest.loot = key;
        chest.seed = Long.MIN_VALUE + 77;
        var lootSave = LegacyNbt.toTag(registries, chest::addChestVehicleSaveData);
        check(!lootSave.contains("Items"), "unopened loot table avoids materializing inventory");
        loadedChest.readChestVehicleSaveData(TagValueInput.create(ProblemReporter.DISCARDING, registries, lootSave));
        check(key.equals(loadedChest.loot) && loadedChest.seed == chest.seed, "native container loot key and full-width seed round trip");
        check(loadedChest.items.stream().allMatch(ItemStack::isEmpty), "loot-table load clears prior materialized inventory");
        return checks;
    }

    // Deliberately no Level/Player: only native save/load helpers may execute in this fixture.
    private static final class ChestFixture implements ContainerEntity {
        NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
        ResourceKey<LootTable> loot;
        long seed;
        public Vec3 position() { return Vec3.ZERO; }
        public AABB getBoundingBox() { return AABB.ofSize(Vec3.ZERO,1,1,1); }
        public ResourceKey<LootTable> getContainerLootTable() { return loot; }
        public void setContainerLootTable(ResourceKey<LootTable> value) { loot = value; }
        public long getContainerLootTableSeed() { return seed; }
        public void setContainerLootTableSeed(long value) { seed = value; }
        public NonNullList<ItemStack> getItemStacks() { return items; }
        public void clearItemStacks() { items = NonNullList.withSize(27, ItemStack.EMPTY); }
        public Level level() { throw new AssertionError("world unexpectedly required by save fixture"); }
        public boolean isRemoved() { return false; }
        public int getContainerSize() { return 27; }
        public ItemStack getItem(int slot) { return items.get(slot); }
        public ItemStack removeItem(int slot,int amount) { return removeChestVehicleItem(slot,amount); }
        public ItemStack removeItemNoUpdate(int slot) { return removeChestVehicleItemNoUpdate(slot); }
        public void setItem(int slot,ItemStack stack) { items.set(slot,stack); }
        public void setChanged() {}
        public boolean stillValid(Player player) { return false; }
        public void clearContent() { clearItemStacks(); }
        public Component getDisplayName() { return Component.literal("contract chest"); }
        public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player) { throw new AssertionError("menu opened in save fixture"); }
    }
}
