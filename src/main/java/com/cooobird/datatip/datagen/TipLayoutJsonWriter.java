package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.content.CarouselContent;
import com.cooobird.datatip.api.content.HBoxContent;
import com.cooobird.datatip.api.content.VBoxContent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * 布局内容 JSON 写出器。
 */
final class TipLayoutJsonWriter {
    private TipLayoutJsonWriter() {
    }

    static void writeVBox(
        JsonObject json,
        List<TipContent> children,
        int gap,
        int padding,
        VBoxContent.HorizontalAlign horizontalAlign
    ) {
        json.addProperty("type", "vbox");
        json.addProperty("gap", gap);
        if (padding > 0) json.addProperty("padding", padding);
        if (horizontalAlign != VBoxContent.HorizontalAlign.LEFT)
            json.addProperty("align", horizontalAlign.toString().toLowerCase());
        json.add("children", DatagenJsonUtils.childrenToJson(children));
    }

    static void writeHBox(
        JsonObject json,
        List<TipContent> children,
        int gap,
        int padding,
        HBoxContent.VerticalAlign verticalAlign
    ) {
        json.addProperty("type", "hbox");
        json.addProperty("gap", gap);
        if (padding > 0) json.addProperty("padding", padding);
        if (verticalAlign != HBoxContent.VerticalAlign.TOP)
            json.addProperty("align", verticalAlign.toString().toLowerCase());
        json.add("children", DatagenJsonUtils.childrenToJson(children));
    }

    static void writeCarousel(JsonObject json, CarouselContent carousel) {
        json.addProperty("type", "carousel");
        json.addProperty("intervalSeconds", carousel.getIntervalSeconds());
        if (carousel.getTransition() != CarouselContent.TransitionType.NONE)
            json.addProperty("transition", carousel.getTransition().toString().toLowerCase());
        JsonArray frames = new JsonArray();
        for (TipContent frame : carousel.getFrames()) {
            frames.add(TipContentJsonSerializer.toJson(frame));
        }
        json.add("frames", frames);
    }
}
