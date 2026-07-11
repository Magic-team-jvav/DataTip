package com.cooobird.datatip.api.expression;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表达式变量替换工具。
 */
final class ExpressionVariables {
    private ExpressionVariables() {
    }

    /**
     * 替换表达式中的变量。
     * 支持两种格式：{variable} 和 variable（不带大括号）。
     *
     * @param expression 表达式字符串
     * @param variables  变量映射
     * @return 替换后的字符串
     */
    static String resolve(String expression, Map<String, String> variables) {
        String result = expression;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String varName = entry.getKey();
            String value = entry.getValue();

            String placeholderWithBraces = "{" + varName + "}";
            if (result.contains(placeholderWithBraces)) {
                result = result.replace(placeholderWithBraces, value);
            }

            // 只替换完整单词，避免部分匹配。
            String regex = "\\b" + Pattern.quote(varName) + "\\b";
            result = result.replaceAll(regex, Matcher.quoteReplacement(value));
        }
        return result;
    }
}
