package com.cooobird.datatip.api.expression;

import java.util.Map;

/**
 * 表达式解析器。
 * <p>
 * 支持变量、比较、逻辑、算术和三元表达式，用于 tooltip 文本中的动态内容。
 * </p>
 *
 * @author cooobird
 * @since 1.2.0
 */
public class ExpressionParser {
    private final ExpressionReader reader;

    /**
     * 创建表达式解析器。
     *
     * @param expression 表达式字符串
     */
    public ExpressionParser(String expression) {
        this.reader = new ExpressionReader(expression);
    }

    /**
     * 解析并求值表达式。
     *
     * @param expression 表达式字符串
     * @param variables  变量映射
     * @return 求值结果，可能是 Boolean、Number 或 String
     */
    public static Object evaluate(String expression, Map<String, String> variables) {
        try {
            String resolved = ExpressionVariables.resolve(expression, variables);
            ExpressionParser parser = new ExpressionParser(resolved);
            return parser.parseExpression();
        } catch (Exception e) {
            return expression;
        }
    }

    private Object parseExpression() {
        reader.skipWhitespace();

        Object condition = parseOr();

        reader.skipWhitespace();
        if (reader.peek() == '?') {
            reader.advance();
            reader.skipWhitespace();

            Object trueValue = parseExpression();
            reader.skipWhitespace();

            if (reader.peek() == ':') {
                reader.advance();
                reader.skipWhitespace();
                Object falseValue = parseExpression();

                return ExpressionValues.toBoolean(condition) ? trueValue : falseValue;
            }
        }

        return condition;
    }

    private Object parseOr() {
        Object left = parseAnd();

        reader.skipWhitespace();
        while (reader.peek() == '|' && reader.peekNext() == '|') {
            reader.advance();
            reader.advance();
            reader.skipWhitespace();
            Object right = parseAnd();
            left = ExpressionOperations.or(left, right);
        }

        return left;
    }

    private Object parseAnd() {
        Object left = parseEquality();

        reader.skipWhitespace();
        while (reader.peek() == '&' && reader.peekNext() == '&') {
            reader.advance();
            reader.advance();
            reader.skipWhitespace();
            Object right = parseEquality();
            left = ExpressionOperations.and(left, right);
        }

        return left;
    }

    private Object parseEquality() {
        Object left = parseComparison();

        reader.skipWhitespace();
        if (reader.peek() == '=' && reader.peekNext() == '=') {
            reader.advance();
            reader.advance();
            reader.skipWhitespace();
            return ExpressionOperations.isEqual(left, parseComparison());
        } else if (reader.peek() == '!' && reader.peekNext() == '=') {
            reader.advance();
            reader.advance();
            reader.skipWhitespace();
            return ExpressionOperations.isNotEqual(left, parseComparison());
        }

        return left;
    }

    private Object parseComparison() {
        Object left = parseAddition();

        reader.skipWhitespace();
        if (reader.peek() == '>' && reader.peekNext() == '=') {
            reader.advance();
            reader.advance();
            reader.skipWhitespace();
            return ExpressionOperations.compare(left, parseAddition(), ">=");
        } else if (reader.peek() == '<' && reader.peekNext() == '=') {
            reader.advance();
            reader.advance();
            reader.skipWhitespace();
            return ExpressionOperations.compare(left, parseAddition(), "<=");
        } else if (reader.peek() == '>') {
            reader.advance();
            reader.skipWhitespace();
            return ExpressionOperations.compare(left, parseAddition(), ">");
        } else if (reader.peek() == '<') {
            reader.advance();
            reader.skipWhitespace();
            return ExpressionOperations.compare(left, parseAddition(), "<");
        }

        return left;
    }

    private Object parseAddition() {
        Object left = parseMultiplication();

        reader.skipWhitespace();
        while (reader.peek() == '+' || reader.peek() == '-') {
            char op = reader.advance();
            reader.skipWhitespace();
            Object right = parseMultiplication();
            left = op == '+'
                ? ExpressionOperations.add(left, right)
                : ExpressionOperations.subtract(left, right);
        }

        return left;
    }

    private Object parseMultiplication() {
        Object left = parseUnary();

        reader.skipWhitespace();
        while (reader.peek() == '*' || reader.peek() == '/') {
            char op = reader.advance();
            reader.skipWhitespace();
            Object right = parseUnary();
            left = op == '*'
                ? ExpressionOperations.multiply(left, right)
                : ExpressionOperations.divide(left, right);
        }

        return left;
    }

    private Object parseUnary() {
        reader.skipWhitespace();

        if (reader.peek() == '!') {
            reader.advance();
            reader.skipWhitespace();
            return ExpressionOperations.not(parseUnary());
        } else if (reader.peek() == '-') {
            reader.advance();
            reader.skipWhitespace();
            return ExpressionOperations.negate(parseUnary());
        }

        return parsePrimary();
    }

    private Object parsePrimary() {
        reader.skipWhitespace();

        char c = reader.peek();
        if (c == '\'' || c == '"') {
            return reader.readString();
        }
        if (Character.isDigit(c) || c == '.') {
            return reader.readNumber();
        }
        if (c == '(') {
            reader.advance();
            reader.skipWhitespace();
            Object value = parseExpression();
            reader.skipWhitespace();
            if (reader.peek() == ')') {
                reader.advance();
            }
            return value;
        }

        return reader.readIdentifier();
    }
}
