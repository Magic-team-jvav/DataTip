package com.cooobird.datatip.internal.condition;

/**
 * 条件判断中的数字解析工具。
 */
final class ConditionNumbers {
    private ConditionNumbers() {
    }

    static Float parseFloatOrNull(String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    static Integer parseIntOrNull(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
