package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.content.*;

import java.util.Arrays;
import java.util.Locale;

/**
 * 布局类 TipContent 创建工具。
 */
final class TipLayoutContentFactory {
    private TipLayoutContentFactory() {
    }

    static VBoxContent vbox(TipContent... children) {
        VBoxContent vbox = VBoxContent.create();
        for (TipContent child : children) {
            vbox.addChild(child);
        }
        return vbox;
    }

    static VBoxContent vbox(int gap, TipContent... children) {
        VBoxContent vbox = VBoxContent.withGap(gap);
        for (TipContent child : children) {
            vbox.addChild(child);
        }
        return vbox;
    }

    static VBoxContent vbox(int gap, int padding, String align, TipContent... children) {
        return new VBoxContent(Arrays.asList(children), gap, padding, parseHorizontalAlign(align));
    }

    static HBoxContent hbox(TipContent... children) {
        HBoxContent hbox = HBoxContent.create();
        for (TipContent child : children) {
            hbox.addChild(child);
        }
        return hbox;
    }

    static HBoxContent hbox(int gap, TipContent... children) {
        HBoxContent hbox = HBoxContent.withGap(gap);
        for (TipContent child : children) {
            hbox.addChild(child);
        }
        return hbox;
    }

    static HBoxContent hbox(int gap, int padding, String align, TipContent... children) {
        return new HBoxContent(Arrays.asList(children), gap, padding, parseVerticalAlign(align));
    }

    static StackContent stack(TipContent... children) {
        return new StackContent(
            Arrays.asList(children),
            0,
            StackContent.HorizontalAlign.LEFT,
            StackContent.VerticalAlign.TOP
        );
    }

    static StackContent stack(
        int padding,
        String horizontalAlign,
        String verticalAlign,
        TipContent... children
    ) {
        return new StackContent(
            Arrays.asList(children),
            padding,
            parseStackHorizontalAlign(horizontalAlign),
            parseStackVerticalAlign(verticalAlign)
        );
    }

    static CarouselContent carousel(int intervalSeconds, TipContent... frames) {
        CarouselContent carousel = CarouselContent.withInterval(intervalSeconds);
        for (TipContent frame : frames) {
            carousel.addFrame(frame);
        }
        return carousel;
    }

    static CarouselContent carousel(int intervalSeconds, String transition, TipContent... frames) {
        return new CarouselContent(Arrays.asList(frames), intervalSeconds, parseTransition(transition));
    }

    private static VBoxContent.HorizontalAlign parseHorizontalAlign(String align) {
        try {
            return VBoxContent.HorizontalAlign.valueOf(align.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return VBoxContent.HorizontalAlign.LEFT;
        }
    }

    private static HBoxContent.VerticalAlign parseVerticalAlign(String align) {
        try {
            return HBoxContent.VerticalAlign.valueOf(align.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return HBoxContent.VerticalAlign.TOP;
        }
    }

    private static StackContent.HorizontalAlign parseStackHorizontalAlign(
        String align
    ) {
        try {
            return StackContent.HorizontalAlign.valueOf(
                align.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return StackContent.HorizontalAlign.LEFT;
        }
    }

    private static StackContent.VerticalAlign parseStackVerticalAlign(
        String align
    ) {
        try {
            return StackContent.VerticalAlign.valueOf(
                align.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return StackContent.VerticalAlign.TOP;
        }
    }

    private static CarouselContent.TransitionType parseTransition(String transition) {
        try {
            return CarouselContent.TransitionType.valueOf(transition.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return CarouselContent.TransitionType.FADE;
        }
    }

    static AlignedContent aligned(TipContent content, VBoxContent.HorizontalAlign align) {
        return new AlignedContent(content, align);
    }

    static AlignedContent centeredAligned(TipContent content) {
        return new AlignedContent(content, VBoxContent.HorizontalAlign.CENTER);
    }

    static AlignedContent rightAlignedContent(TipContent content) {
        return new AlignedContent(content, VBoxContent.HorizontalAlign.RIGHT);
    }
}
