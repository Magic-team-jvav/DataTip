package com.cooobird.datatip.internal.variable;

import com.cooobird.datatip.internal.nbt.NbtPathReader;
import net.minecraft.world.item.ItemStack;

/**
 * 解析 Forge 1.20.1 的 {nbt:path} 变量。
 */
public final class DataVariableResolver {
    private DataVariableResolver() {
    }

    public static String resolve(String text, ItemStack stack) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            DataVariable dataVariable = read(text, i);
            if (dataVariable != null) {
                result.append(NbtPathReader.readAsString(stack, dataVariable.path()));
                i = dataVariable.endIndex() + 1;
                continue;
            }

            result.append(text.charAt(i));
            i++;
        }
        return result.toString();
    }

    private static DataVariable read(String text, int start) {
        if (!text.startsWith("{nbt:", start)) {
            return null;
        }

        int end = text.indexOf('}', start);
        if (end <= start + 5) {
            return null;
        }

        return new DataVariable(text.substring(start + 5, end), end);
    }

    private record DataVariable(String path, int endIndex) {
    }
}
