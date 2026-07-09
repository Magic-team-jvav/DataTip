package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.content.AtlasContent;
import com.cooobird.datatip.api.content.BlockContent;
import com.cooobird.datatip.api.content.EntityContent;
import com.cooobird.datatip.api.content.ImageContent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

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

    static BlockContent block(String blockId, int size) {
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockId));
        return BlockContent.of(block, size);
    }

    static BlockContent block(String blockId, int size, int offsetX, int offsetY) {
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockId));
        return BlockContent.withOffset(block, size, offsetX, offsetY);
    }

    static AtlasContent atlas(String itemId, int size) {
        return AtlasContent.fromItem(ResourceLocation.parse(itemId), size);
    }

    static AtlasContent atlas(String itemId, int size, int offsetX, int offsetY) {
        return AtlasContent.withOffset(ResourceLocation.parse(itemId), size, offsetX, offsetY);
    }

    static ImageContent image(String texture, int width, int height) {
        return ImageContent.of(ResourceLocation.parse(texture), width, height);
    }

    static ImageContent image(String texture, int width, int height, int offsetX, int offsetY) {
        return ImageContent.withOffset(ResourceLocation.parse(texture), width, height, offsetX, offsetY);
    }
}
