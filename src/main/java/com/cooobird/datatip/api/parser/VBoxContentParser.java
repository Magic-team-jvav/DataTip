package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.content.VBoxContent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Locale;

/**
 * VBoxContent 解析器。
 * <p>
 * 负责将 JSON 对象解析为 {@link VBoxContent} 实例。
 * 垂直布局容器将子元素从上到下排列。
 * </p>
 *
 * <h3>支持的 JSON 格式</h3>
 * <pre>{@code
 * // 基础垂直布局
 * {
 *   "type": "vbox",
 *   "children": [
 *     {"type": "text", "text": "第一行"},
 *     {"type": "text", "text": "第二行"}
 *   ]
 * }
 *
 * // 带间距和内边距
 * {
 *   "type": "vbox",
 *   "gap": 4,
 *   "padding": 2,
 *   "children": [...]
 * }
 *
 * // 居中对齐
 * {
 *   "type": "vbox",
 *   "align": "center",
 *   "children": [...]
 * }
 * }</pre>
 *
 * <h3>字段说明</h3>
 * <table border="1">
 *   <tr><th>字段</th><th>类型</th><th>默认值</th><th>说明</th></tr>
 *   <tr><td>gap</td><td>int</td><td>0</td><td>子元素间距（像素）</td></tr>
 *   <tr><td>padding</td><td>int</td><td>0</td><td>内边距（像素）</td></tr>
 *   <tr><td>align</td><td>String</td><td>"left"</td><td>子元素水平对齐（left/center/right）</td></tr>
 *   <tr><td>children</td><td>Array</td><td>[]</td><td>子元素数组</td></tr>
 * </table>
 *
 * @author cooobird
 * @see VBoxContent 垂直布局内容类
 * @since 1.2.0
 */
public class VBoxContentParser implements ContentParser {

    @Override
    public VBoxContent parse(JsonObject json, ParseContext context) {
        // 获取布局选项
        int gap = context.getInt(json, "gap", 0);
        int padding = context.getInt(json, "padding", 0);

        // 解析对齐方式
        String alignStr = context.getString(json, "align", "left");
        VBoxContent.HorizontalAlign align = switch (alignStr.toLowerCase(Locale.ROOT)) {
            case "center" -> VBoxContent.HorizontalAlign.CENTER;
            case "right" -> VBoxContent.HorizontalAlign.RIGHT;
            default -> VBoxContent.HorizontalAlign.LEFT;
        };

        // 创建 VBoxContent
        VBoxContent vbox = new VBoxContent(List.of(), gap, padding, align);

        // 解析子元素
        if (context.has(json, "children")) {
            JsonArray childrenArray = context.getArray(json, "children");
            if (childrenArray != null) {
                List<TipContent> children = context.parseContentArray(childrenArray);
                children.forEach(vbox::addChild);
            }
        }

        return vbox;
    }
}
