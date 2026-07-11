package com.cooobird.datatip.api.content;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 打字机当前语言文本选择。
 */
final class TypewriterTextSource {
    private TypewriterTextSource() {
    }

    static List<String> currentLines(TypewriterContent content) {
        if (content.langStyledLines != null && !content.langStyledLines.isEmpty()) {
            return styledTextLines(content);
        }
        if (content.langLines != null && !content.langLines.isEmpty()) {
            String lang = currentLanguage();
            List<String> langLinesList = content.langLines.get(lang);
            if (langLinesList == null) langLinesList = content.langLines.get("en_us");
            if (langLinesList == null) langLinesList = firstNonNull(content.langLines.values());
            return langLinesList != null ? langLinesList : List.of();
        }
        return content.lines;
    }

    @Nullable
    static BaseTextContent.LangStyle currentLineStyle(TypewriterContent content, int lineIndex) {
        if (content.langStyledLines == null || content.langStyledLines.isEmpty()) {
            return null;
        }

        String lang = currentLanguage();
        List<BaseTextContent.LangStyle> styledLines = content.langStyledLines.get(lang);
        if (styledLines == null) styledLines = content.langStyledLines.get("en_us");
        if (styledLines == null) styledLines = firstNonNull(content.langStyledLines.values());
        if (styledLines != null && lineIndex >= 0 && lineIndex < styledLines.size()) {
            return styledLines.get(lineIndex);
        }
        return null;
    }

    private static List<String> styledTextLines(TypewriterContent content) {
        String lang = currentLanguage();
        List<BaseTextContent.LangStyle> styledLines = content.langStyledLines.get(lang);
        if (styledLines == null) styledLines = content.langStyledLines.get("en_us");
        if (styledLines == null) styledLines = firstNonNull(content.langStyledLines.values());
        if (styledLines == null) return List.of();

        List<String> result = new ArrayList<>();
        for (BaseTextContent.LangStyle line : styledLines) {
            result.add(line.text());
        }
        return result;
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
