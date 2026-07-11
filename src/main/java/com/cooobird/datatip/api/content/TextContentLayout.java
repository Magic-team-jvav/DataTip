package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipLayoutContext;
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
        return getHeight(content, new TipLayoutContext(
            Minecraft.getInstance().font, net.minecraft.world.item.ItemStack.EMPTY, Math.max(0, availableWidth)));
    }

    static int getHeight(TextContent content, Font font, int availableWidth) {
        return getHeight(content, new TipLayoutContext(
            font, net.minecraft.world.item.ItemStack.EMPTY, Math.max(0, availableWidth)));
    }

    static int getHeight(TextContent content, TipLayoutContext context) {
        int effectiveWidth = effectiveWidth(content, context);
        FormattedText text = content.formattedText(context.itemStack());
        if (text == FormattedText.EMPTY) return 0;

        List<FormattedCharSequence> lines = context.font().split(text, effectiveWidth);
        return Math.max(1, lines.size()) * content.lineHeight;
    }

    static boolean hasContent(TextContent content) {
        return content.formattedText((net.minecraft.world.item.ItemStack) null) != FormattedText.EMPTY;
    }

    static int getWidth(TextContent content, int availableWidth) {
        if (content.shift && !BaseTextContent.isShowTipDown()) {
            return 0;
        }
        return getWidth(content, new TipLayoutContext(
            Minecraft.getInstance().font, net.minecraft.world.item.ItemStack.EMPTY, Math.max(0, availableWidth)));
    }

    static int getWidth(TextContent content, Font font, int availableWidth) {
        return getWidth(content, new TipLayoutContext(
            font, net.minecraft.world.item.ItemStack.EMPTY, Math.max(0, availableWidth)));
    }

    static int getWidth(TextContent content, TipLayoutContext context) {
        int effectiveWidth = effectiveWidth(content, context);
        FormattedText text = content.formattedText(context.itemStack());
        if (text == FormattedText.EMPTY) return 0;

        List<FormattedCharSequence> lines = context.font().split(text, effectiveWidth);
        int measuredWidth = 0;
        for (FormattedCharSequence line : lines) {
            measuredWidth = Math.max(measuredWidth, context.font().width(line));
        }
        if (content.maxWidth() > 0 && (content.align != BaseTextContent.TextAlign.LEFT || context.font().width(text) > effectiveWidth)) {
            return effectiveWidth;
        }
        return context.constrainWidth(measuredWidth);
    }

    static void render(TextContent content, TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0) return;
        if (content.formattedText(context.itemStack()) == FormattedText.EMPTY) return;

        if (content.shift && !BaseTextContent.isShowTipDown()) {
            BaseTextContent.renderShiftHint(context, x, y);
            return;
        }

        Font font = context.font();
        int resolvedColor = TipRenderContext.applyAlpha(content.resolveColor(context), alpha);
        int renderWidth = effectiveWidth(content,
            TipLayoutContext.bounded(font, context.itemStack(), Math.max(1, maxWidth)));
        renderWrapped(content, context, font, x, y, renderWidth, resolvedColor);
    }

    private static int effectiveWidth(TextContent content, TipLayoutContext context) {
        if (content.maxWidth() > 0) {
            return Math.max(1, Math.min(content.maxWidth(), context.availableWidth()));
        }
        return Math.max(1, context.availableWidth());
    }

    private static void renderWrapped(TextContent content, TipRenderContext context, Font font, int x, int y, int maxWidth, int color) {
        FormattedText text = content.formattedText(context.itemStack());
        List<FormattedCharSequence> lines = font.split(text, maxWidth);

        for (FormattedCharSequence line : lines) {
            int lineX = content.calcLineX(font, line, x, maxWidth);
            context.graphics().drawString(font, line, lineX, y, color, content.shadow);
            y += content.lineHeight;
        }
    }

}
