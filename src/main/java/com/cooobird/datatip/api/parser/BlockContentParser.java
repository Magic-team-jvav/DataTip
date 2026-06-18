package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.BlockContent;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * BlockContent 解析器。
 * <p>
 * 负责将 JSON 对象解析为 {@link BlockContent} 实例。
 * 支持自动旋转和可选标签。
 * </p>
 *
 * <h3>支持的 JSON 格式</h3>
 * <pre>{@code
 * // 基础方块渲染
 * {
 *   "type": "block",
 *   "block": "minecraft:stone",
 *   "size": 32
 * }
 *
 * // 带旋转动画
 * {
 *   "type": "block",
 *   "block": "minecraft:crafting_table",
 *   "size": 48,
 *   "rotationSpeed": 0.5,
 *   "autoRotate": true
 * }
 *
 * // 带标签
 * {
 *   "type": "block",
 *   "block": "minecraft:diamond_block",
 *   "size": 32,
 *   "label": "钻石块"
 * }
 * }</pre>
 *
 * <h3>字段说明</h3>
 * <table border="1">
 *   <tr><th>字段</th><th>类型</th><th>默认值</th><th>说明</th></tr>
 *   <tr><td>block</td><td>String</td><td>"minecraft:stone"</td><td>方块 ID</td></tr>
 *   <tr><td>size</td><td>int</td><td>32</td><td>渲染大小（像素）</td></tr>
 *   <tr><td>rotationSpeed</td><td>float</td><td>0.5</td><td>旋转速度（度/tick）</td></tr>
 *   <tr><td>autoRotate</td><td>boolean</td><td>true</td><td>是否自动旋转</td></tr>
 *   <tr><td>label</td><td>String</td><td>null</td><td>标签文本</td></tr>
 * </table>
 *
 * @author cooobird
 * @see BlockContent 方块内容类
 * @since 1.2.0
 */
public class BlockContentParser implements ContentParser {

    @Override
    public BlockContent parse(JsonObject json, ParseContext context) {
        // 解析方块类型
        String blockId = context.getString(json, "block", "minecraft:stone");
        ResourceLocation blockLocation = ResourceLocation.parse(blockId);

        Block block = BuiltInRegistries.BLOCK.get(blockLocation);
        if (block == null) {
            block = Blocks.STONE;  // 回退到默认方块
        }

        // 获取渲染选项
        int size = context.getInt(json, "size", 32);
        float rotationSpeed = context.getFloat(json, "rotationSpeed", 0.5f);
        boolean autoRotate = context.getBoolean(json, "autoRotate", true);

        // 获取标签（可选）
        Component label = null;
        if (context.has(json, "label")) {
            String labelText = context.getString(json, "label", "");
            label = Component.literal(labelText);
        }

        return new BlockContent(block, size, rotationSpeed, autoRotate, label);
    }
}
