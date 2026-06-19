package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.ChartContent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;

/**
 * ChartContent 解析器。
 * <p>
 * 负责将 JSON 对象解析为 {@link ChartContent} 实例。
 * 图表用于可视化数据，支持柱状图、饼图、折线图。
 * </p>
 *
 * <h3>支持的 JSON 格式</h3>
 * <pre>{@code
 * // 柱状图
 * {
 *   "type": "chart",
 *   "chartType": "bar",
 *   "width": 100,
 *   "height": 60,
 *   "title": "数据统计",
 *   "entries": [
 *     {"label": "A", "value": 10, "color": "#FF5555"},
 *     {"label": "B", "value": 20, "color": "#55FF55"},
 *     {"label": "C", "value": 15, "color": "#5555FF"}
 *   ]
 * }
 *
 * // 饼图
 * {
 *   "type": "chart",
 *   "chartType": "pie",
 *   "width": 80,
 *   "entries": [
 *     {"label": "苹果", "value": 30, "color": "#FF5555"},
 *     {"label": "香蕉", "value": 20, "color": "#FFFF55"}
 *   ]
 * }
 *
 * // 折线图
 * {
 *   "type": "chart",
 *   "chartType": "line",
 *   "width": 120,
 *   "height": 60,
 *   "entries": [...]
 * }
 * }</pre>
 *
 * <h3>字段说明</h3>
 * <table border="1">
 *   <tr><th>字段</th><th>类型</th><th>默认值</th><th>说明</th></tr>
 *   <tr><td>chartType</td><td>String</td><td>"bar"</td><td>图表类型（bar/pie/line）</td></tr>
 *   <tr><td>width</td><td>int</td><td>100</td><td>宽度（像素）</td></tr>
 *   <tr><td>height</td><td>int</td><td>60</td><td>高度（像素）</td></tr>
 *   <tr><td>title</td><td>String</td><td>null</td><td>图表标题</td></tr>
 *   <tr><td>entries</td><td>Array</td><td>[]</td><td>数据条目数组</td></tr>
 *   <tr><td>align</td><td>String</td><td>"left"</td><td>对齐方式（left/center/right）</td></tr>
 * </table>
 *
 * @author cooobird
 * @see ChartContent 图表内容类
 * @since 1.2.0
 */
public class ChartContentParser implements ContentParser {

    @Override
    public ChartContent parse(JsonObject json, ParseContext context) {
        // 解析图表类型
        String chartTypeStr = context.getString(json, "chartType", "bar");
        ChartContent.ChartType chartType = switch (chartTypeStr.toLowerCase()) {
            case "pie" -> ChartContent.ChartType.PIE;
            case "line" -> ChartContent.ChartType.LINE;
            default -> ChartContent.ChartType.BAR;
        };

        // 获取尺寸
        int width = context.getInt(json, "width", 100);
        int height = context.getInt(json, "height", 60);

        // 获取颜色参数
        int titleColor = context.getColor(json, "titleColor", 0xFFFFFF);
        int labelColor = context.getColor(json, "labelColor", 0xAAAAAA);
        int valueColor = context.getColor(json, "valueColor", 0xFFFFFF);
        int zeroLineColor = context.getColor(json, "zeroLineColor", 0x888888);

        // 创建图表
        ChartContent chart = ChartContent.withColors(chartType, width, height, titleColor, labelColor, valueColor, zeroLineColor);

        // 设置标题
        if (context.has(json, "title")) {
            String titleStr = context.getString(json, "title", "");
            chart = chart.title(Component.literal(titleStr));
        }

        // 解析数据条目
        if (context.has(json, "entries")) {
            JsonArray entriesArray = context.getArray(json, "entries");
            if (entriesArray != null) {
                for (var element : entriesArray) {
                    if (element.isJsonObject()) {
                        JsonObject entryObj = element.getAsJsonObject();
                        String label = context.getString(entryObj, "label", "");
                        // 支持字符串格式的 value（可能是数字或变量表达式）
                        String valueStr = context.getString(entryObj, "value", "0");
                        int color = context.getColor(entryObj, "color", 0xFFFFFF);
                        chart = chart.addEntry(label, valueStr, color);
                    }
                }
            }
        }

        return chart;
    }
}
