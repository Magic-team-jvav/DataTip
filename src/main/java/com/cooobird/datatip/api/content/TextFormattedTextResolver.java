package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

/**
 * TextContent 当前文本解析工具。
 */
final class TextFormattedTextResolver {
    private TextFormattedTextResolver() {
    }

    static FormattedText resolve(TextContent content, @Nullable TipRenderContext context) {
        if (content.formattedText() != null) return content.formattedText();
        if (content.component() != null) return content.component();

        FormattedText styledLanguage = resolveStyledLanguage(content);
        if (styledLanguage != FormattedText.EMPTY) {
            return styledLanguage;
        }

        Style style = content.buildStyle();
        FormattedText language = resolveLanguage(content, style);
        if (language != FormattedText.EMPTY) {
            return language;
        }

        if (content.text() != null) {
            String resolvedText = (context != null) ? context.resolveVariables(content.text()) : content.text();
            return Component.literal(resolvedText).withStyle(style);
        }
        return FormattedText.EMPTY;
    }

    private static FormattedText resolveStyledLanguage(TextContent content) {
        if (content.langStyledText() == null || content.langStyledText().isEmpty()) {
            return FormattedText.EMPTY;
        }

        String lang = Minecraft.getInstance().getLanguageManager().getSelected();
        BaseTextContent.LangStyle langStyle = content.langStyledText().get(lang);
        if (langStyle == null) {
            return FormattedText.EMPTY;
        }

        Style style = Style.EMPTY.withColor(langStyle.color());
        if (content.font() != null) style = style.withFont(content.font());
        if (langStyle.bold()) style = style.withBold(true);
        if (langStyle.italic()) style = style.withItalic(true);
        if (langStyle.underlined()) style = style.withUnderlined(true);
        if (langStyle.strikethrough()) style = style.withStrikethrough(true);
        return Component.literal(langStyle.text()).withStyle(style);
    }

    private static FormattedText resolveLanguage(TextContent content, Style style) {
        if (content.langText() == null || content.langText().isEmpty()) {
            return FormattedText.EMPTY;
        }

        String lang = Minecraft.getInstance().getLanguageManager().getSelected();
        String langContent = content.langText().get(lang);
        if (langContent == null) {
            return FormattedText.EMPTY;
        }

        return Component.literal(langContent).withStyle(style);
    }
}
