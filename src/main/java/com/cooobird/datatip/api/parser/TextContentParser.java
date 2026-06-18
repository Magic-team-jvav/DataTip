package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.TextContent;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;

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

        JsonElement textElement = json.get("text");
        if (textElement != null) {
            if (textElement.isJsonPrimitive()) {
                // 简单字符串
                text = textElement.getAsString();
            } else if (textElement.isJsonObject()) {
                // 多语言对象 {"zh_cn": "...", "en_us": "..."}
                langText = new HashMap<>();
                JsonObject langObj = textElement.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : langObj.entrySet()) {
                    langText.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
        }

        Component component = null;

        // 如果有 trans 字段，创建翻译组件（优先级最高）
        if (context.has(json, "trans")) {
            String transKey = context.getString(json, "trans", "");
            component = Component.translatable(transKey);
        }

        // 获取颜色 - 支持静态颜色和表达式
        int color = 0xFFFFFF;
        String colorExpression = null;

        JsonElement colorElement = json.get("color");
        if (colorElement != null && colorElement.isJsonPrimitive()) {
            String colorStr = colorElement.getAsString();
            // 检查是否是表达式（包含变量或运算符）
            if (colorStr.contains("{") && colorStr.contains("}")) {
                // 颜色表达式，如 "{durability > 100 ? 'green' : 'red'}"
                colorExpression = colorStr;
                color = 0xFFFFFF; // 默认颜色，渲染时会动态解析
            } else {
                // 静态颜色
                color = context.getColor(json, "color", 0xFFFFFF);
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

        // 获取对齐方式
        TextContent.TextAlign align = TextContent.TextAlign.LEFT;
        String alignStr = context.getString(json, "align", "left");
        if ("center".equals(alignStr)) {
            align = TextContent.TextAlign.CENTER;
        } else if ("right".equals(alignStr)) {
            align = TextContent.TextAlign.RIGHT;
        }

        // 构建 TextContent（按优先级：component > langText > text）
        if (component != null) {
            return new TextContent(null, component, null, null, null, color, colorExpression, shadow, align, lineHeight, maxWidth, bold, italic, underlined, strikethrough, shift);
        } else if (langText != null && !langText.isEmpty()) {
            // 多语言文本
            return new TextContent(null, null, null, langText, null, color, colorExpression, shadow, align, lineHeight, maxWidth, bold, italic, underlined, strikethrough, shift);
        } else if (text != null) {
            return new TextContent(text, null, null, null, null, color, colorExpression, shadow, align, lineHeight, maxWidth, bold, italic, underlined, strikethrough, shift);
        }

        // 默认返回空文本
        return TextContent.of("");
    }
}
