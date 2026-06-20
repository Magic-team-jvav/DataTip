package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.BaseTextContent;
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
 * <h3>支持的 JSON 格式</h3>
 * <pre>{@code
 * // 基础文本
 * {
 *   "type": "text",
 *   "text": "Hello World",
 *   "color": "white"
 * }
 *
 * // 带样式的文本
 * {
 *   "type": "text",
 *   "text": "粗体红色",
 *   "color": "red",
 *   "bold": true,
 *   "italic": true
 * }
 *
 * // 居中对齐
 * {
 *   "type": "text",
 *   "text": "居中文本",
 *   "color": "gold",
 *   "align": "center"
 * }
 *
 * // 多语言文本
 * {
 *   "type": "text",
 *   "text": {
 *     "zh_cn": "你好世界",
 *     "en_us": "Hello World"
 *   },
 *   "color": "aqua"
 * }
 *
 * // 颜色表达式（动态颜色）
 * {
 *   "type": "text",
 *   "text": "状态",
 *   "color": "{durability > 100 ? 'green' : 'red'}"
 * }
 *
 * // 自动换行
 * {
 *   "type": "text",
 *   "text": "这是一段很长的文本...",
 *   "color": "white",
 *   "maxWidth": 200
 * }
 *
 * // Shift 显示
 * {
 *   "type": "text",
 *   "text": "需要按 Shift 才能看到",
 *   "color": "gray",
 *   "shift": true
 * }
 * }</pre>
 *
 * <h3>字段说明</h3>
 * <table border="1">
 *   <tr><th>字段</th><th>类型</th><th>默认值</th><th>说明</th></tr>
 *   <tr><td>text</td><td>String/Object</td><td>null</td><td>文本内容（支持多语言对象）</td></tr>
 *   <tr><td>trans</td><td>String</td><td>null</td><td>翻译键（优先级高于 text）</td></tr>
 *   <tr><td>color</td><td>String</td><td>"white"</td><td>颜色（命名色或十六进制，支持表达式）</td></tr>
 *   <tr><td>shadow</td><td>boolean</td><td>true</td><td>是否显示阴影</td></tr>
 *   <tr><td>align</td><td>String</td><td>"left"</td><td>对齐方式（left/center/right）</td></tr>
 *   <tr><td>lineHeight</td><td>int</td><td>12</td><td>行高（像素）</td></tr>
 *   <tr><td>maxWidth</td><td>int</td><td>0</td><td>最大宽度（0=不换行）</td></tr>
 *   <tr><td>bold</td><td>boolean</td><td>false</td><td>是否粗体</td></tr>
 *   <tr><td>italic</td><td>boolean</td><td>false</td><td>是否斜体</td></tr>
 *   <tr><td>underlined</td><td>boolean</td><td>false</td><td>是否下划线</td></tr>
 *   <tr><td>strikethrough</td><td>boolean</td><td>false</td><td>是否删除线</td></tr>
 *   <tr><td>shift</td><td>boolean</td><td>false</td><td>是否需要按 Shift 才显示</td></tr>
 * </table>
 *
 * @author cooobird
 * @see TextContent 文本内容类
 * @since 1.2.0
 */
public class TextContentParser implements ContentParser {

    @Override
    public TextContent parse(JsonObject json, ParseContext context) {
        // 获取文本内容 - 支持字符串或对象（多语言）
        String text = null;
        Map<String, String> langText = null;
        Map<String, BaseTextContent.LangStyle> langStyledText = null;

        JsonElement textElement = json.get("text");
        if (textElement != null) {
            if (textElement.isJsonPrimitive()) {
                // 简单字符串
                text = textElement.getAsString();
            } else if (textElement.isJsonObject()) {
                // 检查是否是多语言对象（key 包含 "_"，如 "zh_cn", "en_us"）
                JsonObject langObj = textElement.getAsJsonObject();
                boolean isMultiLang = false;
                for (String key : langObj.keySet()) {
                    if (key.contains("_")) {
                        isMultiLang = true;
                        break;
                    }
                }

                if (isMultiLang) {
                    // 检查是否有带样式的语言（值为对象而非字符串）
                    boolean hasStyledLangs = false;
                    for (Map.Entry<String, JsonElement> entry : langObj.entrySet()) {
                        if (entry.getValue().isJsonObject()) {
                            hasStyledLangs = true;
                            break;
                        }
                    }

                    if (hasStyledLangs) {
                        // 每语言独立样式：{"zh_cn": {"text": "...", "color": "red"}, "en_us": {"text": "...", "italic": true}}
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
                                BaseTextContent.TextAlign langAlign = BaseTextContent.TextAlign.LEFT;
                                if (styleObj.has("align")) {
                                    String a = styleObj.get("align").getAsString();
                                    if ("center".equals(a)) langAlign = BaseTextContent.TextAlign.CENTER;
                                    else if ("right".equals(a)) langAlign = BaseTextContent.TextAlign.RIGHT;
                                }
                                boolean langShift = styleObj.has("shift") && styleObj.get("shift").getAsBoolean();
                                langStyledText.put(entry.getKey(), new BaseTextContent.LangStyle(
                                    langTextStr, langColor, langBold, langItalic, langUnderlined, langStrikethrough, langAlign, langShift));
                            } else if (entry.getValue().isJsonPrimitive()) {
                                // 混合格式：有些语言是字符串，有些是对象
                                langStyledText.put(entry.getKey(), new BaseTextContent.LangStyle(
                                    entry.getValue().getAsString(), DatatipConfig.DEFAULT_COLOR.get(), false, false, false, false));
                            }
                        }
                    } else {
                        // 简单多语言：{"zh_cn": "...", "en_us": "..."}
                        langText = new HashMap<>();
                        for (Map.Entry<String, JsonElement> entry : langObj.entrySet()) {
                            langText.put(entry.getKey(), entry.getValue().getAsString());
                        }
                    }
                }
                // 如果 key 不包含 "_"，不是多语言对象，忽略
            }
        }

        Component component = null;

        // 如果有 trans 字段，创建翻译组件（优先级最高）
        if (context.has(json, "trans")) {
            String transKey = context.getString(json, "trans", "");
            component = Component.translatable(transKey);
        }

        // 获取颜色 - 支持静态颜色和表达式
        int color = DatatipConfig.DEFAULT_COLOR.get();
        String colorExpression = null;

        JsonElement colorElement = json.get("color");
        if (colorElement != null && colorElement.isJsonPrimitive()) {
            String colorStr = colorElement.getAsString();
            // 检查是否是表达式（包含变量或运算符）
            if (colorStr.contains("{") && colorStr.contains("}")) {
                // 颜色表达式，如 "{durability > 100 ? 'green' : 'red'}"
                colorExpression = colorStr;
                color = DatatipConfig.DEFAULT_COLOR.get(); // 默认颜色，渲染时会动态解析
            } else {
                // 静态颜色
                color = context.getColor(json, "color", DatatipConfig.DEFAULT_COLOR.get());
            }
        }

        // 获取样式选项
        boolean shadow = context.getBoolean(json, "shadow", true);
        int lineHeight = context.getInt(json, "lineHeight", 12);
        int maxWidth = context.getInt(json, "maxWidth", 0);
        boolean bold = context.getBoolean(json, "bold", false);
        boolean italic = context.getBoolean(json, "italic", false);
        boolean underlined = context.getBoolean(json, "underlined", false);
        boolean strikethrough = context.getBoolean(json, "strikethrough", false);
        boolean shift = context.getBoolean(json, "shift", false);

        // 获取自定义字体
        ResourceLocation font = null;
        if (context.has(json, "font")) {
            String fontStr = context.getString(json, "font", "");
            if (!fontStr.isEmpty()) {
                font = ResourceLocation.tryParse(fontStr);
            }
        }

        // 获取对齐方式
        BaseTextContent.TextAlign align = BaseTextContent.TextAlign.LEFT;
        String alignStr = context.getString(json, "align", "left");
        if ("center".equals(alignStr)) {
            align = BaseTextContent.TextAlign.CENTER;
        } else if ("right".equals(alignStr)) {
            align = BaseTextContent.TextAlign.RIGHT;
        }

        // 构建 TextContent（按优先级：component > langStyledText > langText > text）
        if (component != null) {
            return new TextContent(null, component, null, null, null, font, color, colorExpression, shadow, align, lineHeight, maxWidth, bold, italic, underlined, strikethrough, shift);
        } else if (langStyledText != null && !langStyledText.isEmpty()) {
            // 带样式的多语言文本
            return new TextContent(null, null, null, null, langStyledText, font, color, colorExpression, shadow, align, lineHeight, maxWidth, bold, italic, underlined, strikethrough, shift);
        } else if (langText != null && !langText.isEmpty()) {
            // 简单多语言文本
            return new TextContent(null, null, null, langText, null, font, color, colorExpression, shadow, align, lineHeight, maxWidth, bold, italic, underlined, strikethrough, shift);
        } else if (text != null) {
            return new TextContent(text, null, null, null, null, font, color, colorExpression, shadow, align, lineHeight, maxWidth, bold, italic, underlined, strikethrough, shift);
        }

        // 默认返回空文本
        return TextContent.of("");
    }

    // 解析颜色字符串
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
