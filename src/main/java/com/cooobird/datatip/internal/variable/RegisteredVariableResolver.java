package com.cooobird.datatip.internal.variable;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析通过 VariableResolver.registerVariable 注册的变量。
 */
public final class RegisteredVariableResolver {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b");
    private static final Set<String> REPORTED_FAILURES = ConcurrentHashMap.newKeySet();

    private RegisteredVariableResolver() {
    }

    public static Result resolve(String text, ItemStack stack) {
        Map<String, Function<ItemStack, String>> registeredVariables = VariableRegistry.variables();
        Set<String> requiredVariables = findRequiredVariables(text, registeredVariables.keySet());
        Map<String, String> values = new HashMap<>();

        String result = text;
        for (String varName : requiredVariables) {
            Function<ItemStack, String> resolver = registeredVariables.get(varName);
            if (resolver == null) {
                continue;
            }

            String value;
            try {
                value = resolver.apply(stack);
                REPORTED_FAILURES.remove(varName);
            } catch (RuntimeException e) {
                if (REPORTED_FAILURES.add(varName)) {
                    LOGGER.warn("Failed to resolve DataTip variable '{}'; repeated failures will be suppressed", varName, e);
                }
                value = "";
            }
            String safeValue = value != null ? value : "";
            values.put(varName, safeValue);

            String placeholder = "{" + varName + "}";
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, safeValue);
            }
        }

        return new Result(result, values);
    }

    private static Set<String> findRequiredVariables(String text, Set<String> registeredNames) {
        Set<String> result = new HashSet<>();
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) != '{') {
                i++;
                continue;
            }

            int end = VariableTextScanner.findClosingBrace(text, i);
            if (end <= i + 1) {
                i++;
                continue;
            }

            String body = text.substring(i + 1, end);
            if (registeredNames.contains(body)) {
                result.add(body);
            }
            if (VariableTextScanner.isExpression(body)) {
                collectExpressionVariables(body, registeredNames, result);
            }
            i = end + 1;
        }
        return result;
    }

    private static void collectExpressionVariables(String expression, Set<String> registeredNames, Set<String> result) {
        Matcher matcher = IDENTIFIER_PATTERN.matcher(expression);
        while (matcher.find()) {
            String identifier = matcher.group();
            if (registeredNames.contains(identifier)) {
                result.add(identifier);
            }
        }
    }

    public record Result(String text, Map<String, String> variables) {
    }
}
