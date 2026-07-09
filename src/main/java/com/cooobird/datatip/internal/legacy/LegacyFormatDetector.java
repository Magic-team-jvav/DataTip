package com.cooobird.datatip.internal.legacy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * 老版本 datatip JSON 格式检测工具。
 */
final class LegacyFormatDetector {
    private LegacyFormatDetector() {
    }

    static boolean isLegacyFormat(JsonObject json) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (isMetadataKey(key)) continue;

            if (value.isJsonObject()) {
                JsonObject obj = value.getAsJsonObject();
                if (!obj.has("type")) {
                    return true;
                }
            } else if (value.isJsonArray() || value.isJsonPrimitive()) {
                return true;
            }
        }
        return false;
    }

    static boolean isMetadataKey(String key) {
        return key.startsWith("_") || key.equals("$schema");
    }
}
