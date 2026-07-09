package com.cooobird.datatip.api.util;

import com.cooobird.datatip.api.expression.ExpressionParser;
import com.cooobird.datatip.internal.variable.*;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

/**
 * 变量解析器。
 * <p>
 * 支持在文本中使用变量占位符，例如 {durability}、{count}、{nbt:path} 和表达式。
 * </p>
 *
 * @author cooobird
 * @see ExpressionParser
 * @since 1.2.0
 */
public class VariableResolver {
    /**
     * 清空变量解析缓存。
     */
    public static void clearCache() {
        VariableCache.clear();
    }

    /**
     * 解析文本中的变量。
     *
     * @param text  包含变量或表达式的文本
     * @param stack 物品栈
     * @return 替换后的文本
     */
    public static String resolve(String text, ItemStack stack) {
        if (text == null || !text.contains("{")) {
            return text;
        }

        String cached = VariableCache.get(text, stack);
        if (cached != null) {
            return cached;
        }

        String result = DataVariableResolver.resolve(text, stack);
        RegisteredVariableResolver.Result registered = RegisteredVariableResolver.resolve(result, stack);
        result = EventVariableResolver.resolve(registered.text(), stack);
        result = ExpressionVariableResolver.resolve(result, registered.variables());

        VariableCache.put(text, stack, result);
        return result;
    }

    /**
     * 注册自定义变量。
     *
     * @param name     变量名，不含大括号
     * @param resolver 解析函数
     */
    public static void registerVariable(String name, Function<ItemStack, String> resolver) {
        VariableRegistry.register(name, resolver);
    }
}
