package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipLayoutContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 打字机尺寸计算。
 */
final class TypewriterLayout {
    private TypewriterLayout() {
    }

    static int getHeight(TypewriterContent content) {
        if (content.shift && !BaseTextContent.isShowTipDown()) {
            return content.lineHeight;
        }
        return TypewriterTextSource.currentLines(content).size() * content.lineHeight;
    }

    static boolean hasContent(TypewriterContent content) {
        return !TypewriterTextSource.currentLines(content).isEmpty();
    }

    static int getWidth(TypewriterContent content, int maxWidth) {
        TipLayoutContext context = maxWidth > 0
            ? TipLayoutContext.bounded(Minecraft.getInstance().font,
            net.minecraft.world.item.ItemStack.EMPTY, maxWidth)
            : TipLayoutContext.unbounded(Minecraft.getInstance().font,
            net.minecraft.world.item.ItemStack.EMPTY);
        return getWidth(content, context);
    }

    static int getWidth(TypewriterContent content, TipLayoutContext context) {
        if (content.shift && !BaseTextContent.isShowTipDown()) {
            return 0;
        }

        List<String> currentLines = TypewriterTextSource.currentLines(content);
        if (currentLines.isEmpty()) return 0;

        Font font = context.font();
        int maxLineWidth = 0;
        for (int i = 0; i < currentLines.size(); i++) {
            TypewriterRenderer.RenderLine line = TypewriterRenderer.styledLine(
                content, currentLines.get(i) + "▌", i, content.color);
            maxLineWidth = Math.max(maxLineWidth,
                font.width(Component.literal(line.text()).withStyle(line.style())));
        }
        return context.constrainWidth(maxLineWidth);
    }
}
