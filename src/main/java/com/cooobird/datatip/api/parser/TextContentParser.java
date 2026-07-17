package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.TextContent;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;

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
        if (json.has("translate")) {
            return options.createWithComponent(Component.translatable(json.get("translate").getAsString()));
        }

        TextContentSource source = TextContentSource.parse(json);
        if (source.hasStyledLanguages()) {
            return options.createWithStyledLanguages(source.langStyledText());
        } else if (source.hasLanguages()) {
            return options.createWithLanguages(source.langText());
        } else if (source.hasText()) {
            return options.createWithText(source.text());
        }

        throw new IllegalArgumentException("Text content requires either 'text' or 'translate'");
    }

    private static void validateTextSource(JsonObject json) {
        if (json.has("trans")) {
            throw new IllegalArgumentException("Property 'trans' is not supported; use 'translate'");
        }

        boolean hasText = json.has("text") && !json.get("text").isJsonNull();
        boolean hasTranslate = json.has("translate") && !json.get("translate").isJsonNull();
        if (hasText && hasTranslate) {
            throw new IllegalArgumentException("Properties 'text' and 'translate' are mutually exclusive");
        }
        if (!hasText && !hasTranslate) {
            throw new IllegalArgumentException("Text content requires either 'text' or 'translate'");
        }
        if (hasTranslate
            && (!json.get("translate").isJsonPrimitive()
            || !json.getAsJsonPrimitive("translate").isString())) {
            throw new IllegalArgumentException("Property 'translate' must be a string");
        }
    }
}
