package com.cooobird.datatip.internal.legacy;

import com.cooobird.datatip.api.content.BaseTextContent;
import com.cooobird.datatip.api.content.TextContent;
import com.cooobird.datatip.api.content.VBoxContent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 旧格式多语言文本转换器。
 */
final class LegacyMultiLanguageTextConverter {
    private LegacyMultiLanguageTextConverter() {
    }

    static void convert(JsonObject textObj, VBoxContent vbox, LegacyTextStyle topStyle) {
        if (hasStyledLines(textObj)) {
            convertStyled(textObj, vbox, topStyle);
        } else {
            convertPlain(textObj, vbox, topStyle);
        }
    }

    private static boolean hasStyledLines(JsonObject textObj) {
        for (Map.Entry<String, JsonElement> langEntry : textObj.entrySet()) {
            JsonElement langValue = langEntry.getValue();
            if (langValue.isJsonArray()) {
                for (JsonElement line : langValue.getAsJsonArray()) {
                    if (line.isJsonObject()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void convertStyled(JsonObject textObj, VBoxContent vbox, LegacyTextStyle topStyle) {
        JsonArray longestArray = findLongestArray(textObj);
        if (longestArray == null) {
            return;
        }

        for (int i = 0; i < longestArray.size(); i++) {
            Map<String, BaseTextContent.LangStyle> lineLangStyles = collectLineLangStyles(textObj, i, topStyle);

            if (!lineLangStyles.isEmpty()) {
                vbox.addChild(TextContent.ofLangStyled(lineLangStyles));
            }
        }
    }

    @Nullable
    private static JsonArray findLongestArray(JsonObject textObj) {
        JsonArray longestArray = null;
        for (Map.Entry<String, JsonElement> langEntry : textObj.entrySet()) {
            JsonElement langValue = langEntry.getValue();
            if (langValue.isJsonArray()) {
                JsonArray arr = langValue.getAsJsonArray();
                if (longestArray == null || arr.size() > longestArray.size()) {
                    longestArray = arr;
                }
            }
        }
        return longestArray;
    }

    private static Map<String, BaseTextContent.LangStyle> collectLineLangStyles(
        JsonObject textObj,
        int lineIndex,
        LegacyTextStyle topStyle
    ) {
        Map<String, BaseTextContent.LangStyle> lineLangStyles = new HashMap<>();

        for (Map.Entry<String, JsonElement> langEntry : textObj.entrySet()) {
            String lang = langEntry.getKey();
            JsonElement langValue = langEntry.getValue();
            if (langValue.isJsonArray()) {
                addArrayLineStyle(lineLangStyles, lang, langValue.getAsJsonArray(), lineIndex, topStyle);
            } else if (langValue.isJsonPrimitive() && lineIndex == 0) {
                lineLangStyles.put(lang, topStyle.toLangStyle(langValue.getAsString()));
            }
        }

        return lineLangStyles;
    }

    private static void addArrayLineStyle(
        Map<String, BaseTextContent.LangStyle> lineLangStyles,
        String lang,
        JsonArray lines,
        int lineIndex,
        LegacyTextStyle topStyle
    ) {
        if (lineIndex >= lines.size()) {
            return;
        }

        JsonElement line = lines.get(lineIndex);
        if (line.isJsonPrimitive()) {
            lineLangStyles.put(lang, topStyle.toLangStyle(line.getAsString()));
        } else if (line.isJsonObject()) {
            JsonObject lineObj = line.getAsJsonObject();
            String lineText = lineObj.has("text") ? lineObj.get("text").getAsString() : "";
            lineLangStyles.put(lang, LegacyTextStyle.fromLine(lineObj, topStyle).toLangStyle(lineText));
        }
    }

    private static void convertPlain(JsonObject textObj, VBoxContent vbox, LegacyTextStyle topStyle) {
        Map<String, String> langMap = new HashMap<>();
        for (Map.Entry<String, JsonElement> langEntry : textObj.entrySet()) {
            String text = extractPlainLanguageText(langEntry.getValue());
            if (!text.isEmpty()) {
                langMap.put(langEntry.getKey(), text);
            }
        }

        if (!langMap.isEmpty()) {
            vbox.addChild(TextContent.ofLang(
                langMap,
                topStyle.color(),
                topStyle.bold(),
                topStyle.italic(),
                topStyle.underlined(),
                topStyle.strikethrough()
            ));
        }
    }

    private static String extractPlainLanguageText(JsonElement langValue) {
        if (langValue.isJsonArray()) {
            JsonArray lines = langValue.getAsJsonArray();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lines.size(); i++) {
                if (i > 0) {
                    sb.append("\n");
                }
                sb.append(lines.get(i).getAsString());
            }
            return sb.toString();
        } else if (langValue.isJsonPrimitive()) {
            return langValue.getAsString();
        }

        return "";
    }
}
