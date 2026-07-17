package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.text.LocalizedText;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 解析标签、标题等通用多语言文本字段。
 */
public final class LocalizedTextParser {
    private LocalizedTextParser() {
    }

    public static LocalizedText parse(JsonObject json, String field, ParseContext context) {
        return json.has(field) ? parse(json.get(field), context) : LocalizedText.empty();
    }

    public static LocalizedText parse(@Nullable JsonElement element, ParseContext context) {
        if (element == null || element.isJsonNull()) return LocalizedText.empty();
        if (element.isJsonPrimitive()) return LocalizedText.literal(element.getAsString());
        if (!element.isJsonObject()) return LocalizedText.empty();

        JsonObject object = element.getAsJsonObject();
        if (object.has("trans")) {
            throw new IllegalArgumentException("Property 'trans' is not supported; use 'translate'");
        }
        if (object.has("translate")) {
            if (object.size() != 1) {
                throw new IllegalArgumentException("Translated localized text must contain only 'translate'");
            }
            JsonElement translate = object.get("translate");
            if (!translate.isJsonPrimitive() || !translate.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException("Property 'translate' must be a string");
            }
            return LocalizedText.translatable(translate.getAsString());
        }

        Map<String, Component> translations = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            Component text = parseLanguageValue(entry.getValue(), context);
            if (text != null) translations.put(entry.getKey(), text);
        }
        return LocalizedText.languages(translations);
    }

    @Nullable
    private static Component parseLanguageValue(@Nullable JsonElement element, ParseContext context) {
        if (element == null || element.isJsonNull()) return null;
        if (element.isJsonPrimitive()) return Component.literal(element.getAsString());
        if (!element.isJsonObject()) return null;

        JsonObject object = element.getAsJsonObject();
        String text = context.getString(object, "text", "");
        var component = Component.literal(text);
        if (object.has("color"))
            component.withStyle(style -> style.withColor(context.getColor(object, "color", 0xFFFFFF)));
        if (context.getBoolean(object, "bold", false)) component.withStyle(style -> style.withBold(true));
        if (context.getBoolean(object, "italic", false)) component.withStyle(style -> style.withItalic(true));
        if (context.getBoolean(object, "underlined", false)) component.withStyle(style -> style.withUnderlined(true));
        if (context.getBoolean(object, "strikethrough", false))
            component.withStyle(style -> style.withStrikethrough(true));
        return component;
    }
}
