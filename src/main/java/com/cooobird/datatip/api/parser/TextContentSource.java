package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.content.BaseTextContent;
import com.cooobird.datatip.api.util.ColorParser;
import com.cooobird.datatip.config.DatatipConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * TextContent 的文本来源。
 */
record TextContentSource(
    String text,
    Map<String, String> langText,
    Map<String, BaseTextContent.LangStyle> langStyledText
) {
    static TextContentSource parse(JsonObject json) {
        JsonElement textElement = json.get("text");
        if (textElement == null) {
            return empty();
        }

        if (textElement.isJsonPrimitive()) {
            return new TextContentSource(textElement.getAsString(), null, null);
        }

        if (textElement.isJsonObject()) {
            return parseLanguageObject(textElement.getAsJsonObject());
        }

        return empty();
    }

    boolean hasText() {
        return text != null;
    }

    boolean hasLanguages() {
        return langText != null && !langText.isEmpty();
    }

    boolean hasStyledLanguages() {
        return langStyledText != null && !langStyledText.isEmpty();
    }

    private static TextContentSource parseLanguageObject(JsonObject langObj) {
        if (!isMultiLanguageObject(langObj)) {
            return empty();
        }

        if (hasStyledLanguages(langObj)) {
            return new TextContentSource(null, null, parseStyledLanguages(langObj));
        }

        return new TextContentSource(null, parsePlainLanguages(langObj), null);
    }

    private static boolean isMultiLanguageObject(JsonObject langObj) {
        for (String key : langObj.keySet()) {
            if (key.contains("_")) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasStyledLanguages(JsonObject langObj) {
        for (Map.Entry<String, JsonElement> entry : langObj.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, String> parsePlainLanguages(JsonObject langObj) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : langObj.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getAsString());
        }
        return result;
    }

    private static Map<String, BaseTextContent.LangStyle> parseStyledLanguages(JsonObject langObj) {
        Map<String, BaseTextContent.LangStyle> result = new HashMap<>();
        int defaultColor = DatatipConfig.DEFAULT_COLOR.get();

        for (Map.Entry<String, JsonElement> entry : langObj.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                result.put(entry.getKey(), parseStyledLanguage(entry.getValue().getAsJsonObject(), defaultColor));
            } else if (entry.getValue().isJsonPrimitive()) {
                result.put(entry.getKey(), new BaseTextContent.LangStyle(
                    entry.getValue().getAsString(), defaultColor, false, false, false, false));
            }
        }

        return result;
    }

    private static BaseTextContent.LangStyle parseStyledLanguage(JsonObject styleObj, int defaultColor) {
        String langTextStr = styleObj.has("text") ? styleObj.get("text").getAsString() : "";
        int langColor = styleObj.has("color") ? ColorParser.parse(styleObj.get("color").getAsString(), defaultColor) : defaultColor;
        boolean langBold = styleObj.has("bold") && styleObj.get("bold").getAsBoolean();
        boolean langItalic = styleObj.has("italic") && styleObj.get("italic").getAsBoolean();
        boolean langUnderlined = styleObj.has("underlined") && styleObj.get("underlined").getAsBoolean();
        boolean langStrikethrough = styleObj.has("strikethrough") && styleObj.get("strikethrough").getAsBoolean();
        BaseTextContent.TextAlign langAlign = parseAlign(styleObj);
        boolean langShift = styleObj.has("shift") && styleObj.get("shift").getAsBoolean();

        return new BaseTextContent.LangStyle(
            langTextStr, langColor, langBold, langItalic, langUnderlined, langStrikethrough, langAlign, langShift);
    }

    private static BaseTextContent.TextAlign parseAlign(JsonObject styleObj) {
        if (styleObj.has("align")) {
            String align = styleObj.get("align").getAsString();
            if ("center".equals(align)) return BaseTextContent.TextAlign.CENTER;
            if ("right".equals(align)) return BaseTextContent.TextAlign.RIGHT;
        }
        return BaseTextContent.TextAlign.LEFT;
    }

    private static TextContentSource empty() {
        return new TextContentSource(null, null, null);
    }
}
