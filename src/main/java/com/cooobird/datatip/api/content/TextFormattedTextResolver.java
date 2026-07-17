package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.util.VariableResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * TextContent 当前文本解析工具。
 */
final class TextFormattedTextResolver {
    private TextFormattedTextResolver() {
    }

    static FormattedText resolve(TextContent content, @Nullable ItemStack stack) {
        if (content.formattedText() != null) return content.formattedText();
        if (content.component() != null) {
            return content.component().copy().withStyle(content.buildStyle());
        }

        FormattedText styledLanguage = resolveStyledLanguage(content, stack);
        if (styledLanguage != FormattedText.EMPTY) {
            return styledLanguage;
        }

        Style style = content.buildStyle();
        FormattedText language = resolveLanguage(content, style, stack);
        if (language != FormattedText.EMPTY) {
            return language;
        }

        if (content.text() != null) {
            String resolvedText = stack != null && !stack.isEmpty()
                ? VariableResolver.resolve(content.text(), stack)
                : content.text();
            return Component.literal(resolvedText != null ? resolvedText : "").withStyle(style);
        }
        return FormattedText.EMPTY;
    }

    private static FormattedText resolveStyledLanguage(TextContent content, @Nullable ItemStack stack) {
        if (content.langStyledText() == null || content.langStyledText().isEmpty()) {
            return FormattedText.EMPTY;
        }

        String lang = currentLanguage();
        BaseTextContent.LangStyle langStyle = content.langStyledText().get(lang);
        if (langStyle == null) langStyle = content.langStyledText().get("en_us");
        if (langStyle == null) langStyle = firstNonNull(content.langStyledText().values());
        if (langStyle == null) return FormattedText.EMPTY;

        Style style = Style.EMPTY.withColor(langStyle.color());
        if (content.font() != null) style = style.withFont(content.font());
        if (langStyle.bold()) style = style.withBold(true);
        if (langStyle.italic()) style = style.withItalic(true);
        if (langStyle.underlined()) style = style.withUnderlined(true);
        if (langStyle.strikethrough()) style = style.withStrikethrough(true);
        return Component.literal(resolveVariables(langStyle.text(), stack)).withStyle(style);
    }

    private static FormattedText resolveLanguage(TextContent content, Style style, @Nullable ItemStack stack) {
        if (content.langText() == null || content.langText().isEmpty()) {
            return FormattedText.EMPTY;
        }

        String lang = currentLanguage();
        String langContent = content.langText().get(lang);
        if (langContent == null) langContent = content.langText().get("en_us");
        if (langContent == null) langContent = firstNonNull(content.langText().values());
        if (langContent == null) return FormattedText.EMPTY;

        return Component.literal(resolveVariables(langContent, stack)).withStyle(style);
    }

    private static String resolveVariables(String text, @Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return text;
        String resolved = VariableResolver.resolve(text, stack);
        return resolved != null ? resolved : "";
    }

    private static String currentLanguage() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getLanguageManager() == null) return "en_us";
        String selected = minecraft.getLanguageManager().getSelected();
        return selected != null && !selected.isBlank() ? selected : "en_us";
    }

    @Nullable
    private static <T> T firstNonNull(Iterable<T> values) {
        for (T value : values) {
            if (value != null) return value;
        }
        return null;
    }
}
