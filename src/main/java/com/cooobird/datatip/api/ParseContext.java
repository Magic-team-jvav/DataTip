package com.cooobird.datatip.api;

import com.cooobird.datatip.api.content.CarouselContent;
import com.cooobird.datatip.api.content.HBoxContent;
import com.cooobird.datatip.api.content.StackContent;
import com.cooobird.datatip.api.content.VBoxContent;
import com.cooobird.datatip.api.node.TipNode;
import com.cooobird.datatip.api.util.ColorParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 解析上下文。
 * <p>
 * 每次资源重载时创建新的 ParseContext 实例，收集解析过程中的警告，并提供常用 JSON 取值方法。
 * </p>
 *
 * @author cooobird
 * @since 1.2.0
 */
public class ParseContext {

    private final LinkedHashSet<String> warnings = new LinkedHashSet<>();

    /**
     * 添加解析警告。
     *
     * @param warning 警告信息
     */
    public void addWarning(String warning) {
        warnings.add(warning);
    }

    /**
     * 获取所有解析警告。
     *
     * @return 警告列表
     */
    public List<String> getWarnings() {
        return List.copyOf(warnings);
    }

    /**
     * 是否存在警告。
     *
     * @return true 表示存在警告
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * 解析嵌套的 TipContent。
     *
     * @param json 包含 type 字段的 JSON 对象
     * @return 解析后的内容，失败返回 null
     */
    @Nullable
    public TipContent parseContent(JsonObject json) {
        if (!isBuiltInContainer(json)) {
            return TipContentRegistry.parseSingle(json, this);
        }

        ParseFrame root = new ParseFrame(json);
        ArrayDeque<ParseFrame> work = new ArrayDeque<>();
        work.push(root);
        while (!work.isEmpty()) {
            ParseFrame frame = work.peek();
            if (!frame.initialized) {
                frame.initialize(this);
            }
            if (frame.result != null || frame.failed) {
                work.pop();
                continue;
            }
            if (frame.nextChild < frame.children.size()) {
                work.push(frame.children.get(frame.nextChild++));
                continue;
            }
            frame.finish();
            work.pop();
        }
        return root.result;
    }

    /**
     * 解析内容数组。
     *
     * @param json TipContent JSON 对象数组
     * @return 解析后的内容列表
     */
    public List<TipContent> parseContentArray(JsonArray json) {
        List<TipContent> result = new ArrayList<>();
        for (JsonElement element : json) {
            if (element.isJsonObject()) {
                TipContent content = parseContent(element.getAsJsonObject());
                if (content != null) {
                    result.add(content);
                }
            }
        }
        return result;
    }

    /**
     * 从对象里的指定键解析内容数组。
     *
     * @param json 父 JSON 对象
     * @param key  数组键名
     * @return 解析后的内容列表，键不存在时返回空列表
     */
    public List<TipContent> parseContentArray(JsonObject json, String key) {
        if (!json.has(key) || !json.get(key).isJsonArray()) {
            return List.of();
        }
        return parseContentArray(json.getAsJsonArray(key));
    }

    public String getString(JsonObject json, String key, String defaultValue) {
        JsonElement element = getPrimitive(json, key);
        return element != null ? element.getAsString() : defaultValue;
    }

    @Nullable
    public String getStringOrNull(JsonObject json, String key) {
        return getString(json, key, null);
    }

    public int getInt(JsonObject json, String key, int defaultValue) {
        JsonElement element = getPrimitive(json, key);
        if (element != null && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsInt();
        }
        return defaultValue;
    }

    public float getFloat(JsonObject json, String key, float defaultValue) {
        JsonElement element = getPrimitive(json, key);
        if (element != null && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsFloat();
        }
        return defaultValue;
    }

    public boolean getBoolean(JsonObject json, String key, boolean defaultValue) {
        JsonElement element = getPrimitive(json, key);
        if (element != null && element.getAsJsonPrimitive().isBoolean()) {
            return element.getAsBoolean();
        }
        return defaultValue;
    }

    public int getColor(JsonObject json, String key, int defaultValue) {
        JsonElement element = getPrimitive(json, key);
        if (element != null) {
            return parseColor(element.getAsString(), defaultValue);
        }
        return defaultValue;
    }

    /**
     * 解析颜色字符串为 ARGB 颜色值。
     *
     * @param colorStr     颜色字符串
     * @param defaultValue 默认颜色
     * @return ARGB 颜色值
     */
    public static int parseColor(String colorStr, int defaultValue) {
        return ColorParser.parse(colorStr, defaultValue);
    }

    @Nullable
    public JsonObject getObject(JsonObject json, String key) {
        if (!json.has(key)) return null;
        JsonElement element = json.get(key);
        return element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    @Nullable
    public JsonArray getArray(JsonObject json, String key) {
        if (!json.has(key)) return null;
        JsonElement element = json.get(key);
        return element.isJsonArray() ? element.getAsJsonArray() : null;
    }

    public boolean has(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull();
    }

    @Nullable
    private JsonElement getPrimitive(JsonObject json, String key) {
        if (!json.has(key)) return null;
        JsonElement element = json.get(key);
        return element.isJsonPrimitive() ? element : null;
    }

    private static boolean isBuiltInContainer(JsonObject json) {
        if (!json.has("type") || !json.get("type").isJsonPrimitive()) {
            return false;
        }
        String type = json.get("type").getAsString()
            .trim()
            .toLowerCase(Locale.ROOT);
        return type.equals("vbox")
            || type.equals("hbox")
            || type.equals("stack")
            || type.equals("carousel");
    }

    /**
     * 用显式堆栈完成内置容器的后序解析，不限制合法 JSON 的嵌套深度。
     */
    private static final class ParseFrame {
        private final JsonObject source;
        private final ArrayList<ParseFrame> children = new ArrayList<>();
        private TipNode shell;
        private boolean initialized;
        private boolean failed;
        private int nextChild;
        @Nullable
        private TipContent result;

        private ParseFrame(JsonObject source) {
            this.source = source;
        }

        private void initialize(ParseContext context) {
            initialized = true;
            if (!isBuiltInContainer(source)) {
                result = TipContentRegistry.parseSingle(source, context);
                if (result == null) failed = true;
                return;
            }
            String type = source.get("type").getAsString()
                .trim()
                .toLowerCase(Locale.ROOT);
            String childKey = type.equals("carousel") ? "frames" : "children";
            JsonArray sourceChildren = context.getArray(source, childKey);
            JsonObject shallow = new JsonObject();
            for (var entry : source.entrySet()) {
                if (!entry.getKey().equals(childKey)) {
                    shallow.add(entry.getKey(), entry.getValue());
                }
            }

            TipContent parsed = TipContentRegistry.parseSingle(shallow, context);
            if (!(parsed instanceof TipNode node)) {
                failed = true;
                return;
            }
            shell = node;
            if (sourceChildren == null) return;
            for (JsonElement child : sourceChildren) {
                if (child.isJsonObject()) {
                    children.add(new ParseFrame(child.getAsJsonObject()));
                }
            }
        }

        private void finish() {
            ArrayList<TipContent> parsedChildren = new ArrayList<>(
                children.size()
            );
            for (ParseFrame child : children) {
                if (child.result != null) parsedChildren.add(child.result);
            }

            TipContent inner = shell.inner();
            TipContent rebuilt;
            if (inner instanceof VBoxContent vbox) {
                rebuilt = new VBoxContent(
                    parsedChildren,
                    vbox.gap(),
                    vbox.padding(),
                    vbox.horizontalAlign()
                );
            } else if (inner instanceof HBoxContent hbox) {
                rebuilt = new HBoxContent(
                    parsedChildren,
                    hbox.gap(),
                    hbox.padding(),
                    hbox.verticalAlign()
                );
            } else if (inner instanceof StackContent stack) {
                rebuilt = new StackContent(
                    parsedChildren,
                    stack.padding(),
                    stack.horizontalAlign(),
                    stack.verticalAlign()
                );
            } else if (inner instanceof CarouselContent carousel) {
                rebuilt = new CarouselContent(
                    parsedChildren,
                    carousel.getIntervalSeconds(),
                    carousel.getTransition()
                );
            } else {
                rebuilt = inner;
            }
            result = TipNode.wrap(rebuilt, shell.modifiers());
        }
    }
}
