package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.BaseTextContent;
import com.cooobird.datatip.api.content.TextContent;
import com.cooobird.datatip.api.content.TextContentDefaults;
import com.cooobird.datatip.config.DatatipConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * TextContent 的通用样式选项。
 */
record TextContentStyleOptions(
    @Nullable ResourceLocation font,
    int color,
    @Nullable String colorExpression,
    boolean shadow,
    BaseTextContent.TextAlign align,
    int lineHeight,
    int maxWidth,
    boolean bold,
    boolean italic,
    boolean underlined,
    boolean strikethrough,
    boolean shift
) {
    static TextContentStyleOptions parse(JsonObject json, ParseContext context) {
        int color = DatatipConfig.DEFAULT_COLOR.get();
        String colorExpression = null;

        JsonElement colorElement = json.get("color");
        if (colorElement != null && colorElement.isJsonPrimitive()) {
            String colorStr = colorElement.getAsString();
            if (colorStr.contains("{") && colorStr.contains("}")) {
                colorExpression = colorStr;
                color = DatatipConfig.DEFAULT_COLOR.get();
            } else {
                color = context.getColor(json, "color", DatatipConfig.DEFAULT_COLOR.get());
            }
        }

        return new TextContentStyleOptions(
            parseFont(json, context),
            color,
            colorExpression,
            context.getBoolean(json, "shadow", true),
            parseAlign(json, context),
            context.getInt(json, "lineHeight", TextContentDefaults.lineHeight()),
            context.getInt(json, "maxWidth", 0),
            context.getBoolean(json, "bold", false),
            context.getBoolean(json, "italic", false),
            context.getBoolean(json, "underlined", false),
            context.getBoolean(json, "strikethrough", false),
            context.getBoolean(json, "shift", false)
        );
    }

    TextContent createWithComponent(Component component) {
        return create(null, component, null, null, null);
    }

    TextContent createWithStyledLanguages(Map<String, BaseTextContent.LangStyle> langStyledText) {
        return create(null, null, null, null, langStyledText);
    }

    TextContent createWithLanguages(Map<String, String> langText) {
        return create(null, null, null, langText, null);
    }

    TextContent createWithText(String text) {
        return create(text, null, null, null, null);
    }

    private TextContent create(
        @Nullable String text,
        @Nullable Component component,
        @Nullable net.minecraft.network.chat.FormattedText formattedText,
        @Nullable Map<String, String> langText,
        @Nullable Map<String, BaseTextContent.LangStyle> langStyledText
    ) {
        return new TextContent(text, component, formattedText, langText, langStyledText, font, color, colorExpression,
            shadow, align, lineHeight, maxWidth, bold, italic, underlined, strikethrough, shift);
    }

    @Nullable
    private static ResourceLocation parseFont(JsonObject json, ParseContext context) {
        if (!context.has(json, "font")) {
            return null;
        }

        String fontStr = context.getString(json, "font", "");
        if (fontStr.isEmpty()) {
            return null;
        }
        return ResourceLocation.tryParse(fontStr);
    }

    private static BaseTextContent.TextAlign parseAlign(JsonObject json, ParseContext context) {
        String alignStr = context.getString(json, "align", "left");
        if ("center".equals(alignStr)) {
            return BaseTextContent.TextAlign.CENTER;
        } else if ("right".equals(alignStr)) {
            return BaseTextContent.TextAlign.RIGHT;
        }
        return BaseTextContent.TextAlign.LEFT;
    }
}
