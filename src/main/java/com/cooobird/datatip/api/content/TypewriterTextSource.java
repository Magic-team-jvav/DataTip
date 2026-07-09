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
            String lang = Minecraft.getInstance().getLanguageManager().getSelected();
            List<String> langLinesList = content.langLines.get(lang);
            return langLinesList != null ? langLinesList : List.of();
        }
        return content.lines;
    }

    @Nullable
    static BaseTextContent.LangStyle currentLineStyle(TypewriterContent content, int lineIndex) {
        if (content.langStyledLines == null || content.langStyledLines.isEmpty()) {
            return null;
        }

        String lang = Minecraft.getInstance().getLanguageManager().getSelected();
        List<BaseTextContent.LangStyle> styledLines = content.langStyledLines.get(lang);
        if (styledLines != null && lineIndex < styledLines.size()) {
            return styledLines.get(lineIndex);
        }
        return null;
    }

    private static List<String> styledTextLines(TypewriterContent content) {
        String lang = Minecraft.getInstance().getLanguageManager().getSelected();
        List<BaseTextContent.LangStyle> styledLines = content.langStyledLines.get(lang);
        if (styledLines == null) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        for (BaseTextContent.LangStyle line : styledLines) {
            result.add(line.text());
        }
        return result;
    }
}
