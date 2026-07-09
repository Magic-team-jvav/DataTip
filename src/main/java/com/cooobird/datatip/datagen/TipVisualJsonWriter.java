package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.content.BlockContent;
import com.cooobird.datatip.api.content.EntityContent;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * 视觉内容 JSON 写出器。
 */
final class TipVisualJsonWriter {
    private TipVisualJsonWriter() {
    }

    static void writeEntity(JsonObject json, EntityContent entity) {
        json.addProperty("type", "entity");
        json.addProperty("entity", BuiltInRegistries.ENTITY_TYPE.getKey(entity.entityType()).toString());
        json.addProperty("size", entity.size());
        if (entity.rotationSpeed() != 1.0f) json.addProperty("rotationSpeed", entity.rotationSpeed());
        if (!entity.autoRotate()) json.addProperty("autoRotate", false);
        if (entity.label() != null) json.addProperty("label", entity.label().getString());
        if (entity.offsetX() != 0) json.addProperty("offsetX", entity.offsetX());
        if (entity.offsetY() != 0) json.addProperty("offsetY", entity.offsetY());
    }

    static void writeBlock(JsonObject json, BlockContent block) {
        json.addProperty("type", "block");
        json.addProperty("block", BuiltInRegistries.BLOCK.getKey(block.block()).toString());
        json.addProperty("size", block.size());
        if (block.rotationSpeed() != 1.0f) json.addProperty("rotationSpeed", block.rotationSpeed());
        if (!block.autoRotate()) json.addProperty("autoRotate", false);
        if (block.label() != null) json.addProperty("label", block.label().getString());
        if (block.offsetX() != 0) json.addProperty("offsetX", block.offsetX());
        if (block.offsetY() != 0) json.addProperty("offsetY", block.offsetY());
    }

    static void writeAtlas(
        JsonObject json,
        ResourceLocation texturePath,
        int width,
        int height,
        String label,
        int x,
        int y
    ) {
        json.addProperty("type", "atlas");
        json.addProperty("texture", texturePath.toString());
        if (width == height) {
            json.addProperty("size", width);
        } else {
            json.addProperty("width", width);
            json.addProperty("height", height);
        }
        if (label != null) json.addProperty("label", label);
        if (x != 0) json.addProperty("offsetX", x);
        if (y != 0) json.addProperty("offsetY", y);
    }

    static void writeImage(
        JsonObject json,
        ResourceLocation texture,
        int width,
        int height,
        int u,
        int v,
        int textureWidth,
        int textureHeight,
        float scale,
        int offsetX,
        int offsetY
    ) {
        json.addProperty("type", "image");
        json.addProperty("texture", texture.toString());
        json.addProperty("width", width);
        json.addProperty("height", height);
        if (u != 0 || v != 0) {
            json.addProperty("u", u);
            json.addProperty("v", v);
        }
        if (textureWidth != width || textureHeight != height) {
            json.addProperty("textureWidth", textureWidth);
            json.addProperty("textureHeight", textureHeight);
        }
        if (scale != 1.0f) json.addProperty("scale", scale);
        if (offsetX != 0) json.addProperty("offsetX", offsetX);
        if (offsetY != 0) json.addProperty("offsetY", offsetY);
    }
}
