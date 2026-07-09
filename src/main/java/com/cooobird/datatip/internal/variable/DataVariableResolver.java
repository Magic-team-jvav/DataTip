package com.cooobird.datatip.internal.variable;

import com.cooobird.datatip.api.condition.ItemComponentMatcher;
import net.minecraft.world.item.ItemStack;

/**
 * 解析 {component:path} 和 {custom_data:path} 变量。
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
                result.append(resolveOne(stack, dataVariable));
                i = dataVariable.endIndex() + 1;
                continue;
            }

            result.append(text.charAt(i));
            i++;
        }
        return result.toString();
    }

    private static DataVariable read(String text, int start) {
        if (text.charAt(start) != '{') {
            return null;
        }

        int end = text.indexOf('}', start);
        if (end <= start) {
            return null;
        }

        String body = text.substring(start + 1, end);
        int separator = body.indexOf(':');
        if (separator <= 0) {
            return null;
        }

        String prefix = body.substring(0, separator);
        if (!prefix.equals("component") && !prefix.equals("custom_data")) {
            return null;
        }

        return new DataVariable(prefix, body.substring(separator + 1), end);
    }

    private static String resolveOne(ItemStack stack, DataVariable variable) {
        return switch (variable.prefix()) {
            case "component" -> ItemComponentMatcher.getComponentValue(stack, variable.path());
            case "custom_data" -> {
                String value = ItemComponentMatcher.getCustomDataValue(stack, variable.path());
                yield value != null ? value : "";
            }
            default -> "";
        };
    }

    private record DataVariable(String prefix, String path, int endIndex) {
    }
}
