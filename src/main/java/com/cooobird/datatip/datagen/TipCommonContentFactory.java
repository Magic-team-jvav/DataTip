package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.content.*;
import com.cooobird.datatip.api.text.LocalizedText;
import com.cooobird.datatip.api.util.ColorParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * 基础类 TipContent 创建工具。
 */
final class TipCommonContentFactory {
    private TipCommonContentFactory() {
    }

    static SpacerContent spacer(int height) {
        return SpacerContent.of(height);
    }

    static DividerContent divider() {
        return DividerContent.create();
    }

    static DividerContent divider(String color) {
        return DividerContent.of(parseColor(color));
    }

    static DividerContent divider(String color, String style) {
        return DividerContent.of(parseColor(color), switch (style.toLowerCase()) {
            case "dashed" -> DividerContent.DividerStyle.DASHED;
            case "dotted" -> DividerContent.DividerStyle.DOTTED;
            default -> DividerContent.DividerStyle.SOLID;
        });
    }

    static DividerContent divider(String color, int thickness, int width, int marginTop, int marginBottom,
                                  String style, String widthMode) {
        return new DividerContent(parseColor(color), thickness, width, marginTop, marginBottom,
            parseDividerStyle(style), parseWidthMode(widthMode));
    }

    static ItemContent item(String itemId) {
        return ItemContent.of(new ItemStack(
            BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId))
        ));
    }

    static ItemContent item(String itemId, String label) {
        return ItemContent.withLabel(
            new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId))),
            Component.literal(label)
        );
    }

    static ItemContent item(String itemId, int count, int size, boolean showCount, boolean showDurability,
                            boolean showLabel, Map<String, String> label, String labelColor,
                            int offsetX, int offsetY) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId)), Math.max(1, count));
        return new ItemContent(stack, size, showCount, showDurability, showLabel, localized(label),
            parseColor(labelColor), offsetX, offsetY);
    }

    static ProgressContent progress(float progress, int width) {
        return ProgressContent.of(progress, width);
    }

    static ProgressContent progress(float progress, int width, String label) {
        return ProgressContent.withCustomLabel(progress, width, Component.literal(label));
    }

    static ProgressContent progress(float progress, int width, int height, String colorFg, String colorBg,
                                    String colorFgLight, String colorBgDark, String style,
                                    Map<String, String> label, String labelAlign, int animSpeed) {
        ProgressContent.ProgressStyle progressStyle = parseProgressStyle(style);
        return new ProgressContent(progress, width, height, parseColor(colorFg), parseColor(colorBg),
            parseColor(colorFgLight), parseColor(colorBgDark), progressStyle, label != null,
            localized(label), parseLabelAlign(labelAlign), progressStyle == ProgressContent.ProgressStyle.ANIMATED,
            animSpeed);
    }

    static TypewriterContent typewriter(String... lines) {
        return TypewriterContent.of(lines);
    }

    static TypewriterContent typewriter(int color, String... lines) {
        return TypewriterContent.of(color, lines);
    }

    static TypewriterContent typewriter(int color, String font, String... lines) {
        return new TypewriterContent(List.of(lines), 2, 1, false, color, ResourceLocation.tryParse(font));
    }

    static TypewriterContent typewriter(int color, int charsPerSecond, int pauseSeconds, boolean loop, String... lines) {
        return new TypewriterContent(List.of(lines), charsPerSecond, pauseSeconds, loop, color);
    }

    static TypewriterContent typewriter(int color, int charsPerSecond, int pauseSeconds, boolean loop, boolean shift, String... lines) {
        return new TypewriterContent(List.of(lines), null, null, charsPerSecond, pauseSeconds, loop, color,
            null, null, false, false, false, false,
            BaseTextContent.TextAlign.LEFT, true, 12, shift);
    }

    static TypewriterContent typewriter(Map<String, List<String>> lines, int charsPerSecond, int pauseSeconds,
                                        boolean loop, String color, String font, boolean bold, boolean italic,
                                        boolean underlined, boolean strikethrough, String align, boolean shadow,
                                        int lineHeight, boolean shift) {
        return new TypewriterContent(null, lines, null, charsPerSecond, pauseSeconds, loop, parseColor(color),
            null, ResourceLocation.tryParse(font), bold, italic, underlined, strikethrough,
            parseTextAlign(align), shadow, lineHeight, shift);
    }

    private static LocalizedText localized(Map<String, String> values) {
        if (values == null || values.isEmpty()) return null;
        Map<String, Component> components = new java.util.LinkedHashMap<>();
        values.forEach((language, value) -> components.put(language, Component.literal(value)));
        return LocalizedText.languages(components);
    }

    private static DividerContent.DividerStyle parseDividerStyle(String style) {
        if (style == null) return DividerContent.DividerStyle.SOLID;
        return switch (style.toLowerCase(java.util.Locale.ROOT)) {
            case "dashed" -> DividerContent.DividerStyle.DASHED;
            case "dotted" -> DividerContent.DividerStyle.DOTTED;
            default -> DividerContent.DividerStyle.SOLID;
        };
    }

    private static DividerContent.WidthMode parseWidthMode(String mode) {
        if (mode == null) return DividerContent.WidthMode.FILL;
        return switch (mode.toLowerCase(java.util.Locale.ROOT)) {
            case "fixed" -> DividerContent.WidthMode.FIXED;
            case "centered" -> DividerContent.WidthMode.CENTERED;
            default -> DividerContent.WidthMode.FILL;
        };
    }

    private static ProgressContent.ProgressStyle parseProgressStyle(String style) {
        if (style == null) return ProgressContent.ProgressStyle.GRADIENT;
        try {
            return ProgressContent.ProgressStyle.valueOf(style.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return ProgressContent.ProgressStyle.GRADIENT;
        }
    }

    private static ProgressContent.LabelAlign parseLabelAlign(String align) {
        if (align == null) return ProgressContent.LabelAlign.LEFT;
        try {
            return ProgressContent.LabelAlign.valueOf(align.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return ProgressContent.LabelAlign.LEFT;
        }
    }

    private static BaseTextContent.TextAlign parseTextAlign(String align) {
        if (align == null) return BaseTextContent.TextAlign.LEFT;
        try {
            return BaseTextContent.TextAlign.valueOf(align.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return BaseTextContent.TextAlign.LEFT;
        }
    }

    private static int parseColor(String color) {
        return ColorParser.parse(color, ColorParser.WHITE);
    }
}
