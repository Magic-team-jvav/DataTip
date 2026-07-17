package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.layout.OverflowPolicy;
import com.cooobird.datatip.internal.layout.PreparedLeafSupport;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 进度条内容，支持多种样式：平面、渐变、分段、动画。
 */
public record ProgressContent(
    float progress,                // 进度值
    int width,                     // 宽度
    int height,                    // 高度
    int colorFg,                   // 前景色
    int colorBg,                   // 背景色
    @Nullable Integer colorFgLight, // 前景色
    @Nullable Integer colorBgDark,  // 背景色
    ProgressStyle style,           // 样式
    boolean showLabel,             // 显示标签
    @Nullable Component customLabel, // 自定义标签
    LabelAlign labelAlign,         // 标签对齐
    boolean animated,              // 是否动画
    int animSpeed                  // 动画速度
) implements com.cooobird.datatip.api.layout.PreparedContent {

    public enum ProgressStyle {
        FLAT, GRADIENT, SEGMENTED, ANIMATED
    }

    public enum LabelAlign {
        LEFT, CENTER, RIGHT
    }

    public ProgressContent(
        float progress,
        int width,
        int height,
        int colorFg,
        int colorBg,
        @Nullable Integer colorFgLight,
        @Nullable Integer colorBgDark,
        ProgressStyle style,
        boolean showLabel,
        @Nullable Component customLabel,
        LabelAlign labelAlign,
        boolean animated,
        int animSpeed
    ) {
        ProgressStyle resolvedStyle = style != null
            ? style
            : ProgressStyle.GRADIENT;
        if (animated) {
            resolvedStyle = ProgressStyle.ANIMATED;
        }
        this.progress = Float.isFinite(progress) ? progress : 0.0f;
        this.width = ContentBounds.dimension(width);
        this.height = ContentBounds.dimension(height);
        this.colorFg = colorFg;
        this.colorBg = colorBg;
        this.colorFgLight = colorFgLight;
        this.colorBgDark = colorBgDark;
        this.style = resolvedStyle;
        this.showLabel = showLabel;
        this.customLabel = customLabel;
        this.labelAlign = labelAlign != null ? labelAlign : LabelAlign.LEFT;
        this.animated = resolvedStyle == ProgressStyle.ANIMATED;
        this.animSpeed = ContentBounds.dimension(animSpeed);
    }

    public static ProgressContent of(float progress, int width) {
        return new ProgressContent(progress, width, 8,
            0xFF55FF55, 0xFF333333, 0xFF81C784, 0xFF1A1A1A,
            ProgressStyle.GRADIENT, false, null, LabelAlign.LEFT, false, 0);
    }

    public static ProgressContent withLabel(float progress, int width) {
        return new ProgressContent(progress, width, 8,
            0xFF55FF55, 0xFF333333, 0xFF81C784, 0xFF1A1A1A,
            ProgressStyle.GRADIENT, true, null, LabelAlign.LEFT, false, 0);
    }

    public static ProgressContent colored(float progress, int width, int colorFg) {
        return new ProgressContent(progress, width, 8,
            colorFg, 0xFF333333, null, null,
            ProgressStyle.FLAT, false, null, LabelAlign.LEFT, false, 0);
    }

    public static ProgressContent segmented(float progress, int width, int segments) {
        return new ProgressContent(progress, width * segments, 8,
            0xFF55FF55, 0xFF333333, null, null,
            ProgressStyle.SEGMENTED, false, null, LabelAlign.LEFT, false, 0);
    }

    public static ProgressContent animated(float progress, int width, int speed) {
        return new ProgressContent(progress, width, 8,
            0xFF55FF55, 0xFF333333, 0xFF81C784, 0xFF1A1A1A,
            ProgressStyle.ANIMATED, false, null, LabelAlign.LEFT, true, speed);
    }

    public static ProgressContent withCustomLabel(float progress, int width, Component label) {
        return new ProgressContent(progress, width, 8,
            0xFF55FF55, 0xFF333333, 0xFF81C784, 0xFF1A1A1A,
            ProgressStyle.GRADIENT, true, label, LabelAlign.LEFT, false, 0);
    }

    @Override
    public int getHeight(int maxWidth) {
        return getHeight(new TipLayoutContext(
            net.minecraft.client.Minecraft.getInstance().font,
            net.minecraft.world.item.ItemStack.EMPTY,
            Math.max(0, maxWidth)
        ));
    }

    @Override
    public int getHeight(TipLayoutContext context) {
        return height + (showLabel ? 2 + context.font().lineHeight + 1 : 0);
    }

    @Override
    public int getWidth(int maxWidth) {
        return getWidth(new TipLayoutContext(
            net.minecraft.client.Minecraft.getInstance().font,
            net.minecraft.world.item.ItemStack.EMPTY,
            Math.max(0, maxWidth)
        ));
    }

    @Override
    public int getWidth(TipLayoutContext context) {
        if (!showLabel) return width;
        return Math.max(width, context.font().width(labelText()) + 1);
    }

    @Override
    public boolean isAnimated() {
        return animated;
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0) return;

        // 限制宽度不超过 maxWidth
        int totalWidth = Math.max(1, maxWidth);
        int renderWidth = Math.max(1, Math.min(width, totalWidth));
        float clampedProgress = Math.max(0, Math.min(1, progress));
        int filledWidth = (int) (renderWidth * clampedProgress);

        switch (style) {
            case FLAT -> {
                context.fill(x, y, x + renderWidth, y + height, colorBg);
                if (filledWidth > 0) {
                    context.fill(x, y, x + filledWidth, y + height, colorFg);
                }
            }
            case GRADIENT -> {
                int bgDark = colorBgDark != null ? colorBgDark : colorBg;
                context.fillGradientV(x, y, x + renderWidth, y + height, colorBg, bgDark);
                if (filledWidth > 0) {
                    int fgLight = colorFgLight != null ? colorFgLight : colorFg;
                    context.fillGradientV(x, y, x + filledWidth, y + height, fgLight, colorFg);
                }
            }
            case SEGMENTED -> {
                int segmentWidth = 8;
                int gap = 2;
                int segments = renderWidth / (segmentWidth + gap);
                int filledSegments = (int) (segments * clampedProgress);
                for (int i = 0; i < segments; i++) {
                    int segX = x + i * (segmentWidth + gap);
                    int color = i < filledSegments ? colorFg : colorBg;
                    context.fill(segX, y, segX + segmentWidth, y + height, color);
                }
            }
            case ANIMATED -> {
                context.fill(x, y, x + renderWidth, y + height, colorBg);
                if (filledWidth > 0) {
                    // 先绘制稳定的实际进度，再让高光在已填充区域内移动，避免动画过程中
                    // 填充长度变化造成闪烁或误导。
                    context.fill(x, y, x + filledWidth, y + height, colorFg);
                    int highlightWidth = Math.min(12, Math.max(2, filledWidth / 4));
                    int travel = filledWidth + highlightWidth;
                    int highlightStart = Math.floorMod(context.tickCount() / animSpeed, travel) - highlightWidth;
                    int clippedStart = Math.max(0, highlightStart);
                    int clippedEnd = Math.min(filledWidth, highlightStart + highlightWidth);
                    if (clippedEnd > clippedStart) {
                        int highlightColor = colorFgLight != null ? colorFgLight : brighten(colorFg);
                        context.fill(x + clippedStart, y, x + clippedEnd, y + height, highlightColor);
                    }
                }
            }
        }

        if (showLabel) {
            Component labelText = labelText();
            int labelY = y + height + 2;
            int labelX = switch (labelAlign) {
                case LEFT -> x;
                case CENTER -> x + totalWidth / 2;
                case RIGHT -> x + totalWidth;
            };
            if (labelAlign == LabelAlign.CENTER) {
                context.drawCenteredString(labelText, labelX, labelY, 0xFFFFFF);
            } else if (labelAlign == LabelAlign.RIGHT) {
                context.drawRightAlignedString(labelText, labelX, labelY, 0xFFFFFF);
            } else {
                context.drawString(labelText, labelX, labelY, 0xFFFFFF);
            }
        }
    }

    private static int brighten(int color) {
        int alpha = color >>> 24;
        if (alpha == 0) alpha = 0xFF;
        int red = Math.min(255, ((color >>> 16) & 0xFF) + 48);
        int green = Math.min(255, ((color >>> 8) & 0xFF) + 48);
        int blue = Math.min(255, (color & 0xFF) + 48);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private Component labelText() {
        float clampedProgress = Math.max(0, Math.min(1, progress));
        return customLabel != null
            ? customLabel
            : Component.literal((int) (clampedProgress * 100) + "%");
    }

    @Override
    public com.cooobird.datatip.api.layout.PreparedLayout prepare(
        com.cooobird.datatip.api.layout.TipPrepareContext context
    ) {
        var font = context.requireLayoutContext().font();
        int hardWidth = (int) Math.min(
            Integer.MAX_VALUE,
            context.measureSpec().hardMaxWidth()
        );
        Component frozenLabel = showLabel ? labelText().copy() : null;
        int naturalLabelWidth = frozenLabel != null
            ? font.width(frozenLabel) + 1
            : 0;
        int naturalWidth = Math.max(width, naturalLabelWidth);
        int naturalHeight = height + (frozenLabel != null
            ? 2 + font.lineHeight + 1
            : 0);
        int preparedBarWidth = Math.min(width, hardWidth);

        List<FormattedCharSequence> split = frozenLabel != null
            ? font.split(frozenLabel, Math.max(1, hardWidth - 1))
            : List.of();
        int allocatedWidth = preparedBarWidth;
        ArrayList<Integer> lineWidths = new ArrayList<>(split.size());
        for (FormattedCharSequence line : split) {
            int lineWidth = font.width(line) + 1;
            lineWidths.add(lineWidth);
            allocatedWidth = Math.max(allocatedWidth, lineWidth);
        }
        int allocatedHeight = height;
        if (!split.isEmpty()) {
            allocatedHeight = ContentBounds.add(
                height,
                2,
                split.size() * font.lineHeight,
                1
            );
        }

        int frozenAllocatedWidth = allocatedWidth;
        int barX = alignedOffset(
            frozenAllocatedWidth,
            preparedBarWidth,
            labelAlign
        );
        List<FormattedCharSequence> frozenLines = List.copyOf(split);
        List<Integer> frozenLineWidths = List.copyOf(lineWidths);
        return PreparedLeafSupport.draw(
            naturalWidth,
            naturalHeight,
            naturalWidth,
            naturalHeight,
            allocatedWidth,
            allocatedHeight,
            OverflowPolicy.WRAP,
            com.cooobird.datatip.api.render.RenderPhase.OVERLAY,
            "progress",
            (renderContext, x, y, alpha) -> {
                drawPreparedBar(
                    renderContext,
                    ContentBounds.coordinate(x, barX),
                    y,
                    preparedBarWidth,
                    alpha
                );
                for (int index = 0; index < frozenLines.size(); index++) {
                    int lineX = alignedOffset(
                        frozenAllocatedWidth,
                        frozenLineWidths.get(index),
                        labelAlign
                    );
                    renderContext.graphics().drawString(
                        font,
                        frozenLines.get(index),
                        ContentBounds.coordinate(x, lineX),
                        ContentBounds.coordinate(
                            y,
                            height,
                            2,
                            (long) index * font.lineHeight
                        ),
                        TipRenderContext.applyAlpha(0xFFFFFFFF, alpha),
                        true
                    );
                }
            }
        );
    }

    private void drawPreparedBar(
        TipRenderContext context,
        int x,
        int y,
        int renderWidth,
        float alpha
    ) {
        float clampedProgress = Math.max(0, Math.min(1, progress));
        int filledWidth = (int) (renderWidth * clampedProgress);
        int background = TipRenderContext.applyAlpha(colorBg, alpha);
        int foreground = TipRenderContext.applyAlpha(colorFg, alpha);
        switch (style) {
            case FLAT -> {
                context.fill(x, y, x + renderWidth, y + height, background);
                if (filledWidth > 0) {
                    context.fill(x, y, x + filledWidth, y + height, foreground);
                }
            }
            case GRADIENT -> {
                int backgroundDark = TipRenderContext.applyAlpha(
                    colorBgDark != null ? colorBgDark : colorBg,
                    alpha
                );
                context.fillGradientV(
                    x,
                    y,
                    x + renderWidth,
                    y + height,
                    background,
                    backgroundDark
                );
                if (filledWidth > 0) {
                    int foregroundLight = TipRenderContext.applyAlpha(
                        colorFgLight != null ? colorFgLight : colorFg,
                        alpha
                    );
                    context.fillGradientV(
                        x,
                        y,
                        x + filledWidth,
                        y + height,
                        foregroundLight,
                        foreground
                    );
                }
            }
            case SEGMENTED -> {
                int segmentWidth = 8;
                int gap = 2;
                int segments = renderWidth / (segmentWidth + gap);
                int filledSegments = (int) (segments * clampedProgress);
                for (int index = 0; index < segments; index++) {
                    int segmentX = x + index * (segmentWidth + gap);
                    context.fill(
                        segmentX,
                        y,
                        segmentX + segmentWidth,
                        y + height,
                        index < filledSegments ? foreground : background
                    );
                }
            }
            case ANIMATED -> {
                context.fill(x, y, x + renderWidth, y + height, background);
                if (filledWidth <= 0) return;
                context.fill(x, y, x + filledWidth, y + height, foreground);
                int highlightWidth = Math.min(
                    12,
                    Math.max(2, filledWidth / 4)
                );
                int travel = filledWidth + highlightWidth;
                int highlightStart = Math.floorMod(
                    context.tickCount() / animSpeed,
                    travel
                ) - highlightWidth;
                int clippedStart = Math.max(0, highlightStart);
                int clippedEnd = Math.min(
                    filledWidth,
                    highlightStart + highlightWidth
                );
                if (clippedEnd > clippedStart) {
                    int highlight = TipRenderContext.applyAlpha(
                        colorFgLight != null
                            ? colorFgLight
                            : brighten(colorFg),
                        alpha
                    );
                    context.fill(
                        x + clippedStart,
                        y,
                        x + clippedEnd,
                        y + height,
                        highlight
                    );
                }
            }
        }
    }

    private static int alignedOffset(
        int available,
        int contentWidth,
        LabelAlign alignment
    ) {
        return switch (alignment) {
            case LEFT -> 0;
            case CENTER -> (available - contentWidth) / 2;
            case RIGHT -> available - contentWidth;
        };
    }
}
