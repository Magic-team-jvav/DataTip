package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.content.*;
import com.cooobird.datatip.api.node.TipModifiers;
import com.cooobird.datatip.api.node.TipNode;
import com.cooobird.datatip.internal.condition.ConditionJsonCodec;
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
        if (content instanceof TipNode(TipContent inner1, TipModifiers modifiers)) {
            JsonObject nodeJson = toJson(inner1);
            writeModifiers(nodeJson, modifiers);
            return nodeJson;
        }
        if (content instanceof AlignedContent(TipContent inner, VBoxContent.HorizontalAlign align)) {
            JsonObject alignedJson = toJson(inner);
            alignedJson.addProperty(
                "selfAlign",
                align.toString().toLowerCase(java.util.Locale.ROOT)
            );
            return alignedJson;
        }
        JsonObject json = new JsonObject();

        switch (content) {
            case TextContent textContent -> TipTextJsonWriter.write(json, textContent);
            case CyclingTextContent cycling -> TipTextJsonWriter.writeCycling(json, cycling);
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
            case StackContent stack -> TipLayoutJsonWriter.writeStack(
                json,
                stack.children(),
                stack.padding(),
                stack.horizontalAlign(),
                stack.verticalAlign()
            );
            case CarouselContent carousel -> TipLayoutJsonWriter.writeCarousel(json, carousel);
            case EntityContent entity -> TipVisualJsonWriter.writeEntity(json, entity);
            case BlockContent block -> TipVisualJsonWriter.writeBlock(json, block);
            case AtlasContent atlas -> TipVisualJsonWriter.writeAtlas(
                json, atlas.texturePath(), atlas.width(), atlas.height(), atlas.labelText(), atlas.offsetX(), atlas.offsetY());
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

    private static void writeModifiers(JsonObject json, TipModifiers modifiers) {
        if (modifiers.shift()) json.addProperty("shift", true);
        if (!modifiers.conditions().isEmpty()) {
            json.add(
                "conditions",
                ConditionJsonCodec.write(modifiers.conditions())
            );
        }
        writeNonZero(json, "offsetX", modifiers.offsetX());
        writeNonZero(json, "offsetY", modifiers.offsetY());
        writeNonZero(json, "offsetZ", modifiers.offsetZ());
        if (modifiers.selfAlignX() != TipModifiers.SelfAlignment.INHERIT) {
            json.addProperty("selfAlignX", enumName(modifiers.selfAlignX()));
        }
        if (modifiers.selfAlignY() != TipModifiers.VerticalAlignment.INHERIT) {
            json.addProperty("selfAlignY", enumName(modifiers.selfAlignY()));
        }
        writeMargins(json, modifiers.margins());
        writeSizeConstraints(json, modifiers.sizeConstraints());
        if (Double.compare(modifiers.scaleX(), modifiers.scaleY()) == 0
            && Double.compare(modifiers.scaleX(), 1.0) != 0) {
            json.addProperty("scale", modifiers.scaleX());
        } else {
            writeNonDefault(json, "scaleX", modifiers.scaleX(), 1.0);
            writeNonDefault(json, "scaleY", modifiers.scaleY(), 1.0);
        }
        writeNonDefault(json, "rotation", modifiers.rotation(), 0.0);
        writeNonDefault(json, "pivotX", modifiers.pivotX(), 0.5);
        writeNonDefault(json, "pivotY", modifiers.pivotY(), 0.5);
        writeNonDefault(json, "opacity", modifiers.opacity(), 1.0);
        if (!modifiers.visible()) json.addProperty("visible", false);
        if (modifiers.overflow()
            != com.cooobird.datatip.api.layout.OverflowPolicy.NONE) {
            json.addProperty("overflow", enumName(modifiers.overflow()));
        }
    }

    private static void writeMargins(
        JsonObject json,
        TipModifiers.Margins margins
    ) {
        if (margins.equals(TipModifiers.Margins.ZERO)) return;
        if (margins.top() == margins.right()
            && margins.top() == margins.bottom()
            && margins.top() == margins.left()) {
            json.addProperty("margin", margins.top());
            return;
        }
        writeNonZero(json, "marginTop", margins.top());
        writeNonZero(json, "marginRight", margins.right());
        writeNonZero(json, "marginBottom", margins.bottom());
        writeNonZero(json, "marginLeft", margins.left());
    }

    private static void writeSizeConstraints(
        JsonObject json,
        TipModifiers.SizeConstraints constraints
    ) {
        if (constraints.equals(TipModifiers.SizeConstraints.NONE)) return;
        JsonObject constraintJson = new JsonObject();
        writeNullable(constraintJson, "width", constraints.width());
        writeNullable(constraintJson, "height", constraints.height());
        writeNullable(constraintJson, "minWidth", constraints.minWidth());
        writeNullable(constraintJson, "minHeight", constraints.minHeight());
        writeNullable(constraintJson, "maxWidth", constraints.maxWidth());
        writeNullable(constraintJson, "maxHeight", constraints.maxHeight());
        json.add("constraints", constraintJson);
    }

    private static void writeNullable(
        JsonObject json,
        String property,
        Long value
    ) {
        if (value != null) json.addProperty(property, value);
    }

    private static void writeNonZero(
        JsonObject json,
        String property,
        long value
    ) {
        if (value != 0) json.addProperty(property, value);
    }

    private static void writeNonDefault(
        JsonObject json,
        String property,
        double value,
        double defaultValue
    ) {
        if (Double.compare(value, defaultValue) != 0) {
            json.addProperty(property, value);
        }
    }

    private static String enumName(Enum<?> value) {
        return value.toString().toLowerCase(java.util.Locale.ROOT);
    }
}
