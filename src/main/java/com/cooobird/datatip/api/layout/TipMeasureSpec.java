package com.cooobird.datatip.api.layout;

/**
 * 将作者偏好宽度与物理视口边界分开的测量约束。
 *
 * @param softMaxWidth  作者建议的换行宽度，0 表示没有建议
 * @param hardMaxWidth  物理可分配宽度
 * @param hardMaxHeight 物理可分配高度
 */
public record TipMeasureSpec(long softMaxWidth, long hardMaxWidth, long hardMaxHeight) {
    public TipMeasureSpec {
        if (softMaxWidth < 0) {
            throw new IllegalArgumentException("Soft maximum width must not be negative");
        }
        if (hardMaxWidth <= 0 || hardMaxHeight <= 0) {
            throw new IllegalArgumentException("Hard viewport dimensions must be positive");
        }
    }
}
