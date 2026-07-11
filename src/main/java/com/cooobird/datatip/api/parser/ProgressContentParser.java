package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.ProgressContent;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;

/**
 * ProgressContent 解析器。
 * <p>
 * 负责将 JSON 对象解析为 {@link ProgressContent} 实例。
 * 进度条用于显示进度、血量、耐久等数值。
 * </p>
 *
 * <h3>支持的 JSON 格式</h3>
 * <pre>{@code
 * // 基础进度条
 * {
 *   "type": "progress",
 *   "progress": 0.75,
 *   "width": 100,
 *   "height": 8
 * }
 *
 * // 带标签
 * {
 *   "type": "progress",
 *   "progress": 0.75,
 *   "width": 100,
 *   "height": 6,
 *   "showLabel": true,
 *   "label": "75%",
 *   "labelAlign": "right"
 * }
 *
 * // 分段样式
 * {
 *   "type": "progress",
 *   "progress": 0.5,
 *   "width": 100,
 *   "height": 8,
 *   "style": "segmented"
 * }
 *
 * // 动画效果
 * {
 *   "type": "progress",
 *   "progress": 0.9,
 *   "width": 100,
 *   "height": 6,
 *   "colorFg": "#FFD700",
 *   "animated": true,
 *   "animSpeed": 3
 * }
 * }</pre>
 *
 * <h3>字段说明</h3>
 * <table border="1">
 *   <tr><th>字段</th><th>类型</th><th>默认值</th><th>说明</th></tr>
 *   <tr><td>progress</td><td>float</td><td>0.0</td><td>进度值（0.0-1.0）</td></tr>
 *   <tr><td>width</td><td>int</td><td>100</td><td>宽度（像素）</td></tr>
 *   <tr><td>height</td><td>int</td><td>8</td><td>高度（像素）</td></tr>
 *   <tr><td>colorFg</td><td>String</td><td>"#55FF55"</td><td>前景色</td></tr>
 *   <tr><td>colorBg</td><td>String</td><td>"#333333"</td><td>背景色</td></tr>
 *   <tr><td>colorFgLight</td><td>String</td><td>null</td><td>渐变前景色（可选）</td></tr>
 *   <tr><td>colorBgDark</td><td>String</td><td>null</td><td>渐变背景色（可选）</td></tr>
 *   <tr><td>style</td><td>String</td><td>"gradient"</td><td>样式（gradient/flat/segmented/animated）</td></tr>
 *   <tr><td>showLabel</td><td>boolean</td><td>false</td><td>是否显示标签</td></tr>
 *   <tr><td>label</td><td>String</td><td>null</td><td>自定义标签文本</td></tr>
 *   <tr><td>labelAlign</td><td>String</td><td>"left"</td><td>标签对齐（left/center/right）</td></tr>
 *   <tr><td>animated</td><td>boolean</td><td>false</td><td>是否启用动画</td></tr>
 *   <tr><td>animSpeed</td><td>int</td><td>2</td><td>动画速度</td></tr>
 * </table>
 *
 * @author cooobird
 * @see ProgressContent 进度条内容类
 * @since 1.2.0
 */
public class ProgressContentParser implements ContentParser {

    @Override
    public ProgressContent parse(JsonObject json, ParseContext context) {
        // 获取进度值
        float progress = context.getFloat(json, "progress", 0.0f);

        // 获取尺寸
        int width = context.getInt(json, "width", 100);
        int height = context.getInt(json, "height", 8);

        // 获取颜色
        int colorFg = context.getColor(json, "colorFg", 0xFF55FF55);
        int colorBg = context.getColor(json, "colorBg", 0xFF333333);

        // 可选的渐变颜色
        Integer colorFgLight = context.has(json, "colorFgLight") ?
            context.getColor(json, "colorFgLight", 0xFF81C784) : null;
        Integer colorBgDark = context.has(json, "colorBgDark") ?
            context.getColor(json, "colorBgDark", 0xFF1A1A1A) : null;

        // 解析样式
        String styleStr = context.getString(json, "style", "gradient");
        ProgressContent.ProgressStyle style = switch (styleStr.toLowerCase()) {
            case "flat" -> ProgressContent.ProgressStyle.FLAT;
            case "segmented" -> ProgressContent.ProgressStyle.SEGMENTED;
            case "animated" -> ProgressContent.ProgressStyle.ANIMATED;
            default -> ProgressContent.ProgressStyle.GRADIENT;
        };

        // 获取标签选项
        boolean showLabel = context.getBoolean(json, "showLabel", false);
        Component customLabel = null;
        if (context.has(json, "label")) {
            customLabel = LocalizedTextParser.parse(json, "label", context);
            showLabel = true;
        }

        // 获取动画选项
        boolean animated = context.getBoolean(json, "animated", false);
        if (style == ProgressContent.ProgressStyle.ANIMATED) {
            animated = true;
        }
        int animSpeed = context.getInt(json, "animSpeed", 2);

        // 获取标签对齐方式
        String labelAlignStr = context.getString(json, "labelAlign", "left");
        ProgressContent.LabelAlign labelAlign = switch (labelAlignStr.toLowerCase()) {
            case "center" -> ProgressContent.LabelAlign.CENTER;
            case "right" -> ProgressContent.LabelAlign.RIGHT;
            default -> ProgressContent.LabelAlign.LEFT;
        };

        return new ProgressContent(progress, width, height, colorFg, colorBg,
            colorFgLight, colorBgDark, style, showLabel, customLabel, labelAlign, animated, animSpeed);
    }
}
