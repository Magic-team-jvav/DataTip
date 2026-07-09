package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.content.BaseTextContent;
import com.cooobird.datatip.api.content.TextContent;
import com.cooobird.datatip.api.util.ColorParser;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * 文本类 TipContent 创建工具。
 */
final class TipTextContentFactory {
    private TipTextContentFactory() {
    }

    static TextContent text(String text) {
        return TextContent.of(text);
    }

    static TextContent text(String text, String color) {
        return TextContent.of(text, parseColor(color));
    }

    static TextContent text(String text, String color, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
        return text(text, color, bold, italic, underlined, strikethrough, false);
    }

    static TextContent text(String text, String color, boolean bold, boolean italic, boolean underlined, boolean strikethrough, boolean shift) {
        return new TextContent(text, null, null, null, null, null,
            parseColor(color), null, true, BaseTextContent.TextAlign.LEFT, 12, 0,
            bold, italic, underlined, strikethrough, shift);
    }

    static TextContent text(String text, String color, String font) {
        return new TextContent(text, null, null, null, null,
            ResourceLocation.tryParse(font),
            parseColor(color), null, true, BaseTextContent.TextAlign.LEFT, 12, 0,
            false, false, false, false, false);
    }

    static TextContent langText(Map<String, String> langText) {
        return TextContent.ofLang(langText);
    }

    static TextContent langText(Map<String, String> langText, String color) {
        return TextContent.ofLang(langText, parseColor(color));
    }

    static TextContent langText(Map<String, String> langText, String color, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
        return langText(langText, color, bold, italic, underlined, strikethrough, false);
    }

    static TextContent langText(Map<String, String> langText, String color, boolean bold, boolean italic, boolean underlined, boolean strikethrough, boolean shift) {
        return new TextContent(null, null, null, langText, null, null,
            parseColor(color), null, true, BaseTextContent.TextAlign.LEFT, 12, 0,
            bold, italic, underlined, strikethrough, shift);
    }

    static TextContent langText(Map<String, String> langText, String color, String font) {
        return new TextContent(null, null, null, langText, null,
            ResourceLocation.tryParse(font),
            parseColor(color), null, true, BaseTextContent.TextAlign.LEFT, 12, 0,
            false, false, false, false, false);
    }

    static TextContent centered(String text) {
        return TextContent.centered(text);
    }

    static TextContent centered(String text, String color) {
        return TextContent.centered(text, parseColor(color));
    }

    static TextContent centered(String text, String color, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
        return new TextContent(text, null, null, null, null, null,
            parseColor(color), null, true, BaseTextContent.TextAlign.CENTER, 12, 0,
            bold, italic, underlined, strikethrough, false);
    }

    static TextContent rightAligned(String text) {
        return TextContent.rightAligned(text);
    }

    static TextContent rightAligned(String text, String color) {
        return TextContent.rightAligned(text, parseColor(color));
    }

    private static int parseColor(String color) {
        return ColorParser.parse(color, ColorParser.WHITE);
    }
}
