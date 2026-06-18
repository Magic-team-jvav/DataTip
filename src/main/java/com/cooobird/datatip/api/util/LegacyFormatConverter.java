package com.cooobird.datatip.api.util;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.content.DividerContent;
import com.cooobird.datatip.api.content.SpacerContent;
import com.cooobird.datatip.api.content.TextContent;
import com.cooobird.datatip.api.content.VBoxContent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

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

    /**
     * 检测是否为老版本格式。
     */
    public static boolean isLegacyFormat(JsonObject json) {
        // 检查是否有 "type" 字段（新版本格式的标志）
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            JsonElement value = entry.getValue();
            if (value.isJsonObject()) {
                JsonObject obj = value.getAsJsonObject();
                if (obj.has("type")) {
                    return false; // 新版本格式
                }
            }
        }
        return true; // 老版本格式
    }

    /**
     * 将老版本格式转换为新版本格式。
     */
    public static JsonObject convert(JsonObject legacyJson) {
        JsonObject result = new JsonObject();

        for (Map.Entry<String, JsonElement> entry : legacyJson.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            TipContent content = convertEntry(key, value);
            if (content != null) {
                // 将 TipContent 转换为 JSON
                result.add(key, convertToJson(content));
            }
        }

        return result;
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
        int color = 0xFFFFFF;
        if (obj.has("color")) {
            color = parseColor(obj.get("color").getAsString());
        }

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
                        vbox.addChild(convertStyledLine(item.getAsJsonObject()));
                    }
                }
            } else if (textElement.isJsonObject()) {
                // 多语言格式
                JsonObject textObj = textElement.getAsJsonObject();
                // 使用第一个语言
                for (Map.Entry<String, JsonElement> langEntry : textObj.entrySet()) {
                    JsonArray lines = langEntry.getValue().getAsJsonArray();
                    for (JsonElement line : lines) {
                        if (line.isJsonPrimitive()) {
                            vbox.addChild(TextContent.of(line.getAsString(), color));
                        } else if (line.isJsonObject()) {
                            vbox.addChild(convertStyledLine(line.getAsJsonObject()));
                        }
                    }
                    break; // 只使用第一个语言
                }
            } else if (textElement.isJsonPrimitive()) {
                // 单个字符串
                vbox.addChild(TextContent.of(textElement.getAsString(), color));
            }
        }

        return vbox;
    }

    /**
     * 转换带样式的行。
     */
    private static TipContent convertStyledLine(JsonObject line) {
        String text = line.has("text") ? line.get("text").getAsString() : "";

        // 获取颜色
        int color = 0xFFFFFF;
        if (line.has("color")) {
            color = parseColor(line.get("color").getAsString());
        }

        // 创建带样式的 Component
        Style style = Style.EMPTY.withColor(color);

        if (line.has("bold") && line.get("bold").getAsBoolean()) {
            style = style.withBold(true);
        }
        if (line.has("italic") && line.get("italic").getAsBoolean()) {
            style = style.withItalic(true);
        }
        if (line.has("underlined") && line.get("underlined").getAsBoolean()) {
            style = style.withUnderlined(true);
        }
        if (line.has("strikethrough") && line.get("strikethrough").getAsBoolean()) {
            style = style.withStrikethrough(true);
        }

        Component component = Component.literal(text).withStyle(style);
        return new TextContent(null, component, null, null, null, color, null, true, TextContent.TextAlign.LEFT, 12, 0, false, false, false, false, false);
    }

    /**
     * 将 TipContent 转换为 JSON。
     */
    public static JsonObject convertToJson(TipContent content) {
        JsonObject json = new JsonObject();

        if (content instanceof TextContent textContent) {
            json.addProperty("type", "text");
            // 获取文本内容
            if (textContent.text() != null) {
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
        } else if (content instanceof VBoxContent vbox) {
            json.addProperty("type", "vbox");
            json.addProperty("gap", vbox.gap());
            JsonArray children = new JsonArray();
            for (TipContent child : vbox.children()) {
                children.add(convertToJson(child));
            }
            json.add("children", children);
        } else if (content instanceof SpacerContent(int height)) {
            json.addProperty("type", "spacer");
            json.addProperty("height", height);
        } else if (content instanceof DividerContent divider) {
            json.addProperty("type", "divider");
            json.addProperty("color", String.format("#%06X", divider.color() & 0xFFFFFF));
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
