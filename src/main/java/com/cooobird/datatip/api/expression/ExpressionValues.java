package com.cooobird.datatip.api.expression;

/**
 * 表达式运行时值转换工具。
 */
final class ExpressionValues {
    private ExpressionValues() {
    }

    static boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean bool) return bool;
        if (value instanceof Number number) return number.doubleValue() != 0;
        if (value instanceof String str) return !str.isEmpty() && !str.equals("false") && !str.equals("0");
        return true;
    }

    static double toDouble(Object value) {
        if (value == null) return 0;
        if (value instanceof Number number) return number.doubleValue();
        if (value instanceof String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    static String toStringValue(Object value) {
        if (value == null) return "null";
        return value.toString();
    }
}
