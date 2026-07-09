package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.content.*;
import com.google.gson.JsonObject;

/**
 * TipContent JSON 序列化调度器。
 */
final class TipContentJsonSerializer {
    private TipContentJsonSerializer() {
    }

    static JsonObject toJson(TipContent content) {
        JsonObject json = new JsonObject();

        if (content instanceof TextContent textContent) {
            TipTextJsonWriter.write(json, textContent);
        } else if (content instanceof SpacerContent spacer) {
            json.addProperty("type", "spacer");
            json.addProperty("height", spacer.height());
        } else if (content instanceof DividerContent divider) {
            TipCommonJsonWriter.writeDivider(json, divider.color(), divider.thickness(), divider.width(),
                divider.marginTop(), divider.marginBottom(), divider.style(), divider.widthMode());
        } else if (content instanceof ItemContent item) {
            TipCommonJsonWriter.writeItem(json, item);
        } else if (content instanceof ProgressContent progress) {
            TipCommonJsonWriter.writeProgress(json, progress);
        } else if (content instanceof TypewriterContent typewriter) {
            TipCommonJsonWriter.writeTypewriter(json, typewriter);
        } else if (content instanceof VBoxContent vbox) {
            TipLayoutJsonWriter.writeVBox(json, vbox.children(), vbox.gap(), vbox.padding(), vbox.horizontalAlign());
        } else if (content instanceof HBoxContent hbox) {
            TipLayoutJsonWriter.writeHBox(json, hbox.children(), hbox.gap(), hbox.padding(), hbox.verticalAlign());
        } else if (content instanceof CarouselContent carousel) {
            TipLayoutJsonWriter.writeCarousel(json, carousel);
        } else if (content instanceof EntityContent entity) {
            TipVisualJsonWriter.writeEntity(json, entity);
        } else if (content instanceof BlockContent block) {
            TipVisualJsonWriter.writeBlock(json, block);
        } else if (content instanceof AtlasContent atlas) {
            TipVisualJsonWriter.writeAtlas(json, atlas.texturePath(), atlas.width(), atlas.height(),
                atlas.label(), atlas.offsetX(), atlas.offsetY());
        } else if (content instanceof ImageContent image) {
            TipVisualJsonWriter.writeImage(json, image.texture(), image.width(), image.height(), image.u(), image.v(),
                image.textureWidth(), image.textureHeight(), image.scale(), image.offsetX(), image.offsetY());
        } else if (content instanceof ChartContent chart) {
            TipChartJsonWriter.write(json, chart.type(), chart.entries(), chart.width(), chart.height(),
                chart.title(), chart.showLabels(), chart.showValues(), chart.titleColor(), chart.labelColor(),
                chart.valueColor(), chart.zeroLineColor());
        }

        return json;
    }
}
