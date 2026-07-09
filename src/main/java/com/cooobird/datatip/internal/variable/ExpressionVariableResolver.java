package com.cooobird.datatip.internal.variable;

import com.cooobird.datatip.api.expression.ExpressionParser;

import java.util.Map;

/**
 * 解析文本中的表达式变量。
 */
public final class ExpressionVariableResolver {
    private ExpressionVariableResolver() {
    }

    public static String resolve(String text, Map<String, String> variables) {
        if (!VariableTextScanner.containsExpression(text)) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) == '{') {
                int end = VariableTextScanner.findClosingBrace(text, i);
                if (end > i) {
                    String expr = text.substring(i + 1, end);

                    if (VariableTextScanner.isExpression(expr)) {
                        Object value = ExpressionParser.evaluate(expr, variables);
                        result.append(value != null ? value.toString() : "");
                    } else {
                        result.append("{").append(expr).append("}");
                    }

                    i = end + 1;
                    continue;
                }
            }
            result.append(text.charAt(i));
            i++;
        }
        return result.toString();
    }
}
