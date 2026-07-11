package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.content.AtlasContent;
import com.cooobird.datatip.api.content.BlockContent;
import com.cooobird.datatip.api.content.EntityContent;
import com.cooobird.datatip.api.content.ImageContent;
import com.cooobird.datatip.api.text.LocalizedText;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 视觉类 TipContent 创建工具。
 */
final class TipVisualContentFactory {
    private TipVisualContentFactory() {
    }

    static EntityContent entity(String entityId, int size) {
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityId));
        return EntityContent.of(entityType, size);
    }

    static EntityContent entity(String entityId, int size, int offsetX, int offsetY) {
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityId));
        return EntityContent.withOffset(entityType, size, offsetX, offsetY);
    }

    static EntityContent entity(String entityId, int size, float rotationSpeed, boolean autoRotate,
                                Map<String, String> label, int offsetX, int offsetY) {
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityId));
        return new EntityContent(entityType, size, rotationSpeed, autoRotate, offsetX, offsetY, localized(label));
    }

    static BlockContent block(String blockId, int size) {
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockId));
        return BlockContent.of(block, size);
    }

    static BlockContent block(String blockId, int size, int offsetX, int offsetY) {
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockId));
        return BlockContent.withOffset(block, size, offsetX, offsetY);
    }

    static BlockContent block(String blockId, int size, float rotationSpeed, boolean autoRotate,
                              Map<String, String> label, int offsetX, int offsetY) {
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockId));
        return new BlockContent(block, size, rotationSpeed, autoRotate, localized(label), offsetX, offsetY);
    }

    static AtlasContent atlas(String itemId, int size) {
        return AtlasContent.fromItem(ResourceLocation.parse(itemId), size);
    }

    static AtlasContent atlas(String itemId, int size, int offsetX, int offsetY) {
        return AtlasContent.withOffset(ResourceLocation.parse(itemId), size, offsetX, offsetY);
    }

    static AtlasContent atlas(String itemId, int size, Map<String, String> label, int offsetX, int offsetY) {
        ResourceLocation item = ResourceLocation.parse(itemId);
        ResourceLocation texture = ResourceLocation.parse(
            item.getNamespace() + ":textures/item/" + item.getPath() + ".png");
        return new AtlasContent(texture, size, size, localized(label), offsetX, offsetY);
    }

    static ImageContent image(String texture, int width, int height) {
        return ImageContent.of(ResourceLocation.parse(texture), width, height);
    }

    static ImageContent image(String texture, int width, int height, int offsetX, int offsetY) {
        return ImageContent.withOffset(ResourceLocation.parse(texture), width, height, offsetX, offsetY);
    }

    static ImageContent image(String texture, int width, int height, int u, int v,
                              int textureWidth, int textureHeight, float scale, int offsetX, int offsetY) {
        return new ImageContent(ResourceLocation.parse(texture), width, height, u, v, textureWidth, textureHeight,
            scale, offsetX, offsetY);
    }

    private static LocalizedText localized(Map<String, String> values) {
        if (values == null || values.isEmpty()) return null;
        Map<String, Component> components = new LinkedHashMap<>();
        values.forEach((language, value) -> components.put(language, Component.literal(value)));
        return LocalizedText.languages(components);
    }
}
