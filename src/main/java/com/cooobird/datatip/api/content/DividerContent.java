package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;

/**
 * 分割线内容，用于视觉上分隔不同的内容区域。
 */
public record DividerContent(
    int color,          // 颜色
    int thickness,      // 厚度
    int width,          // 宽度
    int marginTop,      // 上边距
    int marginBottom,   // 下边距
    DividerStyle style, // 样式
    WidthMode widthMode // 宽度模式
) implements TipContent {

    public DividerContent {
        thickness = ContentBounds.dimension(thickness);
        width = ContentBounds.spacing(width);
        marginTop = ContentBounds.spacing(marginTop);
        marginBottom = ContentBounds.spacing(marginBottom);
        style = style != null ? style : DividerStyle.SOLID;
        widthMode = widthMode != null ? widthMode : WidthMode.FILL;
    }

    public enum DividerStyle {SOLID, DASHED, DOTTED}

    public enum WidthMode {
        FIXED,    // 固定宽度
        FILL,     // 填充可用宽度
        CENTERED  // 居中
    }

    public static DividerContent create() {
        return new DividerContent(0xFF555555, 1, 0, 2, 2, DividerStyle.SOLID, WidthMode.FILL);
    }

    public static DividerContent of(int color) {
        return new DividerContent(color, 1, 0, 2, 2, DividerStyle.SOLID, WidthMode.FILL);
    }

    public static DividerContent of(int color, DividerStyle style) {
        return new DividerContent(color, 1, 0, 2, 2, style, WidthMode.FILL);
    }

    public static DividerContent fixed(int width) {
        return new DividerContent(0xFF555555, 1, width, 2, 2, DividerStyle.SOLID, WidthMode.FIXED);
    }

    public static DividerContent centered(int width) {
        return new DividerContent(0xFF555555, 1, width, 2, 2, DividerStyle.SOLID, WidthMode.CENTERED);
    }

    public static DividerContent dashed() {
        return new DividerContent(0xFF555555, 1, 0, 2, 2, DividerStyle.DASHED, WidthMode.FILL);
    }

    public static DividerContent dotted() {
        return new DividerContent(0xFF555555, 1, 0, 2, 2, DividerStyle.DOTTED, WidthMode.FILL);
    }

    @Override
    public int getHeight(int maxWidth) {
        return marginTop + thickness + marginBottom;
    }

    @Override
    public int getWidth(int maxWidth) {
        return widthMode == WidthMode.FILL ? maxWidth : Math.min(width, maxWidth);
    }

    @Override
    public int getWidth(TipLayoutContext context) {
        if (widthMode == WidthMode.FILL) {
            return context.hasWidthLimit() ? context.maxWidth() : (width > 0 ? width : 100);
        }
        return context.constrainWidth(width);
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0) return;

        int lineWidth = widthMode == WidthMode.FILL ? maxWidth : Math.min(width, maxWidth);
        int lineX = switch (widthMode) {
            case FILL, FIXED -> x;
            case CENTERED -> x + (maxWidth - lineWidth) / 2;
        };
        int lineY = y + marginTop;
        int renderColor = TipRenderContext.applyAlpha(color, alpha);

        switch (style) {
            case SOLID -> context.fill(lineX, lineY, lineX + lineWidth, lineY + thickness, renderColor);
            case DASHED -> {
                int segLen = 8, gap = 4, curX = lineX;
                while (curX < lineX + lineWidth) {
                    int endX = Math.min(curX + segLen, lineX + lineWidth);
                    context.fill(curX, lineY, endX, lineY + thickness, renderColor);
                    curX += segLen + gap;
                }
            }
            case DOTTED -> {
                int dotSize = 2, dotGap = 4, curX = lineX;
                while (curX < lineX + lineWidth) {
                    context.fill(curX, lineY, curX + dotSize, lineY + thickness, renderColor);
                    curX += dotSize + dotGap;
                }
            }
        }
    }
}
