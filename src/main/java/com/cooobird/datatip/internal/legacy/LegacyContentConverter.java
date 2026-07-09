package com.cooobird.datatip.internal.legacy;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.content.TextContent;
import com.cooobird.datatip.api.content.VBoxContent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 负责把旧格式的单个条目转换成 TipContent。
 */
final class LegacyContentConverter {

    private LegacyContentConverter() {
    }

    @Nullable
    static TipContent convertEntry(String key, JsonElement value) {
        if (value.isJsonArray()) {
            return convertStringArray(value.getAsJsonArray());
        } else if (value.isJsonObject()) {
            return convertObject(value.getAsJsonObject());
        } else if (value.isJsonPrimitive()) {
            return TextContent.of(value.getAsString());
        }
        return null;
    }

    private static TipContent convertStringArray(JsonArray array) {
        VBoxContent vbox = VBoxContent.create();

        for (JsonElement element : array) {
            if (element.isJsonPrimitive()) {
                vbox.addChild(TextContent.of(element.getAsString()));
            }
        }

        return vbox;
    }

    private static TipContent convertObject(JsonObject obj) {
        VBoxContent vbox = VBoxContent.create();
        LegacyTextStyle topStyle = LegacyTextStyle.fromTopLevel(obj);

        if (obj.has("text")) {
            JsonElement textElement = obj.get("text");

            if (textElement.isJsonArray()) {
                convertTextArray(textElement.getAsJsonArray(), vbox, topStyle);
            } else if (textElement.isJsonObject()) {
                convertTextObject(textElement.getAsJsonObject(), vbox, topStyle);
            } else if (textElement.isJsonPrimitive()) {
                vbox.addChild(topStyle.toTextContent(textElement.getAsString()));
            }
        }

        return unwrapSingleText(vbox);
    }

    private static TipContent unwrapSingleText(VBoxContent vbox) {
        if (vbox.children().size() == 1) {
            TipContent singleChild = vbox.children().get(0);
            if (singleChild instanceof TextContent) {
                return singleChild;
            }
        }

        return vbox;
    }

    private static void convertTextArray(JsonArray array, VBoxContent vbox, LegacyTextStyle topStyle) {
        for (JsonElement item : array) {
            if (item.isJsonPrimitive()) {
                vbox.addChild(TextContent.of(item.getAsString(), topStyle.color()));
            } else if (item.isJsonObject()) {
                vbox.addChild(convertStyledLine(item.getAsJsonObject(), topStyle));
            }
        }
    }

    private static void convertTextObject(JsonObject textObj, VBoxContent vbox, LegacyTextStyle topStyle) {
        if (isMultiLanguageText(textObj)) {
            LegacyMultiLanguageTextConverter.convert(textObj, vbox, topStyle);
            return;
        }

        for (Map.Entry<String, JsonElement> langEntry : textObj.entrySet()) {
            JsonArray lines = langEntry.getValue().getAsJsonArray();
            convertTextArray(lines, vbox, topStyle);
        }
    }

    private static boolean isMultiLanguageText(JsonObject textObj) {
        for (String key : textObj.keySet()) {
            if (key.contains("_")) {
                return true;
            }
        }
        return false;
    }

    private static TipContent convertStyledLine(JsonObject line, LegacyTextStyle topStyle) {
        String text = line.has("text") ? line.get("text").getAsString() : "";
        return LegacyTextStyle.fromLine(line, topStyle).toTextContent(text);
    }
}
