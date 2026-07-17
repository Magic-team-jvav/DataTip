package com.cooobird.datatip.api.session;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * 使用物品身份、数量和不可变组件补丁构成的结构化指纹。
 */
public final class ItemStackFingerprint {
    private final Item item;
    private final int count;
    private final DataComponentPatch components;
    private final int hash;

    private ItemStackFingerprint(
        Item item,
        int count,
        DataComponentPatch components
    ) {
        this.item = Objects.requireNonNull(item, "item");
        this.count = count;
        this.components = Objects.requireNonNull(components, "components");
        this.hash = 31 * (31 * System.identityHashCode(item) + count)
            + components.hashCode();
    }

    public static ItemStackFingerprint capture(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        return new ItemStackFingerprint(
            stack.getItem(),
            stack.getCount(),
            stack.getComponentsPatch()
        );
    }

    public Item item() {
        return item;
    }

    public int count() {
        return count;
    }

    public DataComponentPatch components() {
        return components;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ItemStackFingerprint fingerprint
            && item == fingerprint.item
            && count == fingerprint.count
            && components.equals(fingerprint.components);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
