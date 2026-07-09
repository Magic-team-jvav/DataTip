package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.content.*;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * TipContent JSON 序列化调度器。
 */
final class TipContentJsonSerializer {
    private TipContentJsonSerializer() {
    }

    static JsonObject toJson(TipContent content) {
        JsonObject json = new JsonObject();

        switch (content) {
            case TextContent textContent -> TipTextJsonWriter.write(json, textContent);
            case SpacerContent(int height) -> {
                json.addProperty("type", "spacer");
                json.addProperty("height", height);
            }
            case DividerContent(
                int color, int thickness, int width, int marginTop, int marginBottom, DividerContent.DividerStyle style,
                DividerContent.WidthMode widthMode
            ) ->
                TipCommonJsonWriter.writeDivider(json, color, thickness, width, marginTop, marginBottom, style, widthMode);
            case ItemContent item -> TipCommonJsonWriter.writeItem(json, item);
            case ProgressContent progress -> TipCommonJsonWriter.writeProgress(json, progress);
            case TypewriterContent typewriter -> TipCommonJsonWriter.writeTypewriter(json, typewriter);
            case VBoxContent(
                List<TipContent> children, int gap, int padding, VBoxContent.HorizontalAlign horizontalAlign
            ) -> TipLayoutJsonWriter.writeVBox(json, children, gap, padding, horizontalAlign);
            case HBoxContent(
                List<TipContent> children, int gap, int padding, HBoxContent.VerticalAlign verticalAlign
            ) -> TipLayoutJsonWriter.writeHBox(json, children, gap, padding, verticalAlign);
            case CarouselContent carousel -> TipLayoutJsonWriter.writeCarousel(json, carousel);
            case EntityContent entity -> TipVisualJsonWriter.writeEntity(json, entity);
            case BlockContent block -> TipVisualJsonWriter.writeBlock(json, block);
            case AtlasContent(
                ResourceLocation texturePath, int width, int height, String label, int x, int y
            ) -> TipVisualJsonWriter.writeAtlas(json, texturePath, width, height, label, x, y);
            case ImageContent(
                ResourceLocation texture, int width, int height, int u, int v, int textureWidth, int textureHeight,
                float scale, int offsetX, int offsetY
            ) ->
                TipVisualJsonWriter.writeImage(json, texture, width, height, u, v, textureWidth, textureHeight, scale, offsetX, offsetY);
            case ChartContent(
                ChartContent.ChartType type, List<ChartContent.ChartEntry> entries, int width, int height,
                Component title,
                boolean showLabels, boolean showValues, int titleColor, int labelColor, int valueColor,
                int zeroLineColor
            ) -> TipChartJsonWriter.write(json, type, entries, width, height, title, showLabels, showValues, titleColor,
                labelColor, valueColor, zeroLineColor);
            default -> {
            }
        }

        return json;
    }
}
