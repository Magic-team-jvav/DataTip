package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.layout.*;
import com.cooobird.datatip.api.render.*;
import com.cooobird.datatip.api.text.LocalizedText;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * 图表的几何、标题和语义图例快照。
 */
final class PreparedChartLayout {
    private PreparedChartLayout() {
    }

    static PreparedLayout prepare(
        ChartContent content,
        TipPrepareContext context
    ) {
        TipLayoutContext layout = context.requireLayoutContext();
        int hardWidth = (int) Math.min(
            Integer.MAX_VALUE,
            context.measureSpec().hardMaxWidth()
        );
        int splitWidth = Math.max(1, hardWidth - 1);
        ArrayList<ChartContent.ChartEntry> entries =
            new ArrayList<>(content.entries().size());
        ArrayList<PreparedChartLine> titleLines = new ArrayList<>();
        ArrayList<PreparedChartLine> legendLines = new ArrayList<>();
        int measuredWidth = Math.min(content.width(), hardWidth);

        if (content.title() != null) {
            for (FormattedCharSequence line
                : layout.font().split(content.title().copy(), splitWidth)) {
                int lineWidth = layout.font().width(line) + 1;
                measuredWidth = Math.max(measuredWidth, lineWidth);
                titleLines.add(new PreparedChartLine(
                    line,
                    content.titleColor(),
                    lineWidth
                ));
            }
        }

        for (ChartContent.ChartEntry entry : content.entries()) {
            String value = resolveValue(entry, layout);
            Component label = entry.labelComponent().copy();
            entries.add(new ChartContent.ChartEntry(
                LocalizedText.component(label),
                value,
                entry.color()
            ));
            Component legend = legend(content, entry, label, value);
            if (legend.getString().isEmpty()) continue;
            for (FormattedCharSequence line
                : layout.font().split(legend, splitWidth)) {
                int lineWidth = layout.font().width(line) + 1;
                measuredWidth = Math.max(measuredWidth, lineWidth);
                legendLines.add(new PreparedChartLine(
                    line,
                    0xFFFFFFFF,
                    lineWidth
                ));
            }
        }

        measuredWidth = Math.max(1, Math.min(hardWidth, measuredWidth));
        int lineStep = layout.font().lineHeight + 2;
        int titleHeight = titleLines.size() * lineStep;
        int legendHeight = legendLines.isEmpty()
            ? 0
            : 4 + legendLines.size() * lineStep;
        int totalHeight = ContentBounds.add(
            titleHeight,
            content.height(),
            legendHeight
        );

        ChartContent geometry = new ChartContent(
            content.type(),
            entries,
            content.width(),
            content.height(),
            null,
            false,
            false,
            content.titleColor(),
            content.labelColor(),
            content.valueColor(),
            content.zeroLineColor()
        );
        PreparedViewportDraw preparedGeometry = hasDynamicValues(content)
            ? new DynamicPreparedChartGeometry(content, measuredWidth)
            : PreparedChartGeometry.prepare(geometry, measuredWidth);
        ArrayList<RenderCommandNode> commands = new ArrayList<>();
        int sourceIndex = 0;
        int finalWidth = measuredWidth;
        commands.add(RenderCommand.positioned(
            RenderPhase.VISUAL_2D,
            0,
            sourceIndex++,
            new TipRect(0, titleHeight, finalWidth, content.height()),
            null,
            new Visual2DCommandPayload(
                "chart-geometry",
                preparedGeometry
            )
        ));

        int y = 0;
        for (PreparedChartLine line : titleLines) {
            commands.add(textCommand(
                "chart-title",
                sourceIndex++,
                line,
                centeredX(finalWidth, line.width()),
                y,
                layout.font().lineHeight + 1
            ));
            y += lineStep;
        }
        y = titleHeight + content.height() + (legendLines.isEmpty() ? 0 : 4);
        for (PreparedChartLine line : legendLines) {
            commands.add(textCommand(
                "chart-legend",
                sourceIndex++,
                line,
                0,
                y,
                layout.font().lineHeight + 1
            ));
            y += lineStep;
        }

        TipRect bounds = new TipRect(0, 0, finalWidth, totalHeight);
        return PreparedLayout.create(
            bounds,
            new TipSize(finalWidth, totalHeight),
            bounds,
            bounds,
            null,
            OverflowPolicy.WRAP,
            new RenderCommandPipeline(
                List.of(),
                RenderCommandGroup.root(commands)
            )
        );
    }

    private static RenderCommand textCommand(
        String token,
        int sourceIndex,
        PreparedChartLine line,
        int x,
        int y,
        int height
    ) {
        return RenderCommand.positioned(
            RenderPhase.OVERLAY,
            0,
            sourceIndex,
            new TipRect(x, y, line.width(), height),
            null,
            new OverlayCommandPayload(
                token,
                (context, drawX, drawY, alpha) ->
                    context.graphics().drawString(
                        context.font(),
                        line.text(),
                        drawX,
                        drawY,
                        TipRenderContext.applyAlpha(line.color(), alpha),
                        true
                    )
            )
        );
    }

    private static Component legend(
        ChartContent content,
        ChartContent.ChartEntry entry,
        Component label,
        String value
    ) {
        String labelValue = content.showLabels() ? label.getString() : "";
        String numberValue = content.showValues() ? value : "";
        int labelColor = content.type() == ChartContent.ChartType.PIE
            ? entry.color()
            : content.labelColor();
        int valueColor = content.type() == ChartContent.ChartType.PIE
            ? entry.color()
            : content.valueColor();
        Component coloredLabel = label.copy().withStyle(
            style -> style.withColor(labelColor)
        );
        Component coloredValue = Component.literal(numberValue).withStyle(
            style -> style.withColor(valueColor)
        );
        if (!labelValue.isEmpty() && !numberValue.isEmpty()) {
            return Component.empty()
                .append(coloredLabel)
                .append(Component.literal(": ").withStyle(
                    style -> style.withColor(labelColor)
                ))
                .append(coloredValue);
        }
        if (!labelValue.isEmpty()) return coloredLabel;
        return coloredValue;
    }

    private static String resolveValue(
        ChartContent.ChartEntry entry,
        TipLayoutContext context
    ) {
        String resolved = com.cooobird.datatip.api.util.VariableResolver.resolve(
            entry.valueExpr(),
            context.itemStack()
        );
        if (resolved == null || resolved.isBlank()) return "0";
        try {
            return ChartContent.formatValue(Double.parseDouble(resolved));
        } catch (NumberFormatException ignored) {
            return resolved;
        }
    }

    private static int centeredX(int totalWidth, int lineWidth) {
        return Math.max(0, (totalWidth - lineWidth) / 2);
    }

    private static boolean hasDynamicValues(ChartContent content) {
        for (ChartContent.ChartEntry entry : content.entries()) {
            if (!com.cooobird.datatip.internal.variable.VariableRegistry
                .isItemStatic(entry.valueExpr())) {
                return true;
            }
        }
        return false;
    }

    private record PreparedChartLine(
        FormattedCharSequence text,
        int color,
        int width
    ) {
    }
}
