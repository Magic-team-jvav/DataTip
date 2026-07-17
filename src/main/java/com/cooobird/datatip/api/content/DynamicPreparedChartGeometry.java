package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.render.PreparedViewportDraw;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 动态变量图表只在解析值真正变化时重建紧凑几何，避免每帧展开像素数据。
 */
final class DynamicPreparedChartGeometry implements PreparedViewportDraw {
    private final ChartContent source;
    private final int maxWidth;
    private double[] cachedValues;
    private PreparedChartGeometry cachedGeometry;

    DynamicPreparedChartGeometry(ChartContent source, int maxWidth) {
        this.source = source;
        this.maxWidth = maxWidth;
    }

    @Override
    public void render(
        TipRenderContext context,
        int x,
        int y,
        float alpha,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight
    ) {
        double[] values = resolveValues(context);
        if (cachedGeometry == null || !Arrays.equals(values, cachedValues)) {
            cachedValues = values;
            cachedGeometry = PreparedChartGeometry.prepare(
                resolvedChart(values),
                maxWidth
            );
        }
        cachedGeometry.render(
            context,
            x,
            y,
            alpha,
            viewportX,
            viewportY,
            viewportWidth,
            viewportHeight
        );
    }

    private double[] resolveValues(TipRenderContext context) {
        double[] values = new double[source.entries().size()];
        for (int index = 0; index < values.length; index++) {
            double value = source.entries().get(index).resolveValue(context);
            values[index] = Double.isFinite(value) ? value : 0;
        }
        return values;
    }

    private ChartContent resolvedChart(double[] values) {
        ArrayList<ChartContent.ChartEntry> entries = new ArrayList<>(
            values.length
        );
        for (int index = 0; index < values.length; index++) {
            ChartContent.ChartEntry entry = source.entries().get(index);
            entries.add(new ChartContent.ChartEntry(
                entry.labelText(),
                Double.toString(values[index]),
                entry.color()
            ));
        }
        return new ChartContent(
            source.type(),
            entries,
            source.width(),
            source.height(),
            null,
            false,
            false,
            source.titleColor(),
            source.labelColor(),
            source.valueColor(),
            source.zeroLineColor()
        );
    }
}
