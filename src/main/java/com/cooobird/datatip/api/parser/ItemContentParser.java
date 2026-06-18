package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.ItemContent;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * ItemContent 解析器。
 * <p>
 * 负责将 JSON 对象解析为 {@link ItemContent} 实例。
 * 物品内容用于渲染物品图标，支持显示数量、耐久、标签等。
 * </p>
 *
 * <h3>支持的 JSON 格式</h3>
 * <pre>{@code
 * // 基础物品渲染
 * {
 *   "type": "item",
 *   "item": "minecraft:diamond"
 * }
 *
 * // 指定数量和大小
 * {
 *   "type": "item",
 *   "item": "minecraft:golden_apple",
 *   "count": 64,
 *   "size": 32
 * }
 *
 * // 显示标签
 * {
 *   "type": "item",
 *   "item": "minecraft:diamond_sword",
 *   "size": 32,
 *   "showLabel": true,
 *   "label": "钻石剑",
 *   "labelColor": "#55FFFF"
 * }
 * }</pre>
 *
 * <h3>字段说明</h3>
 * <table border="1">
 *   <tr><th>字段</th><th>类型</th><th>默认值</th><th>说明</th></tr>
 *   <tr><td>item</td><td>String</td><td>"minecraft:air"</td><td>物品 ID</td></tr>
 *   <tr><td>count</td><td>int</td><td>1</td><td>物品数量</td></tr>
 *   <tr><td>size</td><td>int</td><td>16</td><td>渲染大小（像素）</td></tr>
 *   <tr><td>showCount</td><td>boolean</td><td>true</td><td>是否显示数量</td></tr>
 *   <tr><td>showDurability</td><td>boolean</td><td>true</td><td>是否显示耐久条</td></tr>
 *   <tr><td>showLabel</td><td>boolean</td><td>false</td><td>是否显示标签</td></tr>
 *   <tr><td>label</td><td>String</td><td>null</td><td>自定义标签文本</td></tr>
 *   <tr><td>labelColor</td><td>String</td><td>"#FFFFFF"</td><td>标签颜色</td></tr>
 *   <tr><td>align</td><td>String</td><td>"left"</td><td>对齐方式（left/center/right）</td></tr>
 * </table>
 *
 * @author cooobird
 * @see ItemContent 物品内容类
 * @since 1.2.0
 */
public class ItemContentParser implements ContentParser {

    @Override
    public ItemContent parse(JsonObject json, ParseContext context) {
        // 解析物品 ID
        String itemId = context.getString(json, "item", "minecraft:air");
        ResourceLocation itemLocation = ResourceLocation.parse(itemId);

        // 获取物品
        Item item = BuiltInRegistries.ITEM.get(itemLocation);
        if (item == null || item == Items.AIR) {
            return ItemContent.of(ItemStack.EMPTY);
        }

        // 创建 ItemStack
        int count = context.getInt(json, "count", 1);
        ItemStack stack = new ItemStack(item, count);

        // 获取渲染选项
        int size = context.getInt(json, "size", 16);
        boolean showCount = context.getBoolean(json, "showCount", true);
        boolean showDurability = context.getBoolean(json, "showDurability", true);
        boolean showLabel = context.getBoolean(json, "showLabel", false);

        // 获取标签
        Component label = null;
        if (context.has(json, "label")) {
            String labelText = context.getString(json, "label", "");
            label = Component.literal(labelText);
            showLabel = true;
        }

        // 获取标签颜色
        Integer labelColor = null;
        if (context.has(json, "labelColor")) {
            labelColor = context.getColor(json, "labelColor", 0xFFFFFF);
        }

        return new ItemContent(stack, size, showCount, showDurability, showLabel, label, labelColor);
    }
}
