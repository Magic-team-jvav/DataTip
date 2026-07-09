package com.cooobird.datatip.api.content;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

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
        if (content.shift && !BaseTextContent.isShowTipDown()) {
            return 0;
        }

        List<String> currentLines = TypewriterTextSource.currentLines(content);
        if (currentLines.isEmpty()) return 0;

        Font font = Minecraft.getInstance().font;
        int maxLineWidth = 0;
        for (String line : currentLines) {
            maxLineWidth = Math.max(maxLineWidth, font.width(line));
        }
        return Math.min(maxLineWidth, maxWidth);
    }
}
