package com.cooobird.datatip.internal.loader;

import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipContentRegistry;
import com.cooobird.datatip.api.condition.ConditionChecker;
import com.cooobird.datatip.api.content.TextContent;
import com.cooobird.datatip.api.content.VBoxContent;
import com.cooobird.datatip.api.util.ColorParser;
import com.cooobird.datatip.config.DatatipConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 单个 datatip 条目的 JSON 解析器。
 */
public final class TipContentEntryParser {
    private static final Logger LOGGER = LogUtils.getLogger();

    public List<TipContent> parseItemContent(String itemKey, JsonElement element, ParseContext context) {
        List<TipContent> result = new ArrayList<>();

        if (element.isJsonArray()) {
            parseLegacyStringArray(element.getAsJsonArray(), result);
        } else if (element.isJsonObject()) {
            parseObjectContent(itemKey, element.getAsJsonObject(), context, result);
        } else if (element.isJsonPrimitive()) {
            result.add(TextContent.of(element.getAsString()));
        }

        return result;
    }

    public List<ConditionChecker.Condition> parseConditions(JsonElement element) {
        List<ConditionChecker.Condition> conditions = new ArrayList<>();

        if (!element.isJsonObject()) {
            return conditions;
        }

        JsonObject obj = element.getAsJsonObject();
        if (!obj.has("conditions") || !obj.get("conditions").isJsonObject()) {
            return conditions;
        }

        JsonObject conditionsObj = obj.getAsJsonObject("conditions");
        for (var entry : conditionsObj.entrySet()) {
            String type = entry.getKey();
            Object value = parseValue(entry.getValue());
            conditions.add(new ConditionChecker.Condition(type, value));
        }

        return conditions;
    }

    public boolean parseBoolean(JsonElement element, String key, boolean defaultValue) {
        if (!element.isJsonObject()) {
            return defaultValue;
        }

        JsonObject obj = element.getAsJsonObject();
        if (!obj.has(key)) {
            return defaultValue;
        }

        JsonElement val = obj.get(key);
        if (val.isJsonPrimitive() && val.getAsJsonPrimitive().isBoolean()) {
            return val.getAsBoolean();
        }

        return defaultValue;
    }

    private void parseLegacyStringArray(JsonArray array, List<TipContent> result) {
        for (JsonElement item : array) {
            if (item.isJsonPrimitive()) {
                result.add(TextContent.of(item.getAsString()));
            }
        }
    }

    private void parseObjectContent(
        String itemKey,
        JsonObject obj,
        ParseContext context,
        List<TipContent> result
    ) {
        if (obj.has("type")) {
            TipContent content = TipContentRegistry.parse(obj, context);
            if (content != null) {
                result.add(content);
            } else {
                LOGGER.warn("Failed to parse content for '{}'", itemKey);
            }
        } else if (obj.has("text")) {
            result.add(parseLegacyFormat(obj));
        }
    }

    /**
     * 解析加载器兼容路径中的老格式内容。
     */
    private TipContent parseLegacyFormat(JsonObject json) {
        VBoxContent vbox = VBoxContent.create();

        int color = getDefaultColor();
        if (json.has("color")) {
            color = parseColor(json.get("color").getAsString());
        }

        JsonElement textElement = json.get("text");
        if (textElement.isJsonArray()) {
            addTextLines(textElement.getAsJsonArray(), vbox, color);
        } else if (textElement.isJsonObject()) {
            addFirstLanguageText(textElement.getAsJsonObject(), vbox, color);
        }

        return vbox;
    }

    private void addTextLines(JsonArray array, VBoxContent vbox, int color) {
        for (JsonElement item : array) {
            if (item.isJsonPrimitive()) {
                vbox.addChild(TextContent.of(item.getAsString(), color));
            }
        }
    }

    private void addFirstLanguageText(JsonObject textObj, VBoxContent vbox, int color) {
        for (var langEntry : textObj.entrySet()) {
            JsonArray lines = langEntry.getValue().getAsJsonArray();
            addTextLines(lines, vbox, color);
            break;
        }
    }

    private Object parseValue(JsonElement element) {
        if (element.isJsonPrimitive()) {
            var prim = element.getAsJsonPrimitive();
            if (prim.isBoolean()) return prim.getAsBoolean();
            if (prim.isNumber()) return prim.getAsNumber();
            return prim.getAsString();
        }
        if (element.isJsonArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonElement item : element.getAsJsonArray()) {
                list.add(parseValue(item));
            }
            return list;
        }
        if (element.isJsonObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (var entry : element.getAsJsonObject().entrySet()) {
                map.put(entry.getKey(), parseValue(entry.getValue()));
            }
            return map;
        }
        return null;
    }

    private int getDefaultColor() {
        try {
            return DatatipConfig.DEFAULT_COLOR.get();
        } catch (IllegalStateException ignored) {
            return 0xFFAAAAAA;
        }
    }

    private int parseColor(String colorStr) {
        return ColorParser.parse(colorStr, ColorParser.WHITE);
    }
}
