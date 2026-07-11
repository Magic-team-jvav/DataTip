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

        int outerWidth = Math.max(1, Math.min(chart.width(), maxWidth));
        int plotInset = Math.min(2, Math.max(0, (outerWidth - 1) / 2));
        int renderWidth = Math.max(0, outerWidth - 1 - plotInset * 2);
        x += plotInset;
        int chartY = y;
        int verticalInset = Math.min(2, Math.max(0, (chart.height() - 1) / 2));
        int plotHeight = Math.max(0, chart.height() - 1 - verticalInset * 2);
        y += verticalInset;
        int markerRadius = Math.min(plotInset, verticalInset);
        double[] values = entries.stream()
            .mapToDouble(entry -> entry.resolveValue(context))
            .toArray();
        double maxValue = Arrays.stream(values).max().orElse(1);
        double minValue = Arrays.stream(values).min().orElse(0);
        maxValue = Math.max(0, maxValue);
        if (minValue > 0) minValue = 0;

        double range = maxValue - minValue;
        if (range == 0) range = 1;

        for (int i = 0; i < entries.size() - 1; i++) {
            ChartContent.ChartEntry current = entries.get(i);
            double currentValue = values[i];
            double nextValue = values[i + 1];

            int x1 = pointX(x, renderWidth, i, entries.size());
            int y1 = y + plotHeight - (int) (((currentValue - minValue) / range) * plotHeight);
            int x2 = pointX(x, renderWidth, i + 1, entries.size());
            int y2 = y + plotHeight - (int) (((nextValue - minValue) / range) * plotHeight);

            ChartRenderUtils.drawLine(context, x1, y1, x2, y2, current.color());
            drawPoint(context, x1, y1, markerRadius, current.color());
            renderValue(chart, context, currentValue, x1, y1,
                renderWidth / entries.size(), x, x + renderWidth);
        }

        renderLastPoint(chart, context, x, y, renderWidth, plotHeight, markerRadius,
            values, minValue, range);
        renderLabels(chart, context, x, chartY, renderWidth);
    }

    private static void renderLastPoint(
        ChartContent chart,
        TipRenderContext context,
        int x,
        int y,
        int renderWidth,
        int plotHeight,
        int markerRadius,
        double[] values,
        double minValue,
        double range
    ) {
        List<ChartContent.ChartEntry> entries = chart.entries();
        ChartContent.ChartEntry last = entries.getLast();
        double lastValue = values[entries.size() - 1];
        int lastX = pointX(x, renderWidth, entries.size() - 1, entries.size());
        int lastY = y + plotHeight - (int) (((lastValue - minValue) / range) * plotHeight);
        drawPoint(context, lastX, lastY, markerRadius, last.color());
        renderValue(chart, context, lastValue, lastX, lastY,
            renderWidth / entries.size(), x, x + renderWidth);
    }

    private static void renderLabels(
        ChartContent chart,
        TipRenderContext context,
        int x,
        int y,
        int renderWidth
    ) {
        if (!chart.showLabels()) return;

        List<ChartContent.ChartEntry> entries = chart.entries();
        for (int i = 0; i < entries.size(); i++) {
            ChartContent.ChartEntry entry = entries.get(i);
            int labelX = pointX(x, renderWidth, i, entries.size());
            int available = Math.max(1, renderWidth / entries.size());
            int labelWidth = context.getStringWidth(entry.labelComponent());
            if (labelWidth <= available) {
                int centerX = clampCenter(labelX, labelWidth, x, x + renderWidth);
                context.drawCenteredString(entry.labelComponent(), centerX, y + chart.height() + 2,
                    chart.labelColor());
            }
        }
    }

    private static int pointX(int x, int renderWidth, int index, int count) {
        return x + (index * renderWidth) / Math.max(1, count - 1);
    }

    private static void renderValue(
        ChartContent chart,
        TipRenderContext context,
        double value,
        int pointX,
        int pointY,
        int availableWidth,
        int left,
        int right
    ) {
        if (!chart.showValues()) return;
        String valueText = ChartContent.formatValue(value);
        int valueWidth = context.getStringWidth(valueText);
        if (valueWidth <= Math.max(1, availableWidth)) {
            context.drawCenteredString(valueText, clampCenter(pointX, valueWidth, left, right),
                pointY - 12, chart.valueColor());
        }
    }

    private static int clampCenter(int center, int textWidth, int left, int right) {
        int halfLeft = textWidth / 2;
        int halfRight = textWidth - halfLeft;
        return Math.max(left + halfLeft, Math.min(center, right - halfRight));
    }

    private static void drawPoint(TipRenderContext context, int x, int y, int radius, int color) {
        int positiveExtent = Math.max(1, radius);
        context.fill(x - radius, y - radius, x + positiveExtent, y + positiveExtent, color);
    }
}
