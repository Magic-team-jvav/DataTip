package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.text.LocalizedText;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.Nullable;

/**
 * 多语言文本 JSON 写出工具。
 */
final class LocalizedTextJsonWriter {
    private LocalizedTextJsonWriter() {
    }

    static void add(JsonObject json, String field, @Nullable Component component) {
        if (component != null) json.add(field, toJson(component));
    }

    static void add(JsonObject json, String field, @Nullable LocalizedText text) {
        if (text != null) json.add(field, toJson(text));
    }

    private static JsonElement toJson(Component component) {
        if (component instanceof LocalizedText localized) return toJson(localized);
        if (component.getContents() instanceof TranslatableContents translatable) {
            JsonObject object = new JsonObject();
            object.addProperty("trans", translatable.getKey());
            return object;
        }
        return componentValue(component);
    }

    private static JsonElement toJson(LocalizedText text) {
        if (text.translations().isEmpty()) return toJson(text.fallback());

        JsonObject languages = new JsonObject();
        text.translations().forEach((language, component) ->
            languages.add(language, componentValue(component)));
        return languages;
    }

    private static JsonElement componentValue(Component component) {
        Style style = component.getStyle();
        if (style.equals(Style.EMPTY)) return new JsonPrimitive(component.getString());

        JsonObject object = new JsonObject();
        object.addProperty("text", component.getString());
        if (style.getColor() != null) {
            object.addProperty("color", String.format("#%06X", style.getColor().getValue()));
        }
        if (style.isBold()) object.addProperty("bold", true);
        if (style.isItalic()) object.addProperty("italic", true);
        if (style.isUnderlined()) object.addProperty("underlined", true);
        if (style.isStrikethrough()) object.addProperty("strikethrough", true);
        return object;
    }
}
