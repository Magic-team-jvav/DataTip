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
 *
 * @author cooobird
 * @since 1.2.0
 */
public class LegacyFormatConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger("datatip");

    public static boolean isLegacyFormat(JsonObject json) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (key.startsWith("_")) continue;
            if (value.isJsonObject()) {
                JsonObject obj = value.getAsJsonObject();
                if (!obj.has("type")) return true;
            } else if (value.isJsonArray() || value.isJsonPrimitive()) {
                return true;
            }
        }
        return false;
    }

    public static JsonObject convert(JsonObject legacyJson, ResourceLocation location) {
        JsonObject result = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : legacyJson.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            TipContent content = convertEntry(key, value);
            if (content != null) {
                JsonObject contentJson = convertToJson(content);
                if (value.isJsonObject()) {
                    JsonObject originalObj = value.getAsJsonObject();
                    if (originalObj.has("shift")) contentJson.add("shift", originalObj.get("shift"));
                    if (originalObj.has("prepend")) contentJson.add("prepend", originalObj.get("prepend"));
                    if (originalObj.has("conditions")) contentJson.add("conditions", originalObj.get("conditions"));
                }
                result.add(key, contentJson);
            }
        }
        writeConvertedJson(location, result);
        return result;
    }

    private static void writeConvertedJson(ResourceLocation location, JsonObject json) {
        try {
            File outputDir = new File(Minecraft.getInstance().gameDirectory, "datatip_converted");
            if (!outputDir.exists() && !outputDir.mkdirs()) {
                LOGGER.error("Failed to create output directory: {}", outputDir.getAbsolutePath());
                return;
            }
            File namespaceDir = new File(outputDir, location.getNamespace());
            if (!namespaceDir.exists() && !namespaceDir.mkdirs()) {
                LOGGER.error("Failed to create namespace directory: {}", namespaceDir.getAbsolutePath());
                return;
            }
            File outputFile = new File(namespaceDir, location.getPath() + ".json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.writeString(outputFile.toPath(), gson.toJson(json));
            LOGGER.info("Converted legacy format saved to: {}", outputFile.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.error("Failed to write converted JSON for {}", location, e);
        }
    }

    @Nullable
    public static TipContent convertEntry(String key, JsonElement value) {
        if (value.isJsonArray()) return convertStringArray(value.getAsJsonArray());
        else if (value.isJsonObject()) return convertObject(value.getAsJsonObject());
        else if (value.isJsonPrimitive()) return TextContent.of(value.getAsString());
        return null;
    }

    private static TipContent convertStringArray(JsonArray array) {
        VBoxContent vbox = VBoxContent.create();
        for (JsonElement element : array) {
            if (element.isJsonPrimitive()) vbox.addChild(TextContent.of(element.getAsString()));
        }
        return vbox;
    }

    private static TipContent convertObject(JsonObject obj) {
        VBoxContent vbox = VBoxContent.create();
        int color = DatatipConfig.DEFAULT_COLOR.get();
        if (obj.has("color")) color = parseColor(obj.get("color").getAsString());

        boolean topStrikethrough = obj.has("strikethrough") && obj.get("strikethrough").getAsBoolean();
        boolean topBold = obj.has("bold") && obj.get("bold").getAsBoolean();
        boolean topItalic = obj.has("italic") && obj.get("italic").getAsBoolean();
        boolean topUnderlined = obj.has("underlined") && obj.get("underlined").getAsBoolean();

        if (obj.has("text")) {
            JsonElement textElement = obj.get("text");
            if (textElement.isJsonArray()) {
                for (JsonElement item : textElement.getAsJsonArray()) {
                    if (item.isJsonPrimitive()) {
                        vbox.addChild(TextContent.of(item.getAsString(), color));
                    } else if (item.isJsonObject()) {
                        vbox.addChild(convertStyledLine(item.getAsJsonObject(), color, topStrikethrough, topBold, topItalic, topUnderlined));
                    }
                }
            } else if (textElement.isJsonObject()) {
                JsonObject textObj = textElement.getAsJsonObject();
                boolean isMultiLang = false;
                for (String k : textObj.keySet()) {
                    if (k.contains("_")) { isMultiLang = true; break; }
                }

                if (isMultiLang) {
                    boolean hasStyledLines = false;
                    for (Map.Entry<String, JsonElement> langEntry : textObj.entrySet()) {
                        JsonElement langValue = langEntry.getValue();
                        if (langValue.isJsonArray()) {
                            for (JsonElement line : langValue.getAsJsonArray()) {
                                if (line.isJsonObject()) { hasStyledLines = true; break; }
                            }
                        }
                        if (hasStyledLines) break;
                    }

                    if (hasStyledLines) {
                        JsonArray longestArray = null;
                        for (Map.Entry<String, JsonElement> langEntry : textObj.entrySet()) {
                            JsonElement langValue = langEntry.getValue();
                            if (langValue.isJsonArray()) {
                                JsonArray arr = langValue.getAsJsonArray();
                                if (longestArray == null || arr.size() > longestArray.size()) longestArray = arr;
                            }
                        }
                        if (longestArray != null) {
                            for (int i = 0; i < longestArray.size(); i++) {
                                Map<String, TextContent.LangStyle> lineLangStyles = new HashMap<>();
                                for (Map.Entry<String, JsonElement> langEntry : textObj.entrySet()) {
                                    String lang = langEntry.getKey();
                                    JsonElement langValue = langEntry.getValue();
                                    if (langValue.isJsonArray()) {
                                        JsonArray lines = langValue.getAsJsonArray();
                                        if (i < lines.size()) {
                                            JsonElement line = lines.get(i);
                                            if (line.isJsonPrimitive()) {
                                                lineLangStyles.put(lang, new TextContent.LangStyle(line.getAsString(), color, topBold, topItalic, topUnderlined, topStrikethrough));
                                            } else if (line.isJsonObject()) {
                                                JsonObject lineObj = line.getAsJsonObject();
                                                String lineText = lineObj.has("text") ? lineObj.get("text").getAsString() : "";
                                                int lineColor = lineObj.has("color") ? parseColor(lineObj.get("color").getAsString()) : color;
                                                boolean lineBold = lineObj.has("bold") ? lineObj.get("bold").getAsBoolean() : topBold;
                                                boolean lineItalic = lineObj.has("italic") ? lineObj.get("italic").getAsBoolean() : topItalic;
                                                boolean lineUnderlined = lineObj.has("underlined") ? lineObj.get("underlined").getAsBoolean() : topUnderlined;
                                                boolean lineStrikethrough = lineObj.has("strikethrough") ? lineObj.get("strikethrough").getAsBoolean() : topStrikethrough;
                                                lineLangStyles.put(lang, new TextContent.LangStyle(lineText, lineColor, lineBold, lineItalic, lineUnderlined, lineStrikethrough));
                                            }
                                        }
                                    } else if (langValue.isJsonPrimitive() && i == 0) {
                                        lineLangStyles.put(lang, new TextContent.LangStyle(langValue.getAsString(), color, topBold, topItalic, topUnderlined, topStrikethrough));
                                    }
                                }
                                if (!lineLangStyles.isEmpty()) vbox.addChild(TextContent.ofLangStyled(lineLangStyles));
                            }
                        }
                    } else {
                        Map<String, String> langMap = new HashMap<>();
                        for (Map.Entry<String, JsonElement> langEntry : textObj.entrySet()) {
                            JsonElement langValue = langEntry.getValue();
                            String text;
                            if (langValue.isJsonArray()) {
                                StringBuilder sb = new StringBuilder();
                                JsonArray lines = langValue.getAsJsonArray();
                                for (int i = 0; i < lines.size(); i++) {
                                    if (i > 0) sb.append("\n");
                                    sb.append(lines.get(i).getAsString());
                                }
                                text = sb.toString();
                            } else if (langValue.isJsonPrimitive()) {
                                text = langValue.getAsString();
                            } else continue;
                            if (!text.isEmpty()) langMap.put(langEntry.getKey(), text);
                        }
                        if (!langMap.isEmpty()) vbox.addChild(TextContent.ofLang(langMap, color, topBold, topItalic, topUnderlined, topStrikethrough));
                    }
                } else {
                    for (Map.Entry<String, JsonElement> langEntry : textObj.entrySet()) {
                        for (JsonElement line : langEntry.getValue().getAsJsonArray()) {
                            if (line.isJsonPrimitive()) vbox.addChild(TextContent.of(line.getAsString(), color));
                            else if (line.isJsonObject()) vbox.addChild(convertStyledLine(line.getAsJsonObject(), color, topStrikethrough, topBold, topItalic, topUnderlined));
                        }
                    }
                }
            } else if (textElement.isJsonPrimitive()) {
                vbox.addChild(new TextContent(textElement.getAsString(), null, null, null, null, null,
                    color, null, true, TextContent.TextAlign.LEFT, 12, 0, topBold, topItalic, topUnderlined, topStrikethrough, false));
            }
        }

        if (vbox.children().size() == 1) {
            TipContent singleChild = vbox.children().get(0);
            if (singleChild instanceof TextContent) return singleChild;
        }
        return vbox;
    }

    private static TipContent convertStyledLine(JsonObject line, int defaultColor, boolean topStrikethrough, boolean topBold, boolean topItalic, boolean topUnderlined) {
        String text = line.has("text") ? line.get("text").getAsString() : "";
        int color = line.has("color") ? parseColor(line.get("color").getAsString()) : defaultColor;
        boolean bold = line.has("bold") ? line.get("bold").getAsBoolean() : topBold;
        boolean italic = line.has("italic") ? line.get("italic").getAsBoolean() : topItalic;
        boolean underlined = line.has("underlined") ? line.get("underlined").getAsBoolean() : topUnderlined;
        boolean strikethrough = line.has("strikethrough") ? line.get("strikethrough").getAsBoolean() : topStrikethrough;
        return new TextContent(text, null, null, null, null, null, color, null, true, TextContent.TextAlign.LEFT, 12, 0, bold, italic, underlined, strikethrough, false);
    }

    public static JsonObject convertToJson(TipContent content) {
        JsonObject json = new JsonObject();
        if (content instanceof TextContent textContent) {
            json.addProperty("type", "text");
            if (textContent.langStyledText() != null && !textContent.langStyledText().isEmpty()) {
                JsonObject langObj = new JsonObject();
                for (Map.Entry<String, TextContent.LangStyle> entry : textContent.langStyledText().entrySet()) {
                    TextContent.LangStyle style = entry.getValue();
                    JsonObject styleObj = new JsonObject();
                    styleObj.addProperty("text", style.text());
                    if (style.color() != 0xFFFFFF) styleObj.addProperty("color", String.format("#%06X", style.color() & 0xFFFFFF));
                    if (style.bold()) styleObj.addProperty("bold", true);
                    if (style.italic()) styleObj.addProperty("italic", true);
                    if (style.underlined()) styleObj.addProperty("underlined", true);
                    if (style.strikethrough()) styleObj.addProperty("strikethrough", true);
                    langObj.add(entry.getKey(), styleObj);
                }
                json.add("text", langObj);
            } else if (textContent.langText() != null && !textContent.langText().isEmpty()) {
                JsonObject langObj = new JsonObject();
                for (Map.Entry<String, String> entry : textContent.langText().entrySet()) langObj.addProperty(entry.getKey(), entry.getValue());
                json.add("text", langObj);
            } else if (textContent.text() != null) {
                json.addProperty("text", textContent.text());
            }
            if (textContent.color() != 0xFFFFFF) json.addProperty("color", String.format("#%06X", textContent.color() & 0xFFFFFF));
            if (textContent.align() == TextContent.TextAlign.CENTER) json.addProperty("align", "center");
            else if (textContent.align() == TextContent.TextAlign.RIGHT) json.addProperty("align", "right");
            if (textContent.bold()) json.addProperty("bold", true);
            if (textContent.italic()) json.addProperty("italic", true);
            if (textContent.underlined()) json.addProperty("underlined", true);
            if (textContent.strikethrough()) json.addProperty("strikethrough", true);
        } else if (content instanceof VBoxContent vbox) {
            json.addProperty("type", "vbox");
            json.addProperty("gap", vbox.gap());
            JsonArray children = new JsonArray();
            for (TipContent child : vbox.children()) children.add(convertToJson(child));
            json.add("children", children);
        } else if (content instanceof SpacerContent spacer) {
            json.addProperty("type", "spacer");
            json.addProperty("height", spacer.height());
        } else if (content instanceof DividerContent divider) {
            json.addProperty("type", "divider");
            json.addProperty("color", String.format("#%06X", divider.color() & 0xFFFFFF));
        }
        return json;
    }

    private static int parseColor(String colorStr) {
        if (colorStr.startsWith("#")) {
            try { return (int) Long.parseLong(colorStr.substring(1), 16) | 0xFF000000; }
            catch (NumberFormatException e) { return 0xFFFFFFFF; }
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
