package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.DividerContent;
import com.google.gson.JsonObject;

import java.util.Locale;

/**
 * DividerContent 解析器。
 * <p>
 * 负责将 JSON 对象解析为 {@link DividerContent} 实例。
 * 分割线用于在 tooltip 中分隔不同内容区域。
 * </p>
 *
 * <h3>支持的 JSON 格式</h3>
 * <pre>{@code
 * // 基础分割线（填充整个宽度）
 * {
 *   "type": "divider",
 *   "color": "#555555"
 * }
 *
 * // 固定宽度
 * {
 *   "type": "divider",
 *   "color": "#555555",
 *   "width": 100,
 *   "widthMode": "fixed"
 * }
 *
 * // 居中显示
 * {
 *   "type": "divider",
 *   "color": "#555555",
 *   "width": 80,
 *   "widthMode": "centered"
 * }
 *
 * // 虚线样式
 * {
 *   "type": "divider",
 *   "color": "#555555",
 *   "style": "dashed"
 * }
 * }</pre>
 *
 * <h3>字段说明</h3>
 * <table border="1">
 *   <tr><th>字段</th><th>类型</th><th>默认值</th><th>说明</th></tr>
 *   <tr><td>color</td><td>String</td><td>"#555555"</td><td>颜色（命名色或十六进制）</td></tr>
 *   <tr><td>thickness</td><td>int</td><td>1</td><td>线条粗细（像素）</td></tr>
 *   <tr><td>width</td><td>int</td><td>0</td><td>宽度（像素，0=自动）</td></tr>
 *   <tr><td>widthMode</td><td>String</td><td>"fill"</td><td>宽度模式（fill/fixed/centered）</td></tr>
 *   <tr><td>marginTop</td><td>int</td><td>2</td><td>上边距（像素）</td></tr>
 *   <tr><td>marginBottom</td><td>int</td><td>2</td><td>下边距（像素）</td></tr>
 *   <tr><td>style</td><td>String</td><td>"solid"</td><td>线条样式（solid/dashed/dotted）</td></tr>
 * </table>
 *
 * @author cooobird
 * @see DividerContent 分割线内容类
 * @since 1.2.0
 */
public class DividerContentParser implements ContentParser {

    @Override
    public DividerContent parse(JsonObject json, ParseContext context) {
        // 获取基本属性
        int color = context.getColor(json, "color", 0xFF555555);
        int thickness = context.getInt(json, "thickness", 1);
        int width = context.getInt(json, "width", 0);
        int marginTop = context.getInt(json, "marginTop", 2);
        int marginBottom = context.getInt(json, "marginBottom", 2);

        // 解析线条样式
        String styleStr = context.getString(json, "style", "solid");
        DividerContent.DividerStyle style = switch (styleStr.toLowerCase(Locale.ROOT)) {
            case "dashed" -> DividerContent.DividerStyle.DASHED;
            case "dotted" -> DividerContent.DividerStyle.DOTTED;
            default -> DividerContent.DividerStyle.SOLID;
        };

        // 解析宽度模式
        String widthModeStr = context.getString(json, "widthMode", "fill");
        DividerContent.WidthMode widthMode = switch (widthModeStr.toLowerCase(Locale.ROOT)) {
            case "fixed" -> DividerContent.WidthMode.FIXED;
            case "centered" -> DividerContent.WidthMode.CENTERED;
            default -> DividerContent.WidthMode.FILL;
        };

        return new DividerContent(color, thickness, width, marginTop, marginBottom, style, widthMode);
    }
}
