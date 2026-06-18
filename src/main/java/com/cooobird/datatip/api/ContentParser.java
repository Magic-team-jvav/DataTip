package com.cooobird.datatip.api;

import com.google.gson.JsonObject;

/**
 * 内容解析器接口。
 * <p>
 * 将 JSON 对象转换为 {@link TipContent} 实例。
 * 每种内容类型都需要注册一个对应的解析器。
 * </p>
 *
 * <h3>注册自定义解析器</h3>
 * <pre>{@code
 * TipContentRegistry.registerParser("my_type", (json, context) -> {
 *     String text = context.getString(json, "text", "");
 *     int color = context.getColor(json, "color", 0xFFFFFF);
 *     return new MyCustomContent(text, color);
 * });
 * }</pre>
 *
 * <h3>JSON 格式</h3>
 * <pre>{@code
 * {
 *   "type": "my_type",
 *   "text": "Hello World",
 *   "color": "#FF5555"
 * }
 * }</pre>
 *
 * @author cooobird
 * @see TipContentRegistry#registerParser(String, ContentParser)
 * @see ParseContext 解析上下文
 * @since 1.2.0
 */
@FunctionalInterface
public interface ContentParser {

    /**
     * 解析 JSON 为 TipContent 实例。
     *
     * @param json    要解析的 JSON 对象（包含 "type" 字段）
     * @param context 解析上下文，提供便捷的 JSON 解析方法
     * @return 解析后的 TipContent 实例
     * @throws Exception 解析失败时抛出异常
     */
    TipContent parse(JsonObject json, ParseContext context) throws Exception;
}
