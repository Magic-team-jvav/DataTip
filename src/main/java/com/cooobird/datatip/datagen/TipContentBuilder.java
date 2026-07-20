package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.content.*;
import com.cooobird.datatip.api.node.TipModifiers;
import com.cooobird.datatip.api.node.TipNode;
import com.cooobird.datatip.api.util.ColorParser;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;

import java.util.List;
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

    public static TextContent translate(String key) {
        return TextContent.of(Component.translatable(key));
    }

    public static CyclingTextContent cycleText(
        TextContent text,
        double cycleSeconds,
        String transition,
        String... colors
    ) {
        CyclingTextContent.Transition parsedTransition = switch (
            transition.toLowerCase(java.util.Locale.ROOT)
            ) {
            case "smooth" -> CyclingTextContent.Transition.SMOOTH;
            case "step" -> CyclingTextContent.Transition.STEP;
            default -> throw new IllegalArgumentException(
                "transition must be 'smooth' or 'step'"
            );
        };
        return new CyclingTextContent(
            text,
            java.util.Arrays.stream(colors)
                .map(ColorParser::parseStrict)
                .toList(),
            cycleSeconds,
            parsedTransition,
            0.0
        );
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

    public static DividerContent divider(String color, int thickness, int width, int marginTop, int marginBottom,
                                         String style, String widthMode) {
        return TipCommonContentFactory.divider(color, thickness, width, marginTop, marginBottom, style, widthMode);
    }

    public static ItemContent item(String itemId) {
        return TipCommonContentFactory.item(itemId);
    }

    public static ItemContent item(String itemId, String label) {
        return TipCommonContentFactory.item(itemId, label);
    }

    public static ItemContent item(String itemId, int count, int size, boolean showCount, boolean showDurability,
                                   boolean showLabel, Map<String, String> label, String labelColor,
                                   int offsetX, int offsetY) {
        return TipCommonContentFactory.item(itemId, count, size, showCount, showDurability, showLabel,
            label, labelColor, offsetX, offsetY);
    }

    public static ProgressContent progress(float progress, int width) {
        return TipCommonContentFactory.progress(progress, width);
    }

    public static ProgressContent progress(float progress, int width, String label) {
        return TipCommonContentFactory.progress(progress, width, label);
    }

    public static ProgressContent progress(float progress, int width, int height, String colorFg, String colorBg,
                                           String colorFgLight, String colorBgDark, String style,
                                           Map<String, String> label, String labelAlign, int animSpeed) {
        return TipCommonContentFactory.progress(progress, width, height, colorFg, colorBg, colorFgLight,
            colorBgDark, style, label, labelAlign, animSpeed);
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

    public static TypewriterContent typewriter(Map<String, List<String>> lines, int charsPerSecond, int pauseSeconds,
                                               boolean loop, String color, String font, boolean bold,
                                               boolean italic, boolean underlined, boolean strikethrough,
                                               String align, boolean shadow, int lineHeight, boolean shift) {
        return TipCommonContentFactory.typewriter(lines, charsPerSecond, pauseSeconds, loop, color, font, bold,
            italic, underlined, strikethrough, align, shadow, lineHeight, shift);
    }

    // 布局内容
    public static VBoxContent vbox(TipContent... children) {
        return TipLayoutContentFactory.vbox(children);
    }

    public static VBoxContent vbox(int gap, TipContent... children) {
        return TipLayoutContentFactory.vbox(gap, children);
    }

    public static VBoxContent vbox(int gap, int padding, String align, TipContent... children) {
        return TipLayoutContentFactory.vbox(gap, padding, align, children);
    }

    public static HBoxContent hbox(TipContent... children) {
        return TipLayoutContentFactory.hbox(children);
    }

    public static HBoxContent hbox(int gap, TipContent... children) {
        return TipLayoutContentFactory.hbox(gap, children);
    }

    public static HBoxContent hbox(int gap, int padding, String align, TipContent... children) {
        return TipLayoutContentFactory.hbox(gap, padding, align, children);
    }

    public static StackContent stack(TipContent... children) {
        return TipLayoutContentFactory.stack(children);
    }

    public static StackContent stack(
        int padding,
        String horizontalAlign,
        String verticalAlign,
        TipContent... children
    ) {
        return TipLayoutContentFactory.stack(
            padding,
            horizontalAlign,
            verticalAlign,
            children
        );
    }

    public static CarouselContent carousel(int intervalSeconds, TipContent... frames) {
        return TipLayoutContentFactory.carousel(intervalSeconds, frames);
    }

    public static CarouselContent carousel(int intervalSeconds, String transition, TipContent... frames) {
        return TipLayoutContentFactory.carousel(intervalSeconds, transition, frames);
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

    public static TipNode modifiers(
        TipContent content,
        boolean shift,
        long offsetZ,
        TipModifiers.SelfAlignment selfAlignment
    ) {
        return TipNode.wrap(
            content,
            new TipModifiers(shift, offsetZ, selfAlignment)
        );
    }

    /**
     * 为任意内容节点应用完整的公共修饰符集合。
     */
    public static TipNode modifiers(
        TipContent content,
        TipModifiers modifiers
    ) {
        return TipNode.wrap(content, modifiers);
    }

    // 视觉内容
    public static EntityContent entity(String entityId, int size) {
        return TipVisualContentFactory.entity(entityId, size);
    }

    public static EntityContent entity(String entityId, int size, int offsetX, int offsetY) {
        return TipVisualContentFactory.entity(entityId, size, offsetX, offsetY);
    }

    public static EntityContent entity(String entityId, int size, float rotationSpeed, boolean autoRotate,
                                       Map<String, String> label, int offsetX, int offsetY) {
        return TipVisualContentFactory.entity(entityId, size, rotationSpeed, autoRotate, label, offsetX, offsetY);
    }

    public static BlockContent block(String blockId, int size) {
        return TipVisualContentFactory.block(blockId, size);
    }

    public static BlockContent block(String blockId, int size, int offsetX, int offsetY) {
        return TipVisualContentFactory.block(blockId, size, offsetX, offsetY);
    }

    public static BlockContent block(String blockId, int size, float rotationSpeed, boolean autoRotate,
                                     Map<String, String> label, int offsetX, int offsetY) {
        return TipVisualContentFactory.block(blockId, size, rotationSpeed, autoRotate, label, offsetX, offsetY);
    }

    public static AtlasContent atlas(String itemId, int size) {
        return TipVisualContentFactory.atlas(itemId, size);
    }

    public static AtlasContent atlas(String itemId, int size, int offsetX, int offsetY) {
        return TipVisualContentFactory.atlas(itemId, size, offsetX, offsetY);
    }

    public static AtlasContent atlas(String itemId, int size, Map<String, String> label, int offsetX, int offsetY) {
        return TipVisualContentFactory.atlas(itemId, size, label, offsetX, offsetY);
    }

    public static ImageContent image(String texture, int width, int height) {
        return TipVisualContentFactory.image(texture, width, height);
    }

    public static ImageContent image(String texture, int width, int height, int offsetX, int offsetY) {
        return TipVisualContentFactory.image(texture, width, height, offsetX, offsetY);
    }

    public static ImageContent image(String texture, int width, int height, int u, int v,
                                     int textureWidth, int textureHeight, float scale, int offsetX, int offsetY) {
        return TipVisualContentFactory.image(texture, width, height, u, v, textureWidth, textureHeight,
            scale, offsetX, offsetY);
    }

    // 图表内容
    public static ChartContent chart(String chartType, int width, int height) {
        return TipChartContentFactory.chart(chartType, width, height);
    }

    public static ChartContent chart(String chartType, int width, int height, String title, ChartContent.ChartEntry... entries) {
        return TipChartContentFactory.chart(chartType, width, height, title, entries);
    }

    public static ChartContent chart(String chartType, int width, int height, Map<String, String> title,
                                     boolean showLabels, boolean showValues, String titleColor,
                                     String labelColor, String valueColor, String zeroLineColor,
                                     ChartContent.ChartEntry... entries) {
        return TipChartContentFactory.chart(chartType, width, height, title, showLabels, showValues,
            titleColor, labelColor, valueColor, zeroLineColor, entries);
    }

    public static ChartContent.ChartEntry chartEntry(String label, double value, String color) {
        return TipChartContentFactory.chartEntry(label, value, color);
    }

    public static ChartContent.ChartEntry chartEntry(String label, String valueExpr, String color) {
        return TipChartContentFactory.chartEntry(label, valueExpr, color);
    }

    public static ChartContent.ChartEntry chartEntry(Map<String, String> label, String valueExpr, String color) {
        return TipChartContentFactory.chartEntry(label, valueExpr, color);
    }

    public static Map<String, String> languages(String zhCn, String enUs) {
        return Map.of("zh_cn", zhCn, "en_us", enUs);
    }

    public static Map<String, List<String>> languageLines(List<String> zhCn, List<String> enUs) {
        return Map.of("zh_cn", List.copyOf(zhCn), "en_us", List.copyOf(enUs));
    }

    /**
     * 将 TipContent 转换为 JSON（完整序列化所有属性）。
     */
    public static JsonObject toJson(TipContent content) {
        return TipContentJsonSerializer.toJson(content);
    }

    public static JsonObject entry(TipContent content, Map<String, ?> conditions) {
        return entry(content, conditions, false, false);
    }

    public static JsonObject entry(TipContent content, Map<String, ?> conditions, boolean shift, boolean prepend) {
        JsonObject json = toJson(content);
        if (conditions != null && !conditions.isEmpty()) {
            json.add("conditions", new com.google.gson.Gson().toJsonTree(conditions));
        }
        if (shift) json.addProperty("shift", true);
        if (prepend) json.addProperty("prepend", true);
        return json;
    }
}
