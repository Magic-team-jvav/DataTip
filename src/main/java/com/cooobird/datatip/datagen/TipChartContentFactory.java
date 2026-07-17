package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.content.ChartContent;
import com.cooobird.datatip.api.text.LocalizedText;
import com.cooobird.datatip.api.util.ColorParser;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 图表类 TipContent 创建工具。
 */
final class TipChartContentFactory {
    private TipChartContentFactory() {
    }

    static ChartContent chart(String chartType, int width, int height) {
        return switch (chartType.toLowerCase(Locale.ROOT)) {
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

    static ChartContent chart(String chartType, int width, int height, Map<String, String> title,
                              boolean showLabels, boolean showValues, String titleColor, String labelColor,
                              String valueColor, String zeroLineColor, ChartContent.ChartEntry... entries) {
        ChartContent base = chart(chartType, width, height);
        return new ChartContent(base.type(), new java.util.ArrayList<>(java.util.List.of(entries)), width, height,
            localized(title), showLabels, showValues, parseColor(titleColor), parseColor(labelColor),
            parseColor(valueColor), parseColor(zeroLineColor));
    }

    static ChartContent.ChartEntry chartEntry(String label, double value, String color) {
        return new ChartContent.ChartEntry(label, String.valueOf(value), parseColor(color));
    }

    static ChartContent.ChartEntry chartEntry(String label, String valueExpr, String color) {
        return new ChartContent.ChartEntry(label, valueExpr, parseColor(color));
    }

    static ChartContent.ChartEntry chartEntry(Map<String, String> label, String valueExpr, String color) {
        return new ChartContent.ChartEntry(localized(label), valueExpr, parseColor(color));
    }

    private static LocalizedText localized(Map<String, String> values) {
        if (values == null || values.isEmpty()) return null;
        Map<String, Component> components = new LinkedHashMap<>();
        values.forEach((language, value) -> components.put(language, Component.literal(value)));
        return LocalizedText.languages(components);
    }

    private static int parseColor(String color) {
        return ColorParser.parse(color, ColorParser.WHITE);
    }
}
