package com.cooobird.datatip.internal.legacy;

import com.cooobird.datatip.api.content.BaseTextContent;
import com.cooobird.datatip.api.content.TextContent;
import com.google.gson.JsonObject;

/**
 * 旧格式文本样式。
 */
record LegacyTextStyle(
    int color,
    boolean bold,
    boolean italic,
    boolean underlined,
    boolean strikethrough
) {
    static LegacyTextStyle fromTopLevel(JsonObject obj) {
        int color = obj.has("color") ? LegacyColorParser.parse(obj.get("color").getAsString()) : LegacyColorParser.defaultColor();
        boolean bold = obj.has("bold") && obj.get("bold").getAsBoolean();
        boolean italic = obj.has("italic") && obj.get("italic").getAsBoolean();
        boolean underlined = obj.has("underlined") && obj.get("underlined").getAsBoolean();
        boolean strikethrough = obj.has("strikethrough") && obj.get("strikethrough").getAsBoolean();
        return new LegacyTextStyle(color, bold, italic, underlined, strikethrough);
    }

    static LegacyTextStyle fromLine(JsonObject lineObj, LegacyTextStyle parent) {
        int color = lineObj.has("color") ? LegacyColorParser.parse(lineObj.get("color").getAsString()) : parent.color;
        boolean bold = lineObj.has("bold") ? lineObj.get("bold").getAsBoolean() : parent.bold;
        boolean italic = lineObj.has("italic") ? lineObj.get("italic").getAsBoolean() : parent.italic;
        boolean underlined = lineObj.has("underlined") ? lineObj.get("underlined").getAsBoolean() : parent.underlined;
        boolean strikethrough = lineObj.has("strikethrough")
            ? lineObj.get("strikethrough").getAsBoolean()
            : parent.strikethrough;
        return new LegacyTextStyle(color, bold, italic, underlined, strikethrough);
    }

    TextContent toTextContent(String text) {
        return new TextContent(text, null, null, null, null, null, color, null, true,
            BaseTextContent.TextAlign.LEFT, 12, 0, bold, italic, underlined, strikethrough, false);
    }

    BaseTextContent.LangStyle toLangStyle(String text) {
        return new BaseTextContent.LangStyle(text, color, bold, italic, underlined, strikethrough);
    }
}
