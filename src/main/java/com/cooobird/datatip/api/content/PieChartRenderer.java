package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipRenderContext;

import java.util.Arrays;
import java.util.List;

/**
 * 饼图渲染器。
 */
final class PieChartRenderer {
    private PieChartRenderer() {
    }

    static void render(ChartContent chart, TipRenderContext context, int x, int y, int maxWidth) {
        List<ChartContent.ChartEntry> entries = chart.entries();
        double[] values = entries.stream()
            .mapToDouble(entry -> Math.abs(entry.resolveValue(context)))
            .toArray();
        double total = Arrays.stream(values).sum();
        if (total == 0) return;

        int renderWidth = Math.max(1, Math.min(chart.width(), maxWidth));
        int renderHeight = chart.height();

        int centerX = x + renderWidth / 2;
        int centerY = y + renderHeight / 2;
        int radius = Math.min(renderWidth, renderHeight) / 2 - 4;
        if (radius <= 0) return;
        List<ChartGeometryBuckets.Bucket> buckets =
            ChartGeometryBuckets.sumAbsolute(
                entries,
                values,
                Math.max(1, (int) Math.ceil(2.0 * Math.PI * radius))
            );
        values = buckets.stream()
            .mapToDouble(ChartGeometryBuckets.Bucket::value)
            .toArray();
        total = Arrays.stream(values).sum();

        double startAngle = -90; // 从顶部开始

        for (int i = 0; i < buckets.size(); i++) {
            ChartGeometryBuckets.Bucket bucket = buckets.get(i);
            double sweepAngle = (values[i] / total) * 360;
            double endAngle = startAngle + sweepAngle;

            ChartRenderUtils.drawPieSlice(
                context,
                centerX,
                centerY,
                radius,
                startAngle,
                endAngle,
                bucket.color()
            );

            startAngle = endAngle;
        }
    }
}
