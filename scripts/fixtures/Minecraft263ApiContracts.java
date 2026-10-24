/*
 * Copyright (C) 2026 IdeasInTheWorks
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Compiles against the real target classes; it does not launch Minecraft. */
final class Minecraft263ApiContracts {
    public static void main(String[] args) {
        String[] values = {"cobblemon:pokemon", "cobblemon:storage/box_01", "minecraft:stone",
                "cobblemon:" + "long_path_".repeat(35)};
        for (String value : values) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                Identifier id = Identifier.parse(value);
                buffer.writeIdentifier(id);
                byte[] expected = value.getBytes(StandardCharsets.UTF_8);
                int length = buffer.readVarInt();
                if (length != expected.length) throw new AssertionError("Identifier length changed");
                byte[] actual = new byte[length];
                buffer.readBytes(actual);
                if (!Arrays.equals(expected, actual) || buffer.readableBytes() != 0)
                    throw new AssertionError("Identifier wire format changed");
                buffer.readerIndex(0);
                if (!id.equals(buffer.readIdentifier()) || buffer.readableBytes() != 0)
                    throw new AssertionError("Identifier round trip failed");
            } finally {
                buffer.release();
            }
        }
        System.out.println("4 real Minecraft identifier wire-format/round-trip checks passed; no game launch");
    }

    static <T extends Entity> EntityType<T> entity(EntityType.Builder<T> builder, Identifier id) {
        return builder.build(ResourceKey.create(Registries.ENTITY_TYPE, id));
    }

    static BlockBehaviour.Properties collision(BlockBehaviour.Properties properties) {
        return properties.noCollision();
    }

    static Identifier payload(FriendlyByteBuf buffer, Identifier id) {
        buffer.writeIdentifier(id);
        return buffer.readIdentifier();
    }

    static boolean tag(EntityType<?> type, TagKey<EntityType<?>> tag) {
        return type.builtInRegistryHolder().is(tag);
    }
}
