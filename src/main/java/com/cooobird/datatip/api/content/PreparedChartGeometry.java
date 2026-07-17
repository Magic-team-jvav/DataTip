package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.render.PreparedViewportDraw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图表在布局阶段生成的紧凑几何快照。
 * 折线保留为线段并在绘制时按物理视口裁剪，饼图只缓存圆内的可见扫描线。
 */
final class PreparedChartGeometry implements PreparedViewportDraw {
    private final List<Fill> fills;
    private final List<Segment> segments;
    private final PieRaster pie;

    private PreparedChartGeometry(
        List<Fill> fills,
        List<Segment> segments,
        PieRaster pie
    ) {
        this.fills = List.copyOf(fills);
        this.segments = List.copyOf(segments);
        this.pie = pie;
    }

    static PreparedChartGeometry prepare(
        ChartContent chart,
        int maxWidth
    ) {
        return switch (chart.type()) {
            case BAR -> bar(chart, maxWidth);
            case LINE -> line(chart, maxWidth);
            case PIE -> pie(
                Math.max(1, Math.min(chart.width(), maxWidth)),
                chart.height(),
                slices(chart.entries())
            );
        };
    }

    static PreparedChartGeometry pie(
        int width,
        int height,
        List<Slice> slices
    ) {
        return new PreparedChartGeometry(
            List.of(),
            List.of(),
            new PieRaster(width, height, slices)
        );
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
        for (Fill fill : fills) {
            fill.render(
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
        for (Segment segment : segments) {
            segment.render(
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
        if (pie != null) {
            pie.render(
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
    }

    int primitiveCount() {
        int count = fills.size() + segments.size();
        return pie != null ? count + pie.primitiveCount() : count;
    }

    private static PreparedChartGeometry bar(
        ChartContent chart,
        int maxWidth
    ) {
        int renderWidth = Math.max(1, Math.min(chart.width(), maxWidth));
        List<ChartGeometryBuckets.Bucket> buckets =
            ChartGeometryBuckets.maximumAbsolute(
                chart.entries(),
                values(chart.entries()),
                renderWidth
            );
        if (buckets.isEmpty()) return empty();

        double maxValue = 0;
        double minValue = 0;
        for (ChartGeometryBuckets.Bucket bucket : buckets) {
            maxValue = Math.max(maxValue, bucket.value());
            minValue = Math.min(minValue, bucket.value());
        }
        double range = maxValue - minValue;
        if (!Double.isFinite(range) || range == 0) range = 1;
        long zeroY = chart.height()
            - (long) (((0 - minValue) / range) * chart.height());

        ArrayList<Fill> fills = new ArrayList<>(buckets.size() + 1);
        for (int index = 0; index < buckets.size(); index++) {
            ChartGeometryBuckets.Bucket bucket = buckets.get(index);
            int slotStart = index * renderWidth / buckets.size();
            int slotEnd = (index + 1) * renderWidth / buckets.size();
            int barWidth = Math.max(1, slotEnd - slotStart - 1);
            long barHeight = (long) (
                Math.abs(bucket.value()) / range * chart.height()
            );
            long barY = bucket.value() >= 0 ? zeroY - barHeight : zeroY;
            barY = Math.max(0, Math.min(chart.height(), barY));
            barHeight = Math.min(barHeight, (long) chart.height() - barY);
            if (barHeight > 0) {
                fills.add(new Fill(
                    slotStart,
                    barY,
                    barWidth,
                    barHeight,
                    bucket.color()
                ));
            }
        }
        if (minValue < 0) {
            fills.add(new Fill(
                0,
                zeroY,
                renderWidth,
                1,
                chart.zeroLineColor()
            ));
        }
        return new PreparedChartGeometry(fills, List.of(), null);
    }

    private static PreparedChartGeometry line(
        ChartContent chart,
        int maxWidth
    ) {
        int outerWidth = Math.max(1, Math.min(chart.width(), maxWidth));
        int plotInset = Math.min(2, Math.max(0, (outerWidth - 1) / 2));
        int renderWidth = Math.max(0, outerWidth - 1 - plotInset * 2);
        int verticalInset = Math.min(
            2,
            Math.max(0, (chart.height() - 1) / 2)
        );
        long plotHeight = Math.max(
            0L,
            (long) chart.height() - 1 - verticalInset * 2L
        );
        List<ChartGeometryBuckets.Bucket> buckets =
            ChartGeometryBuckets.average(
                chart.entries(),
                values(chart.entries()),
                Math.max(2, renderWidth + 1)
            );
        if (buckets.size() < 2) return empty();

        double maxValue = 0;
        double minValue = 0;
        for (ChartGeometryBuckets.Bucket bucket : buckets) {
            maxValue = Math.max(maxValue, bucket.value());
            minValue = Math.min(minValue, bucket.value());
        }
        double range = maxValue - minValue;
        if (!Double.isFinite(range) || range == 0) range = 1;

        ArrayList<Segment> segments = new ArrayList<>(buckets.size() - 1);
        ArrayList<Fill> markers = new ArrayList<>(buckets.size());
        int markerRadius = Math.min(plotInset, verticalInset);
        for (int index = 0; index < buckets.size() - 1; index++) {
            ChartGeometryBuckets.Bucket current = buckets.get(index);
            ChartGeometryBuckets.Bucket next = buckets.get(index + 1);
            long x1 = plotInset + point(renderWidth, index, buckets.size());
            long y1 = verticalInset + pointY(
                plotHeight,
                current.value(),
                minValue,
                range
            );
            long x2 = plotInset + point(
                renderWidth,
                index + 1,
                buckets.size()
            );
            long y2 = verticalInset + pointY(
                plotHeight,
                next.value(),
                minValue,
                range
            );
            segments.add(new Segment(
                x1,
                y1,
                x2,
                y2,
                current.color()
            ));
            marker(markers, x1, y1, markerRadius, current.color());
        }
        ChartGeometryBuckets.Bucket last = buckets.get(buckets.size() - 1);
        marker(
            markers,
            plotInset + renderWidth,
            verticalInset + pointY(
                plotHeight,
                last.value(),
                minValue,
                range
            ),
            markerRadius,
            last.color()
        );
        return new PreparedChartGeometry(markers, segments, null);
    }

    private static PreparedChartGeometry empty() {
        return new PreparedChartGeometry(List.of(), List.of(), null);
    }

    private static List<Slice> slices(
        List<ChartContent.ChartEntry> entries
    ) {
        ArrayList<Slice> slices = new ArrayList<>(entries.size());
        for (ChartContent.ChartEntry entry : entries) {
            slices.add(new Slice(
                Math.abs(parseValue(entry.valueExpr())),
                entry.color()
            ));
        }
        return List.copyOf(slices);
    }

    private static double[] values(
        List<ChartContent.ChartEntry> entries
    ) {
        double[] values = new double[entries.size()];
        for (int index = 0; index < entries.size(); index++) {
            values[index] = parseValue(entries.get(index).valueExpr());
        }
        return values;
    }

    private static double parseValue(String value) {
        try {
            double parsed = Double.parseDouble(value);
            return Double.isFinite(parsed) ? parsed : 0;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static long point(int width, int index, int count) {
        return (long) index * width / Math.max(1, count - 1);
    }

    private static long pointY(
        long height,
        double value,
        double minValue,
        double range
    ) {
        double normalized = (value - minValue) / range;
        if (!Double.isFinite(normalized)) normalized = 0;
        return height - (long) (normalized * height);
    }

    private static void marker(
        List<Fill> fills,
        long x,
        long y,
        int radius,
        int color
    ) {
        int positive = Math.max(1, radius);
        fills.add(new Fill(
            x - radius,
            y - radius,
            radius + positive,
            radius + positive,
            color
        ));
    }

    record Slice(double value, int color) {
    }

    private record Fill(
        long x,
        long y,
        long width,
        long height,
        int color
    ) {
        private void render(
            TipRenderContext context,
            int originX,
            int originY,
            float alpha,
            int viewportX,
            int viewportY,
            int viewportWidth,
            int viewportHeight
        ) {
            long left = (long) originX + x;
            long top = (long) originY + y;
            long right = left + width;
            long bottom = top + height;
            long viewportRight = (long) viewportX + viewportWidth;
            long viewportBottom = (long) viewportY + viewportHeight;
            if (right <= viewportX
                || bottom <= viewportY
                || left >= viewportRight
                || top >= viewportBottom) {
                return;
            }
            context.fill(
                coordinate(left),
                coordinate(top),
                coordinate(right),
                coordinate(bottom),
                TipRenderContext.applyAlpha(color, alpha)
            );
        }
    }

    private record Segment(
        long x1,
        long y1,
        long x2,
        long y2,
        int color
    ) {
        private void render(
            TipRenderContext context,
            int originX,
            int originY,
            float alpha,
            int viewportX,
            int viewportY,
            int viewportWidth,
            int viewportHeight
        ) {
            long left = Math.max(0, (long) viewportX - originX);
            long top = Math.max(0, (long) viewportY - originY);
            long right = (long) viewportX + viewportWidth - 1 - originX;
            long bottom = (long) viewportY + viewportHeight - 1 - originY;
            if (right < left || bottom < top) return;
            ClippedLine clipped = clip(
                x1,
                y1,
                x2,
                y2,
                left,
                top,
                right,
                bottom
            );
            if (clipped == null) return;
            drawLine(
                context,
                originX,
                originY,
                clipped,
                TipRenderContext.applyAlpha(color, alpha)
            );
        }
    }

    private static ClippedLine clip(
        long x1,
        long y1,
        long x2,
        long y2,
        long left,
        long top,
        long right,
        long bottom
    ) {
        double dx = (double) x2 - x1;
        double dy = (double) y2 - y1;
        double[] interval = {0.0, 1.0};
        if (!clipEdge(-dx, x1 - left, interval)
            || !clipEdge(dx, right - x1, interval)
            || !clipEdge(-dy, y1 - top, interval)
            || !clipEdge(dy, bottom - y1, interval)) {
            return null;
        }
        long clippedX1 = clamp(
            Math.round(x1 + interval[0] * dx),
            left,
            right
        );
        long clippedY1 = clamp(
            Math.round(y1 + interval[0] * dy),
            top,
            bottom
        );
        long clippedX2 = clamp(
            Math.round(x1 + interval[1] * dx),
            left,
            right
        );
        long clippedY2 = clamp(
            Math.round(y1 + interval[1] * dy),
            top,
            bottom
        );
        return new ClippedLine(
            clippedX1,
            clippedY1,
            clippedX2,
            clippedY2
        );
    }

    private static boolean clipEdge(
        double direction,
        double distance,
        double[] interval
    ) {
        if (direction == 0) return distance >= 0;
        double ratio = distance / direction;
        if (direction < 0) {
            if (ratio > interval[1]) return false;
            interval[0] = Math.max(interval[0], ratio);
        } else {
            if (ratio < interval[0]) return false;
            interval[1] = Math.min(interval[1], ratio);
        }
        return true;
    }

    private static void drawLine(
        TipRenderContext context,
        int originX,
        int originY,
        ClippedLine line,
        int color
    ) {
        long dx = line.x2() - line.x1();
        long dy = line.y2() - line.y1();
        long columns = Math.abs(dx);
        if (columns == 0) {
            fillColumn(
                context,
                originX,
                originY,
                line.x1(),
                line.y1(),
                line.y2(),
                color
            );
            return;
        }
        long stepX = dx > 0 ? 1 : -1;
        for (long column = 0; column < columns; column++) {
            long x = line.x1() + column * stepX;
            long nextX = x + stepX;
            long y = Math.round(
                line.y1() + (double) dy * column / columns
            );
            long nextY = Math.round(
                line.y1() + (double) dy * (column + 1) / columns
            );
            fillColumn(
                context,
                originX,
                originY,
                Math.min(x, nextX),
                y,
                nextY,
                color
            );
        }
    }

    private static void fillColumn(
        TipRenderContext context,
        int originX,
        int originY,
        long x,
        long firstY,
        long secondY,
        int color
    ) {
        int drawX = coordinate((long) originX + x);
        int top = coordinate(
            (long) originY + Math.min(firstY, secondY)
        );
        int bottom = coordinate(
            (long) originY + Math.max(firstY, secondY) + 1
        );
        context.fill(
            drawX,
            top,
            coordinate((long) drawX + 1),
            bottom,
            color
        );
    }

    static long lineRasterColumnCount(long x1, long x2) {
        return Math.max(1L, Math.abs(x2 - x1));
    }

    private static int coordinate(long value) {
        return (int) Math.max(
            Integer.MIN_VALUE,
            Math.min(Integer.MAX_VALUE, value)
        );
    }

    private static long clamp(long value, long minimum, long maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private record ClippedLine(long x1, long y1, long x2, long y2) {
    }

    private static final class PieRaster {
        private final int width;
        private final int height;
        private final int centerX;
        private final int centerY;
        private final int radius;
        private final List<SliceBoundary> boundaries;
        private final Map<Integer, List<Fill>> rows = new HashMap<>();

        private PieRaster(int width, int height, List<Slice> slices) {
            this.width = Math.max(1, width);
            this.height = Math.max(1, height);
            this.centerX = this.width / 2;
            this.centerY = this.height / 2;
            this.radius = Math.min(this.width, this.height) / 2 - 4;
            this.boundaries = boundaries(slices);
        }

        private void render(
            TipRenderContext context,
            int x,
            int y,
            float alpha,
            int viewportX,
            int viewportY,
            int viewportWidth,
            int viewportHeight
        ) {
            if (radius <= 0 || boundaries.isEmpty()) return;
            long firstValue = Math.max(0, (long) viewportY - y);
            long lastValue = Math.min(
                height,
                (long) viewportY + viewportHeight - y
            );
            if (lastValue <= firstValue) return;
            int first = (int) firstValue;
            int last = (int) lastValue;
            for (int row = first; row < last; row++) {
                List<Fill> spans = rows.get(row);
                if (spans == null) {
                    spans = prepareRow(row);
                    if (!spans.isEmpty()) rows.put(row, spans);
                }
                for (Fill fill : spans) {
                    fill.render(
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
            }
        }

        private int primitiveCount() {
            int count = 0;
            int first = Math.max(0, centerY - radius);
            int last = Math.min(height, centerY + radius + 1);
            for (int row = first; row < last; row++) {
                List<Fill> spans = rows.get(row);
                if (spans == null) {
                    spans = prepareRow(row);
                    if (!spans.isEmpty()) rows.put(row, spans);
                }
                count += spans.size();
            }
            return count;
        }

        private List<Fill> prepareRow(int row) {
            int dy = row - centerY;
            long remaining = (long) radius * radius - (long) dy * dy;
            if (remaining < 0) return List.of();
            int extent = (int) Math.floor(Math.sqrt(remaining));
            int start = Math.max(0, centerX - extent);
            int end = Math.min(width - 1, centerX + extent);
            ArrayList<Fill> spans = new ArrayList<>();
            int spanStart = start;
            int currentColor = colorAt(start - centerX, dy);
            for (int column = start + 1; column <= end; column++) {
                int color = colorAt(column - centerX, dy);
                if (color == currentColor) continue;
                spans.add(new Fill(
                    spanStart,
                    row,
                    column - spanStart,
                    1,
                    currentColor
                ));
                spanStart = column;
                currentColor = color;
            }
            spans.add(new Fill(
                spanStart,
                row,
                end - spanStart + 1L,
                1,
                currentColor
            ));
            return List.copyOf(spans);
        }

        private int colorAt(int dx, int dy) {
            double angle = Math.toDegrees(Math.atan2(dy, dx)) + 90.0;
            if (angle < 0) angle += 360.0;
            int low = 0;
            int high = boundaries.size() - 1;
            while (low < high) {
                int middle = (low + high) >>> 1;
                if (angle < boundaries.get(middle).endAngle()) {
                    high = middle;
                } else {
                    low = middle + 1;
                }
            }
            return boundaries.get(low).color();
        }

        private static List<SliceBoundary> boundaries(List<Slice> slices) {
            double total = 0;
            for (Slice slice : slices) total += Math.abs(slice.value());
            if (!Double.isFinite(total) || total == 0) return List.of();
            ArrayList<SliceBoundary> result = new ArrayList<>(slices.size());
            double end = 0;
            for (Slice slice : slices) {
                double value = Math.abs(slice.value());
                if (value == 0) continue;
                end += value / total * 360.0;
                result.add(new SliceBoundary(end, slice.color()));
            }
            return List.copyOf(result);
        }
    }

    private record SliceBoundary(double endAngle, int color) {
    }
}
