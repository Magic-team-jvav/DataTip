package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipRenderContext;

/**
 * 图表绘制分发器。
 * 只负责按图表类型转发，具体绘制逻辑由各类型渲染器承担。
 */
final class ChartRenderers {
    private ChartRenderers() {
    }

    static void render(ChartContent chart, TipRenderContext context, int x, int y, int maxWidth) {
        switch (chart.type()) {
            case BAR -> BarChartRenderer.render(chart, context, x, y, maxWidth);
            case PIE -> PieChartRenderer.render(chart, context, x, y, maxWidth);
            case LINE -> LineChartRenderer.render(chart, context, x, y, maxWidth);
        }
    }
}
