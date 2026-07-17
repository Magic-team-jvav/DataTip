package com.cooobird.datatip.internal.layout;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * 图标、模型和标签共享行的完整像素边界。
 */
public final class LabeledVisualBounds {
    private LabeledVisualBounds() {
    }

    public static int width(
        int bodyWidth,
        @Nullable Component label,
        Font font
    ) {
        if (label == null) return bodyWidth;
        long width = (long) bodyWidth + 4 + font.width(label) + 1L;
        return (int) Math.min(Integer.MAX_VALUE, width);
    }

    public static int height(
        int bodyHeight,
        @Nullable Component label,
        Font font
    ) {
        return label == null
            ? bodyHeight
            : Math.max(bodyHeight, font.lineHeight + 1);
    }

    public static int labelY(int y, int rowHeight, Font font) {
        return y + Math.max(0, (rowHeight - font.lineHeight) / 2);
    }
}
