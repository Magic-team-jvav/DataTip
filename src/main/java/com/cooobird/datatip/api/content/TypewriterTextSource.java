package com.cooobird.datatip.api.content;

import com.cooobird.datatip.internal.text.LanguageTextSelector;
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
            List<String> langLinesList = LanguageTextSelector.selectCurrent(content.langLines);
            return langLinesList != null ? langLinesList : List.of();
        }
        return content.lines;
    }

    @Nullable
    static BaseTextContent.LangStyle currentLineStyle(TypewriterContent content, int lineIndex) {
        if (content.langStyledLines == null || content.langStyledLines.isEmpty()) {
            return null;
        }

        List<BaseTextContent.LangStyle> styledLines =
            LanguageTextSelector.selectCurrent(content.langStyledLines);
        if (styledLines != null && lineIndex >= 0 && lineIndex < styledLines.size()) {
            return styledLines.get(lineIndex);
        }
        return null;
    }

    private static List<String> styledTextLines(TypewriterContent content) {
        List<BaseTextContent.LangStyle> styledLines =
            LanguageTextSelector.selectCurrent(content.langStyledLines);
        if (styledLines == null) return List.of();

        List<String> result = new ArrayList<>();
        for (BaseTextContent.LangStyle line : styledLines) {
            result.add(line.text());
        }
        return result;
    }

}
