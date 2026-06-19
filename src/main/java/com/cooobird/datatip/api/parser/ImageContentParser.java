package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.ImageContent;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

/**
 * ImageContent 解析器。
 * <p>
 * 负责将 JSON 对象解析为 {@link ImageContent} 实例。
 * 图片内容用于渲染自定义纹理，支持 UV 偏移和缩放。
 * </p>
 *
 * <h3>支持的 JSON 格式</h3>
 * <pre>{@code
 * // 基础图片
 * {
 *   "type": "image",
 *   "texture": "mymod:textures/gui/image.png",
 *   "width": 64,
 *   "height": 64
 * }
 *
 * // 带 UV 偏移（精灵图）
 * {
 *   "type": "image",
 *   "texture": "minecraft:textures/gui/icons.png",
 *   "width": 16,
 *   "height": 16,
 *   "u": 0,
 *   "v": 0,
 *   "textureWidth": 256,
 *   "textureHeight": 256
 * }
 *
 * // 带缩放
 * {
 *   "type": "image",
 *   "texture": "mymod:textures/gui/icon.png",
 *   "width": 32,
 *   "height": 32,
 *   "scale": 2.0
 * }
 * }</pre>
 *
 * <h3>字段说明</h3>
 * <table border="1">
 *   <tr><th>字段</th><th>类型</th><th>默认值</th><th>说明</th></tr>
 *   <tr><td>texture</td><td>String</td><td>-</td><td>纹理资源路径</td></tr>
 *   <tr><td>width</td><td>int</td><td>64</td><td>渲染宽度（像素）</td></tr>
 *   <tr><td>height</td><td>int</td><td>64</td><td>渲染高度（像素）</td></tr>
 *   <tr><td>u</td><td>int</td><td>0</td><td>纹理 U 偏移</td></tr>
 *   <tr><td>v</td><td>int</td><td>0</td><td>纹理 V 偏移</td></tr>
 *   <tr><td>textureWidth</td><td>int</td><td>width</td><td>纹理总宽度</td></tr>
 *   <tr><td>textureHeight</td><td>int</td><td>height</td><td>纹理总高度</td></tr>
 *   <tr><td>scale</td><td>float</td><td>1.0</td><td>缩放倍数</td></tr>
 *   <tr><td>align</td><td>String</td><td>"left"</td><td>对齐方式（left/center/right）</td></tr>
 * </table>
 *
 * @author cooobird
 * @see ImageContent 图片内容类
 * @since 1.2.0
 */
public class ImageContentParser implements ContentParser {

    @Override
    public ImageContent parse(JsonObject json, ParseContext context) {
        // 解析纹理位置
        String textureStr = context.getString(json, "texture", "minecraft:textures/gui/icons.png");
        ResourceLocation texture = ResourceLocation.parse(textureStr);

        // 获取尺寸
        int width = context.getInt(json, "width", 64);
        int height = context.getInt(json, "height", 64);

        // 获取纹理偏移
        int u = context.getInt(json, "u", 0);
        int v = context.getInt(json, "v", 0);

        // 获取纹理尺寸
        int textureWidth = context.getInt(json, "textureWidth", width);
        int textureHeight = context.getInt(json, "textureHeight", height);

        // 获取缩放
        float scale = context.getFloat(json, "scale", 1.0f);

        // 获取 Y 轴偏移
        int offsetY = context.getInt(json, "offsetY", 0);

        return new ImageContent(texture, width, height, u, v, textureWidth, textureHeight, scale, offsetY);
    }
}
