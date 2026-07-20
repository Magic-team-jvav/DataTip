package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.BaseTextContent;
import com.cooobird.datatip.api.content.TextContentDefaults;
import com.cooobird.datatip.api.content.TypewriterContent;
import com.cooobird.datatip.config.DatatipConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TypewriterContent 解析器。
 * <p>
 * 负责将 JSON 对象解析为 {@link TypewriterContent} 实例。
 * 打字机效果会逐字显示文本，支持多语言和完整样式。
 * </p>
 *
 * @author cooobird
 * @since 1.2.0
 */
public class TypewriterContentParser implements ContentParser {

    @Override
    public TypewriterContent parse(JsonObject json, ParseContext context) {
        int charsPerSecond = context.getInt(json, "charsPerSecond", 2);
        int pauseSeconds = context.getInt(json, "pauseSeconds", 1);
        boolean loop = context.getBoolean(json, "loop", false);
        int color = context.getColor(json, "color", DatatipConfig.defaultColor());

        boolean shadow = context.getBoolean(json, "shadow", true);
        int lineHeight = context.getInt(json, "lineHeight", TextContentDefaults.lineHeight());
        boolean bold = context.getBoolean(json, "bold", false);
        boolean italic = context.getBoolean(json, "italic", false);
        boolean underlined = context.getBoolean(json, "underlined", false);
        boolean strikethrough = context.getBoolean(json, "strikethrough", false);

        BaseTextContent.TextAlign align = BaseTextContent.TextAlign.LEFT;
        String alignStr = context.getString(json, "align", "left");
        if ("center".equals(alignStr)) align = BaseTextContent.TextAlign.CENTER;
        else if ("right".equals(alignStr)) align = BaseTextContent.TextAlign.RIGHT;

        // 颜色表达式
        String colorExpression = null;
        JsonElement colorElement = json.get("color");
        if (colorElement != null && colorElement.isJsonPrimitive()) {
            String colorStr = colorElement.getAsString();
            if (colorStr.contains("{") && colorStr.contains("}")) {
                colorExpression = colorStr;
            }
        }

        ResourceLocation font = null;
        if (context.has(json, "font")) {
            String fontStr = context.getString(json, "font", "");
            if (!fontStr.isEmpty()) font = ResourceLocation.tryParse(fontStr);
        }

        // 解析文本行
        List<String> lines = new ArrayList<>();
        Map<String, List<String>> langLines = null;
        Map<String, List<TypewriterContent.LangStyle>> langStyledLines = null;

        JsonElement linesElement = json.get("lines");
        if (linesElement != null) {
            if (linesElement.isJsonArray()) {
                for (var element : linesElement.getAsJsonArray()) {
                    if (element.isJsonPrimitive()) lines.add(element.getAsString());
                }
            } else if (linesElement.isJsonObject()) {
                JsonObject langObj = linesElement.getAsJsonObject();
                boolean isMultiLang = false;
                for (String key : langObj.keySet()) {
                    if (key.contains("_")) {
                        isMultiLang = true;
                        break;
                    }
                }

                if (isMultiLang) {
                    boolean hasStyledLines = false;
                    for (Map.Entry<String, JsonElement> entry : langObj.entrySet()) {
                        if (entry.getValue().isJsonArray()) {
                            for (var el : entry.getValue().getAsJsonArray()) {
                                if (el.isJsonObject()) {
                                    hasStyledLines = true;
                                    break;
                                }
                            }
                        }
                        if (hasStyledLines) break;
                    }

                    if (hasStyledLines) {
                        langStyledLines = new HashMap<>();
                        for (Map.Entry<String, JsonElement> entry : langObj.entrySet()) {
                            if (entry.getValue().isJsonArray()) {
                                List<TypewriterContent.LangStyle> styledList = new ArrayList<>();
                                for (var el : entry.getValue().getAsJsonArray()) {
                                    if (el.isJsonPrimitive()) {
                                        styledList.add(new TypewriterContent.LangStyle(
                                            el.getAsString(), DatatipConfig.defaultColor(), false, false, false, false));
                                    } else if (el.isJsonObject()) {
                                        JsonObject lineObj = el.getAsJsonObject();
                                        String lineText = lineObj.has("text") ? lineObj.get("text").getAsString() : "";
                                        int lineColor = lineObj.has("color") ? parseColor(lineObj.get("color").getAsString()) : color;
                                        boolean lineBold = lineObj.has("bold") && lineObj.get("bold").getAsBoolean();
                                        boolean lineItalic = lineObj.has("italic") && lineObj.get("italic").getAsBoolean();
                                        boolean lineUnderlined = lineObj.has("underlined") && lineObj.get("underlined").getAsBoolean();
                                        boolean lineStrikethrough = lineObj.has("strikethrough") && lineObj.get("strikethrough").getAsBoolean();
                                        styledList.add(new TypewriterContent.LangStyle(
                                            lineText, lineColor, lineBold, lineItalic, lineUnderlined, lineStrikethrough));
                                    }
                                }
                                langStyledLines.put(entry.getKey(), styledList);
                            }
                        }
                    } else {
                        langLines = new HashMap<>();
                        for (Map.Entry<String, JsonElement> entry : langObj.entrySet()) {
                            if (entry.getValue().isJsonArray()) {
                                List<String> langLinesList = new ArrayList<>();
                                for (var el : entry.getValue().getAsJsonArray()) {
                                    if (el.isJsonPrimitive()) langLinesList.add(el.getAsString());
                                }
                                langLines.put(entry.getKey(), langLinesList);
                            }
                        }
                    }
                }
            }
        }

        return new TypewriterContent(lines, langLines, langStyledLines, charsPerSecond, pauseSeconds, loop, color,
            colorExpression, font, bold, italic, underlined, strikethrough, align, shadow, lineHeight, false);
    }

    private static int parseColor(String colorStr) {
        return BaseTextContent.parseColorString(colorStr, DatatipConfig.defaultColor());
    }
}
