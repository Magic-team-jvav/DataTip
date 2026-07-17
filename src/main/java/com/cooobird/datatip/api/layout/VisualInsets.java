package com.cooobird.datatip.api.layout;

/**
 * 布局框之外仍可能出现像素的四向扩展。
 */
public record VisualInsets(long left, long top, long right, long bottom) {
    public static final VisualInsets NONE = new VisualInsets(0, 0, 0, 0);

    public VisualInsets {
        if (left < 0 || top < 0 || right < 0 || bottom < 0) {
            throw new IllegalArgumentException("Visual insets must not be negative");
        }
    }

    public TipRect expand(TipRect bounds) {
        return new TipRect(
            TipMath.subtract(bounds.x(), left),
            TipMath.subtract(bounds.y(), top),
            TipMath.add(TipMath.add(bounds.width(), left), right),
            TipMath.add(TipMath.add(bounds.height(), top), bottom)
        );
    }
}
