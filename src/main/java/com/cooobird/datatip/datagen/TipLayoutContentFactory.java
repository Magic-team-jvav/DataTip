package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.content.AlignedContent;
import com.cooobird.datatip.api.content.CarouselContent;
import com.cooobird.datatip.api.content.HBoxContent;
import com.cooobird.datatip.api.content.VBoxContent;

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

    static CarouselContent carousel(int intervalSeconds, TipContent... frames) {
        CarouselContent carousel = CarouselContent.withInterval(intervalSeconds);
        for (TipContent frame : frames) {
            carousel.addFrame(frame);
        }
        return carousel;
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
