package com.cooobird.datatip.api.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析文本节点中的原版组件来源和翻译参数。
 */
final class TextComponentSourceParser {
    private TextComponentSourceParser() {
    }

    static Component parse(JsonObject json) {
        if (json.has("keybind")) {
            return Component.keybind(requiredString(json, "keybind"));
        }
        String key = requiredString(json, "translate");
        return Component.translatable(key, translationArguments(json));
    }

    private static Object[] translationArguments(JsonObject json) {
        if (!json.has("with") || json.get("with").isJsonNull()) {
            return new Object[0];
        }
        JsonElement element = json.get("with");
        if (!element.isJsonArray()) {
            throw new IllegalArgumentException("Property 'with' must be an array");
        }

        JsonArray array = element.getAsJsonArray();
        List<Object> arguments = new ArrayList<>(array.size());
        for (JsonElement argument : array) {
            arguments.add(parseArgument(argument));
        }
        return arguments.toArray();
    }

    private static Object parseArgument(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            throw new IllegalArgumentException("Translation arguments must not be null");
        }
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException(
                "Translation arguments must be strings or component objects"
            );
        }

        JsonObject object = element.getAsJsonObject();
        int sourceCount = present(object, "text")
            + present(object, "translate")
            + present(object, "keybind");
        if (sourceCount != 1) {
            throw new IllegalArgumentException(
                "Translation component arguments require exactly one of 'text', 'translate', or 'keybind'"
            );
        }
        if (object.has("text")) {
            return Component.literal(requiredString(object, "text"));
        }
        return parse(object);
    }

    private static int present(JsonObject json, String property) {
        return json.has(property) && !json.get(property).isJsonNull() ? 1 : 0;
    }

    private static String requiredString(JsonObject json, String property) {
        JsonElement element = json.get(property);
        if (element == null
            || !element.isJsonPrimitive()
            || !element.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException(
                "Property '" + property + "' must be a string"
            );
        }
        return element.getAsString();
    }
}

