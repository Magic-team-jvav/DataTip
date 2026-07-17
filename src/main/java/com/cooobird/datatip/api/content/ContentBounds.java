package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.layout.TipMath;

/**
 * 旧内容接口使用的溢出安全尺寸工具。
 */
final class ContentBounds {
    private ContentBounds() {
    }

    static int dimension(int value) {
        return Math.max(1, value);
    }

    static int spacing(int value) {
        return Math.max(0, value);
    }

    static int add(int... values) {
        long total = 0;
        for (int value : values) {
            total += value;
            if (total >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        }
        return (int) Math.max(0, total);
    }

    static int offset(int value) {
        return value;
    }

    static int negativeInset(int offset) {
        if (offset >= 0) return 0;
        return offset == Integer.MIN_VALUE ? Integer.MAX_VALUE : -offset;
    }

    static long negativeInsetLong(int offset) {
        return offset < 0 ? -(long) offset : 0;
    }

    static int coordinate(int origin, long... offsets) {
        long value = origin;
        for (long offset : offsets) {
            value = TipMath.add(value, offset);
        }
        return (int) Math.max(
            Integer.MIN_VALUE,
            Math.min(Integer.MAX_VALUE, value)
        );
    }

    static int extent(int dimension, int offset) {
        long absoluteOffset = offset == Integer.MIN_VALUE
            ? (long) Integer.MAX_VALUE + 1
            : Math.abs((long) offset);
        return (int) Math.min(Integer.MAX_VALUE, (long) dimension + absoluteOffset);
    }

    static int scaledDimension(int dimension, float scale) {
        double scaled = dimension * (double) scale;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1, scaled));
    }

    static int pieDimension(int value) {
        return dimension(value);
    }

    static boolean beginHorizontalClip(
        TipRenderContext context, int x, int y, int availableWidth, int height, int naturalWidth
    ) {
        // 旧横向 scissor 会截断标签和旋转模型；物理裁剪统一交给根 viewport。
        return false;
    }

    static void endHorizontalClip(TipRenderContext context, boolean clipped) {
        if (clipped) {
            context.graphics().flush();
            context.graphics().disableScissor();
        }
    }
}
