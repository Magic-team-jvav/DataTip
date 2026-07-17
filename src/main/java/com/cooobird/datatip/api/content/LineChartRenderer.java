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
        int verticalInset = Math.min(2, Math.max(0, (chart.height() - 1) / 2));
        int plotHeight = Math.max(0, chart.height() - 1 - verticalInset * 2);
        y += verticalInset;
        int markerRadius = Math.min(plotInset, verticalInset);
        double[] values = entries.stream()
            .mapToDouble(entry -> entry.resolveValue(context))
            .toArray();
        List<ChartGeometryBuckets.Bucket> buckets =
            ChartGeometryBuckets.average(
                entries,
                values,
                Math.max(2, renderWidth + 1)
            );
        values = buckets.stream()
            .mapToDouble(ChartGeometryBuckets.Bucket::value)
            .toArray();
        double maxValue = Arrays.stream(values).max().orElse(1);
        double minValue = Arrays.stream(values).min().orElse(0);
        maxValue = Math.max(0, maxValue);
        if (minValue > 0) minValue = 0;

        double range = maxValue - minValue;
        if (range == 0) range = 1;

        for (int i = 0; i < buckets.size() - 1; i++) {
            ChartGeometryBuckets.Bucket current = buckets.get(i);
            double currentValue = values[i];
            double nextValue = values[i + 1];

            int x1 = pointX(x, renderWidth, i, buckets.size());
            int y1 = y + plotHeight - (int) (((currentValue - minValue) / range) * plotHeight);
            int x2 = pointX(x, renderWidth, i + 1, buckets.size());
            int y2 = y + plotHeight - (int) (((nextValue - minValue) / range) * plotHeight);

            ChartRenderUtils.drawLine(context, x1, y1, x2, y2, current.color());
            drawPoint(context, x1, y1, markerRadius, current.color());
        }

        renderLastPoint(context, x, y, renderWidth, plotHeight, markerRadius,
            buckets, values, minValue, range);
    }

    private static void renderLastPoint(
        TipRenderContext context,
        int x,
        int y,
        int renderWidth,
        int plotHeight,
        int markerRadius,
        List<ChartGeometryBuckets.Bucket> buckets,
        double[] values,
        double minValue,
        double range
    ) {
        ChartGeometryBuckets.Bucket last = buckets.getLast();
        double lastValue = values[buckets.size() - 1];
        int lastX = pointX(x, renderWidth, buckets.size() - 1, buckets.size());
        int lastY = y + plotHeight - (int) (((lastValue - minValue) / range) * plotHeight);
        drawPoint(context, lastX, lastY, markerRadius, last.color());
    }

    private static int pointX(int x, int renderWidth, int index, int count) {
        return x + (index * renderWidth) / Math.max(1, count - 1);
    }

    private static void drawPoint(TipRenderContext context, int x, int y, int radius, int color) {
        int positiveExtent = Math.max(1, radius);
        context.fill(x - radius, y - radius, x + positiveExtent, y + positiveExtent, color);
    }
}
