/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.porting;

import com.cobblemon.mod.common.client.model.*;
import com.cobblemon.mod.common.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;

/** Native helper/descriptor contracts; no Fabric mixin transformation, world, game or GPU. */
final class Minecraft263Sprint12Contracts {
    private static int checks;
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        checks++;
    }
    static int run(HolderLookup.Provider registries) throws Exception {
        checks = 0;
        var inventory = new SimpleContainer(42);
        inventory.setItem(0, new ItemStack(Items.STONE, 7));
        inventory.setItem(2, new ItemStack(Items.DIAMOND, 3));
        inventory.setItem(36, new ItemStack(Items.DIAMOND, 4));
        inventory.setItem(40, new ItemStack(Items.DIAMOND, 8));
        check(InventoryRemoval.removeMatching(inventory, 5, stack -> stack.is(Items.DIAMOND)) == 5, "one total budget across normal/equipment/offhand slot IDs");
        check(inventory.getItem(2).isEmpty() && inventory.getItem(36).getCount() == 2, "remove exact remainder from actual later slot");
        check(inventory.getItem(40).getCount() == 8 && inventory.getItem(0).getCount() == 7, "later and nonmatching stacks untouched");
        check(InventoryRemoval.removeMatching(inventory, 0, stack -> { throw new AssertionError("zero must not evaluate predicate"); }) == 0, "zero removal does nothing");
        check(InventoryRemoval.removeMatching(inventory, -7, stack -> true) == 0, "negative removal does nothing");
        check(InventoryRemoval.removeMatching(inventory, 99, stack -> stack.is(Items.DIAMOND)) == 10, "short inventory returns exact removed count");
        check(inventory.getItem(40).isEmpty(), "offhand/native slot removed once");

        var result = new ItemStack(Items.DIAMOND, 2);
        result.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("trade fixture"));
        var trade = NativeTrades.offer(new ItemCost(Items.STONE, 4), result, 12, 15,
                Optional.of(new ItemCost(Items.DIAMOND, 3)), 0.05F);
        var ops = registries.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
        var encoded = VillagerTrade.CODEC.encodeStart(ops, trade).getOrThrow();
        var json = encoded.getAsJsonObject();
        check(json.getAsJsonObject("wants").get("count").getAsInt() == 4, "native trade retains primary amount");
        check(json.getAsJsonObject("additional_wants").get("count").getAsInt() == 3, "native trade retains secondary amount");
        check(json.getAsJsonObject("gives").get("count").getAsInt() == 2, "native trade retains output amount");
        check(json.get("max_uses").getAsInt() == 12 && json.get("xp").getAsInt() == 15, "uses and XP retained");
        check(json.get("reputation_discount").getAsFloat() == 0.05F, "legacy price multiplier retained in native offer policy");
        check(encoded.toString().contains("trade fixture"), "output components retained");
        var decoded = VillagerTrade.CODEC.parse(ops, encoded).getOrThrow();
        check(VillagerTrade.CODEC.encodeStart(ops, decoded).getOrThrow().equals(encoded), "native trade codec round trip");
        var sequence = Optional.of(Identifier.parse("addon:trades"));
        var original = new TradeSet(HolderSet.direct(Holder.direct(trade)),
                net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.exactly(7), true, sequence);
        var augmented = NativeTrades.augment(Optional.of(original), List.of(decoded)).orElseThrow();
        check(augmented.trades().size() == 2 && augmented.trades().get(0).value() == trade, "native/addon selection order retained");
        check(original.trades().size() == 1, "augmentation never mutates datapack trade sets");
        check(augmented.amount() == original.amount() && augmented.allowDuplicates() && augmented.randomSequence().equals(sequence), "selection amount, duplicate policy and random sequence preserved");
        check(NativeTrades.augment(Optional.of(original), List.of()).orElseThrow() == original, "unrelated trade sets unchanged");
        var key = ResourceKey.create(Registries.TRADE_SET, Identifier.parse("fixture:nurse/1"));
        NativeTrades.register(key, List.of(trade));
        check(NativeTrades.resolve(key, Optional.empty()).orElseThrow().trades().size() == 1, "nurse fallback supplies native offers without registry mutation");
        check(!NativeTrades.resolve(key, Optional.empty()).orElseThrow().allowDuplicates(), "nurse fallback retains two distinct offers policy");

        var part = new net.minecraft.client.model.geom.ModelPart(List.of(), Map.of());
        part.setPos(1, 2, 3); part.setRotation(0.2F, 0.3F, 0.4F); part.xScale = 0.5F;
        var deltas = new HashMap<String, RiderPose.Part>(); deltas.put("arm_left", RiderPose.Part.capture(part));
        var pose = new RiderPose(deltas); deltas.clear(); part.setPos(99, 99, 99);
        var target = new net.minecraft.client.model.geom.ModelPart(List.of(), Map.of()); target.x = -5F;
        pose.apply("arm_left", target);
        check(target.x == -4F && target.y == 2F && target.z == 3F, "rider snapshot uses immutable position deltas");
        check(target.xRot == 0.2F && target.xScale == 0.5F, "rider rotations/scales retained");
        check(!RiderPose.EMPTY.mounted(), "empty reused render state cannot apply previous player's pose");
        var seat = new org.joml.Matrix4f().translate(1, 2, 3);
        var data = new RiderRenderData(pose, seat, 0.5F); seat.identity(); data.seat().identity();
        check(data.seat().m30() == 1F && data.recall() == 0.5F, "seat transform defensively copied on input and output");
        var root = new RiderPose(Map.of("body", new RiderPose.Part(16, -10, 32, 0, 0, 0, 1, 1, 1)));
        var matrices = new PoseStack(); root.applyRoot(matrices);
        check(matrices.last().pose().m30() == -1F && matrices.last().pose().m31() == -1.25F && matrices.last().pose().m32() == 2F, "legacy root sign/offset and sixteenth scale retained");

        check(net.minecraft.client.gui.Font.class.getDeclaredClasses().length > 0, "font class loads without a renderer");
        var prepared = Class.forName("net.minecraft.client.gui.Font$PreparedTextBuilder");
        check(prepared.getDeclaredMethod("accept", int.class, net.minecraft.network.chat.Style.class, int.class).getReturnType() == boolean.class, "text-clipping hook has exact native descriptor");
        var settings = new net.minecraft.stats.RecipeBookSettings();
        var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {
            net.minecraft.stats.RecipeBookSettings.STREAM_CODEC.encode(buffer, settings);
            check(buffer.readableBytes() == 8, "native recipe wire remains exactly four boolean pairs");
            net.minecraft.stats.RecipeBookSettings.STREAM_CODEC.decode(buffer);
            check(!buffer.isReadable(), "native recipe codec consumes exactly its frame");
        } finally { buffer.release(); }
        return checks;
    }
}
