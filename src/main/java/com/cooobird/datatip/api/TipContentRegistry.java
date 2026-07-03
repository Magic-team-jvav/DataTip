package com.cooobird.datatip.api;

import com.cooobird.datatip.api.content.AlignedContent;
import com.cooobird.datatip.api.content.TextContent;
import com.cooobird.datatip.api.content.VBoxContent;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    /**
     * 注册内容解析器。
     *
     * @param type   类型名称（如 "text", "item", "progress"）
     * @param parser 解析器实现
     * @throws IllegalArgumentException 如果类型已注册
     */
    public static void registerParser(String type, ContentParser parser) {
        if (parsers.containsKey(type)) {
            throw new IllegalArgumentException("Content type already registered: " + type);
        }
        parsers.put(type, parser);
    }

    /**
     * 注册内容解析器（允许覆盖已有类型）。
     *
     * @param type   类型名称
     * @param parser 解析器实现
     */
    public static void registerOrReplaceParser(String type, ContentParser parser) {
        parsers.put(type, parser);
    }

    /**
     * 获取内容解析器。
     *
     * @param type 类型名称
     * @return 解析器实例，未注册返回 null
     */
    @Nullable
    public static ContentParser getParser(String type) {
        return parsers.get(type);
    }

    /**
     * 检查类型是否已注册。
     *
     * @param type 类型名称
     * @return true 如果已注册
     */
    public static boolean hasParser(String type) {
        return parsers.containsKey(type);
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

        String type = json.get("type").getAsString().toLowerCase();
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
        return parsers.keySet();
    }

    /**
     * 清除所有注册的解析器（用于测试）。
     */
    public static void clear() {
        parsers.clear();
    }
}
