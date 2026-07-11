package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipRenderContext;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 打字机文本绘制逻辑。
 */
final class TypewriterRenderer {
    private TypewriterRenderer() {
    }

    static void render(
        TypewriterContent content,
        TypewriterState state,
        TipRenderContext context,
        int x,
        int y,
        int maxWidth,
        float alpha
    ) {
        List<String> currentLines = TypewriterTextSource.currentLines(content);
        if (alpha <= 0 || currentLines.isEmpty()) return;

        if (content.shift && !BaseTextContent.isShowTipDown()) {
            BaseTextContent.renderShiftHint(context, x, y);
            return;
        }

        int resolvedColor = content.resolveColor(context);
        Font font = context.font();
        int renderY = y;

        for (int i = 0; i <= state.currentLine() && i < currentLines.size(); i++) {
            RenderLine line = buildLine(content, state, currentLines.get(i), i, resolvedColor);
            if (line == null) break;

            int lineX = content.calcLineX(font, line.text(), x, maxWidth);
            int renderColor = TipRenderContext.applyAlpha(line.color(), alpha);
            context.graphics().drawString(font, Component.literal(line.text()).withStyle(line.style()),
                lineX, renderY, renderColor, content.shadow);
            renderY += content.lineHeight;
        }
    }

    @Nullable
    private static RenderLine buildLine(TypewriterContent content, TypewriterState state, String sourceText,
                                        int lineIndex, int resolvedColor) {
        if (lineIndex < state.currentLine()) {
            return styledLine(content, sourceText, lineIndex, resolvedColor);
        }
        if (lineIndex == state.currentLine()) {
            String displayText = sourceText.substring(0, Math.min(state.currentChar(), sourceText.length()));
            if (!state.completed() && state.tickCount() % 20 < 10) {
                displayText += "▌";
            }
            return styledLine(content, displayText, lineIndex, resolvedColor);
        }
        return null;
    }

    static RenderLine styledLine(TypewriterContent content, String text, int lineIndex, int resolvedColor) {
        int lineColor = resolvedColor;
        boolean lineBold = content.bold;
        boolean lineItalic = content.italic;
        boolean lineUnderlined = content.underlined;
        boolean lineStrikethrough = content.strikethrough;

        BaseTextContent.LangStyle lineStyle = TypewriterTextSource.currentLineStyle(content, lineIndex);
        if (lineStyle != null) {
            lineColor = lineStyle.color();
            lineBold = lineStyle.bold();
            lineItalic = lineStyle.italic();
            lineUnderlined = lineStyle.underlined();
            lineStrikethrough = lineStyle.strikethrough();
        }

        Style style = content.buildStyle(lineColor);
        if (lineBold != content.bold) style = lineBold ? style.withBold(true) : style.withBold(false);
        if (lineItalic != content.italic) style = lineItalic ? style.withItalic(true) : style.withItalic(false);
        if (lineUnderlined != content.underlined)
            style = lineUnderlined ? style.withUnderlined(true) : style.withUnderlined(false);
        if (lineStrikethrough != content.strikethrough)
            style = lineStrikethrough ? style.withStrikethrough(true) : style.withStrikethrough(false);

        return new RenderLine(text, lineColor, style);
    }

    record RenderLine(String text, int color, Style style) {
    }
}
