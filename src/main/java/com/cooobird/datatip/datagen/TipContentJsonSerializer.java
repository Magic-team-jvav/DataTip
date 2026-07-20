package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.content.*;
import com.cooobird.datatip.api.node.TipModifiers;
import com.cooobird.datatip.api.node.TipNode;
import com.cooobird.datatip.internal.condition.ConditionJsonCodec;
import com.google.gson.JsonObject;

/**
 * TipContent JSON 序列化调度器。
 */
final class TipContentJsonSerializer {
    private TipContentJsonSerializer() {
    }

    static JsonObject toJson(TipContent content) {
        if (content instanceof TipNode node) {
            JsonObject nodeJson = toJson(node.inner());
            writeModifiers(nodeJson, node.modifiers());
            return nodeJson;
        }
        if (content instanceof AlignedContent aligned) {
            JsonObject alignedJson = toJson(aligned.inner());
            alignedJson.addProperty(
                "selfAlign",
                aligned.align().toString().toLowerCase(java.util.Locale.ROOT)
            );
            return alignedJson;
        }
        JsonObject json = new JsonObject();

        if (content instanceof CyclingTextContent cycling) {
            TipTextJsonWriter.writeCycling(json, cycling);
        } else if (content instanceof TextContent textContent) {
            TipTextJsonWriter.write(json, textContent);
        } else if (content instanceof SpacerContent spacer) {
            json.addProperty("type", "spacer");
            json.addProperty("height", spacer.height());
        } else if (content instanceof DividerContent divider) {
            TipCommonJsonWriter.writeDivider(
                json,
                divider.color(),
                divider.thickness(),
                divider.width(),
                divider.marginTop(),
                divider.marginBottom(),
                divider.style(),
                divider.widthMode()
            );
        } else if (content instanceof ItemContent item) {
            TipCommonJsonWriter.writeItem(json, item);
        } else if (content instanceof ProgressContent progress) {
            TipCommonJsonWriter.writeProgress(json, progress);
        } else if (content instanceof TypewriterContent typewriter) {
            TipCommonJsonWriter.writeTypewriter(json, typewriter);
        } else if (content instanceof VBoxContent vbox) {
            TipLayoutJsonWriter.writeVBox(
                json,
                vbox.children(),
                vbox.gap(),
                vbox.padding(),
                vbox.horizontalAlign()
            );
        } else if (content instanceof HBoxContent hbox) {
            TipLayoutJsonWriter.writeHBox(
                json,
                hbox.children(),
                hbox.gap(),
                hbox.padding(),
                hbox.verticalAlign()
            );
        } else if (content instanceof StackContent stack) {
            TipLayoutJsonWriter.writeStack(
                json,
                stack.children(),
                stack.padding(),
                stack.horizontalAlign(),
                stack.verticalAlign()
            );
        } else if (content instanceof CarouselContent carousel) {
            TipLayoutJsonWriter.writeCarousel(json, carousel);
        } else if (content instanceof EntityContent entity) {
            TipVisualJsonWriter.writeEntity(json, entity);
        } else if (content instanceof BlockContent block) {
            TipVisualJsonWriter.writeBlock(json, block);
        } else if (content instanceof AtlasContent atlas) {
            TipVisualJsonWriter.writeAtlas(
                json, atlas.texturePath(), atlas.width(), atlas.height(), atlas.labelText(), atlas.offsetX(), atlas.offsetY());
        } else if (content instanceof ImageContent image) {
            TipVisualJsonWriter.writeImage(
                json,
                image.texture(),
                image.width(),
                image.height(),
                image.u(),
                image.v(),
                image.textureWidth(),
                image.textureHeight(),
                image.scale(),
                image.offsetX(),
                image.offsetY()
            );
        } else if (content instanceof ChartContent chart) {
            TipChartJsonWriter.write(
                json,
                chart.type(),
                chart.entries(),
                chart.width(),
                chart.height(),
                chart.title(),
                chart.showLabels(),
                chart.showValues(),
                chart.titleColor(),
                chart.labelColor(),
                chart.valueColor(),
                chart.zeroLineColor()
            );
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
