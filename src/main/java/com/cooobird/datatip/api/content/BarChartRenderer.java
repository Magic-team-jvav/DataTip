package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipRenderContext;

import java.util.Arrays;
import java.util.List;

/**
 * 柱状图渲染器。
 */
final class BarChartRenderer {
    private BarChartRenderer() {
    }

    static void render(ChartContent chart, TipRenderContext context, int x, int y, int maxWidth) {
        List<ChartContent.ChartEntry> entries = chart.entries();
        int renderWidth = Math.max(1, Math.min(chart.width(), maxWidth));

        double[] values = entries.stream()
            .mapToDouble(entry -> entry.resolveValue(context))
            .toArray();

        double maxValue = Arrays.stream(values).max().orElse(1);
        double minValue = Arrays.stream(values).min().orElse(0);
        maxValue = Math.max(0, maxValue);
        if (minValue > 0) minValue = 0;

        double range = maxValue - minValue;
        if (range == 0) range = 1;

        int zeroLineY = y + chart.height() - (int) (((0 - minValue) / range) * chart.height());
        for (int i = 0; i < entries.size(); i++) {
            ChartContent.ChartEntry entry = entries.get(i);
            double value = values[i];

            int slotStart = x + (i * renderWidth) / entries.size();
            int slotEnd = x + ((i + 1) * renderWidth) / entries.size();
            int slotWidth = Math.max(1, slotEnd - slotStart);
            int barX = slotStart;
            int barWidth = Math.max(1, slotWidth - 1);

            int barHeight = (int) (Math.abs(value) / range * chart.height());
            int barY = value >= 0 ? zeroLineY - barHeight : zeroLineY;

            barY = Math.max(y, Math.min(y + chart.height(), barY));
            barHeight = Math.min(barHeight, y + chart.height() - barY);

            context.fill(barX, barY, barX + barWidth, barY + barHeight, entry.color());
            renderLabel(chart, context, entry, barX, barWidth, slotWidth, y);
            renderValue(chart, context, value, barX, barWidth, slotWidth, barY);
        }

        if (minValue < 0) {
            context.hLine(x, x + renderWidth, zeroLineY, chart.zeroLineColor());
        }
    }

    private static void renderLabel(
        ChartContent chart,
        TipRenderContext context,
        ChartContent.ChartEntry entry,
        int barX,
        int barWidth,
        int slotWidth,
        int y
    ) {
        if (!chart.showLabels()) return;

        int labelWidth = context.getStringWidth(entry.labelComponent());
        if (labelWidth > slotWidth) return;
        context.drawString(entry.labelComponent(), barX + (barWidth - labelWidth) / 2, y + chart.height() + 2,
            chart.labelColor());
    }

    private static void renderValue(
        ChartContent chart,
        TipRenderContext context,
        double value,
        int barX,
        int barWidth,
        int slotWidth,
        int barY
    ) {
        if (!chart.showValues()) return;

        String valueStr = String.format("%.0f", value);
        int valueWidth = context.getStringWidth(valueStr);
        if (valueWidth > slotWidth) return;
        int valueY = value >= 0 ? barY - 12 : barY + 2;
        context.drawString(valueStr, barX + (barWidth - valueWidth) / 2, valueY, chart.valueColor());
    }
}
