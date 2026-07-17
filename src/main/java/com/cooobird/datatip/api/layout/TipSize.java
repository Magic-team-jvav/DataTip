package com.cooobird.datatip.api.layout;

/**
 * 使用 long 保存的非负 Tooltip 尺寸。
 */
public record TipSize(long width, long height) {
    public static final TipSize ZERO = new TipSize(0, 0);

    public TipSize {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Tip size must not be negative");
        }
    }
}
