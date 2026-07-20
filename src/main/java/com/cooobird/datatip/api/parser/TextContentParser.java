package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.TextContent;
import com.google.gson.JsonObject;

/**
 * TextContent 解析器。
 * <p>
 * 负责将 JSON 对象解析为 {@link TextContent} 实例。
 * 支持多种文本格式和样式选项。
 * </p>
 *
 * @author cooobird
 * @see TextContent 文本内容类
 * @since 1.2.0
 */
public class TextContentParser implements ContentParser {

    @Override
    public TextContent parse(JsonObject json, ParseContext context) {
        validateTextSource(json);

        TextContentStyleOptions options = TextContentStyleOptions.parse(json, context);
        if (json.has("translate") || json.has("keybind")) {
            return options.createWithComponent(TextComponentSourceParser.parse(json));
        }

        TextContentSource source = TextContentSource.parse(json);
        if (source.hasStyledLanguages()) {
            return options.createWithStyledLanguages(source.langStyledText());
        } else if (source.hasLanguages()) {
            return options.createWithLanguages(source.langText());
        } else if (source.hasText()) {
            return options.createWithText(source.text());
        }

        throw new IllegalArgumentException(
            "Text content requires one of 'text', 'translate', or 'keybind'"
        );
    }

    private static void validateTextSource(JsonObject json) {
        if (json.has("trans")) {
            throw new IllegalArgumentException("Property 'trans' is not supported; use 'translate'");
        }

        boolean hasText = json.has("text") && !json.get("text").isJsonNull();
        boolean hasTranslate = json.has("translate") && !json.get("translate").isJsonNull();
        boolean hasKeybind = json.has("keybind") && !json.get("keybind").isJsonNull();
        int sourceCount = (hasText ? 1 : 0)
            + (hasTranslate ? 1 : 0)
            + (hasKeybind ? 1 : 0);
        if (sourceCount != 1) {
            throw new IllegalArgumentException(
                "Text content requires exactly one of 'text', 'translate', or 'keybind'"
            );
        }
        if (json.has("with") && !hasTranslate) {
            throw new IllegalArgumentException(
                "Property 'with' is only valid with 'translate'"
            );
        }
    }
}
