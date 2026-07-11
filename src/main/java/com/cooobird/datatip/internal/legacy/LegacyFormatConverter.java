package com.cooobird.datatip.internal.legacy;

import com.cooobird.datatip.api.TipContent;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

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
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 检测是否为老版本格式。
     * 如果任何条目没有 "type" 字段，则认为是老版本格式。
     */
    public static boolean isLegacyFormat(JsonObject json) {
        return LegacyFormatDetector.isLegacyFormat(json);
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

            if (LegacyFormatDetector.isMetadataKey(key)) {
                continue;
            }

            if (isModernEntry(value)) {
                result.add(key, value.deepCopy());
                continue;
            }

            try {
                TipContent content = convertEntry(key, value);
                if (content != null) {
                    JsonObject contentJson = convertToJson(content);
                    preserveTopLevelProperties(value, contentJson);
                    result.add(key, contentJson);
                }
            } catch (RuntimeException e) {
                LOGGER.warn("Skipped invalid legacy DataTip entry '{}'", key, e);
            }
        }

        writeConvertedJson(location, result);
        return result;
    }

    /**
     * 转换单个条目。
     */
    @Nullable
    public static TipContent convertEntry(String key, JsonElement value) {
        return LegacyContentConverter.convertEntry(key, value);
    }

    /**
     * 将 TipContent 转换为 JSON。
     */
    public static JsonObject convertToJson(TipContent content) {
        return LegacyTipContentJsonSerializer.toJson(content);
    }

    private static boolean isModernEntry(JsonElement value) {
        return value.isJsonObject() && value.getAsJsonObject().has("type");
    }

    private static void preserveTopLevelProperties(JsonElement value, JsonObject contentJson) {
        if (!value.isJsonObject()) {
            return;
        }

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

    private static void writeConvertedJson(ResourceLocation location, JsonObject json) {
        LegacyConvertedJsonWriter.write(location, json);
    }
}
