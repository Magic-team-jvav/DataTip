package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.TypewriterContent;
import com.cooobird.datatip.config.DatatipConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * TypewriterContent 解析器。
 *
 * @author cooobird
 * @since 1.2.0
 */
public class TypewriterContentParser implements ContentParser {

    @Override
    public TypewriterContent parse(JsonObject json, ParseContext context) {
        int charsPerSecond = context.getInt(json, "charsPerSecond", 2);
        int pauseSeconds = context.getInt(json, "pauseSeconds", 1);
        boolean loop = context.getBoolean(json, "loop", false);
        int color = context.getColor(json, "color", DatatipConfig.DEFAULT_COLOR.get());

        ResourceLocation font = null;
        if (context.has(json, "font")) {
            String fontStr = context.getString(json, "font", "");
            if (!fontStr.isEmpty()) {
                font = ResourceLocation.tryParse(fontStr);
            }
        }

        List<String> lines = new ArrayList<>();
        if (context.has(json, "lines")) {
            JsonArray linesArray = context.getArray(json, "lines");
            if (linesArray != null) {
                for (var element : linesArray) {
                    if (element.isJsonPrimitive()) {
                        lines.add(element.getAsString());
                    }
                }
            }
        }

        return new TypewriterContent(lines, charsPerSecond, pauseSeconds, loop, color, font);
    }
}
