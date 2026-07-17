package com.cooobird.datatip.internal.layout;

import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.layout.*;
import com.cooobird.datatip.api.render.*;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 带可选标签的图像或模型叶节点布局。
 *
 * <p>主体超过物理 viewport 时等比缩小；标签无法同行时完整换行到
 * 主体下方，不通过裁剪静默丢字。</p>
 */
public final class PreparedLabeledVisualLayout {
    private PreparedLabeledVisualLayout() {
    }

    public static PreparedLayout prepare(
        TipPrepareContext context,
        int naturalBodyWidth,
        int naturalBodyHeight,
        @Nullable Component label,
        int labelColor,
        RenderPhase bodyPhase,
        String bodyToken,
        ScaledBodyDraw bodyDraw
    ) {
        Font font = context.requireLayoutContext().font();
        int hardWidth = (int) Math.min(
            Integer.MAX_VALUE,
            context.measureSpec().hardMaxWidth()
        );
        double bodyScale = naturalBodyWidth > hardWidth
            ? (double) hardWidth / naturalBodyWidth
            : 1.0;
        int bodyWidth = scaled(naturalBodyWidth, bodyScale);
        int bodyHeight = scaled(naturalBodyHeight, bodyScale);
        int naturalWidth = LabeledVisualBounds.width(
            naturalBodyWidth,
            label,
            font
        );
        int naturalHeight = LabeledVisualBounds.height(
            naturalBodyHeight,
            label,
            font
        );

        ArrayList<LabelLine> labelLines = new ArrayList<>();
        int allocatedWidth = bodyWidth;
        int allocatedHeight = bodyHeight;
        if (label != null) {
            int inlineLabelWidth = font.width(label) + 1;
            if ((long) bodyWidth + 4 + inlineLabelWidth <= hardWidth) {
                int rowHeight = Math.max(bodyHeight, font.lineHeight + 1);
                labelLines.add(new LabelLine(
                    label.getVisualOrderText(),
                    bodyWidth + 4,
                    (rowHeight - font.lineHeight) / 2,
                    inlineLabelWidth
                ));
                allocatedWidth = bodyWidth + 4 + inlineLabelWidth;
                allocatedHeight = rowHeight;
            } else {
                int splitWidth = Math.max(1, hardWidth - 1);
                List<FormattedCharSequence> split = font.split(
                    label,
                    splitWidth
                );
                int y = bodyHeight + 2;
                for (FormattedCharSequence line : split) {
                    int lineWidth = font.width(line) + 1;
                    labelLines.add(new LabelLine(line, 0, y, lineWidth));
                    allocatedWidth = Math.max(allocatedWidth, lineWidth);
                    y += font.lineHeight;
                }
                if (!split.isEmpty()) {
                    allocatedHeight = Math.max(
                        allocatedHeight,
                        y + 1
                    );
                }
            }
        }

        TipRect allocated = new TipRect(
            0,
            0,
            allocatedWidth,
            allocatedHeight
        );
        ArrayList<RenderCommandNode> commands = new ArrayList<>();
        var preparedBodyDraw =
            (com.cooobird.datatip.api.render.PreparedImageDraw)
                (renderContext, x, y, alpha) ->
                    bodyDraw.render(
                        renderContext,
                        x,
                        y,
                        bodyScale,
                        alpha
                    );
        commands.add(RenderCommand.positioned(
            bodyPhase,
            0,
            0,
            new TipRect(0, 0, bodyWidth, bodyHeight),
            null,
            switch (bodyPhase) {
                case VISUAL_2D -> new Visual2DCommandPayload(
                    bodyToken,
                    preparedBodyDraw
                );
                case ISOLATED_MODEL -> new ModelCommandPayload(
                    bodyToken,
                    preparedBodyDraw
                );
                case OVERLAY -> new OverlayCommandPayload(
                    bodyToken,
                    preparedBodyDraw
                );
                case ORDINARY_TEXT -> throw new IllegalArgumentException(
                    "Labeled visual body cannot use ordinary text phase"
                );
            }
        ));

        List<LabelLine> frozenLines = List.copyOf(labelLines);
        for (int index = 0; index < frozenLines.size(); index++) {
            LabelLine line = frozenLines.get(index);
            commands.add(RenderCommand.positioned(
                RenderPhase.OVERLAY,
                0,
                index + 1,
                new TipRect(
                    line.x(),
                    line.y(),
                    line.width(),
                    font.lineHeight + 1L
                ),
                null,
                new OverlayCommandPayload(
                    bodyToken + "-label-" + index,
                    (renderContext, x, y, alpha) ->
                        renderContext.graphics().drawString(
                            font,
                            line.text(),
                            x,
                            y,
                            TipRenderContext.applyAlpha(labelColor, alpha),
                            true
                        )
                )
            ));
        }

        return PreparedLayout.create(
            new TipRect(0, 0, naturalWidth, naturalHeight),
            new TipSize(naturalWidth, naturalHeight),
            allocated,
            allocated,
            null,
            labelLines.size() > 1
                ? OverflowPolicy.WRAP
                : OverflowPolicy.SCALE_DOWN,
            new RenderCommandPipeline(
                List.of(),
                RenderCommandGroup.root(commands)
            )
        );
    }

    private static int scaled(int value, double scale) {
        if (value <= 0) return 0;
        return (int) Math.max(
            1,
            Math.min(Integer.MAX_VALUE, Math.round(value * scale))
        );
    }

    @FunctionalInterface
    public interface ScaledBodyDraw {
        void render(
            TipRenderContext context,
            int x,
            int y,
            double scale,
            float alpha
        );
    }

    private record LabelLine(
        FormattedCharSequence text,
        int x,
        int y,
        int width
    ) {
    }
}
