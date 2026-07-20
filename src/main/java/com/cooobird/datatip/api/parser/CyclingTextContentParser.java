package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.CyclingTextContent;
import com.cooobird.datatip.api.content.TextContent;
import com.cooobird.datatip.api.util.ColorParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Locale;

/**
 * 循环变色文本解析器。
 */
public final class CyclingTextContentParser implements ContentParser {
    @Override
    public CyclingTextContent parse(JsonObject json, ParseContext context) {
        TextContent text = new TextContentParser().parse(json, context);
        JsonArray palette = context.getArray(json, "colors");
        if (palette == null || palette.isEmpty()) {
            throw new IllegalArgumentException("cycle_text requires a non-empty 'colors' array");
        }

        ArrayList<Integer> colors = new ArrayList<>(palette.size());
        for (JsonElement element : palette) {
            if (!element.isJsonPrimitive()) {
                throw new IllegalArgumentException("cycle_text colors must be strings or integers");
            }
            colors.add(element.getAsJsonPrimitive().isNumber()
                ? element.getAsInt()
                : ColorParser.parseStrict(element.getAsString()));
        }

        String transitionName = context.getString(json, "transition", "smooth")
            .trim()
            .toLowerCase(Locale.ROOT);
        CyclingTextContent.Transition transition = switch (transitionName) {
            case "smooth" -> CyclingTextContent.Transition.SMOOTH;
            case "step" -> CyclingTextContent.Transition.STEP;
            default -> throw new IllegalArgumentException(
                "cycle_text transition must be 'smooth' or 'step'"
            );
        };
        return new CyclingTextContent(
            text,
            colors,
            context.getFloat(json, "cycleSeconds", 2.0f),
            transition,
            context.getFloat(json, "phase", 0.0f)
        );
    }
}
