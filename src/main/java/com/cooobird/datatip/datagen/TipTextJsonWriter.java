package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.content.BaseTextContent;
import com.cooobird.datatip.api.content.TextContent;
import com.cooobird.datatip.api.content.TextContentDefaults;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * 文本内容 JSON 写出器。
 */
final class TipTextJsonWriter {
    private TipTextJsonWriter() {
    }

    static void write(JsonObject json, TextContent textContent) {
        json.addProperty("type", "text");
        if (textContent.langStyledText() != null && !textContent.langStyledText().isEmpty()) {
            JsonObject languages = new JsonObject();
            for (Map.Entry<String, BaseTextContent.LangStyle> entry : textContent.langStyledText().entrySet()) {
                BaseTextContent.LangStyle style = entry.getValue();
                JsonObject value = new JsonObject();
                value.addProperty("text", style.text());
                value.addProperty("color", DatagenJsonUtils.colorToHex(style.color()));
                if (style.bold()) value.addProperty("bold", true);
                if (style.italic()) value.addProperty("italic", true);
                if (style.underlined()) value.addProperty("underlined", true);
                if (style.strikethrough()) value.addProperty("strikethrough", true);
                languages.add(entry.getKey(), value);
            }
            json.add("text", languages);
        } else if (textContent.langText() != null && !textContent.langText().isEmpty()) {
            JsonObject languages = new JsonObject();
            textContent.langText().forEach(languages::addProperty);
            json.add("text", languages);
        } else if (textContent.text() != null) {
            json.addProperty("text", textContent.text());
        }
        if (textContent.color() != 0xFFFFFF)
            json.addProperty("color", DatagenJsonUtils.colorToHex(textContent.color()));
        if (textContent.font() != null)
            json.addProperty("font", textContent.font().toString());
        if (textContent.bold()) json.addProperty("bold", true);
        if (textContent.italic()) json.addProperty("italic", true);
        if (textContent.underlined()) json.addProperty("underlined", true);
        if (textContent.strikethrough()) json.addProperty("strikethrough", true);
        if (!textContent.shadow()) json.addProperty("shadow", false);
        if (textContent.align() != BaseTextContent.TextAlign.LEFT)
            json.addProperty("align", textContent.align().toString().toLowerCase());
        if (textContent.lineHeight() != TextContentDefaults.lineHeight())
            json.addProperty("lineHeight", textContent.lineHeight());
        if (textContent.shift()) json.addProperty("shift", true);
        if (textContent.maxWidth() > 0) json.addProperty("maxWidth", textContent.maxWidth());
    }
}
