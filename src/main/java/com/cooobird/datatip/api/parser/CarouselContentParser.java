package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.content.CarouselContent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * CarouselContent 解析器。
 * <p>
 * 负责将 JSON 对象解析为 {@link CarouselContent} 实例。
 * 轮播容器会在多个内容帧之间自动切换。
 * </p>
 *
 * <h3>支持的 JSON 格式</h3>
 * <pre>{@code
 * // 基础轮播
 * {
 *   "type": "carousel",
 *   "intervalSeconds": 3,
 *   "frames": [
 *     {"type": "text", "text": "第一帧", "color": "white"},
 *     {"type": "text", "text": "第二帧", "color": "gold"},
 *     {"type": "text", "text": "第三帧", "color": "aqua"}
 *   ]
 * }
 *
 * // 带过渡效果
 * {
 *   "type": "carousel",
 *   "intervalSeconds": 5,
 *   "transition": "slide",
 *   "frames": [...]
 * }
 * }</pre>
 *
 * <h3>字段说明</h3>
 * <table border="1">
 *   <tr><th>字段</th><th>类型</th><th>默认值</th><th>说明</th></tr>
 *   <tr><td>intervalSeconds</td><td>int</td><td>3</td><td>帧切换间隔（秒）</td></tr>
 *   <tr><td>transition</td><td>String</td><td>"fade"</td><td>过渡效果（fade/slide/none）</td></tr>
 *   <tr><td>frames</td><td>Array</td><td>[]</td><td>内容帧数组</td></tr>
 * </table>
 *
 * @author cooobird
 * @see CarouselContent 轮播内容类
 * @since 1.2.0
 */
public class CarouselContentParser implements ContentParser {

    @Override
    public CarouselContent parse(JsonObject json, ParseContext context) {
        // 获取帧切换间隔（秒）
        int intervalSeconds = context.getInt(json, "intervalSeconds", 3);

        // 获取过渡效果类型
        String transitionStr = context.getString(json, "transition", "fade");
        CarouselContent.TransitionType transition = switch (transitionStr.toLowerCase()) {
            case "slide" -> CarouselContent.TransitionType.SLIDE;
            case "none" -> CarouselContent.TransitionType.NONE;
            default -> CarouselContent.TransitionType.FADE;
        };

        // 创建轮播容器
        CarouselContent carousel = CarouselContent.withInterval(intervalSeconds);

        // 解析帧内容
        if (context.has(json, "frames")) {
            JsonArray framesArray = context.getArray(json, "frames");
            if (framesArray != null) {
                List<TipContent> frames = context.parseContentArray(framesArray);
                frames.forEach(carousel::addFrame);
            }
        }

        return carousel;
    }
}
