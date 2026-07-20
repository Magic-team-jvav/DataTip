package com.cooobird.datatip.internal.legacy;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.content.*;
import com.cooobird.datatip.api.util.ColorParser;
import com.cooobird.datatip.config.DatatipConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * 旧格式转换使用的 TipContent JSON 序列化工具。
 */
final class LegacyTipContentJsonSerializer {
    private static final int FALLBACK_COLOR = 0xFFAAAAAA;

    private LegacyTipContentJsonSerializer() {
    }

    static JsonObject toJson(TipContent content) {
        JsonObject json = new JsonObject();

        switch (content) {
            case TextContent textContent -> writeText(json, textContent);
            case VBoxContent vbox -> writeVBox(json, vbox);
            case SpacerContent spacer -> {
                json.addProperty("type", "spacer");
                json.addProperty("height", spacer.height());
            }
            case DividerContent divider -> {
                json.addProperty("type", "divider");
                json.addProperty("color", ColorParser.toHex(divider.color()));
            }
            case null, default -> {
            }
        }

        return json;
    }

    private static void writeText(JsonObject json, TextContent textContent) {
        json.addProperty("type", "text");
        if (textContent.langStyledText() != null && !textContent.langStyledText().isEmpty()) {
            writeStyledLangText(json, textContent);
        } else if (textContent.langText() != null && !textContent.langText().isEmpty()) {
            writeLangText(json, textContent);
        } else if (textContent.text() != null) {
            json.addProperty("text", textContent.text());
        }

        if (textContent.color() != getDefaultColor()) {
            json.addProperty("color", ColorParser.toHex(textContent.color()));
        }
        if (textContent.align() == BaseTextContent.TextAlign.CENTER) {
            json.addProperty("align", "center");
        } else if (textContent.align() == BaseTextContent.TextAlign.RIGHT) {
            json.addProperty("align", "right");
        }
        if (textContent.bold()) json.addProperty("bold", true);
        if (textContent.italic()) json.addProperty("italic", true);
        if (textContent.underlined()) json.addProperty("underlined", true);
        if (textContent.strikethrough()) json.addProperty("strikethrough", true);
    }

    private static void writeStyledLangText(JsonObject json, TextContent textContent) {
        JsonObject langObj = new JsonObject();
        for (Map.Entry<String, BaseTextContent.LangStyle> entry : textContent.langStyledText().entrySet()) {
            BaseTextContent.LangStyle style = entry.getValue();
            JsonObject styleObj = new JsonObject();
            styleObj.addProperty("text", style.text());
            if (style.color() != getDefaultColor()) {
                styleObj.addProperty("color", ColorParser.toHex(style.color()));
            }
            if (style.bold()) styleObj.addProperty("bold", true);
            if (style.italic()) styleObj.addProperty("italic", true);
            if (style.underlined()) styleObj.addProperty("underlined", true);
            if (style.strikethrough()) styleObj.addProperty("strikethrough", true);
            langObj.add(entry.getKey(), styleObj);
        }
        json.add("text", langObj);
    }

    private static void writeLangText(JsonObject json, TextContent textContent) {
        JsonObject langObj = new JsonObject();
        for (Map.Entry<String, String> entry : textContent.langText().entrySet()) {
            langObj.addProperty(entry.getKey(), entry.getValue());
        }
        json.add("text", langObj);
    }

    private static void writeVBox(JsonObject json, VBoxContent vbox) {
        json.addProperty("type", "vbox");
        json.addProperty("gap", vbox.gap());
        JsonArray children = new JsonArray();
        for (TipContent child : vbox.children()) {
            children.add(toJson(child));
        }
        json.add("children", children);
    }

    private static int getDefaultColor() {
        try {
            return DatatipConfig.defaultColor();
        } catch (IllegalStateException ignored) {
            return FALLBACK_COLOR;
        }
    }
}
