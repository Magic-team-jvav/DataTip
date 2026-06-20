package com.cooobird.datatip.api.util;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.content.DividerContent;
import com.cooobird.datatip.api.content.SpacerContent;
import com.cooobird.datatip.api.content.TextContent;
import com.cooobird.datatip.api.content.VBoxContent;
import com.cooobird.datatip.config.DatatipConfig;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * 老版本格式转换器。
 * 将老版本 datatip.json 格式转换为新版本 TipContent 格式。
 * <p>
 * 老版本格式：
 * <pre>{@code
 * {
 *   "minecraft:diamond": ["Line 1", "Line 2"],
 *   "minecraft:diamond_sword": {
 *     "text": {"zh_cn": ["削铁如泥"], "en_us": ["Cuts through iron"]},
 *     "color": "gold",
 *     "shift": true,
 *     "prepend": true
 *   }
 * }
 * }</pre>
 * <p>
 * 新版本格式：
 * <pre>{@code
 * {
 *   "minecraft:diamond": {
 *     "type": "vbox",
 *     "children": [
 *       {"type": "text", "text": "Line 1"},
 *       {"type": "text", "text": "Line 2"}
 *     ]
 *   }
 * }
 * }</pre>
 *
 * @author cooobird
 * @since 1.2.0
 */
public class LegacyFormatConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger("datatip");

    /**
     * 检测是否为老版本格式。
     * 如果任何条目没有 "type" 字段，则认为是老版本格式。
     */
    public static boolean isLegacyFormat(JsonObject json) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            // 跳过注释字段
            if (key.startsWith("_")) continue;

            if (value.isJsonObject()) {
                JsonObject obj = value.getAsJsonObject();
                // 如果没有 "type" 字段，说明是老版本格式
                if (!obj.has("type")) {
                    return true;
                }
            } else if (value.isJsonArray() || value.isJsonPrimitive()) {
                // 数组或字符串格式也是老版本
                return true;
            }
        }
        return false; // 所有条目都有 "type" 字段，是新版本格式
    }

    /**
     * 将老版本格式转换为新版本格式。
     * 保留 shift、prepend、conditions 等顶层属性。
     * 同时将转换结果写入输出目录。
     */
    public static JsonObject convert(JsonObject legacyJson, ResourceLocation location) {
        JsonObject result = new JsonObject();

        for (Map.Entry<String, JsonElement> entry : legacyJson.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            TipContent content = convertEntry(key, value);
            if (content != null) {
                // 将 TipContent 转换为 JSON
                JsonObject contentJson = convertToJson(content);

                // 保留旧格式的顶层属性
                if (value.isJsonObject()) {
                    JsonObject originalObj = value.getAsJsonObject();
                    if (originalObj.has("shift")) {
                        contentJson.add("shift", originalObj.get("shift"));
                    }
                    if (originalObj.has("prepend")) {
                        contentJson.add("prepend", originalObj.get("prepend"));
                    }
                    if (originalObj.has("conditions")) {
                        contentJson.add("conditions", originalObj.get("conditions"));
                    }
                }

                result.add(key, contentJson);
            }
        }

        // 写入转换后的 JSON 到输出目录
        writeConvertedJson(location, result);

        return result;
    }

    /**
     * 将转换后的 JSON 写入输出目录。
     */
    private static void writeConvertedJson(ResourceLocation location, JsonObject json) {
        try {
            // 创建输出目录
            File outputDir = new File(
                Minecraft.getInstance().gameDirectory,
                "datatip_converted"
            );
            if (!outputDir.exists() && !outputDir.mkdirs()) {
                LOGGER.error("Failed to create output directory: {}", outputDir.getAbsolutePath());
                return;
            }

            // 创建命名空间子目录
            File namespaceDir = new File(outputDir, location.getNamespace());
            if (!namespaceDir.exists() && !namespaceDir.mkdirs()) {
                LOGGER.error("Failed to create namespace directory: {}", namespaceDir.getAbsolutePath());
                return;
            }

            // 写入 JSON 文件
            File outputFile = new File(namespaceDir, location.getPath() + ".json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonStr = gson.toJson(json);
            Files.writeString(outputFile.toPath(), jsonStr);

            LOGGER.info("Converted legacy format saved to: {}", outputFile.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.error("Failed to write converted JSON for {}", location, e);
        }
    }

    /**
     * 转换单个条目。
     */
    @Nullable
    public static TipContent convertEntry(String key, JsonElement value) {
        if (value.isJsonArray()) {
            // 字符串数组格式
            // "minecraft:diamond": ["Line 1", "Line 2"]
            return convertStringArray(value.getAsJsonArray());
        } else if (value.isJsonObject()) {
            // 对象格式
            JsonObject obj = value.getAsJsonObject();
            return convertObject(obj);
        } else if (value.isJsonPrimitive()) {
            // 单个字符串
            return TextContent.of(value.getAsString());
        }
        return null;
    }

    /**
     * 转换字符串数组。
     */
    private static TipContent convertStringArray(JsonArray array) {
        VBoxContent vbox = VBoxContent.create();

        for (JsonElement element : array) {
            if (element.isJsonPrimitive()) {
                vbox.addChild(TextContent.of(element.getAsString()));
            }
        }

        return vbox;
    }

    /**
     * 转换对象格式。
     */
    private static TipContent convertObject(JsonObject obj) {
        VBoxContent vbox = VBoxContent.create();

        // 获取颜色
        int color = DatatipConfig.DEFAULT_COLOR.get();
        if (obj.has("color")) {
            color = parseColor(obj.get("color").getAsString());
        }

        // 获取顶层样式属性
        boolean topStrikethrough = obj.has("strikethrough") && obj.get("strikethrough").getAsBoolean();
        boolean topBold = obj.has("bold") && obj.get("bold").getAsBoolean();
        boolean topItalic = obj.has("italic") && obj.get("italic").getAsBoolean();
        boolean topUnderlined = obj.has("underlined") && obj.get("underlined").getAsBoolean();

        // 获取文本
        if (obj.has("text")) {
            JsonElement textElement = obj.get("text");

            if (textElement.isJsonArray()) {
                // 简单数组格式
                JsonArray array = textElement.getAsJsonArray();
                for (JsonElement item : array) {
                    if (item.isJsonPrimitive()) {
                        vbox.addChild(TextContent.of(item.getAsString(), color));
                    } else if (item.isJsonObject()) {
                        // 带样式的行
                        vbox.addChild(convertStyledLine(item.getAsJsonObject(), color, topStrikethrough, topBold, topItalic, topUnderlined));
                    }
                }
            } else if (textElement.isJsonObject()) {
                // 多语言格式 {"zh_cn": ["削铁如泥"], "en_us": ["Cuts through iron"]}
                JsonObject textObj = textElement.getAsJsonObject();

                boolean isMultiLang = false;
                for (String key : textObj.keySet()) {
                    if (key.contains("_")) { // zh_cn, en_us 等
                        isMultiLang = true;
                        break;
                    }
                }

                if (isMultiLang) {
                    // 多语言格式
                    // 检查是否有带样式的行
                    boolean hasStyledLines = false;
                    for (Map.Entry<String, JsonElement> langEntry : textObj.entrySet()) {
                        JsonElement langValue = langEntry.getValue();
                        if (langValue.isJsonArray()) {
                            JsonArray lines = langValue.getAsJsonArray();
                            for (JsonElement line : lines) {
                                if (line.isJsonObject()) {
                                    hasStyledLines = true;
                                    break;
                                }
                            }
                        }
                        if (hasStyledLines) break;
                    }

                    if (hasStyledLines) {
                        // 有带样式的行：需要按行创建多个 TextContent，每语言独立样式
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
                        
                        if (longestArray != null) {
                            for (int i = 0; i < longestArray.size(); i++) {
                                // 收集每行每语言的文本和样式
                                Map<String, TextContent.LangStyle> lineLangStyles = new HashMap<>();
                                
                                for (Map.Entry<String, JsonElement> langEntry : textObj.entrySet()) {
                                    String lang = langEntry.getKey();
                                    JsonElement langValue = langEntry.getValue();
                                    if (langValue.isJsonArray()) {
                                        JsonArray lines = langValue.getAsJsonArray();
                                        if (i < lines.size()) {
                                            JsonElement line = lines.get(i);
                                            if (line.isJsonPrimitive()) {
                                                // 纯文本行，使用顶层样式
                                                lineLangStyles.put(lang, new TextContent.LangStyle(
                                                    line.getAsString(), color, topBold, topItalic, topUnderlined, topStrikethrough));
                                            } else if (line.isJsonObject()) {
                                                // 带样式的行，提取该语言的样式
                                                JsonObject lineObj = line.getAsJsonObject();
                                                String lineText = lineObj.has("text") ? lineObj.get("text").getAsString() : "";
                                                int lineColor = lineObj.has("color") ? parseColor(lineObj.get("color").getAsString()) : color;
                                                boolean lineBold = lineObj.has("bold") ? lineObj.get("bold").getAsBoolean() : topBold;
                                                boolean lineItalic = lineObj.has("italic") ? lineObj.get("italic").getAsBoolean() : topItalic;
                                                boolean lineUnderlined = lineObj.has("underlined") ? lineObj.get("underlined").getAsBoolean() : topUnderlined;
                                                boolean lineStrikethrough = lineObj.has("strikethrough") ? lineObj.get("strikethrough").getAsBoolean() : topStrikethrough;
                                                lineLangStyles.put(lang, new TextContent.LangStyle(
                                                    lineText, lineColor, lineBold, lineItalic, lineUnderlined, lineStrikethrough));
                                            }
                                        }
                                    } else if (langValue.isJsonPrimitive() && i == 0) {
                                        lineLangStyles.put(lang, new TextContent.LangStyle(
                                            langValue.getAsString(), color, topBold, topItalic, topUnderlined, topStrikethrough));
                                    }
                                }
                                
                                if (!lineLangStyles.isEmpty()) {
                                    TextContent lineText = TextContent.ofLangStyled(lineLangStyles);
                                    vbox.addChild(lineText);
                                }
                            }
                        }
                    } else {
                        // 没有带样式的行：合并为单个 TextContent
                        Map<String, String> langMap = new HashMap<>();
                        for (Map.Entry<String, JsonElement> langEntry : textObj.entrySet()) {
                            JsonElement langValue = langEntry.getValue();
                            String text;
                            if (langValue.isJsonArray()) {
                                JsonArray lines = langValue.getAsJsonArray();
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < lines.size(); i++) {
                                    if (i > 0) sb.append("\n");
                                    JsonElement line = lines.get(i);
                                    sb.append(line.getAsString());
                                }
                                text = sb.toString();
                            } else if (langValue.isJsonPrimitive()) {
                                text = langValue.getAsString();
                            } else {
                                continue;
                            }
                            if (!text.isEmpty()) {
                                langMap.put(langEntry.getKey(), text);
                            }
                        }
                        if (!langMap.isEmpty()) {
                            TextContent langText = TextContent.ofLang(langMap, color, topBold, topItalic, topUnderlined, topStrikethrough);
                            vbox.addChild(langText);
                        }
                    }
                } else {
                    // 普通对象格式（带样式的行）
                    for (Map.Entry<String, JsonElement> langEntry : textObj.entrySet()) {
                        JsonArray lines = langEntry.getValue().getAsJsonArray();
                        for (JsonElement line : lines) {
                            if (line.isJsonPrimitive()) {
                                vbox.addChild(TextContent.of(line.getAsString(), color));
                            } else if (line.isJsonObject()) {
                                vbox.addChild(convertStyledLine(line.getAsJsonObject(), color, topStrikethrough, topBold, topItalic, topUnderlined));
                            }
                        }
                    }
                }
            } else if (textElement.isJsonPrimitive()) {
                // 单个字符串（应用顶层样式）
                TextContent singleText = new TextContent(textElement.getAsString(), null, null, null, null, null,
                    color, null, true, TextContent.TextAlign.LEFT, 12, 0,
                    topBold, topItalic, topUnderlined, topStrikethrough, false);
                vbox.addChild(singleText);
            }
        }

        // 如果只有一个子元素且是文本，直接返回文本内容
        if (vbox.children().size() == 1) {
            TipContent singleChild = vbox.children().getFirst();
            if (singleChild instanceof TextContent) {
                return singleChild;
            }
        }

        return vbox;
    }

    private static StringBuilder getStringBuilder(JsonElement langValue) {
        JsonArray lines = langValue.getAsJsonArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append("\n");
            JsonElement line = lines.get(i);
            if (line.isJsonPrimitive()) {
                sb.append(line.getAsString());
            } else if (line.isJsonObject()) {
                // 带样式的行，只取文本
                JsonObject lineObj = line.getAsJsonObject();
                sb.append(lineObj.has("text") ? lineObj.get("text").getAsString() : "");
            }
        }
        return sb;
    }

    /**
     * 转换带样式的行。
     * 返回包含文本和样式的 TextContent（使用文本字符串，而非 Component）。
     */
    private static TipContent convertStyledLine(JsonObject line, int defaultColor, boolean topStrikethrough, boolean topBold, boolean topItalic, boolean topUnderlined) {
        String text = line.has("text") ? line.get("text").getAsString() : "";

        // 获取颜色（行级覆盖顶层）
        int color = defaultColor;
        if (line.has("color")) {
            color = parseColor(line.get("color").getAsString());
        }

        // 获取行级样式（覆盖顶层样式）
        boolean bold = line.has("bold") ? line.get("bold").getAsBoolean() : topBold;
        boolean italic = line.has("italic") ? line.get("italic").getAsBoolean() : topItalic;
        boolean underlined = line.has("underlined") ? line.get("underlined").getAsBoolean() : topUnderlined;
        boolean strikethrough = line.has("strikethrough") ? line.get("strikethrough").getAsBoolean() : topStrikethrough;

        // 创建 TextContent（使用文本字符串，而非 Component）
        return new TextContent(text, null, null, null, null, null, color, null, true,
            TextContent.TextAlign.LEFT, 12, 0, bold, italic, underlined, strikethrough, false);
    }

    /**
     * 将 TipContent 转换为 JSON。
     */
    public static JsonObject convertToJson(TipContent content) {
        JsonObject json = new JsonObject();

        switch (content) {
            case TextContent textContent -> {
                json.addProperty("type", "text");
                // 优先使用带样式的多语言文本
                if (textContent.langStyledText() != null && !textContent.langStyledText().isEmpty()) {
                    JsonObject langObj = new JsonObject();
                    for (Map.Entry<String, TextContent.LangStyle> entry : textContent.langStyledText().entrySet()) {
                        TextContent.LangStyle style = entry.getValue();
                        JsonObject styleObj = new JsonObject();
                        styleObj.addProperty("text", style.text());
                        if (style.color() != 0xFFFFFF) {
                            styleObj.addProperty("color", String.format("#%06X", style.color() & 0xFFFFFF));
                        }
                        if (style.bold()) styleObj.addProperty("bold", true);
                        if (style.italic()) styleObj.addProperty("italic", true);
                        if (style.underlined()) styleObj.addProperty("underlined", true);
                        if (style.strikethrough()) styleObj.addProperty("strikethrough", true);
                        langObj.add(entry.getKey(), styleObj);
                    }
                    json.add("text", langObj);
                } else if (textContent.langText() != null && !textContent.langText().isEmpty()) {
                    // 其次使用简单多语言文本
                    JsonObject langObj = new JsonObject();
                    for (Map.Entry<String, String> entry : textContent.langText().entrySet()) {
                        langObj.addProperty(entry.getKey(), entry.getValue());
                    }
                    json.add("text", langObj);
                } else if (textContent.text() != null) {
                    json.addProperty("text", textContent.text());
                }
                if (textContent.color() != 0xFFFFFF) {
                    json.addProperty("color", String.format("#%06X", textContent.color() & 0xFFFFFF));
                }
                if (textContent.align() == TextContent.TextAlign.CENTER) {
                    json.addProperty("align", "center");
                } else if (textContent.align() == TextContent.TextAlign.RIGHT) {
                    json.addProperty("align", "right");
                }
                if (textContent.bold()) json.addProperty("bold", true);
                if (textContent.italic()) json.addProperty("italic", true);
                if (textContent.underlined()) json.addProperty("underlined", true);
                if (textContent.strikethrough()) json.addProperty("strikethrough", true);
            }
            case VBoxContent vbox -> {
                json.addProperty("type", "vbox");
                json.addProperty("gap", vbox.gap());
                JsonArray children = new JsonArray();
                for (TipContent child : vbox.children()) {
                    children.add(convertToJson(child));
                }
                json.add("children", children);
            }
            case SpacerContent spacer -> {
                json.addProperty("type", "spacer");
                json.addProperty("height", spacer.height());
            }
            case DividerContent divider -> {
                json.addProperty("type", "divider");
                json.addProperty("color", String.format("#%06X", divider.color() & 0xFFFFFF));
            }
            default -> {
            }
        }

        return json;
    }

    /**
     * 解析颜色字符串。
     */
    private static int parseColor(String colorStr) {
        if (colorStr.startsWith("#")) {
            try {
                return (int) Long.parseLong(colorStr.substring(1), 16) | 0xFF000000;
            } catch (NumberFormatException e) {
                return 0xFFFFFFFF;
            }
        }

        return switch (colorStr.toLowerCase()) {
            case "black" -> 0xFF000000;
            case "dark_blue" -> 0xFF0000AA;
            case "dark_green" -> 0xFF00AA00;
            case "dark_aqua" -> 0xFF00AAAA;
            case "dark_red" -> 0xFFAA0000;
            case "dark_purple" -> 0xFFAA00AA;
            case "gold" -> 0xFFFFAA00;
            case "gray", "grey" -> 0xFFAAAAAA;
            case "dark_gray", "dark_grey" -> 0xFF555555;
            case "blue" -> 0xFF5555FF;
            case "green" -> 0xFF55FF55;
            case "aqua" -> 0xFF55FFFF;
            case "red" -> 0xFFFF5555;
            case "light_purple" -> 0xFFFF55FF;
            case "yellow" -> 0xFFFFFF55;
            case "white" -> 0xFFFFFFFF;
            default -> 0xFFFFFFFF;
        };
    }
}
