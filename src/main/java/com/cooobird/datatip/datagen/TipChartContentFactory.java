package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.content.ChartContent;
import com.cooobird.datatip.api.util.ColorParser;
import net.minecraft.network.chat.Component;

/**
 * 图表类 TipContent 创建工具。
 */
final class TipChartContentFactory {
    private TipChartContentFactory() {
    }

    static ChartContent chart(String chartType, int width, int height) {
        return switch (chartType.toLowerCase()) {
            case "pie" -> ChartContent.pie(width);
            case "line" -> ChartContent.line(width, height);
            default -> ChartContent.bar(width, height);
        };
    }

    static ChartContent chart(String chartType, int width, int height, String title, ChartContent.ChartEntry... entries) {
        ChartContent chart = chart(chartType, width, height).title(Component.literal(title));
        for (ChartContent.ChartEntry entry : entries) {
            chart = chart.addEntry(entry.label(), entry.valueExpr(), entry.color());
        }
        return chart;
    }

    static ChartContent.ChartEntry chartEntry(String label, double value, String color) {
        return new ChartContent.ChartEntry(label, String.valueOf(value), parseColor(color));
    }

    static ChartContent.ChartEntry chartEntry(String label, String valueExpr, String color) {
        return new ChartContent.ChartEntry(label, valueExpr, parseColor(color));
    }

    private static int parseColor(String color) {
        return ColorParser.parse(color, ColorParser.WHITE);
    }
}
