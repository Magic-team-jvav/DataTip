package com.cooobird.datatip.api.expression;

import java.util.Objects;

/**
 * 表达式运算工具。
 */
final class ExpressionOperations {
    private ExpressionOperations() {
    }

    static boolean or(Object left, Object right) {
        return ExpressionValues.toBoolean(left) || ExpressionValues.toBoolean(right);
    }

    static boolean and(Object left, Object right) {
        return ExpressionValues.toBoolean(left) && ExpressionValues.toBoolean(right);
    }

    static boolean isEqual(Object left, Object right) {
        return Objects.equals(left, right);
    }

    static boolean isNotEqual(Object left, Object right) {
        return !Objects.equals(left, right);
    }

    static boolean compare(Object left, Object right, String operator) {
        double leftValue = ExpressionValues.toDouble(left);
        double rightValue = ExpressionValues.toDouble(right);
        return switch (operator) {
            case ">=" -> leftValue >= rightValue;
            case "<=" -> leftValue <= rightValue;
            case ">" -> leftValue > rightValue;
            case "<" -> leftValue < rightValue;
            default -> false;
        };
    }

    static Object add(Object left, Object right) {
        if (left instanceof String || right instanceof String) {
            return ExpressionValues.toStringValue(left) + ExpressionValues.toStringValue(right);
        }
        return ExpressionValues.toDouble(left) + ExpressionValues.toDouble(right);
    }

    static double subtract(Object left, Object right) {
        return ExpressionValues.toDouble(left) - ExpressionValues.toDouble(right);
    }

    static double multiply(Object left, Object right) {
        return ExpressionValues.toDouble(left) * ExpressionValues.toDouble(right);
    }

    static double divide(Object left, Object right) {
        double divisor = ExpressionValues.toDouble(right);
        if (divisor == 0) {
            return 0;
        }
        return ExpressionValues.toDouble(left) / divisor;
    }

    static boolean not(Object value) {
        return !ExpressionValues.toBoolean(value);
    }

    static double negate(Object value) {
        return -ExpressionValues.toDouble(value);
    }
}
