package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipRenderContext;

import java.util.Arrays;
import java.util.List;

/**
 * 折线图渲染器。
 */
final class LineChartRenderer {
    private LineChartRenderer() {
    }

    static void render(ChartContent chart, TipRenderContext context, int x, int y, int maxWidth) {
        List<ChartContent.ChartEntry> entries = chart.entries();
        if (entries.size() < 2) return;

        int renderWidth = Math.max(1, Math.min(chart.width(), maxWidth));
        double[] values = entries.stream()
            .mapToDouble(entry -> entry.resolveValue(context))
            .toArray();
        double maxValue = Arrays.stream(values).max().orElse(1);
        double minValue = Arrays.stream(values).min().orElse(0);
        if (minValue > 0) minValue = 0;

        double range = maxValue - minValue;
        if (range == 0) range = 1;

        int stepX = renderWidth / (entries.size() - 1);

        for (int i = 0; i < entries.size() - 1; i++) {
            ChartContent.ChartEntry current = entries.get(i);
            double currentValue = values[i];
            double nextValue = values[i + 1];

            int x1 = x + i * stepX;
            int y1 = y + chart.height() - (int) (((currentValue - minValue) / range) * chart.height());
            int x2 = x + (i + 1) * stepX;
            int y2 = y + chart.height() - (int) (((nextValue - minValue) / range) * chart.height());

            ChartRenderUtils.drawLine(context, x1, y1, x2, y2, current.color());
            context.fill(x1 - 2, y1 - 2, x1 + 2, y1 + 2, current.color());
        }

        renderLastPoint(chart, context, x, y, stepX, values, minValue, range);
        renderLabels(chart, context, x, y, stepX);
    }

    private static void renderLastPoint(
        ChartContent chart,
        TipRenderContext context,
        int x,
        int y,
        int stepX,
        double[] values,
        double minValue,
        double range
    ) {
        List<ChartContent.ChartEntry> entries = chart.entries();
        ChartContent.ChartEntry last = entries.get(entries.size() - 1);
        double lastValue = values[entries.size() - 1];
        int lastX = x + (entries.size() - 1) * stepX;
        int lastY = y + chart.height() - (int) (((lastValue - minValue) / range) * chart.height());
        context.fill(lastX - 2, lastY - 2, lastX + 2, lastY + 2, last.color());
    }

    private static void renderLabels(
        ChartContent chart,
        TipRenderContext context,
        int x,
        int y,
        int stepX
    ) {
        if (!chart.showLabels()) return;

        List<ChartContent.ChartEntry> entries = chart.entries();
        for (int i = 0; i < entries.size(); i++) {
            ChartContent.ChartEntry entry = entries.get(i);
            int labelX = x + i * stepX;
            context.drawCenteredString(entry.label(), labelX, y + chart.height() + 2, chart.labelColor());
        }
    }
}
