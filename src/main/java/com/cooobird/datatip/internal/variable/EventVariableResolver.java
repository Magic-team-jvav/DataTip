package com.cooobird.datatip.internal.variable;

import com.cooobird.datatip.api.TipEventManager;
import net.minecraft.world.item.ItemStack;

/**
 * 解析事件监听器提供的变量。
 */
public final class EventVariableResolver {
    private EventVariableResolver() {
    }

    public static String resolve(String text, ItemStack stack) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) == '{') {
                int end = VariableTextScanner.findClosingBrace(text, i);
                if (end > i + 1) {
                    String varName = text.substring(i + 1, end);
                    if (VariableTextScanner.isEventVariableName(varName)) {
                        var event = TipEventManager.fireVariableResolve(varName, stack);
                        if (event.isResolved() && event.getValue() != null) {
                            result.append(event.getValue());
                            i = end + 1;
                            continue;
                        }
                    }
                }
            }
            result.append(text.charAt(i));
            i++;
        }
        return result.toString();
    }
}
