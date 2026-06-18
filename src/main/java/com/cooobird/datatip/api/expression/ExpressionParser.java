package com.cooobird.datatip.api.expression;

import java.util.Map;
import java.util.Objects;

/**
 * 表达式解析器。
 * <p>
 * 支持条件表达式、比较运算、逻辑运算、算术运算。
 * 用于在 tooltip 文本中实现动态内容。
 * </p>
 *
 * <h3>支持的语法</h3>
 * <table border="1">
 *   <tr><th>类型</th><th>语法</th><th>示例</th></tr>
 *   <tr><td>变量</td><td>variable</td><td>durability, max_durability</td></tr>
 *   <tr><td>比较</td><td>&gt;, &lt;, ==, !=, &gt;=, &lt;=</td><td>durability &gt; 100</td></tr>
 *   <tr><td>逻辑</td><td>&amp;&amp;, ||, !</td><td>is_enchanted &amp;&amp; durability &gt; 50</td></tr>
 *   <tr><td>算术</td><td>+, -, *, /</td><td>durability + 10</td></tr>
 *   <tr><td>三元</td><td>condition ? true : false</td><td>durability &gt; 100 ? '良好' : '需要修复'</td></tr>
 *   <tr><td>字符串</td><td>'text' 或 "text"</td><td>'Hello World'</td></tr>
 *   <tr><td>数字</td><td>123, 3.14</td><td>100, 0.5</td></tr>
 * </table>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 简单比较
 * String expr1 = "durability > 100";
 * Object result1 = ExpressionParser.evaluate(expr1, variables);
 * // 结果: true
 *
 * // 三元表达式
 * String expr2 = "durability > 100 ? '良好' : '需要修复'";
 * Object result2 = ExpressionParser.evaluate(expr2, variables);
 * // 结果: "良好"（当耐久 > 100 时）
 *
 * // 逻辑运算
 * String expr3 = "is_enchanted && durability > 50";
 * Object result3 = ExpressionParser.evaluate(expr3, variables);
 * // 结果: true（当附魔且耐久 > 50 时）
 *
 * // 算术运算
 * String expr4 = "durability * 2 + 10";
 * Object result4 = ExpressionParser.evaluate(expr4, variables);
 * // 结果: 510（当耐久 = 250 时）
 * }</pre>
 *
 * <h3>运算符优先级</h3>
 * <ol>
 *   <li>() - 括号</li>
 *   <li>! - 逻辑非</li>
 *   <li>*, / - 乘除</li>
 *   <li>+, - - 加减</li>
 *   <li>&gt;, &lt;, &gt;=, &lt;= - 比较</li>
 *   <li>==, != - 相等</li>
 *   <li>&amp;&amp; - 逻辑与</li>
 *   <li>|| - 逻辑或</li>
 *   <li>? : - 三元条件</li>
 * </ol>
 *
 * @author cooobird
 * @see com.cooobird.datatip.api.util.VariableResolver 变量解析器
 * @since 1.2.0
 */
public class ExpressionParser {

    /**
     * 表达式字符串
     */
    private final String expression;
    /**
     * 当前解析位置
     */
    private int pos;

    /**
     * 创建表达式解析器。
     *
     * @param expression 表达式字符串
     */
    public ExpressionParser(String expression) {
        this.expression = expression.trim();
        this.pos = 0;
    }

    /**
     * 解析并求值表达式。
     *
     * @param expression 表达式字符串
     * @param variables  变量映射（变量名 → 值）
     * @return 求值结果（可能是 Boolean、Double、String）
     */
    public static Object evaluate(String expression, Map<String, String> variables) {
        try {
            // 替换变量
            String resolved = resolveVariables(expression, variables);

            // 解析表达式
            ExpressionParser parser = new ExpressionParser(resolved);
            return parser.parseExpression();
        } catch (Exception e) {
            // 解析失败，返回原始表达式
            return expression;
        }
    }

    /**
     * 替换表达式中的变量。
     * 支持两种格式：{variable} 和 variable（不带大括号）。
     *
     * @param expression 表达式字符串
     * @param variables  变量映射
     * @return 替换后的字符串
     */
    private static String resolveVariables(String expression, Map<String, String> variables) {
        String result = expression;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String varName = entry.getKey();
            String value = entry.getValue();

            // 替换带大括号的变量 {variable}
            String placeholderWithBraces = "{" + varName + "}";
            if (result.contains(placeholderWithBraces)) {
                result = result.replace(placeholderWithBraces, value);
            }

            // 替换不带大括号的变量（用于表达式内部）
            // 只替换完整的单词，避免部分匹配
            String regex = "\\b" + varName + "\\b";
            result = result.replaceAll(regex, value);
        }
        return result;
    }

    /**
     * 解析表达式（处理三元运算符）。
     *
     * @return 解析结果
     */
    private Object parseExpression() {
        skipWhitespace();

        // 尝试解析三元表达式
        Object condition = parseOr();

        skipWhitespace();
        if (peek() == '?') {
            advance(); // 跳过 '?'
            skipWhitespace();

            Object trueValue = parseExpression();
            skipWhitespace();

            if (peek() == ':') {
                advance(); // 跳过 ':'
                skipWhitespace();
                Object falseValue = parseExpression();

                // 条件求值
                boolean cond = toBoolean(condition);
                return cond ? trueValue : falseValue;
            }
        }

        return condition;
    }

    /**
     * 解析逻辑或运算（||）。
     *
     * @return 解析结果
     */
    private Object parseOr() {
        Object left = parseAnd();

        skipWhitespace();
        while (peek() == '|' && peekNext() == '|') {
            advance();
            advance(); // 跳过 '||'
            skipWhitespace();
            Object right = parseAnd();
            left = toBoolean(left) || toBoolean(right);
        }

        return left;
    }

    /**
     * 解析逻辑与运算（&&）。
     *
     * @return 解析结果
     */
    private Object parseAnd() {
        Object left = parseEquality();

        skipWhitespace();
        while (peek() == '&' && peekNext() == '&') {
            advance();
            advance(); // 跳过 '&&'
            skipWhitespace();
            Object right = parseEquality();
            left = toBoolean(left) && toBoolean(right);
        }

        return left;
    }

    /**
     * 解析相等比较（==, !=）。
     *
     * @return 解析结果
     */
    private Object parseEquality() {
        Object left = parseComparison();

        skipWhitespace();
        if (peek() == '=' && peekNext() == '=') {
            advance();
            advance(); // 跳过 '=='
            skipWhitespace();
            Object right = parseComparison();
            return Objects.equals(left, right);
        } else if (peek() == '!' && peekNext() == '=') {
            advance();
            advance(); // 跳过 '!='
            skipWhitespace();
            Object right = parseComparison();
            return !Objects.equals(left, right);
        }

        return left;
    }

    /**
     * 解析比较运算（>, <, >=, <=）。
     *
     * @return 解析结果
     */
    private Object parseComparison() {
        Object left = parseAddition();

        skipWhitespace();
        if (peek() == '>' && peekNext() == '=') {
            advance();
            advance(); // 跳过 '>='
            skipWhitespace();
            Object right = parseAddition();
            return toDouble(left) >= toDouble(right);
        } else if (peek() == '<' && peekNext() == '=') {
            advance();
            advance(); // 跳过 '<='
            skipWhitespace();
            Object right = parseAddition();
            return toDouble(left) <= toDouble(right);
        } else if (peek() == '>') {
            advance(); // 跳过 '>'
            skipWhitespace();
            Object right = parseAddition();
            return toDouble(left) > toDouble(right);
        } else if (peek() == '<') {
            advance(); // 跳过 '<'
            skipWhitespace();
            Object right = parseAddition();
            return toDouble(left) < toDouble(right);
        }

        return left;
    }

    /**
     * 解析加减运算（+, -）。
     *
     * @return 解析结果
     */
    private Object parseAddition() {
        Object left = parseMultiplication();

        skipWhitespace();
        while (peek() == '+' || peek() == '-') {
            char op = advance();
            skipWhitespace();
            Object right = parseMultiplication();

            if (op == '+') {
                // 字符串拼接或数字加法
                if (left instanceof String || right instanceof String) {
                    left = toString(left) + toString(right);
                } else {
                    left = toDouble(left) + toDouble(right);
                }
            } else {
                left = toDouble(left) - toDouble(right);
            }
        }

        return left;
    }

    /**
     * 解析乘除运算（*, /）。
     *
     * @return 解析结果
     */
    private Object parseMultiplication() {
        Object left = parseUnary();

        skipWhitespace();
        while (peek() == '*' || peek() == '/') {
            char op = advance();
            skipWhitespace();
            Object right = parseUnary();

            if (op == '*') {
                left = toDouble(left) * toDouble(right);
            } else {
                double divisor = toDouble(right);
                if (divisor == 0) {
                    return 0; // 除以零返回 0
                }
                left = toDouble(left) / divisor;
            }
        }

        return left;
    }

    /**
     * 解析一元运算（!, -）。
     *
     * @return 解析结果
     */
    private Object parseUnary() {
        skipWhitespace();

        if (peek() == '!') {
            advance(); // 跳过 '!'
            skipWhitespace();
            Object value = parseUnary();
            return !toBoolean(value);
        } else if (peek() == '-') {
            advance(); // 跳过 '-'
            skipWhitespace();
            Object value = parseUnary();
            return -toDouble(value);
        }

        return parsePrimary();
    }

    /**
     * 解析基本值（字符串、数字、括号、标识符）。
     *
     * @return 解析结果
     */
    private Object parsePrimary() {
        skipWhitespace();

        char c = peek();

        // 字符串
        if (c == '\'' || c == '"') {
            return parseString();
        }

        // 数字
        if (Character.isDigit(c) || c == '.') {
            return parseNumber();
        }

        // 括号
        if (c == '(') {
            advance(); // 跳过 '('
            skipWhitespace();
            Object value = parseExpression();
            skipWhitespace();
            if (peek() == ')') {
                advance(); // 跳过 ')'
            }
            return value;
        }

        // 变量或布尔值
        return parseIdentifier();
    }

    /**
     * 解析字符串。
     *
     * @return 字符串值
     */
    private String parseString() {
        char quote = advance(); // 跳过引号
        StringBuilder sb = new StringBuilder();

        while (pos < expression.length() && peek() != quote) {
            if (peek() == '\\') {
                advance(); // 跳过转义字符
                if (pos < expression.length()) {
                    sb.append(advance());
                }
            } else {
                sb.append(advance());
            }
        }

        if (pos < expression.length()) {
            advance(); // 跳过结束引号
        }

        return sb.toString();
    }

    /**
     * 解析数字。
     *
     * @return 数字值（Integer 或 Double）
     */
    private Object parseNumber() {
        int start = pos;
        boolean hasDot = false;

        while (pos < expression.length() && (Character.isDigit(peek()) || peek() == '.')) {
            if (peek() == '.') {
                if (hasDot) break; // 已经有小数点
                hasDot = true;
            }
            advance();
        }

        String numStr = expression.substring(start, pos);
        if (hasDot) {
            return Double.parseDouble(numStr);
        } else {
            return Integer.parseInt(numStr);
        }
    }

    /**
     * 解析标识符（变量或布尔值）。
     *
     * @return 标识符值
     */
    private Object parseIdentifier() {
        int start = pos;

        while (pos < expression.length() && (Character.isLetterOrDigit(peek()) || peek() == '_')) {
            advance();
        }

        String identifier = expression.substring(start, pos);

        // 布尔值
        if (identifier.equals("true")) return true;
        if (identifier.equals("false")) return false;
        if (identifier.equals("null")) return null;

        // 变量（返回字符串形式）
        return identifier;
    }

    // ========== 辅助方法 ==========

    /**
     * 查看当前字符（不移动位置）。
     *
     * @return 当前字符，到达末尾返回 '\0'
     */
    private char peek() {
        if (pos >= expression.length()) return '\0';
        return expression.charAt(pos);
    }

    /**
     * 查看下一个字符（不移动位置）。
     *
     * @return 下一个字符，到达末尾返回 '\0'
     */
    private char peekNext() {
        if (pos + 1 >= expression.length()) return '\0';
        return expression.charAt(pos + 1);
    }

    /**
     * 读取并移动到下一个字符。
     *
     * @return 当前字符
     */
    private char advance() {
        if (pos >= expression.length()) return '\0';
        return expression.charAt(pos++);
    }

    /**
     * 跳过空白字符。
     */
    private void skipWhitespace() {
        while (pos < expression.length() && Character.isWhitespace(peek())) {
            advance();
        }
    }

    /**
     * 将值转换为布尔值。
     *
     * @param value 值
     * @return 布尔值
     */
    private boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.doubleValue() != 0;
        if (value instanceof String s) return !s.isEmpty() && !s.equals("false") && !s.equals("0");
        return true;
    }

    /**
     * 将值转换为双精度浮点数。
     *
     * @param value 值
     * @return 双精度浮点数
     */
    private double toDouble(Object value) {
        if (value == null) return 0;
        if (value instanceof Number n) return n.doubleValue();
        if (value instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * 将值转换为字符串。
     *
     * @param value 值
     * @return 字符串
     */
    private String toString(Object value) {
        if (value == null) return "null";
        return value.toString();
    }
}
