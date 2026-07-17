package com.cooobird.datatip.api;

import com.cooobird.datatip.api.layout.OverflowPolicy;
import com.cooobird.datatip.api.node.TipModifiers;
import com.cooobird.datatip.api.node.TipNode;
import com.cooobird.datatip.internal.condition.ConditionJsonCodec;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内容类型注册表。
 * <p>
 * 用于注册和查找内容解析器。支持扩展自定义内容类型。
 * </p>
 *
 * <h3>注册自定义内容类型</h3>
 * <pre>{@code
 * // 注册解析器
 * TipContentRegistry.registerParser("my_type", (json, context) -> {
 *     String text = context.getString(json, "text", "");
 *     return new MyCustomContent(text);
 * });
 *
 * // 然后就可以在 JSON 中使用
 * // {"type": "my_type", "text": "Hello"}
 * }</pre>
 *
 * <h3>内置内容类型</h3>
 * <table border="1">
 *   <tr><th>类型</th><th>说明</th></tr>
 *   <tr><td>text</td><td>文本内容</td></tr>
 *   <tr><td>item</td><td>物品内容</td></tr>
 *   <tr><td>block</td><td>方块内容</td></tr>
 *   <tr><td>entity</td><td>实体内容</td></tr>
 *   <tr><td>progress</td><td>进度条内容</td></tr>
 *   <tr><td>carousel</td><td>轮播内容</td></tr>
 *   <tr><td>typewriter</td><td>打字机内容</td></tr>
 *   <tr><td>atlas</td><td>纹理内容</td></tr>
 *   <tr><td>image</td><td>图片内容</td></tr>
 *   <tr><td>chart</td><td>图表内容</td></tr>
 *   <tr><td>vbox</td><td>垂直布局</td></tr>
 *   <tr><td>hbox</td><td>水平布局</td></tr>
 *   <tr><td>stack</td><td>共享 XY 区域的叠放布局</td></tr>
 *   <tr><td>divider</td><td>分割线</td></tr>
 *   <tr><td>spacer</td><td>间距</td></tr>
 * </table>
 *
 * @author cooobird
 * @see ContentParser 内容解析器接口
 * @see TipContent 内容接口
 * @since 1.2.0
 */
public class TipContentRegistry {

    /**
     * 解析器映射表（类型名称 → 解析器）
     */
    private static final Map<String, ContentParser> parsers = new ConcurrentHashMap<>();
    private static final AtomicLong REVISION = new AtomicLong();

    /**
     * 注册内容解析器。
     *
     * @param type   类型名称（如 "text", "item", "progress"）
     * @param parser 解析器实现
     * @throws IllegalArgumentException 如果类型已注册
     */
    public static void registerParser(String type, ContentParser parser) {
        String normalizedType = normalizeType(type);
        if (parsers.putIfAbsent(normalizedType, Objects.requireNonNull(parser, "parser")) != null) {
            throw new IllegalArgumentException("Content type already registered: " + normalizedType);
        }
        REVISION.incrementAndGet();
    }

    /**
     * 注册内容解析器（允许覆盖已有类型）。
     *
     * @param type   类型名称
     * @param parser 解析器实现
     */
    public static void registerOrReplaceParser(String type, ContentParser parser) {
        ContentParser replacement = Objects.requireNonNull(parser, "parser");
        ContentParser previous = parsers.put(normalizeType(type), replacement);
        if (previous != replacement) {
            REVISION.incrementAndGet();
        }
    }

    /**
     * 获取内容解析器。
     *
     * @param type 类型名称
     * @return 解析器实例，未注册返回 null
     */
    @Nullable
    public static ContentParser getParser(String type) {
        String normalizedType = normalizeLookupType(type);
        return normalizedType != null ? parsers.get(normalizedType) : null;
    }

    /**
     * 检查类型是否已注册。
     *
     * @param type 类型名称
     * @return true 如果已注册
     */
    public static boolean hasParser(String type) {
        String normalizedType = normalizeLookupType(type);
        return normalizedType != null && parsers.containsKey(normalizedType);
    }

    /**
     * 解析 JSON 为 TipContent。
     *
     * @param json 包含 "type" 字段的 JSON 对象
     * @return 解析后的内容，失败返回 null
     */
    @Nullable
    public static TipContent parse(JsonObject json) {
        return parse(json, new ParseContext());
    }

    /**
     * 解析 JSON 为 TipContent（使用指定上下文）。
     *
     * @param json    包含 "type" 字段的 JSON 对象
     * @param context 解析上下文
     * @return 解析后的内容，失败返回 null
     */
    @Nullable
    public static TipContent parse(JsonObject json, ParseContext context) {
        return context.parseContent(json);
    }

    @Nullable
    static TipContent parseSingle(JsonObject json, ParseContext context) {
        if (!json.has("type")) {
            context.addWarning("JSON object missing 'type' field");
            return null;
        }

        String type;
        try {
            type = normalizeType(json.get("type").getAsString());
        } catch (RuntimeException e) {
            context.addWarning("Invalid content type: " + e.getMessage());
            return null;
        }
        ContentParser parser = parsers.get(type);

        if (parser == null) {
            context.addWarning("Unknown content type: '" + type + "'");
            return null;
        }

        try {
            TipModifiers modifiers = parseModifiers(json);
            TipContent content = parser.parse(json, context);
            if (content == null) {
                context.addWarning("Parser returned null for type: '" + type + "'");
                return null;
            }

            return TipNode.wrap(content, modifiers);
        } catch (Exception e) {
            context.addWarning("Failed to parse type '" + type + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取所有已注册的类型名称。
     *
     * @return 类型名称集合
     */
    public static Set<String> getRegisteredTypes() {
        return Set.copyOf(parsers.keySet());
    }

    /**
     * 注销内容解析器。
     */
    public static boolean unregisterParser(String type) {
        String normalizedType = normalizeLookupType(type);
        boolean removed = normalizedType != null && parsers.remove(normalizedType) != null;
        if (removed) REVISION.incrementAndGet();
        return removed;
    }

    public static long getRevision() {
        return REVISION.get();
    }

    /**
     * 清除所有注册的解析器（用于测试）。
     */
    public static void clear() {
        if (!parsers.isEmpty()) {
            parsers.clear();
            REVISION.incrementAndGet();
        }
    }

    private static String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Content type must not be blank");
        }
        return type.trim().toLowerCase(Locale.ROOT);
    }

    @Nullable
    private static String normalizeLookupType(String type) {
        return type == null || type.isBlank() ? null : type.trim().toLowerCase(Locale.ROOT);
    }

    private static TipModifiers parseModifiers(JsonObject json) {
        boolean shift = parseBoolean(json, "shift", false);
        var conditions = ConditionJsonCodec.parse(json);
        long offsetX = parseLong(json, "offsetX", 0);
        long offsetY = parseLong(json, "offsetY", 0);
        long offsetZ = parseLong(json, "offsetZ", 0);
        TipModifiers.SelfAlignment selfAlignX = parseSelfAlignment(json);
        TipModifiers.VerticalAlignment selfAlignY = parseVerticalAlignment(json);
        TipModifiers.Margins margins = parseMargins(json);
        TipModifiers.SizeConstraints sizeConstraints = parseSizeConstraints(json);
        double commonScale = parseDouble(json, "scale", 1.0);
        double scaleX = parseDouble(json, "scaleX", commonScale);
        double scaleY = parseDouble(json, "scaleY", commonScale);
        double rotation = parseDouble(json, "rotation", 0.0);
        double pivotX = parseDouble(json, "pivotX", 0.5);
        double pivotY = parseDouble(json, "pivotY", 0.5);
        double opacity = parseDouble(json, "opacity", 1.0);
        boolean visible = parseBoolean(json, "visible", true);
        OverflowPolicy overflow = parseOverflow(json);
        return new TipModifiers(
            shift,
            conditions,
            offsetX,
            offsetY,
            offsetZ,
            selfAlignX,
            selfAlignY,
            margins,
            sizeConstraints,
            scaleX,
            scaleY,
            rotation,
            pivotX,
            pivotY,
            opacity,
            visible,
            overflow
        );
    }

    private static long parseLong(
        JsonObject json,
        String property,
        long defaultValue
    ) {
        if (!json.has(property) || json.get(property).isJsonNull()) {
            return defaultValue;
        }

        JsonElement element = json.get(property);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(
                "Property '" + property + "' must be a 64-bit integer"
            );
        }

        try {
            BigDecimal value = element.getAsBigDecimal();
            return value.longValueExact();
        } catch (ArithmeticException | NumberFormatException exception) {
            throw new IllegalArgumentException(
                "Property '" + property + "' must be a 64-bit integer",
                exception
            );
        }
    }

    private static TipModifiers.SelfAlignment parseSelfAlignment(JsonObject json) {
        String property = json.has("selfAlignX") ? "selfAlignX" : "selfAlign";
        if (!json.has(property) || json.get(property).isJsonNull()) {
            return TipModifiers.SelfAlignment.INHERIT;
        }

        String value = parseString(json, property).toLowerCase(Locale.ROOT);
        return switch (value) {
            case "inherit" -> TipModifiers.SelfAlignment.INHERIT;
            case "left" -> TipModifiers.SelfAlignment.LEFT;
            case "center" -> TipModifiers.SelfAlignment.CENTER;
            case "right" -> TipModifiers.SelfAlignment.RIGHT;
            default -> throw new IllegalArgumentException(
                "Property '" + property
                    + "' must be one of: inherit, left, center, right"
            );
        };
    }

    private static TipModifiers.VerticalAlignment parseVerticalAlignment(
        JsonObject json
    ) {
        if (!json.has("selfAlignY") || json.get("selfAlignY").isJsonNull()) {
            return TipModifiers.VerticalAlignment.INHERIT;
        }

        String value = parseString(json, "selfAlignY").toLowerCase(Locale.ROOT);
        return switch (value) {
            case "inherit" -> TipModifiers.VerticalAlignment.INHERIT;
            case "top" -> TipModifiers.VerticalAlignment.TOP;
            case "center" -> TipModifiers.VerticalAlignment.CENTER;
            case "bottom" -> TipModifiers.VerticalAlignment.BOTTOM;
            default -> throw new IllegalArgumentException(
                "Property 'selfAlignY' must be one of: inherit, top, center, bottom"
            );
        };
    }

    private static TipModifiers.Margins parseMargins(JsonObject json) {
        long common = parseLong(json, "margin", 0);
        return new TipModifiers.Margins(
            parseLong(json, "marginTop", common),
            parseLong(json, "marginRight", common),
            parseLong(json, "marginBottom", common),
            parseLong(json, "marginLeft", common)
        );
    }

    private static TipModifiers.SizeConstraints parseSizeConstraints(
        JsonObject json
    ) {
        JsonObject source = json;
        if (json.has("constraints") && !json.get("constraints").isJsonNull()) {
            JsonElement element = json.get("constraints");
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException(
                    "Property 'constraints' must be an object"
                );
            }
            source = element.getAsJsonObject();
        } else if (!json.has("minWidth")
            && !json.has("minHeight")
            && !json.has("maxWidth")
            && !json.has("maxHeight")) {
            // 旧内容类型常用 width/height 表示自身尺寸，不能把它们误判为公共约束。
            return TipModifiers.SizeConstraints.NONE;
        }
        return new TipModifiers.SizeConstraints(
            parseOptionalSize(source, "width"),
            parseOptionalSize(source, "height"),
            parseOptionalSize(source, "minWidth"),
            parseOptionalSize(source, "minHeight"),
            parseOptionalSize(source, "maxWidth"),
            parseOptionalSize(source, "maxHeight")
        );
    }

    @Nullable
    private static Long parseOptionalSize(JsonObject json, String property) {
        if (!json.has(property) || json.get(property).isJsonNull()) return null;
        long value = parseLong(json, property, 0);
        if (value < 0) {
            throw new IllegalArgumentException(
                "Property '" + property + "' must be greater than or equal to 0"
            );
        }
        return value;
    }

    private static double parseDouble(
        JsonObject json,
        String property,
        double defaultValue
    ) {
        if (!json.has(property) || json.get(property).isJsonNull()) {
            return defaultValue;
        }
        JsonElement element = json.get(property);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(
                "Property '" + property + "' must be a finite number"
            );
        }
        double value;
        try {
            value = element.getAsBigDecimal().doubleValue();
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                "Property '" + property + "' must be a finite number",
                exception
            );
        }
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(
                "Property '" + property + "' must be a finite number"
            );
        }
        return value;
    }

    private static boolean parseBoolean(
        JsonObject json,
        String property,
        boolean defaultValue
    ) {
        if (!json.has(property) || json.get(property).isJsonNull()) {
            return defaultValue;
        }
        JsonElement element = json.get(property);
        if (!element.isJsonPrimitive()
            || !element.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException(
                "Property '" + property + "' must be a boolean"
            );
        }
        return element.getAsBoolean();
    }

    private static String parseString(JsonObject json, String property) {
        JsonElement element = json.get(property);
        if (!element.isJsonPrimitive()
            || !element.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException(
                "Property '" + property + "' must be a string"
            );
        }
        return element.getAsString();
    }

    private static OverflowPolicy parseOverflow(JsonObject json) {
        if (!json.has("overflow") || json.get("overflow").isJsonNull()) {
            return OverflowPolicy.NONE;
        }
        String value = parseString(json, "overflow").toLowerCase(Locale.ROOT);
        return switch (value) {
            case "none" -> OverflowPolicy.NONE;
            case "wrap" -> OverflowPolicy.WRAP;
            case "scale_down", "scale-down" -> OverflowPolicy.SCALE_DOWN;
            case "clip" -> OverflowPolicy.CLIP;
            default -> throw new IllegalArgumentException(
                "Property 'overflow' must be one of: none, wrap, scale_down, clip"
            );
        };
    }
}
