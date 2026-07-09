package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.content.*;
import com.cooobird.datatip.api.util.ColorParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

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

    static ProgressContent progress(float progress, int width) {
        return ProgressContent.of(progress, width);
    }

    static ProgressContent progress(float progress, int width, String label) {
        return ProgressContent.withCustomLabel(progress, width, Component.literal(label));
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

    private static int parseColor(String color) {
        return ColorParser.parse(color, ColorParser.WHITE);
    }
}
