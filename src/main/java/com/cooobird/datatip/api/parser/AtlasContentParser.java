package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.AtlasContent;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

/**
 * AtlasContent 解析器。
 * <p>
 * 负责将 JSON 对象解析为 {@link AtlasContent} 实例。
 * 支持三种方式指定纹理：直接路径、方块 ID、物品 ID。
 * </p>
 *
 * <h3>支持的 JSON 格式</h3>
 * <pre>{@code
 * // 直接指定纹理路径
 * {
 *   "type": "atlas",
 *   "texture": "minecraft:textures/block/stone.png",
 *   "width": 32,
 *   "height": 32
 * }
 *
 * // 使用方块 ID（自动转换路径）
 * {
 *   "type": "atlas",
 *   "block": "minecraft:red_concrete",
 *   "size": 32
 * }
 *
 * // 使用物品 ID（自动转换路径）
 * {
 *   "type": "atlas",
 *   "item": "minecraft:apple",
 *   "size": 32
 * }
 *
 * // 带标签
 * {
 *   "type": "atlas",
 *   "block": "minecraft:diamond_block",
 *   "size": 32,
 *   "label": "钻石块"
 * }
 * }</pre>
 *
 * <h3>字段说明</h3>
 * <table border="1">
 *   <tr><th>字段</th><th>类型</th><th>默认值</th><th>说明</th></tr>
 *   <tr><td>texture</td><td>String</td><td>-</td><td>完整纹理路径（优先级最高）</td></tr>
 *   <tr><td>block</td><td>String</td><td>-</td><td>方块 ID（自动转换为纹理路径）</td></tr>
 *   <tr><td>item</td><td>String</td><td>-</td><td>物品 ID（自动转换为纹理路径）</td></tr>
 *   <tr><td>size</td><td>int</td><td>16</td><td>渲染大小（正方形）</td></tr>
 *   <tr><td>width</td><td>int</td><td>16</td><td>渲染宽度（优先级高于 size）</td></tr>
 *   <tr><td>height</td><td>int</td><td>16</td><td>渲染高度（优先级高于 size）</td></tr>
 *   <tr><td>label</td><td>String</td><td>null</td><td>标签文本</td></tr>
 * </table>
 *
 * <h3>路径转换规则</h3>
 * <ul>
 *   <li>方块 ID：minecraft:red_concrete → minecraft:textures/block/red_concrete.png</li>
 *   <li>物品 ID：minecraft:apple → minecraft:textures/item/apple.png</li>
 * </ul>
 *
 * @author cooobird
 * @see AtlasContent 纹理内容类
 * @since 1.2.0
 */
public class AtlasContentParser implements ContentParser {

    @Override
    public AtlasContent parse(JsonObject json, ParseContext context) {
        // 获取尺寸（优先级：width/height > size > 默认值 16）
        int width = context.getInt(json, "width", 16);
        int height = context.getInt(json, "height", width);  // 默认正方形
        int size = context.getInt(json, "size", 0);
        if (size > 0) {
            width = size;
            height = size;
        }

        // 获取标签
        String label = context.getStringOrNull(json, "label");

        // 优先级：texture > block > item
        // 1. 直接指定纹理路径
        if (context.has(json, "texture")) {
            String textureStr = context.getString(json, "texture", "");
            ResourceLocation texturePath = ResourceLocation.parse(textureStr);
            return new AtlasContent(texturePath, width, height, label);
        }

        // 2. 从方块 ID 自动转换
        if (context.has(json, "block")) {
            String blockStr = context.getString(json, "block", "");
            ResourceLocation blockId = ResourceLocation.parse(blockStr);
            return AtlasContent.fromBlock(blockId, width);
        }

        // 3. 从物品 ID 自动转换
        if (context.has(json, "item")) {
            String itemStr = context.getString(json, "item", "");
            ResourceLocation itemId = ResourceLocation.parse(itemStr);
            return AtlasContent.fromItem(itemId, width);
        }

        // 默认返回石头纹理
        return AtlasContent.of(ResourceLocation.parse("minecraft:textures/block/stone.png"), width, height);
    }
}
