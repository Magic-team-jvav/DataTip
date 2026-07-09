package com.cooobird.datatip.api;

import com.cooobird.datatip.api.util.ColorParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析上下文。
 * <p>
 * 每次资源重载时创建新的 ParseContext 实例，收集解析过程中的警告，并提供常用 JSON 取值方法。
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
     * 是否存在警告。
     *
     * @return true 表示存在警告
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * 解析嵌套的 TipContent。
     *
     * @param json 包含 type 字段的 JSON 对象
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
     * 从对象里的指定键解析内容数组。
     *
     * @param json 父 JSON 对象
     * @param key  数组键名
     * @return 解析后的内容列表，键不存在时返回空列表
     */
    public List<TipContent> parseContentArray(JsonObject json, String key) {
        if (!json.has(key) || !json.get(key).isJsonArray()) {
            return List.of();
        }
        return parseContentArray(json.getAsJsonArray(key));
    }

    public String getString(JsonObject json, String key, String defaultValue) {
        JsonElement element = getPrimitive(json, key);
        return element != null ? element.getAsString() : defaultValue;
    }

    @Nullable
    public String getStringOrNull(JsonObject json, String key) {
        return getString(json, key, null);
    }

    public int getInt(JsonObject json, String key, int defaultValue) {
        JsonElement element = getPrimitive(json, key);
        if (element != null && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsInt();
        }
        return defaultValue;
    }

    public float getFloat(JsonObject json, String key, float defaultValue) {
        JsonElement element = getPrimitive(json, key);
        if (element != null && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsFloat();
        }
        return defaultValue;
    }

    public boolean getBoolean(JsonObject json, String key, boolean defaultValue) {
        JsonElement element = getPrimitive(json, key);
        if (element != null && element.getAsJsonPrimitive().isBoolean()) {
            return element.getAsBoolean();
        }
        return defaultValue;
    }

    public int getColor(JsonObject json, String key, int defaultValue) {
        JsonElement element = getPrimitive(json, key);
        if (element != null) {
            return parseColor(element.getAsString(), defaultValue);
        }
        return defaultValue;
    }

    /**
     * 解析颜色字符串为 ARGB 颜色值。
     *
     * @param colorStr     颜色字符串
     * @param defaultValue 默认颜色
     * @return ARGB 颜色值
     */
    public static int parseColor(String colorStr, int defaultValue) {
        return ColorParser.parse(colorStr, defaultValue);
    }

    @Nullable
    public JsonObject getObject(JsonObject json, String key) {
        if (!json.has(key)) return null;
        JsonElement element = json.get(key);
        return element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    @Nullable
    public JsonArray getArray(JsonObject json, String key) {
        if (!json.has(key)) return null;
        JsonElement element = json.get(key);
        return element.isJsonArray() ? element.getAsJsonArray() : null;
    }

    public boolean has(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull();
    }

    @Nullable
    private JsonElement getPrimitive(JsonObject json, String key) {
        if (!json.has(key)) return null;
        JsonElement element = json.get(key);
        return element.isJsonPrimitive() ? element : null;
    }
}
