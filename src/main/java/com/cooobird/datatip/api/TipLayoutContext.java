package com.cooobird.datatip.api;

import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * Tooltip 内容布局测量上下文。
 * <p>
 * 测量阶段需要同时知道字体、当前物品栈和可选宽度约束，才能保证变量替换后的文本
 * 与最终绘制使用完全相同的尺寸语义。{@code maxWidth <= 0} 表示不主动限制宽度。
 * </p>
 */
public record TipLayoutContext(Font font, ItemStack itemStack, int maxWidth) {
    private static final int LEGACY_UNBOUNDED_WIDTH = 200;

    public TipLayoutContext(Font font, ItemStack itemStack, int maxWidth) {
        this.font = Objects.requireNonNull(font, "font");
        this.itemStack = itemStack != null ? itemStack : ItemStack.EMPTY;
        this.maxWidth = Math.max(0, maxWidth);
    }

    public static TipLayoutContext unbounded(Font font, ItemStack itemStack) {
        return new TipLayoutContext(font, itemStack, 0);
    }

    public static TipLayoutContext bounded(Font font, ItemStack itemStack, int maxWidth) {
        return new TipLayoutContext(font, itemStack, Math.max(1, maxWidth));
    }

    public boolean hasWidthLimit() {
        return maxWidth > 0;
    }

    public int availableWidth() {
        return hasWidthLimit() ? maxWidth : Integer.MAX_VALUE;
    }

    public int constrainWidth(int width) {
        int normalized = Math.max(0, width);
        return hasWidthLimit() ? Math.min(normalized, maxWidth) : normalized;
    }

    /**
     * 为尚未适配布局上下文的旧内容实现提供有限的测量宽度，避免把
     * {@link Integer#MAX_VALUE} 当作实际像素宽度使用。内置内容不使用此兼容值。
     */
    public int compatibilityWidth() {
        return hasWidthLimit() ? maxWidth : LEGACY_UNBOUNDED_WIDTH;
    }

    public TipLayoutContext withMaxWidth(int width) {
        return width > 0 ? bounded(font, itemStack, width) : unbounded(font, itemStack);
    }

    /**
     * 创建只能在当前可用宽度内继续收紧的子布局上下文。
     */
    public TipLayoutContext constrainTo(int width) {
        if (width <= 0) return this;
        int nestedWidth = hasWidthLimit() ? Math.min(maxWidth, width) : width;
        return bounded(font, itemStack, nestedWidth);
    }
}
