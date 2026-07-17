package com.cooobird.datatip.internal.variable;

import com.cooobird.datatip.api.condition.ComponentReaderRegistry;
import com.cooobird.datatip.internal.nbt.NbtPathReader;
import net.minecraft.world.item.ItemStack;

/**
 * 解析 Forge 1.20.1 的物品数据变量。
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
                String value = switch (dataVariable.source()) {
                    case NBT -> NbtPathReader.readAsString(stack, dataVariable.path());
                    case COMPONENT -> ComponentReaderRegistry.read(stack, dataVariable.path());
                };
                if (value != null) result.append(value);
                i = dataVariable.endIndex() + 1;
                continue;
            }

            result.append(text.charAt(i));
            i++;
        }
        return result.toString();
    }

    private static DataVariable read(String text, int start) {
        DataSource source;
        int pathStart;
        if (text.startsWith("{nbt:", start)) {
            source = DataSource.NBT;
            pathStart = start + 5;
        } else if (text.startsWith("{custom_data:", start)) {
            source = DataSource.NBT;
            pathStart = start + 13;
        } else if (text.startsWith("{component:", start)) {
            source = DataSource.COMPONENT;
            pathStart = start + 11;
        } else {
            return null;
        }

        int end = text.indexOf('}', start);
        if (end <= pathStart) {
            return null;
        }

        return new DataVariable(text.substring(pathStart, end), end, source);
    }

    private record DataVariable(String path, int endIndex, DataSource source) {
    }

    private enum DataSource {
        NBT,
        COMPONENT
    }
}
