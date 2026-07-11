package com.cooobird.datatip.api;

import com.cooobird.datatip.api.content.AlignedContent;
import com.cooobird.datatip.api.content.TextContent;
import com.cooobird.datatip.api.content.VBoxContent;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

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
        parsers.put(normalizeType(type), Objects.requireNonNull(parser, "parser"));
        REVISION.incrementAndGet();
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
            TipContent content = parser.parse(json, context);
            if (content == null) {
                context.addWarning("Parser returned null for type: '" + type + "'");
                return null;
            }

            // 如果 JSON 中有 align 属性，自动包装为 AlignedContent
            if (json.has("align") && !(content instanceof TextContent)) {
                String alignStr = json.get("align").getAsString();
                VBoxContent.HorizontalAlign align = switch (alignStr.toLowerCase()) {
                    case "center" -> VBoxContent.HorizontalAlign.CENTER;
                    case "right" -> VBoxContent.HorizontalAlign.RIGHT;
                    default -> VBoxContent.HorizontalAlign.LEFT;
                };
                return new AlignedContent(content, align);
            }

            return content;
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
}
