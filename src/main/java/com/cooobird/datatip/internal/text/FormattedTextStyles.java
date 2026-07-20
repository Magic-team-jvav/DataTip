package com.cooobird.datatip.internal.text;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;

/**
 * 调整格式化文本样式，同时保留各文本片段的其他格式。
 */
public final class FormattedTextStyles {
    private FormattedTextStyles() {
    }

    /**
     * 移除片段自带颜色，使绘制阶段的统一颜色可以作用于整段文本。
     */
    public static FormattedText withoutColor(FormattedText text) {
        ArrayList<FormattedText> segments = new ArrayList<>();
        text.visit((style, value) -> {
            Style colorless = style.withColor((TextColor) null);
            segments.add(FormattedText.of(value, colorless));
            return java.util.Optional.empty();
        }, Style.EMPTY);
        return FormattedText.composite(segments);
    }
}

