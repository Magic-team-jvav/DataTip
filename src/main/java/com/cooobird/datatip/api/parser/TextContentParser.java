package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.TextContent;
import com.cooobird.datatip.config.DatatipConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * TextContent 解析器。
 * <p>
 * 负责将 JSON 对象解析为 {@link TextContent} 实例。
 * 支持多种文本格式和样式选项。
 * </p>
 *
 * @author cooobird
 * @since 1.2.0
 */
public class TextContentParser implements ContentParser {

    @Override
    public TextContent parse(JsonObject json, ParseContext context) {
        String text = null;
        Map<String, String> langText = null;
        Map<String, TextContent.LangStyle> langStyledText = null;

        JsonElement textElement = json.get("text");
        if (textElement != null) {
            if (textElement.isJsonPrimitive()) {
                text = textElement.getAsString();
            } else if (textElement.isJsonObject()) {
                JsonObject langObj = textElement.getAsJsonObject();
                boolean isMultiLang = false;
                for (String key : langObj.keySet()) {
                    if (key.contains("_")) {
                        isMultiLang = true;
                        break;
                    }
                }

                if (isMultiLang) {
                    boolean hasStyledLangs = false;
                    for (Map.Entry<String, JsonElement> entry : langObj.entrySet()) {
                        if (entry.getValue().isJsonObject()) {
                            hasStyledLangs = true;
                            break;
                        }
                    }

                    if (hasStyledLangs) {
                        langStyledText = new HashMap<>();
                        for (Map.Entry<String, JsonElement> entry : langObj.entrySet()) {
                            if (entry.getValue().isJsonObject()) {
                                JsonObject styleObj = entry.getValue().getAsJsonObject();
                                String langTextStr = styleObj.has("text") ? styleObj.get("text").getAsString() : "";
                                int langColor = styleObj.has("color") ? parseColor(styleObj.get("color").getAsString(), DatatipConfig.DEFAULT_COLOR.get()) : DatatipConfig.DEFAULT_COLOR.get();
                                boolean langBold = styleObj.has("bold") && styleObj.get("bold").getAsBoolean();
                                boolean langItalic = styleObj.has("italic") && styleObj.get("italic").getAsBoolean();
                                boolean langUnderlined = styleObj.has("underlined") && styleObj.get("underlined").getAsBoolean();
                                boolean langStrikethrough = styleObj.has("strikethrough") && styleObj.get("strikethrough").getAsBoolean();
                                TextContent.TextAlign langAlign = TextContent.TextAlign.LEFT;
                                if (styleObj.has("align")) {
                                    String a = styleObj.get("align").getAsString();
                                    if ("center".equals(a)) langAlign = TextContent.TextAlign.CENTER;
                                    else if ("right".equals(a)) langAlign = TextContent.TextAlign.RIGHT;
                                }
                                boolean langShift = styleObj.has("shift") && styleObj.get("shift").getAsBoolean();
                                langStyledText.put(entry.getKey(), new TextContent.LangStyle(
                                    langTextStr, langColor, langBold, langItalic, langUnderlined, langStrikethrough, langAlign, langShift));
                            } else if (entry.getValue().isJsonPrimitive()) {
                                langStyledText.put(entry.getKey(), new TextContent.LangStyle(
                                    entry.getValue().getAsString(), DatatipConfig.DEFAULT_COLOR.get(), false, false, false, false));
                            }
                        }
                    } else {
                        langText = new HashMap<>();
                        for (Map.Entry<String, JsonElement> entry : langObj.entrySet()) {
                            langText.put(entry.getKey(), entry.getValue().getAsString());
                        }
                    }
                }
            }
        }

        Component component = null;
        if (context.has(json, "trans")) {
            String transKey = context.getString(json, "trans", "");
            component = Component.translatable(transKey);
        }

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

        boolean shadow = context.getBoolean(json, "shadow", true);
        int lineHeight = context.getInt(json, "lineHeight", 12);
        int maxWidth = context.getInt(json, "maxWidth", 0);
        boolean bold = context.getBoolean(json, "bold", false);
        boolean italic = context.getBoolean(json, "italic", false);
        boolean underlined = context.getBoolean(json, "underlined", false);
        boolean strikethrough = context.getBoolean(json, "strikethrough", false);
        boolean shift = context.getBoolean(json, "shift", false);

        ResourceLocation font = null;
        if (context.has(json, "font")) {
            String fontStr = context.getString(json, "font", "");
            if (!fontStr.isEmpty()) {
                font = ResourceLocation.tryParse(fontStr);
            }
        }

        TextContent.TextAlign align = TextContent.TextAlign.LEFT;
        String alignStr = context.getString(json, "align", "left");
        if ("center".equals(alignStr)) {
            align = TextContent.TextAlign.CENTER;
        } else if ("right".equals(alignStr)) {
            align = TextContent.TextAlign.RIGHT;
        }

        if (component != null) {
            return new TextContent(null, component, null, null, null, font, color, colorExpression, shadow, align, lineHeight, maxWidth, bold, italic, underlined, strikethrough, shift);
        } else if (langStyledText != null && !langStyledText.isEmpty()) {
            return new TextContent(null, null, null, null, langStyledText, font, color, colorExpression, shadow, align, lineHeight, maxWidth, bold, italic, underlined, strikethrough, shift);
        } else if (langText != null && !langText.isEmpty()) {
            return new TextContent(null, null, null, langText, null, font, color, colorExpression, shadow, align, lineHeight, maxWidth, bold, italic, underlined, strikethrough, shift);
        } else if (text != null) {
            return new TextContent(text, null, null, null, null, font, color, colorExpression, shadow, align, lineHeight, maxWidth, bold, italic, underlined, strikethrough, shift);
        }

        return TextContent.of("");
    }

    private static int parseColor(String colorStr, int defaultValue) {
        if (colorStr == null || colorStr.isEmpty()) return defaultValue;
        return switch (colorStr.toLowerCase()) {
            case "black" -> 0xFF000000;
            case "dark_blue" -> 0xFF0000AA;
            case "dark_green" -> 0xFF00AA00;
            case "dark_aqua" -> 0xFF00AAAA;
            case "dark_red" -> 0xFFAA0000;
            case "dark_purple" -> 0xFFAA00AA;
            case "gold" -> 0xFFFFAA00;
            case "gray", "grey" -> 0xFFAAAAAA;
            case "dark_gray", "dark_grey" -> 0xFF555555;
            case "blue" -> 0xFF5555FF;
            case "green" -> 0xFF55FF55;
            case "aqua" -> 0xFF55FFFF;
            case "red" -> 0xFFFF5555;
            case "light_purple" -> 0xFFFF55FF;
            case "yellow" -> 0xFFFFFF55;
            case "white" -> 0xFFFFFFFF;
            default -> {
                if (colorStr.startsWith("#")) {
                    try {
                        yield (int) Long.parseLong(colorStr.substring(1), 16) | 0xFF000000;
                    } catch (NumberFormatException e) {
                        yield defaultValue;
                    }
                }
                yield defaultValue;
            }
        };
    }
}
