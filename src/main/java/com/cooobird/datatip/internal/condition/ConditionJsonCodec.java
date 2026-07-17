package com.cooobird.datatip.internal.condition;

import com.cooobird.datatip.api.condition.ConditionChecker;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

/**
 * 条件对象与 JSON 之间的共享转换器。
 */
public final class ConditionJsonCodec {
    private static final Gson GSON = new Gson();

    private ConditionJsonCodec() {
    }

    public static List<ConditionChecker.Condition> parse(
        JsonObject owner
    ) {
        if (!owner.has("conditions") || owner.get("conditions").isJsonNull()) {
            return List.of();
        }
        JsonElement element = owner.get("conditions");
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException(
                "Property 'conditions' must be an object"
            );
        }
        ArrayList<ConditionChecker.Condition> conditions = new ArrayList<>();
        for (var entry : element.getAsJsonObject().entrySet()) {
            conditions.add(new ConditionChecker.Condition(
                entry.getKey(),
                parseValue(entry.getValue())
            ));
        }
        return List.copyOf(conditions);
    }

    public static JsonObject write(
        List<ConditionChecker.Condition> conditions
    ) {
        JsonObject result = new JsonObject();
        for (ConditionChecker.Condition condition : conditions) {
            result.add(condition.type(), GSON.toJsonTree(condition.value()));
        }
        return result;
    }

    private static Object parseValue(JsonElement element) {
        Object root = emptyValue(element);
        if (!element.isJsonArray() && !element.isJsonObject()) {
            return root;
        }

        ArrayDeque<ValueFrame> work = new ArrayDeque<>();
        work.push(new ValueFrame(element, root));
        while (!work.isEmpty()) {
            ValueFrame frame = work.pop();
            if (frame.source().isJsonArray()) {
                @SuppressWarnings("unchecked")
                List<Object> destination =
                    (List<Object>) frame.destination();
                for (JsonElement child : frame.source().getAsJsonArray()) {
                    Object converted = emptyValue(child);
                    destination.add(converted);
                    if (child.isJsonArray() || child.isJsonObject()) {
                        work.push(new ValueFrame(child, converted));
                    }
                }
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> destination =
                (Map<String, Object>) frame.destination();
            for (var entry : frame.source().getAsJsonObject().entrySet()) {
                Object converted = emptyValue(entry.getValue());
                destination.put(entry.getKey(), converted);
                if (entry.getValue().isJsonArray()
                    || entry.getValue().isJsonObject()) {
                    work.push(new ValueFrame(entry.getValue(), converted));
                }
            }
        }
        return root;
    }

    private static Object emptyValue(JsonElement element) {
        if (element.isJsonPrimitive()) {
            var primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) return primitive.getAsBoolean();
            if (primitive.isNumber()) return primitive.getAsNumber();
            return primitive.getAsString();
        }
        if (element.isJsonArray()) return new ArrayList<>();
        if (element.isJsonObject()) {
            return new LinkedHashMap<String, Object>();
        }
        return null;
    }

    private record ValueFrame(JsonElement source, Object destination) {
    }
}
