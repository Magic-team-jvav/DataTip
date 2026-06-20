package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.TypewriterContent;
import com.cooobird.datatip.config.DatatipConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * TypewriterContent 解析器。
 * <p>
 * 负责将 JSON 对象解析为 {@link TypewriterContent} 实例。
 * 打字机效果会逐字显示文本，常用于剧情或提示信息。
 * </p>
 *
 * <h3>支持的 JSON 格式</h3>
 * <pre>{@code
 * // 基础打字机效果
 * {
 *   "type": "typewriter",
 *   "lines": ["第一行", "第二行", "第三行"],
 *   "charsPerSecond": 10,
 *   "pauseSeconds": 1,
 *   "color": "gray"
 * }
 *
 * // 循环播放
 * {
 *   "type": "typewriter",
 *   "lines": ["循环文本 1", "循环文本 2"],
 *   "charsPerSecond": 5,
 *   "pauseSeconds": 2,
 *   "loop": true,
 *   "color": "white"
 * }
 * }</pre>
 *
 * <h3>字段说明</h3>
 * <table border="1">
 *   <tr><th>字段</th><th>类型</th><th>默认值</th><th>说明</th></tr>
 *   <tr><td>lines</td><td>Array</td><td>[]</td><td>文本行数组</td></tr>
 *   <tr><td>charsPerSecond</td><td>int</td><td>2</td><td>每秒显示字符数</td></tr>
 *   <tr><td>pauseSeconds</td><td>int</td><td>1</td><td>行间暂停时间（秒）</td></tr>
 *   <tr><td>loop</td><td>boolean</td><td>false</td><td>是否循环播放</td></tr>
 *   <tr><td>color</td><td>String</td><td>"white"</td><td>文本颜色</td></tr>
 * </table>
 *
 * @author cooobird
 * @see TypewriterContent 打字机内容类
 * @since 1.2.0
 */
public class TypewriterContentParser implements ContentParser {

    @Override
    public TypewriterContent parse(JsonObject json, ParseContext context) {
        // 获取打字机选项（单位：秒）
        int charsPerSecond = context.getInt(json, "charsPerSecond", 2);
        int pauseSeconds = context.getInt(json, "pauseSeconds", 1);
        boolean loop = context.getBoolean(json, "loop", false);
        int color = context.getColor(json, "color", DatatipConfig.DEFAULT_COLOR.get());

        // 解析文本行
        List<String> lines = new ArrayList<>();
        if (context.has(json, "lines")) {
            JsonArray linesArray = context.getArray(json, "lines");
            if (linesArray != null) {
                for (var element : linesArray) {
                    if (element.isJsonPrimitive()) {
                        lines.add(element.getAsString());
                    }
                }
            }
        }

        return new TypewriterContent(lines, charsPerSecond, pauseSeconds, loop, color);
    }
}
