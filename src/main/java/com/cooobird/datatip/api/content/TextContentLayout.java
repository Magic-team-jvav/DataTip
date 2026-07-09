package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * TextContent 尺寸计算与绘制逻辑。
 */
final class TextContentLayout {
    private TextContentLayout() {
    }

    static int getHeight(TextContent content, int availableWidth) {
        if (content.shift && !BaseTextContent.isShowTipDown()) {
            return content.lineHeight;
        }
        return getHeight(content, Minecraft.getInstance().font, availableWidth);
    }

    static int getHeight(TextContent content, Font font, int availableWidth) {
        int effectiveWidth = effectiveWidth(content, availableWidth);
        FormattedText text = content.formattedText(null);
        if (text == FormattedText.EMPTY) return 0;
        if (effectiveWidth <= 0) return content.lineHeight;

        List<FormattedCharSequence> lines = font.split(text, effectiveWidth);
        return Math.max(1, lines.size()) * content.lineHeight;
    }

    static boolean hasContent(TextContent content) {
        return content.formattedText(null) != FormattedText.EMPTY;
    }

    static int getWidth(TextContent content, int availableWidth) {
        if (content.shift && !BaseTextContent.isShowTipDown()) {
            return 0;
        }
        return getWidth(content, Minecraft.getInstance().font, availableWidth);
    }

    static int getWidth(TextContent content, Font font, int availableWidth) {
        int effectiveWidth = effectiveWidth(content, availableWidth);
        FormattedText text = content.formattedText(null);
        if (text == FormattedText.EMPTY || effectiveWidth <= 0) return 0;
        if (content.align == BaseTextContent.TextAlign.CENTER || content.align == BaseTextContent.TextAlign.RIGHT) {
            return effectiveWidth;
        }
        if (content.maxWidth() > 0) return effectiveWidth;

        int textWidth = font.width(text);
        return Math.min(textWidth, availableWidth);
    }

    static void render(TextContent content, TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0) return;
        if (content.formattedText(context) == FormattedText.EMPTY) return;

        if (content.shift && !BaseTextContent.isShowTipDown()) {
            BaseTextContent.renderShiftHint(context, x, y);
            return;
        }

        Font font = context.font();
        int resolvedColor = content.resolveColor(context);
        if (maxWidth > 0) {
            renderWrapped(content, context, font, x, y, maxWidth, resolvedColor);
        } else {
            renderSingleLine(content, context, font, x, y, maxWidth, resolvedColor);
        }
    }

    private static int effectiveWidth(TextContent content, int availableWidth) {
        int effectiveMaxWidth = (content.maxWidth() > 0) ? content.maxWidth() : availableWidth;
        return Math.min(effectiveMaxWidth, availableWidth);
    }

    private static void renderWrapped(TextContent content, TipRenderContext context, Font font, int x, int y, int maxWidth, int color) {
        FormattedText text = content.formattedText(context);
        List<FormattedCharSequence> lines = font.split(text, maxWidth);

        for (FormattedCharSequence line : lines) {
            int lineX = content.calcLineX(font, line, x, maxWidth);
            context.graphics().drawString(font, line, lineX, y, color, content.shadow);
            y += content.lineHeight;
        }
    }

    private static void renderSingleLine(TextContent content, TipRenderContext context, Font font, int x, int y, int maxWidth, int color) {
        FormattedText text = content.formattedText(context);
        List<FormattedCharSequence> lines = font.split(text, Integer.MAX_VALUE);
        if (lines.isEmpty()) return;

        FormattedCharSequence visualText = lines.getFirst();
        int lineX = content.calcLineX(font, visualText, x, maxWidth);
        context.graphics().drawString(font, visualText, lineX, y, color, content.shadow);
    }
}
