package com.cooobird.datatip.api.expression;

/**
 * 表达式字符读取器。
 */
final class ExpressionReader {
    private final String expression;
    private int pos;

    ExpressionReader(String expression) {
        this.expression = expression.trim();
        this.pos = 0;
    }

    char peek() {
        if (pos >= expression.length()) {
            return '\0';
        }
        return expression.charAt(pos);
    }

    char peekNext() {
        if (pos + 1 >= expression.length()) {
            return '\0';
        }
        return expression.charAt(pos + 1);
    }

    char advance() {
        if (pos >= expression.length()) {
            return '\0';
        }
        return expression.charAt(pos++);
    }

    void skipWhitespace() {
        while (pos < expression.length() && Character.isWhitespace(peek())) {
            advance();
        }
    }

    String readString() {
        char quote = advance();
        StringBuilder sb = new StringBuilder();

        while (pos < expression.length() && peek() != quote) {
            if (peek() == '\\') {
                advance();
                if (pos < expression.length()) {
                    sb.append(advance());
                }
            } else {
                sb.append(advance());
            }
        }

        if (pos < expression.length()) {
            advance();
        }

        return sb.toString();
    }

    Object readNumber() {
        int start = pos;
        boolean hasDot = false;

        while (pos < expression.length() && (Character.isDigit(peek()) || peek() == '.')) {
            if (peek() == '.') {
                if (hasDot) {
                    break;
                }
                hasDot = true;
            }
            advance();
        }

        String numStr = expression.substring(start, pos);
        if (hasDot) {
            return Double.parseDouble(numStr);
        }
        return Integer.parseInt(numStr);
    }

    Object readIdentifier() {
        int start = pos;

        while (pos < expression.length() && (Character.isLetterOrDigit(peek()) || peek() == '_')) {
            advance();
        }

        String identifier = expression.substring(start, pos);

        if (identifier.equals("true")) return true;
        if (identifier.equals("false")) return false;
        if (identifier.equals("null")) return null;

        return identifier;
    }
}
