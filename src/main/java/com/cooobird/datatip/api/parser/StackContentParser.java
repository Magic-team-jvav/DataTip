package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.StackContent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Locale;

/**
 * 解析共享同一 XY 原点的叠放容器。
 */
public final class StackContentParser implements ContentParser {
    @Override
    public StackContent parse(JsonObject json, ParseContext context) {
        int padding = context.getInt(json, "padding", 0);
        StackContent.HorizontalAlign horizontalAlign = parseHorizontalAlign(
            context.getString(json, "horizontalAlign", "left")
        );
        StackContent.VerticalAlign verticalAlign = parseVerticalAlign(
            context.getString(json, "verticalAlign", "top")
        );
        JsonArray children = context.getArray(json, "children");
        return new StackContent(
            children != null ? context.parseContentArray(children) : List.of(),
            padding,
            horizontalAlign,
            verticalAlign
        );
    }

    private static StackContent.HorizontalAlign parseHorizontalAlign(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "center" -> StackContent.HorizontalAlign.CENTER;
            case "right" -> StackContent.HorizontalAlign.RIGHT;
            default -> StackContent.HorizontalAlign.LEFT;
        };
    }

    private static StackContent.VerticalAlign parseVerticalAlign(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "center" -> StackContent.VerticalAlign.CENTER;
            case "bottom" -> StackContent.VerticalAlign.BOTTOM;
            default -> StackContent.VerticalAlign.TOP;
        };
    }
}
