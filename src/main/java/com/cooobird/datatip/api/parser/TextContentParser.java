package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.TextContent;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

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
        TextContentSource source = TextContentSource.parse(json);
        TextContentStyleOptions options = TextContentStyleOptions.parse(json, context);
        Component component = parseComponent(json, context);

        if (component != null) {
            return options.createWithComponent(component);
        } else if (source.hasStyledLanguages()) {
            return options.createWithStyledLanguages(source.langStyledText());
        } else if (source.hasLanguages()) {
            return options.createWithLanguages(source.langText());
        } else if (source.hasText()) {
            return options.createWithText(source.text());
        }

        return TextContent.of("");
    }

    @Nullable
    private static Component parseComponent(JsonObject json, ParseContext context) {
        if (!context.has(json, "trans")) {
            return null;
        }

        String transKey = context.getString(json, "trans", "");
        return Component.translatable(transKey);
    }
}
