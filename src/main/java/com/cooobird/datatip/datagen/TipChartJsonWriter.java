package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.content.ChartContent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 图表内容 JSON 写出器。
 */
final class TipChartJsonWriter {
    private TipChartJsonWriter() {
    }

    static void write(
        JsonObject json,
        ChartContent.ChartType type,
        List<ChartContent.ChartEntry> entries,
        int width,
        int height,
        Component title,
        boolean showLabels,
        boolean showValues,
        int titleColor,
        int labelColor,
        int valueColor,
        int zeroLineColor
    ) {
        json.addProperty("type", "chart");
        json.addProperty("chartType", type.toString().toLowerCase());
        json.addProperty("width", width);
        json.addProperty("height", height);
        if (title != null) json.addProperty("title", title.getString());
        if (!showLabels) json.addProperty("showLabels", false);
        if (!showValues) json.addProperty("showValues", false);
        if (titleColor != 0xFFFFFFFF)
            json.addProperty("titleColor", DatagenJsonUtils.colorToHex(titleColor));
        if (labelColor != 0xFFAAAAAA)
            json.addProperty("labelColor", DatagenJsonUtils.colorToHex(labelColor));
        if (valueColor != 0xFFFFFFFF)
            json.addProperty("valueColor", DatagenJsonUtils.colorToHex(valueColor));
        if (zeroLineColor != 0xFF888888)
            json.addProperty("zeroLineColor", DatagenJsonUtils.colorToHex(zeroLineColor));

        JsonArray entryArray = new JsonArray();
        for (var entry : entries) {
            JsonObject entryJson = new JsonObject();
            entryJson.addProperty("label", entry.label());
            entryJson.addProperty("value", entry.valueExpr());
            entryJson.addProperty("color", DatagenJsonUtils.colorToHex(entry.color()));
            entryArray.add(entryJson);
        }
        json.add("entries", entryArray);
    }
}
