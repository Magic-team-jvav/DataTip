package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipRenderContext;

/**
 * 图表底层绘制工具。
 */
final class ChartRenderUtils {
    private ChartRenderUtils() {
    }

    static void drawPieSlice(
        TipRenderContext context,
        int centerX,
        int centerY,
        int radius,
        double startAngle,
        double endAngle,
        int color
    ) {
        for (int angle = (int) startAngle; angle < endAngle; angle++) {
            double rad = Math.toRadians(angle);
            double nextRad = Math.toRadians(angle + 1);

            int x1 = centerX + (int) (radius * Math.cos(rad));
            int y1 = centerY + (int) (radius * Math.sin(rad));
            int x2 = centerX + (int) (radius * Math.cos(nextRad));
            int y2 = centerY + (int) (radius * Math.sin(nextRad));

            drawTriangle(context, centerX, centerY, x1, y1, x2, y2, color);
        }
    }

    static void drawLine(TipRenderContext context, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            context.fill(x1, y1, x1 + 1, y1 + 1, color);

            if (x1 == x2 && y1 == y2) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    private static void drawTriangle(TipRenderContext context, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        int minX = Math.min(x1, Math.min(x2, x3));
        int maxX = Math.max(x1, Math.max(x2, x3));
        int minY = Math.min(y1, Math.min(y2, y3));
        int maxY = Math.max(y1, Math.max(y2, y3));

        for (int px = minX; px <= maxX; px++) {
            for (int py = minY; py <= maxY; py++) {
                if (isPointInTriangle(px, py, x1, y1, x2, y2, x3, y3)) {
                    context.fill(px, py, px + 1, py + 1, color);
                }
            }
        }
    }

    private static boolean isPointInTriangle(int px, int py, int x1, int y1, int x2, int y2, int x3, int y3) {
        double d1 = sign(px, py, x1, y1, x2, y2);
        double d2 = sign(px, py, x2, y2, x3, y3);
        double d3 = sign(px, py, x3, y3, x1, y1);

        boolean hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(hasNeg && hasPos);
    }

    private static double sign(int px, int py, int x1, int y1, int x2, int y2) {
        return (px - x2) * (y1 - y2) - (x1 - x2) * (py - y2);
    }
}
