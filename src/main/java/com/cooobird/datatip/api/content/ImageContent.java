package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.layout.OverflowPolicy;
import com.cooobird.datatip.internal.layout.PreparedLeafSupport;
import com.mojang.blaze3d.systems.RenderSystem;
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
) implements com.cooobird.datatip.api.layout.PreparedContent {

    public ImageContent(
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
        this.texture = java.util.Objects.requireNonNull(texture, "texture");
        this.width = ContentBounds.dimension(width);
        this.height = ContentBounds.dimension(height);
        this.u = u;
        this.v = v;
        this.textureWidth = ContentBounds.dimension(textureWidth);
        this.textureHeight = ContentBounds.dimension(textureHeight);
        this.scale = Float.isFinite(scale) && scale > 0 ? scale : 1.0f;
        this.offsetX = ContentBounds.offset(offsetX);
        this.offsetY = ContentBounds.offset(offsetY);
    }

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
        return ContentBounds.extent(renderHeight(), offsetY);
    }

    @Override
    public int getWidth(int maxWidth) {
        return ContentBounds.extent(renderWidth(), offsetX);
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0) return;

        int renderWidth = renderWidth();
        int renderHeight = renderHeight();

        int renderBaseX = x + ContentBounds.negativeInset(offsetX);
        int renderBaseY = y + ContentBounds.negativeInset(offsetY);
        int renderX = renderBaseX + offsetX;
        int renderY = renderBaseY + offsetY;

        boolean clipped = false;
        try {
            // 目标区域使用缩放后的尺寸，源纹理采样区域保持 JSON 声明的原始尺寸。
            // 如果把 renderWidth/renderHeight 同时用于源区域，scale > 1 时会越界采样并重复纹理。
            context.blit(texture, renderX, renderY, renderWidth, renderHeight,
                u, v, width, height, textureWidth, textureHeight);
        } finally {
            ContentBounds.endHorizontalClip(context, clipped);
        }
    }

    private int renderWidth() {
        return ContentBounds.scaledDimension(width, scale);
    }

    private int renderHeight() {
        return ContentBounds.scaledDimension(height, scale);
    }

    @Override
    public com.cooobird.datatip.api.layout.PreparedLayout prepare(
        com.cooobird.datatip.api.layout.TipPrepareContext context
    ) {
        int naturalWidth = getWidth(0);
        int naturalHeight = getHeight(0);
        double fit = naturalWidth > context.measureSpec().hardMaxWidth()
            ? (double) context.measureSpec().hardMaxWidth() / naturalWidth
            : 1.0;
        int allocatedWidth = scaled(naturalWidth, fit);
        int allocatedHeight = scaled(naturalHeight, fit);
        int imageWidth = scaled(renderWidth(), fit);
        int imageHeight = scaled(renderHeight(), fit);
        int imageX = scaled(
            ContentBounds.negativeInsetLong(offsetX) + offsetX,
            fit
        );
        int imageY = scaled(
            ContentBounds.negativeInsetLong(offsetY) + offsetY,
            fit
        );
        return PreparedLeafSupport.draw(
            naturalWidth,
            naturalHeight,
            naturalWidth,
            naturalHeight,
            allocatedWidth,
            allocatedHeight,
            OverflowPolicy.SCALE_DOWN,
            com.cooobird.datatip.api.render.RenderPhase.VISUAL_2D,
            "image",
            (renderContext, x, y, alpha) -> {
                float[] shaderColor = RenderSystem.getShaderColor().clone();
                RenderSystem.setShaderColor(
                    shaderColor[0],
                    shaderColor[1],
                    shaderColor[2],
                    shaderColor[3] * alpha
                );
                try {
                    renderContext.blit(
                        texture,
                        ContentBounds.coordinate(x, imageX),
                        ContentBounds.coordinate(y, imageY),
                        imageWidth,
                        imageHeight,
                        u,
                        v,
                        width,
                        height,
                        textureWidth,
                        textureHeight
                    );
                } finally {
                    RenderSystem.setShaderColor(
                        shaderColor[0],
                        shaderColor[1],
                        shaderColor[2],
                        shaderColor[3]
                    );
                }
            }
        );
    }

    private static int scaled(long value, double scale) {
        if (value <= 0) return 0;
        return (int) Math.max(
            1,
            Math.min(Integer.MAX_VALUE, Math.round(value * scale))
        );
    }
}
