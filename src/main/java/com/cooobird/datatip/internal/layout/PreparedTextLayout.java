package com.cooobird.datatip.internal.layout;

import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.content.TextContent;
import com.cooobird.datatip.api.layout.*;
import com.cooobird.datatip.api.render.*;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本在准备阶段生成的稳定换行和绘制快照。
 */
public final class PreparedTextLayout {
    private PreparedTextLayout() {
    }

    public static PreparedLayout prepare(
        TextContent content,
        TipPrepareContext context,
        FormattedText text,
        int color
    ) {
        return prepareDynamic(content, context, text, ignored -> color);
    }

    /**
     * 准备颜色可随渲染时间变化的文本，布局结果仍只计算一次。
     */
    public static PreparedLayout prepareDynamic(
        TextContent content,
        TipPrepareContext context,
        FormattedText text,
        TextColorProvider colorProvider
    ) {
        TipLayoutContext layoutContext = context.requireLayoutContext();
        Font font = layoutContext.font();
        TipMeasureSpec spec = context.measureSpec();

        TextBlock natural = layout(
            content,
            font,
            text,
            unboundedWidth(font, text, content.shadow())
        );
        if (natural.lines().isEmpty()) {
            return PreparedLayout.create(
                TipRect.ZERO,
                new TipSize(0, 0),
                TipRect.ZERO,
                TipRect.ZERO,
                null,
                OverflowPolicy.WRAP,
                RenderCommandPipeline.empty()
            );
        }

        int preferredLimit = positiveMinimum(
            content.maxWidth(),
            toWidth(spec.softMaxWidth())
        );
        TextBlock preferred = preferredLimit > 0
            ? layout(content, font, text, preferredLimit)
            : natural;
        int allocatedLimit = positiveMinimum(
            preferredLimit,
            toWidth(spec.hardMaxWidth())
        );
        TextBlock allocated = layout(
            content,
            font,
            text,
            allocatedLimit > 0 ? allocatedLimit : natural.width()
        );

        List<PreparedLine> frozenLines = allocated.lines();
        OverlayCommandPayload payload = new OverlayCommandPayload(
            "text",
            new VisibleTextDraw(
                frozenLines,
                font,
                colorProvider,
                content.shadow()
            )
        );
        TipRect allocatedBounds = new TipRect(
            0,
            0,
            allocated.width(),
            allocated.height()
        );
        RenderCommand command = RenderCommand.positioned(
            RenderPhase.OVERLAY,
            0,
            0,
            allocatedBounds,
            null,
            payload
        );
        return PreparedLayout.create(
            new TipRect(0, 0, natural.width(), natural.height()),
            new TipSize(preferred.width(), preferred.height()),
            allocatedBounds,
            allocatedBounds,
            null,
            OverflowPolicy.WRAP,
            new RenderCommandPipeline(
                List.of(),
                RenderCommandGroup.root(List.of(command))
            )
        );
    }

    private static TextBlock layout(
        TextContent content,
        Font font,
        FormattedText text,
        int totalLimit
    ) {
        int shadowRight = content.shadow() ? 1 : 0;
        int shadowBottom = content.shadow() ? 1 : 0;
        int splitWidth = Math.max(1, totalLimit - shadowRight);
        List<FormattedCharSequence> split = font.split(text, splitWidth);
        if (split.isEmpty()) return new TextBlock(List.of(), 0, 0);

        int blockWidth = 0;
        int[] widths = new int[split.size()];
        for (int index = 0; index < split.size(); index++) {
            widths[index] = font.width(split.get(index));
            blockWidth = Math.max(blockWidth, widths[index] + shadowRight);
        }

        ArrayList<PreparedLine> lines = new ArrayList<>(split.size());
        for (int index = 0; index < split.size(); index++) {
            int lineWidth = widths[index] + shadowRight;
            int x = switch (content.align()) {
                case LEFT -> 0;
                case CENTER -> (blockWidth - lineWidth) / 2;
                case RIGHT -> blockWidth - lineWidth;
            };
            lines.add(new PreparedLine(
                split.get(index),
                x,
                (long) index * content.lineHeight()
            ));
        }

        int flowHeight = saturatedMultiply(split.size(), content.lineHeight());
        int visualBottom = saturatedAdd(
            saturatedMultiply(split.size() - 1, content.lineHeight()),
            font.lineHeight,
            shadowBottom
        );
        return new TextBlock(
            List.copyOf(lines),
            blockWidth,
            Math.max(flowHeight, visualBottom)
        );
    }

    private static int unboundedWidth(
        Font font,
        FormattedText text,
        boolean shadow
    ) {
        long width = (long) font.width(text) + (shadow ? 1 : 0);
        return (int) Math.max(
            1,
            Math.min(Integer.MAX_VALUE, width)
        );
    }

    private static int positiveMinimum(int first, int second) {
        if (first <= 0) return Math.max(0, second);
        if (second <= 0) return first;
        return Math.min(first, second);
    }

    private static int toWidth(long value) {
        return value > 0
            ? (int) Math.min(Integer.MAX_VALUE, value)
            : 0;
    }

    private static int saturatedMultiply(int first, int second) {
        long result = (long) first * second;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, result));
    }

    private static int saturatedAdd(int... values) {
        long result = 0;
        for (int value : values) {
            result = Math.min(Integer.MAX_VALUE, result + value);
        }
        return (int) Math.max(0, result);
    }

    private record PreparedLine(
        FormattedCharSequence text,
        int x,
        long y
    ) {
    }

    private record VisibleTextDraw(
        List<PreparedLine> lines,
        Font font,
        TextColorProvider colorProvider,
        boolean shadow
    ) implements PreparedViewportDraw {
        @Override
        public void render(
            com.cooobird.datatip.api.TipRenderContext context,
            int x,
            int y,
            float alpha,
            int viewportX,
            int viewportY,
            int viewportWidth,
            int viewportHeight
        ) {
            long localTop = (long) viewportY - y;
            long localBottom = localTop + Math.max(0, viewportHeight);
            int visualHeight = font.lineHeight + (shadow ? 1 : 0);
            int first = lowerBound(lines, localTop - visualHeight + 1);
            int last = lowerBound(lines, localBottom);
            int drawColor = TipRenderContext.applyAlpha(
                colorProvider.color(context),
                alpha
            );
            for (int index = first; index < last; index++) {
                PreparedLine line = lines.get(index);
                context.graphics().drawString(
                    font,
                    line.text(),
                    coordinate((long) x + line.x()),
                    coordinate((long) y + line.y()),
                    drawColor,
                    shadow
                );
            }
        }

        private static int lowerBound(
            List<PreparedLine> lines,
            long minimumY
        ) {
            int low = 0;
            int high = lines.size();
            while (low < high) {
                int middle = (low + high) >>> 1;
                if (lines.get(middle).y() < minimumY) {
                    low = middle + 1;
                } else {
                    high = middle;
                }
            }
            return low;
        }
    }

    private static int coordinate(long value) {
        return (int) Math.max(
            Integer.MIN_VALUE,
            Math.min(Integer.MAX_VALUE, value)
        );
    }

    private record TextBlock(
        List<PreparedLine> lines,
        int width,
        int height
    ) {
    }

    /**
     * 在实际绘制时解析整段文本颜色。
     */
    @FunctionalInterface
    public interface TextColorProvider {
        int color(TipRenderContext context);
    }
}
