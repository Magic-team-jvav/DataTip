package com.cooobird.datatip.api.session;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * 使用物品身份、数量和不可变 NBT 快照构成的结构化指纹。
 */
public final class ItemStackFingerprint {
    private final Item item;
    private final int count;
    private final CompoundTag tag;
    private final int hash;

    private ItemStackFingerprint(
        Item item,
        int count,
        CompoundTag tag
    ) {
        this.item = Objects.requireNonNull(item, "item");
        this.count = count;
        this.tag = tag;
        this.hash = 31 * (31 * System.identityHashCode(item) + count)
            + Objects.hashCode(tag);
    }

    public static ItemStackFingerprint capture(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        return new ItemStackFingerprint(
            stack.getItem(),
            stack.getCount(),
            stack.getTag() != null ? stack.getTag().copy() : null
        );
    }

    public Item item() {
        return item;
    }

    public int count() {
        return count;
    }

    public CompoundTag tag() {
        return tag != null ? tag.copy() : null;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ItemStackFingerprint fingerprint
            && item == fingerprint.item
            && count == fingerprint.count
            && Objects.equals(tag, fingerprint.tag);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
