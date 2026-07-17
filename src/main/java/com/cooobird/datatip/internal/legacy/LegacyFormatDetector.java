package com.cooobird.datatip.internal.legacy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayDeque;
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
                if (!obj.has("type") || containsLegacyTranslationKey(obj)) {
                    return true;
                }
            } else if (value.isJsonArray() || value.isJsonPrimitive()) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsLegacyTranslationKey(JsonElement root) {
        ArrayDeque<JsonElement> work = new ArrayDeque<>();
        work.push(root);
        while (!work.isEmpty()) {
            JsonElement current = work.pop();
            if (current.isJsonArray()) {
                for (JsonElement child : current.getAsJsonArray()) {
                    if (child.isJsonArray() || child.isJsonObject()) {
                        work.push(child);
                    }
                }
                continue;
            }
            if (!current.isJsonObject()) continue;
            for (Map.Entry<String, JsonElement> entry
                : current.getAsJsonObject().entrySet()) {
                if (entry.getKey().equals("trans")) return true;
                JsonElement child = entry.getValue();
                if (child.isJsonArray() || child.isJsonObject()) {
                    work.push(child);
                }
            }
        }
        return false;
    }

    static boolean isMetadataKey(String key) {
        return key.startsWith("_") || key.equals("$schema");
    }
}
