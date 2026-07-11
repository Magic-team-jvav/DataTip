package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipRenderContext;

/**
 * 可视内容的防御性尺寸边界。
 * <p>
 * 这里限制的是资源包显式声明的图片、模型和图表尺寸，不参与普通文本的原版自然宽度计算。
 * </p>
 */
final class ContentBounds {
    static final int MAX_VISUAL_SIZE = 4096;
    static final int MAX_PIE_SIZE = 128;
    static final int MAX_CHART_ENTRIES = 512;

    private ContentBounds() {
    }

    static int dimension(int value) {
        return Math.max(1, Math.min(MAX_VISUAL_SIZE, value));
    }

    static int spacing(int value) {
        return Math.max(0, Math.min(MAX_VISUAL_SIZE, value));
    }

    static int offset(int value) {
        return Math.max(-MAX_VISUAL_SIZE, Math.min(MAX_VISUAL_SIZE, value));
    }

    static int extent(int dimension, int offset) {
        return Math.min(Integer.MAX_VALUE, dimension + Math.abs(offset));
    }

    static int scaledDimension(int dimension, float scale) {
        double scaled = dimension * (double) scale;
        return dimension((int) Math.min(MAX_VISUAL_SIZE, Math.max(1, scaled)));
    }

    static int pieDimension(int value) {
        return Math.min(MAX_PIE_SIZE, dimension(value));
    }

    static boolean beginHorizontalClip(
        TipRenderContext context, int x, int y, int availableWidth, int height, int naturalWidth
    ) {
        if (availableWidth <= 0 || naturalWidth <= availableWidth) return false;
        // 先提交可能仍在缓冲区中的原版 Tooltip 文字，避免它被当前内容的裁剪区域影响。
        context.graphics().flush();
        context.graphics().enableScissor(x, y, x + availableWidth, y + Math.max(1, height));
        return true;
    }

    static void endHorizontalClip(TipRenderContext context, boolean clipped) {
        if (clipped) {
            context.graphics().flush();
            context.graphics().disableScissor();
        }
    }
}
