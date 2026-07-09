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
        int renderHeight = Math.max(1, Math.min(chart.height(), maxWidth));

        int centerX = x + renderWidth / 2;
        int centerY = y + renderHeight / 2;
        int radius = Math.min(renderWidth, renderHeight) / 2 - 4;
        if (radius <= 0) return;

        double startAngle = -90; // 从顶部开始
        int labelY = y + renderHeight + 4;

        for (int i = 0; i < entries.size(); i++) {
            ChartContent.ChartEntry entry = entries.get(i);
            double sweepAngle = (values[i] / total) * 360;
            double endAngle = startAngle + sweepAngle;

            ChartRenderUtils.drawPieSlice(context, centerX, centerY, radius, startAngle, endAngle, entry.color());

            if (chart.showLabels()) {
                context.drawString(entry.label(), x, labelY, entry.color());
                labelY += 10;
            }

            startAngle = endAngle;
        }
    }
}
