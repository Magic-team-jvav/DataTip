package com.cooobird.datatip.internal.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一处理文本中的显式换行标记。
 */
public final class FormattedTextLineBreaks {
    private FormattedTextLineBreaks() {
    }

    public static String decode(String text) {
        return ExplicitLineBreaks.decode(text);
    }

    public static FormattedText decode(FormattedText text) {
        List<StyledSegment> segments = segments(text);
        boolean changed = false;
        ArrayList<FormattedText> decoded = new ArrayList<>(segments.size());
        for (StyledSegment segment : segments) {
            String value = ExplicitLineBreaks.decode(segment.text());
            changed |= value != segment.text();
            decoded.add(FormattedText.of(value, segment.style()));
        }
        return changed ? FormattedText.composite(decoded) : text;
    }

    public static Component decode(Component component) {
        List<StyledSegment> segments = segments(component);
        boolean changed = false;
        ArrayList<StyledSegment> decoded = new ArrayList<>(segments.size());
        for (StyledSegment segment : segments) {
            String value = ExplicitLineBreaks.decode(segment.text());
            changed |= value != segment.text();
            decoded.add(new StyledSegment(value, segment.style()));
        }
        if (!changed) return component;

        MutableComponent result = Component.empty();
        for (StyledSegment segment : decoded) {
            result.append(Component.literal(segment.text()).withStyle(segment.style()));
        }
        return result;
    }

    private static List<StyledSegment> segments(FormattedText text) {
        ArrayList<StyledSegment> segments = new ArrayList<>();
        text.visit((style, value) -> {
            segments.add(new StyledSegment(value, style));
            return java.util.Optional.empty();
        }, Style.EMPTY);
        return segments;
    }

    private record StyledSegment(String text, Style style) {
    }
}

