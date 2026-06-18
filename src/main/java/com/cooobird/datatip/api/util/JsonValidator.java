package com.cooobird.datatip.api.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON 验证器。
 * 验证 datatip JSON 格式是否正确。
 *
 * @author cooobird
 * @since 1.2.0
 */
public class JsonValidator {

    /**
     * 验证结果。
     */
    public record ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        public static ValidationResult success() {
            return new ValidationResult(true, List.of(), List.of());
        }

        public static ValidationResult failure(List<String> errors) {
            return new ValidationResult(false, errors, List.of());
        }

        public static ValidationResult withWarnings(List<String> warnings) {
            return new ValidationResult(true, List.of(), warnings);
        }
    }

    /**
     * 验证 datatip JSON。
     */
    public static ValidationResult validate(JsonObject json) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (var entry : json.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            // 验证键格式
            if (!isValidKey(key)) {
                errors.add("Invalid key format: '" + key + "'. Expected item ID, tag (#), or wildcard (*, ?)");
            }

            // 验证值格式
            if (value.isJsonObject()) {
                validateEntry(key, value.getAsJsonObject(), errors, warnings);
            } else if (value.isJsonArray()) {
                // 老版本格式：字符串数组
                validateStringArray(key, value.getAsJsonArray(), errors, warnings);
            } else if (!value.isJsonPrimitive()) {
                errors.add("Invalid value format for '" + key + "': expected object, array, or string");
            }
        }

        if (!errors.isEmpty()) {
            return ValidationResult.failure(errors);
        }
        if (!warnings.isEmpty()) {
            return ValidationResult.withWarnings(warnings);
        }
        return ValidationResult.success();
    }

    /**
     * 验证键格式。
     */
    private static boolean isValidKey(String key) {
        if (key.isEmpty()) return false;

        // 标签格式
        if (key.startsWith("#")) {
            return key.length() > 1 && key.contains(":");
        }

        // 通配符格式
        if (key.contains("*") || key.contains("?")) {
            return key.contains(":");
        }

        // 精确匹配格式
        return key.contains(":");
    }

    /**
     * 验证条目格式。
     */
    private static void validateEntry(String key, JsonObject json, List<String> errors, List<String> warnings) {
        // 检查是否是新版本格式（有 type 字段）
        if (json.has("type")) {
            validateNewFormat(key, json, errors, warnings);
        } else if (json.has("text")) {
            // 老版本格式
            validateLegacyFormat(key, json, errors, warnings);
        } else {
            warnings.add("Entry '" + key + "' has no 'type' or 'text' field, may not render");
        }

        // 验证可选字段
        if (json.has("color") && !json.get("color").isJsonPrimitive()) {
            errors.add("Entry '" + key + "': 'color' must be a string");
        }

        if (json.has("shift") && !json.get("shift").isJsonPrimitive()) {
            errors.add("Entry '" + key + "': 'shift' must be a boolean");
        }

        if (json.has("prepend") && !json.get("prepend").isJsonPrimitive()) {
            errors.add("Entry '" + key + "': 'prepend' must be a boolean");
        }

        if (json.has("conditions") && !json.get("conditions").isJsonObject()) {
            errors.add("Entry '" + key + "': 'conditions' must be an object");
        }
    }

    /**
     * 验证新版本格式。
     */
    private static void validateNewFormat(String key, JsonObject json, List<String> errors, List<String> warnings) {
        String type = json.get("type").getAsString();

        switch (type) {
            case "text" -> {
                if (!json.has("text") && !json.has("trans") && !json.has("langText")) {
                    errors.add("Text content '" + key + "' must have 'text', 'trans', or 'langText' field");
                }
            }
            case "spacer" -> {
                if (json.has("height") && !json.get("height").isJsonPrimitive()) {
                    errors.add("Spacer '" + key + "': 'height' must be a number");
                }
            }
            case "divider" -> {
                // 分割线没有必需字段
            }
            case "item" -> {
                if (!json.has("item")) {
                    errors.add("Item content '" + key + "' must have 'item' field");
                }
            }
            case "progress" -> {
                if (!json.has("progress")) {
                    errors.add("Progress content '" + key + "' must have 'progress' field");
                }
            }
            case "vbox", "hbox" -> {
                if (!json.has("children")) {
                    errors.add("Layout content '" + key + "' must have 'children' field");
                }
            }
            case "carousel" -> {
                if (!json.has("frames")) {
                    errors.add("Carousel content '" + key + "' must have 'frames' field");
                }
            }
            case "typewriter" -> {
                if (!json.has("lines")) {
                    errors.add("Typewriter content '" + key + "' must have 'lines' field");
                }
            }
            case "image" -> {
                if (!json.has("texture")) {
                    errors.add("Image content '" + key + "' must have 'texture' field");
                }
            }
            case "chart" -> {
                if (!json.has("chartType")) {
                    errors.add("Chart content '" + key + "' must have 'chartType' field");
                }
                if (!json.has("entries")) {
                    errors.add("Chart content '" + key + "' must have 'entries' field");
                }
            }
            default -> {
                warnings.add("Unknown content type '" + type + "' in '" + key + "'");
            }
        }
    }

    /**
     * 验证老版本格式。
     */
    private static void validateLegacyFormat(String key, JsonObject json, List<String> errors, List<String> warnings) {
        // 老版本格式：text 字段
        if (!json.has("text")) {
            errors.add("Legacy format '" + key + "' must have 'text' field");
            return;
        }

        // 检查 text 字段格式
        // 老版本格式应该被 LegacyFormatConverter 转换
        warnings.add("Legacy format detected at '" + key + "', will be auto-converted");
    }

    /**
     * 验证字符串数组格式。
     */
    private static void validateStringArray(String key, com.google.gson.JsonArray array, List<String> errors, List<String> warnings) {
        for (int i = 0; i < array.size(); i++) {
            if (!array.get(i).isJsonPrimitive()) {
                errors.add("Array format '" + key + "': element at index " + i + " must be a string");
            }
        }
    }
}
