package com.cooobird.datatip.api.parser;

import com.cooobird.datatip.api.ContentParser;
import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.content.EntityContent;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

/**
 * EntityContent 解析器�?
 * <p>
 * 负责�?JSON 对象解析�?{@link EntityContent} 实例�?
 * 支持自动旋转和可选标签�?
 * </p>
 *
 * <h3>支持�?JSON 格式</h3>
 * <pre>{@code
 * // 基础实体渲染
 * {
 *   "type": "entity",
 *   "entity": "minecraft:wolf",
 *   "size": 48
 * }
 *
 * // 带旋转动�?
 * {
 *   "type": "entity",
 *   "entity": "minecraft:creeper",
 *   "size": 48,
 *   "rotationSpeed": 1.0,
 *   "autoRotate": true
 * }
 *
 * // 带标�?
 * {
 *   "type": "entity",
 *   "entity": "minecraft:wolf",
 *   "size": 48,
 *   "label": "�?
 * }
 * }</pre>
 *
 * <h3>字段说明</h3>
 * <table border="1">
 *   <tr><th>字段</th><th>类型</th><th>默认�?/th><th>说明</th></tr>
 *   <tr><td>entity</td><td>String</td><td>"minecraft:pig"</td><td>实体类型 ID</td></tr>
 *   <tr><td>size</td><td>int</td><td>48</td><td>渲染大小（像素）</td></tr>
 *   <tr><td>rotationSpeed</td><td>float</td><td>1.0</td><td>旋转速度（度/tick�?/td></tr>
 *   <tr><td>autoRotate</td><td>boolean</td><td>true</td><td>是否自动旋转</td></tr>
 *   <tr><td>label</td><td>String</td><td>null</td><td>标签文本</td></tr>
 * </table>
 *
 * @author cooobird
 * @see EntityContent 实体内容�?
 * @since 1.2.0
 */
public class EntityContentParser implements ContentParser {

    @Override
    public EntityContent parse(JsonObject json, ParseContext context) {
        // 解析实体类型
        String entityId = context.getString(json, "entity", "minecraft:pig");
        ResourceLocation entityLocation = ResourceLocation.parse(entityId);

        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityLocation);
        if (entityType == null) {
            entityType = EntityType.PIG;  // 回退到默认实�?
        }

        // 获取渲染选项
        int size = context.getInt(json, "size", 48);
        float rotationSpeed = context.getFloat(json, "rotationSpeed", 1.0f);
        boolean autoRotate = context.getBoolean(json, "autoRotate", true);
        int offsetX = context.getInt(json, "offsetX", 0);`n        int offsetY = context.getInt(json, "offsetY", 0);

        // 获取标签（可选）
        Component label = null;
        if (context.has(json, "label")) {
            String labelText = context.getString(json, "label", "");
            label = Component.literal(labelText);
        }

        return new EntityContent(entityType, size, rotationSpeed, autoRotate, offsetX, offsetY, label);
    }
}
