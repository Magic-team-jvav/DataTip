package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.layout.OverflowPolicy;
import com.cooobird.datatip.api.layout.PreparedLayout;
import com.cooobird.datatip.api.layout.TipPrepareContext;
import com.cooobird.datatip.api.render.PreparedViewportDraw;
import com.cooobird.datatip.internal.layout.PreparedLeafSupport;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 打字机在准备阶段冻结语言、变量、样式、换行点和稳定对齐位置。
 */
final class PreparedTypewriterLayout {
    private static final String CURSOR = "▌";

    private PreparedTypewriterLayout() {
    }

    static PreparedLayout prepare(
        TypewriterContent content,
        TipPrepareContext context
    ) {
        Font font = context.requireLayoutContext().font();
        ItemStack stack = context.requireLayoutContext().itemStack();
        List<String> lines = TypewriterLayout.resolvedLines(content, stack);
        if (lines.isEmpty()) return PreparedLeafSupport.empty(0, 0);

        int resolvedColor = content.resolveColor(stack);
        int shadow = content.shadow ? 1 : 0;
        int hardWidth = (int) Math.min(
            Integer.MAX_VALUE,
            context.measureSpec().hardMaxWidth()
        );
        int cursorWidth = font.width(CURSOR);
        int segmentLimit = Math.max(1, hardWidth - cursorWidth - shadow);
        int naturalWidth = 0;
        int naturalHeight = stableHeight(
            lines.size(),
            content.lineHeight,
            font.lineHeight,
            shadow
        );

        ArrayList<RawSegment> rawSegments = new ArrayList<>();
        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            String source = lines.get(lineIndex);
            TypewriterRenderer.RenderLine styled =
                TypewriterRenderer.styledLine(
                    content,
                    source,
                    lineIndex,
                    resolvedColor
                );
            Style style = styled.style();
            naturalWidth = Math.max(
                naturalWidth,
                font.width(
                    Component.literal(source + CURSOR).withStyle(style)
                ) + shadow
            );
            splitLine(
                rawSegments,
                font,
                source,
                style,
                styled.color(),
                lineIndex,
                segmentLimit
            );
        }

        int allocatedWidth = 0;
        for (RawSegment segment : rawSegments) {
            allocatedWidth = Math.max(
                allocatedWidth,
                segment.width() + cursorWidth + shadow
            );
        }
        allocatedWidth = Math.min(hardWidth, allocatedWidth);
        int allocatedHeight = stableHeight(
            rawSegments.size(),
            content.lineHeight,
            font.lineHeight,
            shadow
        );

        ArrayList<Segment> segments = new ArrayList<>(rawSegments.size());
        for (int index = 0; index < rawSegments.size(); index++) {
            RawSegment segment = rawSegments.get(index);
            int fullWidth = segment.width() + cursorWidth + shadow;
            int x = switch (alignment(content, segment.lineIndex())) {
                case LEFT -> 0;
                case CENTER -> (allocatedWidth - fullWidth) / 2;
                case RIGHT -> allocatedWidth - fullWidth;
            };
            segments.add(new Segment(
                segment.source(),
                segment.start(),
                segment.end(),
                segment.lineIndex(),
                segment.style(),
                segment.color(),
                x,
                (long) index * content.lineHeight
            ));
        }

        List<Segment> frozen = List.copyOf(segments);
        return PreparedLeafSupport.draw(
            naturalWidth,
            naturalHeight,
            naturalWidth,
            naturalHeight,
            allocatedWidth,
            allocatedHeight,
            OverflowPolicy.WRAP,
            com.cooobird.datatip.api.render.RenderPhase.OVERLAY,
            "typewriter",
            new VisibleTypewriterDraw(
                content,
                content.state(),
                frozen,
                font
            )
        );
    }

    private static void draw(
        TypewriterContent content,
        TypewriterState state,
        List<Segment> segments,
        Font font,
        TipRenderContext context,
        int x,
        int y,
        float alpha,
        int viewportY,
        int viewportHeight
    ) {
        boolean cursorVisible = !state.completed()
            && state.tickCount() % 20 < 10;
        long localTop = (long) viewportY - y;
        long localBottom = localTop + Math.max(0, viewportHeight);
        int visualHeight = font.lineHeight + (content.shadow ? 1 : 0);
        int first = lowerBound(segments, localTop - visualHeight + 1);
        int last = lowerBound(segments, localBottom);
        for (int index = first; index < last; index++) {
            Segment segment = segments.get(index);
            int currentLine = state.currentLine();
            if (segment.lineIndex() > currentLine) continue;

            String visible;
            boolean drawCursor = false;
            if (segment.lineIndex() < currentLine) {
                visible = segment.source().substring(
                    segment.start(),
                    segment.end()
                );
            } else {
                int caret = Math.min(
                    state.currentChar(),
                    segment.source().length()
                );
                if (caret < segment.start()) continue;
                int end = Math.min(segment.end(), caret);
                visible = segment.source().substring(segment.start(), end);
                drawCursor = cursorVisible
                    && (caret < segment.end()
                    || (caret == segment.end()
                    && segment.end() == segment.source().length()));
            }
            if (drawCursor) visible = visible.concat(CURSOR);
            if (visible.isEmpty()) continue;

            context.graphics().drawString(
                font,
                Component.literal(visible).withStyle(segment.style()),
                coordinate((long) x + segment.x()),
                coordinate((long) y + segment.y()),
                TipRenderContext.applyAlpha(segment.color(), alpha),
                content.shadow
            );
        }
    }

    private static int lowerBound(List<Segment> segments, long minimumY) {
        int low = 0;
        int high = segments.size();
        while (low < high) {
            int middle = (low + high) >>> 1;
            if (segments.get(middle).y() < minimumY) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return low;
    }

    private static int coordinate(long value) {
        return (int) Math.max(
            Integer.MIN_VALUE,
            Math.min(Integer.MAX_VALUE, value)
        );
    }

    private static void splitLine(
        List<RawSegment> destination,
        Font font,
        String source,
        Style style,
        int color,
        int lineIndex,
        int limit
    ) {
        if (source.isEmpty()) {
            destination.add(new RawSegment(
                source,
                0,
                0,
                lineIndex,
                style,
                color,
                0
            ));
            return;
        }

        int start = 0;
        while (start < source.length()) {
            int end = start;
            int width = 0;
            while (end < source.length()) {
                int next = source.offsetByCodePoints(end, 1);
                int candidateWidth = font.width(
                    Component.literal(source.substring(start, next))
                        .withStyle(style)
                );
                if (candidateWidth > limit && end > start) break;
                end = next;
                width = candidateWidth;
                if (candidateWidth > limit) break;
            }
            destination.add(new RawSegment(
                source,
                start,
                end,
                lineIndex,
                style,
                color,
                width
            ));
            start = end;
        }
    }

    private static BaseTextContent.TextAlign alignment(
        TypewriterContent content,
        int lineIndex
    ) {
        BaseTextContent.LangStyle lineStyle =
            TypewriterTextSource.currentLineStyle(content, lineIndex);
        return lineStyle != null ? lineStyle.align() : content.align;
    }

    private static int stableHeight(
        int lineCount,
        int lineHeight,
        int fontHeight,
        int shadow
    ) {
        if (lineCount <= 0) return 0;
        int preceding = (int) Math.min(
            Integer.MAX_VALUE,
            (long) Math.max(0, lineCount - 1) * lineHeight
        );
        int flow = ContentBounds.add(
            preceding,
            lineHeight
        );
        int visual = ContentBounds.add(
            preceding,
            fontHeight,
            shadow
        );
        return Math.max(flow, visual);
    }

    private record RawSegment(
        String source,
        int start,
        int end,
        int lineIndex,
        Style style,
        int color,
        int width
    ) {
    }

    private record Segment(
        String source,
        int start,
        int end,
        int lineIndex,
        Style style,
        int color,
        int x,
        long y
    ) {
    }

    private record VisibleTypewriterDraw(
        TypewriterContent content,
        TypewriterState state,
        List<Segment> segments,
        Font font
    ) implements PreparedViewportDraw {
        @Override
        public void render(
            TipRenderContext context,
            int x,
            int y,
            float alpha,
            int viewportX,
            int viewportY,
            int viewportWidth,
            int viewportHeight
        ) {
            draw(
                content,
                state,
                segments,
                font,
                context,
                x,
                y,
                alpha,
                viewportY,
                viewportHeight
            );
        }
    }
}
