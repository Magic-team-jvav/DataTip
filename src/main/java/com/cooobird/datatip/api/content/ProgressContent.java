package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipRenderContext;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

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
) implements TipContent {

    public enum ProgressStyle {
        FLAT, GRADIENT, SEGMENTED, ANIMATED
    }

    public enum LabelAlign {
        LEFT, CENTER, RIGHT
    }

    private static int animOffset = 0;

    public ProgressContent {
        progress = Float.isFinite(progress) ? progress : 0.0f;
        width = Math.max(1, width);
        height = Math.max(1, height);
        style = style != null ? style : ProgressStyle.GRADIENT;
        labelAlign = labelAlign != null ? labelAlign : LabelAlign.LEFT;
        animSpeed = Math.max(1, animSpeed);
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
        return height + (showLabel ? 12 : 0);
    }

    @Override
    public int getWidth(int maxWidth) {
        return Math.min(width, maxWidth);
    }

    @Override
    public boolean isAnimated() {
        return animated;
    }

    @Override
    public void tick(int tickCount) {
        if (animated && tickCount % animSpeed == 0) {
            animOffset = (animOffset + 1) % width;
        }
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0) return;

        // 限制宽度不超过 maxWidth
        int renderWidth = Math.max(1, Math.min(width, maxWidth));
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
                    for (int i = 0; i < filledWidth; i++) {
                        int posX = (i + animOffset) % renderWidth;
                        if (posX < filledWidth) {
                            context.fill(x + posX, y, x + posX + 1, y + height, colorFg);
                        }
                    }
                }
            }
        }

        if (showLabel) {
            String labelText = customLabel != null ? customLabel.getString() : (int) (clampedProgress * 100) + "%";
            int labelY = y + height + 2;
            int labelX = switch (labelAlign) {
                case LEFT -> x;
                case CENTER -> x + renderWidth / 2;
                case RIGHT -> x + renderWidth;
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
}
