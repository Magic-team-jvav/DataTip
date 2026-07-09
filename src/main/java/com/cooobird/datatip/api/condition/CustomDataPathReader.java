package com.cooobird.datatip.api.condition;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 读取 minecraft:custom_data 组件中的路径值。
 */
final class CustomDataPathReader {
    private CustomDataPathReader() {
    }

    static boolean has(ItemStack stack, String path) {
        return get(stack, path) != null;
    }

    @Nullable
    static String get(ItemStack stack, String path) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (customData.isEmpty()) {
            return null;
        }

        Tag tag = find(customData.copyTag(), path);
        return tag != null ? tag.getAsString() : null;
    }

    static String fullValue(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.isEmpty() ? "" : customData.copyTag().toString();
    }

    static boolean matches(ItemStack stack, @Nullable Map<String, Object> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }

        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            String actual = get(stack, entry.getKey());
            if (!matchesValue(actual, entry.getValue())) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    private static Tag find(CompoundTag root, String path) {
        String[] parts = path.split("\\.");
        CompoundTag current = root;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (!current.contains(part)) {
                return null;
            }

            Tag tag = current.get(part);
            if (i == parts.length - 1) {
                return tag;
            }
            if (!(tag instanceof CompoundTag compound)) {
                return null;
            }
            current = compound;
        }

        return null;
    }

    private static boolean matchesValue(@Nullable String actual, Object expected) {
        if (actual == null) {
            return expected == null;
        }
        return actual.equals(String.valueOf(expected));
    }
}
