package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipRenderContext;
import net.minecraft.resources.ResourceLocation;

/**
 * 图片内容，渲染自定义纹理图片。
 */
public record ImageContent(
    ResourceLocation texture,  // 纹理资源路径
    int width,                 // 渲染宽度
    int height,                // 渲染高度
    int u,                     // 纹理 U 偏移
    int v,                     // 纹理 V 偏移
    int textureWidth,          // 纹理总宽度
    int textureHeight,         // 纹理总高度
    float scale,               // 缩放比例
    int offsetX,               // X 轴偏移量
    int offsetY                // Y 轴偏移量
) implements TipContent {

    // 创建图片内容
    public static ImageContent of(ResourceLocation texture, int width, int height) {
        return new ImageContent(texture, width, height, 0, 0, width, height, 1.0f, 0, 0);
    }

    // 创建图片内容
    public static ImageContent of(ResourceLocation texture, int width, int height, float scale) {
        return new ImageContent(texture, width, height, 0, 0, width, height, scale, 0, 0);
    }

    // 创建纹理图集中的图片
    public static ImageContent fromAtlas(ResourceLocation texture, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        return new ImageContent(texture, width, height, u, v, textureWidth, textureHeight, 1.0f, 0, 0);
    }

    // 创建带偏移的图片内容
    public static ImageContent withOffset(ResourceLocation texture, int width, int height, int offsetX, int offsetY) {
        return new ImageContent(texture, width, height, 0, 0, width, height, 1.0f, offsetX, offsetY);
    }

    @Override
    public int getHeight(int maxWidth) {
        return (int) (height * scale);
    }

    @Override
    public int getWidth(int maxWidth) {
        return Math.min((int) (width * scale), maxWidth);
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0) return;

        int renderWidth = (int) (width * scale);
        int renderHeight = (int) (height * scale);

        int renderX = x + offsetX;
        int renderY = y + offsetY;

        // 渲染纹理
        context.blit(texture, renderX, renderY, u, v, renderWidth, renderHeight, textureWidth, textureHeight);
    }
}
