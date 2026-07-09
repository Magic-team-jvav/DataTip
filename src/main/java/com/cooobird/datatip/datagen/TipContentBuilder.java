package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.content.*;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * 新版本 Tooltip 构建器。
 * 用于 datagen 生成 JSON 配置。
 *
 * @author cooobird
 * @since 1.2.0
 */
public class TipContentBuilder {

    // 文本内容
    public static TextContent text(String text) {
        return TipTextContentFactory.text(text);
    }

    public static TextContent text(String text, String color) {
        return TipTextContentFactory.text(text, color);
    }

    public static TextContent text(String text, String color, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
        return TipTextContentFactory.text(text, color, bold, italic, underlined, strikethrough);
    }

    public static TextContent text(String text, String color, boolean bold, boolean italic, boolean underlined, boolean strikethrough, boolean shift) {
        return TipTextContentFactory.text(text, color, bold, italic, underlined, strikethrough, shift);
    }

    public static TextContent text(String text, String color, String font) {
        return TipTextContentFactory.text(text, color, font);
    }

    public static TextContent langText(Map<String, String> langText) {
        return TipTextContentFactory.langText(langText);
    }

    public static TextContent langText(Map<String, String> langText, String color) {
        return TipTextContentFactory.langText(langText, color);
    }

    public static TextContent langText(Map<String, String> langText, String color, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
        return TipTextContentFactory.langText(langText, color, bold, italic, underlined, strikethrough);
    }

    public static TextContent langText(Map<String, String> langText, String color, boolean bold, boolean italic, boolean underlined, boolean strikethrough, boolean shift) {
        return TipTextContentFactory.langText(langText, color, bold, italic, underlined, strikethrough, shift);
    }

    public static TextContent langText(Map<String, String> langText, String color, String font) {
        return TipTextContentFactory.langText(langText, color, font);
    }

    public static TextContent centered(String text) {
        return TipTextContentFactory.centered(text);
    }

    public static TextContent centered(String text, String color) {
        return TipTextContentFactory.centered(text, color);
    }

    public static TextContent centered(String text, String color, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
        return TipTextContentFactory.centered(text, color, bold, italic, underlined, strikethrough);
    }

    public static TextContent rightAligned(String text) {
        return TipTextContentFactory.rightAligned(text);
    }

    public static TextContent rightAligned(String text, String color) {
        return TipTextContentFactory.rightAligned(text, color);
    }

    // 基础内容
    public static SpacerContent spacer(int height) {
        return TipCommonContentFactory.spacer(height);
    }

    public static DividerContent divider() {
        return TipCommonContentFactory.divider();
    }

    public static DividerContent divider(String color) {
        return TipCommonContentFactory.divider(color);
    }

    public static DividerContent divider(String color, String style) {
        return TipCommonContentFactory.divider(color, style);
    }

    public static ItemContent item(String itemId) {
        return TipCommonContentFactory.item(itemId);
    }

    public static ItemContent item(String itemId, String label) {
        return TipCommonContentFactory.item(itemId, label);
    }

    public static ProgressContent progress(float progress, int width) {
        return TipCommonContentFactory.progress(progress, width);
    }

    public static ProgressContent progress(float progress, int width, String label) {
        return TipCommonContentFactory.progress(progress, width, label);
    }

    public static TypewriterContent typewriter(String... lines) {
        return TipCommonContentFactory.typewriter(lines);
    }

    public static TypewriterContent typewriter(int color, String... lines) {
        return TipCommonContentFactory.typewriter(color, lines);
    }

    public static TypewriterContent typewriter(int color, String font, String... lines) {
        return TipCommonContentFactory.typewriter(color, font, lines);
    }

    public static TypewriterContent typewriter(int color, int charsPerSecond, int pauseSeconds, boolean loop, String... lines) {
        return TipCommonContentFactory.typewriter(color, charsPerSecond, pauseSeconds, loop, lines);
    }

    public static TypewriterContent typewriter(int color, int charsPerSecond, int pauseSeconds, boolean loop, boolean shift, String... lines) {
        return TipCommonContentFactory.typewriter(color, charsPerSecond, pauseSeconds, loop, shift, lines);
    }

    // 布局内容
    public static VBoxContent vbox(TipContent... children) {
        return TipLayoutContentFactory.vbox(children);
    }

    public static VBoxContent vbox(int gap, TipContent... children) {
        return TipLayoutContentFactory.vbox(gap, children);
    }

    public static HBoxContent hbox(TipContent... children) {
        return TipLayoutContentFactory.hbox(children);
    }

    public static HBoxContent hbox(int gap, TipContent... children) {
        return TipLayoutContentFactory.hbox(gap, children);
    }

    public static CarouselContent carousel(int intervalSeconds, TipContent... frames) {
        return TipLayoutContentFactory.carousel(intervalSeconds, frames);
    }

    public static AlignedContent aligned(TipContent content, VBoxContent.HorizontalAlign align) {
        return TipLayoutContentFactory.aligned(content, align);
    }

    public static AlignedContent centeredAligned(TipContent content) {
        return TipLayoutContentFactory.centeredAligned(content);
    }

    public static AlignedContent rightAlignedContent(TipContent content) {
        return TipLayoutContentFactory.rightAlignedContent(content);
    }

    // 视觉内容
    public static EntityContent entity(String entityId, int size) {
        return TipVisualContentFactory.entity(entityId, size);
    }

    public static EntityContent entity(String entityId, int size, int offsetX, int offsetY) {
        return TipVisualContentFactory.entity(entityId, size, offsetX, offsetY);
    }

    public static BlockContent block(String blockId, int size) {
        return TipVisualContentFactory.block(blockId, size);
    }

    public static BlockContent block(String blockId, int size, int offsetX, int offsetY) {
        return TipVisualContentFactory.block(blockId, size, offsetX, offsetY);
    }

    public static AtlasContent atlas(String itemId, int size) {
        return TipVisualContentFactory.atlas(itemId, size);
    }

    public static AtlasContent atlas(String itemId, int size, int offsetX, int offsetY) {
        return TipVisualContentFactory.atlas(itemId, size, offsetX, offsetY);
    }

    public static ImageContent image(String texture, int width, int height) {
        return TipVisualContentFactory.image(texture, width, height);
    }

    public static ImageContent image(String texture, int width, int height, int offsetX, int offsetY) {
        return TipVisualContentFactory.image(texture, width, height, offsetX, offsetY);
    }

    // 图表内容
    public static ChartContent chart(String chartType, int width, int height) {
        return TipChartContentFactory.chart(chartType, width, height);
    }

    public static ChartContent chart(String chartType, int width, int height, String title, ChartContent.ChartEntry... entries) {
        return TipChartContentFactory.chart(chartType, width, height, title, entries);
    }

    public static ChartContent.ChartEntry chartEntry(String label, double value, String color) {
        return TipChartContentFactory.chartEntry(label, value, color);
    }

    public static ChartContent.ChartEntry chartEntry(String label, String valueExpr, String color) {
        return TipChartContentFactory.chartEntry(label, valueExpr, color);
    }

    /**
     * 将 TipContent 转换为 JSON（完整序列化所有属性）。
     */
    public static JsonObject toJson(TipContent content) {
        return TipContentJsonSerializer.toJson(content);
    }
}
