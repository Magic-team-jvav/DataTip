package com.cooobird.datatip.internal.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 读取 Forge 1.20.1 物品 NBT 路径。
 */
public final class NbtPathReader {
    private NbtPathReader() {
    }

    public static boolean has(ItemStack stack, String path) {
        return read(stack, path) != null;
    }

    public static boolean matches(ItemStack stack, Map<String, Object> expectedValues) {
        for (var entry : expectedValues.entrySet()) {
            String actual = readAsString(stack, entry.getKey());
            if (!String.valueOf(entry.getValue()).equals(actual)) {
                return false;
            }
        }
        return true;
    }

    public static String readAsString(ItemStack stack, String path) {
        Object value = read(stack, path);
        return value != null ? String.valueOf(value) : "";
    }

    @Nullable
    public static Object read(ItemStack stack, String path) {
        if (path == null || path.isBlank()) {
            return null;
        }

        Object synthetic = readSynthetic(stack, path);
        if (synthetic != null) {
            return synthetic;
        }

        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return null;
        }

        Tag current = tag;
        for (String segment : path.split("\\.")) {
            current = readSegment(current, segment);
            if (current == null) {
                return null;
            }
        }
        return current.getAsString();
    }

    @Nullable
    private static Object readSynthetic(ItemStack stack, String path) {
        return switch (path) {
            case "Damage" -> stack.getDamageValue();
            case "Count" -> stack.getCount();
            case "RepairCost" -> {
                CompoundTag tag = stack.getTag();
                yield tag != null && tag.contains("RepairCost") ? tag.getInt("RepairCost") : 0;
            }
            default -> null;
        };
    }

    @Nullable
    private static Tag readSegment(Tag current, String segment) {
        int bracket = segment.indexOf('[');
        if (bracket >= 0 && segment.endsWith("]")) {
            String name = segment.substring(0, bracket);
            int index = parseIndex(segment.substring(bracket + 1, segment.length() - 1));
            if (index < 0) {
                return null;
            }

            Tag listTag = readNamedTag(current, name);
            if (!(listTag instanceof ListTag list) || index >= list.size()) {
                return null;
            }
            return list.get(index);
        }

        return readNamedTag(current, segment);
    }

    @Nullable
    private static Tag readNamedTag(Tag current, String name) {
        if (!(current instanceof CompoundTag compound) || !compound.contains(name)) {
            return null;
        }
        return compound.get(name);
    }

    private static int parseIndex(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }
}
