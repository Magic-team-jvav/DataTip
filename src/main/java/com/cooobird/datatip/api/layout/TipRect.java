package com.cooobird.datatip.api.layout;

/**
 * 使用 long 坐标的 Tooltip 矩形。
 */
public record TipRect(long x, long y, long width, long height) {
    public static final TipRect ZERO = new TipRect(0, 0, 0, 0);

    public TipRect {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Tip rectangle size must not be negative");
        }
    }

    public static TipRect ofSize(TipSize size) {
        return new TipRect(0, 0, size.width(), size.height());
    }

    public long right() {
        return TipMath.add(x, width);
    }

    public long bottom() {
        return TipMath.add(y, height);
    }

    public TipSize size() {
        return new TipSize(width, height);
    }

    public TipRect union(TipRect other) {
        long left = Math.min(x, other.x);
        long top = Math.min(y, other.y);
        long right = Math.max(right(), other.right());
        long bottom = Math.max(bottom(), other.bottom());
        return new TipRect(
            left,
            top,
            TipMath.distance(right, left),
            TipMath.distance(bottom, top)
        );
    }

    public TipRect intersection(TipRect other) {
        long left = Math.max(x, other.x);
        long top = Math.max(y, other.y);
        long right = Math.min(right(), other.right());
        long bottom = Math.min(bottom(), other.bottom());
        return new TipRect(
            left,
            top,
            TipMath.distance(right, left),
            TipMath.distance(bottom, top)
        );
    }

    public boolean contains(TipRect other) {
        return other.x >= x
            && other.y >= y
            && other.right() <= right()
            && other.bottom() <= bottom();
    }
}
