package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.content.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * 通用内容 JSON 写出器。
 */
final class TipCommonJsonWriter {
    private TipCommonJsonWriter() {
    }

    static void writeDivider(
        JsonObject json,
        int color,
        int thickness,
        int width,
        int marginTop,
        int marginBottom,
        DividerContent.DividerStyle style,
        DividerContent.WidthMode widthMode
    ) {
        json.addProperty("type", "divider");
        json.addProperty("color", DatagenJsonUtils.colorToHex(color));
        if (thickness != 1) json.addProperty("thickness", thickness);
        if (width > 0) json.addProperty("width", width);
        if (marginTop > 0) json.addProperty("marginTop", marginTop);
        if (marginBottom > 0) json.addProperty("marginBottom", marginBottom);
        if (style != DividerContent.DividerStyle.SOLID)
            json.addProperty("style", style.toString().toLowerCase());
        if (widthMode != DividerContent.WidthMode.FILL)
            json.addProperty("widthMode", widthMode.toString().toLowerCase());
    }

    static void writeItem(JsonObject json, ItemContent item) {
        json.addProperty("type", "item");
        json.addProperty("item", item.getStack().getItem().toString());
        if (item.size() != 16) json.addProperty("size", item.size());
        if (item.showCount()) json.addProperty("showCount", true);
        if (item.showDurability()) json.addProperty("showDurability", true);
        if (item.showLabel()) json.addProperty("showLabel", true);
        if (item.label() != null) json.addProperty("label", item.label().getString());
        if (item.labelColor() != null)
            json.addProperty("labelColor", DatagenJsonUtils.colorToHex(item.labelColor()));
        if (item.offsetX() != 0) json.addProperty("offsetX", item.offsetX());
        if (item.offsetY() != 0) json.addProperty("offsetY", item.offsetY());
    }

    static void writeProgress(JsonObject json, ProgressContent progress) {
        json.addProperty("type", "progress");
        json.addProperty("progress", progress.progress());
        json.addProperty("width", progress.width());
        if (progress.height() != 8) json.addProperty("height", progress.height());
        if (progress.colorFg() != 0xFF55FF55)
            json.addProperty("colorFg", DatagenJsonUtils.colorToHex(progress.colorFg()));
        if (progress.colorBg() != 0xFF333333)
            json.addProperty("colorBg", DatagenJsonUtils.colorToHex(progress.colorBg()));
        if (progress.style() != ProgressContent.ProgressStyle.GRADIENT)
            json.addProperty("style", progress.style().toString().toLowerCase());
        if (progress.showLabel()) {
            json.addProperty("showLabel", true);
            if (progress.customLabel() != null)
                json.addProperty("label", progress.customLabel().getString());
            if (progress.labelAlign() != ProgressContent.LabelAlign.LEFT)
                json.addProperty("labelAlign", progress.labelAlign().toString().toLowerCase());
        }
        if (progress.animated()) {
            json.addProperty("animated", true);
            json.addProperty("animSpeed", progress.animSpeed());
        }
    }

    static void writeTypewriter(JsonObject json, TypewriterContent typewriter) {
        json.addProperty("type", "typewriter");
        JsonArray lines = new JsonArray();
        for (String line : typewriter.getLines()) lines.add(line);
        json.add("lines", lines);
        json.addProperty("charsPerSecond", typewriter.getCharsPerSecond());
        json.addProperty("pauseSeconds", 1);
        json.addProperty("loop", typewriter.isLoop());
        if (typewriter.color() != 0xFFFFFF)
            json.addProperty("color", DatagenJsonUtils.colorToHex(typewriter.color()));
        if (typewriter.font() != null) json.addProperty("font", typewriter.font().toString());
        if (typewriter.bold()) json.addProperty("bold", true);
        if (typewriter.italic()) json.addProperty("italic", true);
        if (typewriter.underlined()) json.addProperty("underlined", true);
        if (typewriter.strikethrough()) json.addProperty("strikethrough", true);
        if (!typewriter.shadow()) json.addProperty("shadow", false);
        if (typewriter.align() != BaseTextContent.TextAlign.LEFT)
            json.addProperty("align", typewriter.align().toString().toLowerCase());
        if (typewriter.lineHeight() != TextContentDefaults.lineHeight())
            json.addProperty("lineHeight", typewriter.lineHeight());
        if (typewriter.shift()) json.addProperty("shift", true);
    }
}
