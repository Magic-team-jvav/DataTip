package com.cooobird.datatip.api.render;

import com.cooobird.datatip.api.layout.TipRect;
import org.joml.Matrix4f;

/**
 * Tooltip 命令树使用的二维仿射变换。
 *
 * <p>变换保留在命令组上，只有遍历叶命令时才组合，避免容器每嵌套一层就
 * 复制全部后代命令。</p>
 */
public record RenderTransform(
    double m00,
    double m01,
    double m02,
    double m10,
    double m11,
    double m12
) {
    public static final RenderTransform IDENTITY = new RenderTransform(
        1.0, 0.0, 0.0,
        0.0, 1.0, 0.0
    );

    public RenderTransform {
        if (!Double.isFinite(m00) || !Double.isFinite(m01)
            || !Double.isFinite(m02) || !Double.isFinite(m10)
            || !Double.isFinite(m11) || !Double.isFinite(m12)) {
            throw new IllegalArgumentException(
                "Render transform values must be finite"
            );
        }
    }

    public static RenderTransform translation(double x, double y) {
        return new RenderTransform(1.0, 0.0, x, 0.0, 1.0, y);
    }

    public static RenderTransform around(
        double pivotX,
        double pivotY,
        double scaleX,
        double scaleY,
        double rotationDegrees,
        double offsetX,
        double offsetY
    ) {
        double radians = Math.toRadians(rotationDegrees);
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        double a = cosine * scaleX;
        double b = -sine * scaleY;
        double d = sine * scaleX;
        double e = cosine * scaleY;
        return new RenderTransform(
            a,
            b,
            offsetX + pivotX - a * pivotX - b * pivotY,
            d,
            e,
            offsetY + pivotY - d * pivotX - e * pivotY
        );
    }

    /**
     * 返回先执行 {@code local}、再执行当前变换的组合。
     */
    public RenderTransform compose(RenderTransform local) {
        return new RenderTransform(
            m00 * local.m00 + m01 * local.m10,
            m00 * local.m01 + m01 * local.m11,
            m00 * local.m02 + m01 * local.m12 + m02,
            m10 * local.m00 + m11 * local.m10,
            m10 * local.m01 + m11 * local.m11,
            m10 * local.m02 + m11 * local.m12 + m12
        );
    }

    public TipRect transformBounds(TipRect bounds) {
        double x0 = bounds.x();
        double y0 = bounds.y();
        double x1 = bounds.right();
        double y1 = bounds.bottom();
        double ax = x(x0, y0);
        double ay = y(x0, y0);
        double bx = x(x1, y0);
        double by = y(x1, y0);
        double cx = x(x0, y1);
        double cy = y(x0, y1);
        double dx = x(x1, y1);
        double dy = y(x1, y1);
        long left = floor(Math.min(Math.min(ax, bx), Math.min(cx, dx)));
        long top = floor(Math.min(Math.min(ay, by), Math.min(cy, dy)));
        long right = ceil(Math.max(Math.max(ax, bx), Math.max(cx, dx)));
        long bottom = ceil(Math.max(Math.max(ay, by), Math.max(cy, dy)));
        return new TipRect(
            left,
            top,
            distance(right, left),
            distance(bottom, top)
        );
    }

    public Matrix4f matrix() {
        return new Matrix4f(
            (float) m00, (float) m10, 0.0f, 0.0f,
            (float) m01, (float) m11, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            (float) m02, (float) m12, 0.0f, 1.0f
        );
    }

    public boolean isIdentity() {
        return equals(IDENTITY);
    }

    public boolean isTranslationOnly() {
        return Double.compare(m00, 1.0) == 0
            && Double.compare(m01, 0.0) == 0
            && Double.compare(m10, 0.0) == 0
            && Double.compare(m11, 1.0) == 0;
    }

    private double x(double x, double y) {
        return m00 * x + m01 * y + m02;
    }

    private double y(double x, double y) {
        return m10 * x + m11 * y + m12;
    }

    private static long floor(double value) {
        value = snapIntegral(value);
        if (value <= Long.MIN_VALUE) return Long.MIN_VALUE;
        if (value >= Long.MAX_VALUE) return Long.MAX_VALUE;
        return (long) Math.floor(value);
    }

    private static long ceil(double value) {
        value = snapIntegral(value);
        if (value <= Long.MIN_VALUE) return Long.MIN_VALUE;
        if (value >= Long.MAX_VALUE) return Long.MAX_VALUE;
        return (long) Math.ceil(value);
    }

    private static double snapIntegral(double value) {
        double nearest = Math.rint(value);
        double tolerance = Math.max(1.0, Math.abs(value)) * 1.0e-12;
        return Math.abs(value - nearest) <= tolerance ? nearest : value;
    }

    private static long distance(long high, long low) {
        if (high <= low) return 0;
        if (low < 0 && high > Long.MAX_VALUE + low) return Long.MAX_VALUE;
        return high - low;
    }
}
