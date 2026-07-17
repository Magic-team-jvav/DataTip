package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.layout.OverflowPolicy;
import com.cooobird.datatip.internal.layout.PreparedLeafSupport;

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
) implements com.cooobird.datatip.api.layout.PreparedContent {

    public DividerContent(
        int color,
        int thickness,
        int width,
        int marginTop,
        int marginBottom,
        DividerStyle style,
        WidthMode widthMode
    ) {
        this.color = color;
        this.thickness = ContentBounds.dimension(thickness);
        this.width = ContentBounds.spacing(width);
        this.marginTop = ContentBounds.spacing(marginTop);
        this.marginBottom = ContentBounds.spacing(marginBottom);
        this.style = style != null ? style : DividerStyle.SOLID;
        this.widthMode = widthMode != null ? widthMode : WidthMode.FILL;
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
        return ContentBounds.add(marginTop, thickness, marginBottom);
    }

    @Override
    public int getWidth(int maxWidth) {
        return widthMode == WidthMode.FILL ? 0 : width;
    }

    @Override
    public int getWidth(TipLayoutContext context) {
        return widthMode == WidthMode.FILL ? 0 : width;
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

    @Override
    public com.cooobird.datatip.api.layout.PreparedLayout prepare(
        com.cooobird.datatip.api.layout.TipPrepareContext context
    ) {
        int height = getHeight(0);
        if (widthMode == WidthMode.FILL) {
            return PreparedLeafSupport.empty(0, height);
        }
        int allocatedWidth = (int) Math.min(
            width,
            context.measureSpec().hardMaxWidth()
        );
        return prepareWidth(width, allocatedWidth);
    }

    com.cooobird.datatip.api.layout.PreparedLayout prepareAllocated(
        int allocatedWidth
    ) {
        int safeWidth = Math.max(0, allocatedWidth);
        return prepareWidth(safeWidth, safeWidth);
    }

    private com.cooobird.datatip.api.layout.PreparedLayout prepareWidth(
        int naturalWidth,
        int allocatedWidth
    ) {
        int totalHeight = getHeight(allocatedWidth);
        int lineY = marginTop;
        return PreparedLeafSupport.draw(
            naturalWidth,
            totalHeight,
            naturalWidth,
            totalHeight,
            allocatedWidth,
            totalHeight,
            OverflowPolicy.SCALE_DOWN,
            com.cooobird.datatip.api.render.RenderPhase.VISUAL_2D,
            "divider",
            (context, x, y, alpha) -> drawPrepared(
                context,
                x,
                y + lineY,
                allocatedWidth,
                alpha
            )
        );
    }

    private void drawPrepared(
        TipRenderContext context,
        int x,
        int y,
        int lineWidth,
        float alpha
    ) {
        int renderColor = TipRenderContext.applyAlpha(color, alpha);
        int right = x + lineWidth;
        switch (style) {
            case SOLID -> context.fill(x, y, right, y + thickness, renderColor);
            case DASHED -> {
                int segmentLength = 8;
                int gap = 4;
                for (int current = x; current < right;
                     current += segmentLength + gap) {
                    context.fill(
                        current,
                        y,
                        Math.min(current + segmentLength, right),
                        y + thickness,
                        renderColor
                    );
                }
            }
            case DOTTED -> {
                int dotSize = 2;
                int gap = 4;
                for (int current = x; current < right;
                     current += dotSize + gap) {
                    context.fill(
                        current,
                        y,
                        Math.min(current + dotSize, right),
                        y + thickness,
                        renderColor
                    );
                }
            }
        }
    }
}
