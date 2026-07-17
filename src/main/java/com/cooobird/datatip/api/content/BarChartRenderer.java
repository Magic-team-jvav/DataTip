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
        List<ChartGeometryBuckets.Bucket> buckets =
            ChartGeometryBuckets.maximumAbsolute(
                entries,
                values,
                renderWidth
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

        int zeroLineY = y + chart.height() - (int) (((0 - minValue) / range) * chart.height());
        for (int i = 0; i < buckets.size(); i++) {
            ChartGeometryBuckets.Bucket bucket = buckets.get(i);
            double value = values[i];

            int slotStart = x + (i * renderWidth) / buckets.size();
            int slotEnd = x + ((i + 1) * renderWidth) / buckets.size();
            int slotWidth = Math.max(1, slotEnd - slotStart);
            int barX = slotStart;
            int barWidth = Math.max(1, slotWidth - 1);

            int barHeight = (int) (Math.abs(value) / range * chart.height());
            int barY = value >= 0 ? zeroLineY - barHeight : zeroLineY;

            barY = Math.max(y, Math.min(y + chart.height(), barY));
            barHeight = Math.min(barHeight, y + chart.height() - barY);

            context.fill(
                barX,
                barY,
                barX + barWidth,
                barY + barHeight,
                bucket.color()
            );
        }

        if (minValue < 0) {
            context.hLine(x, x + renderWidth, zeroLineY, chart.zeroLineColor());
        }
    }

}
