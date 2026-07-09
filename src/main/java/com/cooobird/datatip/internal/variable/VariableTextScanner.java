package com.cooobird.datatip.internal.variable;

/**
 * 变量文本扫描工具。
 */
final class VariableTextScanner {
    private VariableTextScanner() {
    }

    static int findClosingBrace(String text, int start) {
        int depth = 0;
        for (int i = start; i < text.length(); i++) {
            if (text.charAt(i) == '{') depth++;
            if (text.charAt(i) == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    static boolean containsExpression(String text) {
        return text.contains("{") && text.contains("}") &&
            (text.contains("?") || text.contains(">") || text.contains("<") ||
                text.contains("==") || text.contains("!="));
    }

    static boolean isExpression(String text) {
        return text.contains("?") ||
            text.contains(">") || text.contains("<") ||
            text.contains("==") || text.contains("!=") ||
            text.contains("&&") || text.contains("||");
    }

    static boolean isEventVariableName(String text) {
        return !text.contains("?")
            && !text.contains(">")
            && !text.contains("<")
            && !text.contains("nbt:");
    }
}
