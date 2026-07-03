package com.cooobird.datatip.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析上下文。
 * 提供 JSON 解析的便捷方法，支持嵌套内容解析。
 * <p>
 * 每次资源重载时创建新的 ParseContext 实例，收集解析过程中的警告。
 * </p>
 *
 * @author cooobird
 * @since 1.2.0
 */
public class ParseContext {

    private final List<String> warnings = new ArrayList<>();

    /**
     * 添加解析警告。
     *
     * @param warning 警告信息
     */
    public void addWarning(String warning) {
        warnings.add(warning);
    }

    /**
     * 获取所有解析警告。
     *
     * @return 警告列表
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * 是否有任何警告。
     *
     * @return true 存在警告
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * 解析嵌套的 TipContent。
     *
     * @param json 包含 "type" 字段的 JSON 对象
     * @return 解析后的内容，失败返回 null
     */
    @Nullable
    public TipContent parseContent(JsonObject json) {
        return TipContentRegistry.parse(json, this);
    }

    /**
     * 解析内容数组。
     *
     * @param json TipContent JSON 对象数组
     * @return 解析后的内容列表
     */
    public List<TipContent> parseContentArray(JsonArray json) {
        List<TipContent> result = new ArrayList<>();
        for (JsonElement element : json) {
            if (element.isJsonObject()) {
                TipContent content = parseContent(element.getAsJsonObject());
                if (content != null) {
                    result.add(content);
                }
            }
        }
        return result;
    }

    /**
     * 解析内容数组（从对象中的指定键）。
     *
     * @param json 父 JSON 对象
     * @param key  数组键名
     * @return 解析后的内容列表，键不存在返回空列表
     */
    public List<TipContent> parseContentArray(JsonObject json, String key) {
        if (!json.has(key) || !json.get(key).isJsonArray()) {
            return List.of();
        }
        return parseContentArray(json.getAsJsonArray(key));
    }

    /**
     * 获取字符串值。
     *
     * @param json         JSON 对象
     * @param key          键名
     * @param defaultValue 默认值
     * @return 字符串值
     */
    public String getString(JsonObject json, String key, String defaultValue) {
        if (!json.has(key)) return defaultValue;
        JsonElement element = json.get(key);
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        return defaultValue;
    }

    /**
     * 获取可空字符串值。
     *
     * @param json JSON 对象
     * @param key  键名
     * @return 字符串值，不存在返回 null
     */
    @Nullable
    public String getStringOrNull(JsonObject json, String key) {
        return getString(json, key, null);
    }

    /**
     * 获取整数值。
     *
     * @param json         JSON 对象
     * @param key          键名
     * @param defaultValue 默认值
     * @return 整数值
     */
    public int getInt(JsonObject json, String key, int defaultValue) {
        if (!json.has(key)) return defaultValue;
        JsonElement element = json.get(key);
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsInt();
        }
        return defaultValue;
    }

    /**
     * 获取浮点值。
     *
     * @param json         JSON 对象
     * @param key          键名
     * @param defaultValue 默认值
     * @return 浮点值
     */
    public float getFloat(JsonObject json, String key, float defaultValue) {
        if (!json.has(key)) return defaultValue;
        JsonElement element = json.get(key);
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsFloat();
        }
        return defaultValue;
    }

    /**
     * 获取布尔值。
     *
     * @param json         JSON 对象
     * @param key          键名
     * @param defaultValue 默认值
     * @return 布尔值
     */
    public boolean getBoolean(JsonObject json, String key, boolean defaultValue) {
        if (!json.has(key)) return defaultValue;
        JsonElement element = json.get(key);
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
            return element.getAsBoolean();
        }
        return defaultValue;
    }

    /**
     * 获取颜色值。
     * 支持格式：
     * - 十六进制: "#FF6600", "FF6600"
     * - 命名色: "gold", "red", "aqua"
     * - ARGB 整数: 0xFFFF6600
     *
     * @param json         JSON 对象
     * @param key          键名
     * @param defaultValue 默认颜色
     * @return ARGB 颜色值
     */
    public int getColor(JsonObject json, String key, int defaultValue) {
        if (!json.has(key)) return defaultValue;
        JsonElement element = json.get(key);
        if (element.isJsonPrimitive()) {
            return parseColor(element.getAsString(), defaultValue);
        }
        return defaultValue;
    }

    /**
     * 解析颜色字符串。
     *
     * @param colorStr     颜色字符串
     * @param defaultValue 默认颜色
     * @return ARGB 颜色值
     */
    public static int parseColor(String colorStr, int defaultValue) {
        if (colorStr == null || colorStr.isEmpty()) return defaultValue;

        // 十六进制格式
        if (colorStr.startsWith("#")) {
            try {
                return (int) Long.parseLong(colorStr.substring(1), 16) | 0xFF000000;
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        // 命名色
        return switch (colorStr.toLowerCase()) {
            case "black" -> 0xFF000000;
            case "dark_blue" -> 0xFF0000AA;
            case "dark_green" -> 0xFF00AA00;
            case "dark_aqua" -> 0xFF00AAAA;
            case "dark_red" -> 0xFFAA0000;
            case "dark_purple" -> 0xFFAA00AA;
            case "gold" -> 0xFFFFAA00;
            case "gray", "grey" -> 0xFFAAAAAA;
            case "dark_gray", "dark_grey" -> 0xFF555555;
            case "blue" -> 0xFF5555FF;
            case "green" -> 0xFF55FF55;
            case "aqua" -> 0xFF55FFFF;
            case "red" -> 0xFFFF5555;
            case "light_purple" -> 0xFFFF55FF;
            case "yellow" -> 0xFFFFFF55;
            case "white" -> 0xFFFFFFFF;
            default -> defaultValue;
        };
    }

    /**
     * 获取嵌套的 JSON 对象。
     *
     * @param json JSON 对象
     * @param key  键名
     * @return JSON 对象，不存在返回 null
     */
    @Nullable
    public JsonObject getObject(JsonObject json, String key) {
        if (!json.has(key)) return null;
        JsonElement element = json.get(key);
        return element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    /**
     * 获取 JSON 数组。
     *
     * @param json JSON 对象
     * @param key  键名
     * @return JSON 数组，不存在返回 null
     */
    @Nullable
    public JsonArray getArray(JsonObject json, String key) {
        if (!json.has(key)) return null;
        JsonElement element = json.get(key);
        return element.isJsonArray() ? element.getAsJsonArray() : null;
    }

    /**
     * 检查键是否存在。
     *
     * @param json JSON 对象
     * @param key  键名
     * @return 是否存在
     */
    public boolean has(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull();
    }
}
