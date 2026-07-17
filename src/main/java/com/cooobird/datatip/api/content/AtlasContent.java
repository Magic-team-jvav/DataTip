package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.text.LocalizedText;
import com.cooobird.datatip.internal.layout.LabeledVisualBounds;
import com.cooobird.datatip.internal.layout.PreparedLabeledVisualLayout;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * 纹理渲染内容类。
 * <p>
 * 用于在 tooltip 中渲染纹理图片。支持三种方式指定纹理：
 * <ul>
 *   <li><b>直接路径</b>：指定完整的纹理路径</li>
 *   <li><b>方块 ID</b>：自动转换为方块纹理路径</li>
 *   <li><b>物品 ID</b>：自动转换为物品纹理路径</li>
 * </ul>
 * </p>
 *
 * <h3>JSON 示例</h3>
 * <pre>{@code
 * // 直接指定纹理路径
 * {"type": "atlas", "texture": "minecraft:textures/block/stone.png", "width": 32, "height": 32}
 *
 * // 使用方块 ID
 * {"type": "atlas", "block": "minecraft:red_concrete", "size": 32}
 *
 * // 使用物品 ID
 * {"type": "atlas", "item": "minecraft:apple", "size": 32}
 *
 * // 带标签
 * {"type": "atlas", "block": "minecraft:diamond_block", "size": 32, "label": "钻石块"}
 * }</pre>
 *
 * @author cooobird
 * @see com.cooobird.datatip.api.parser.AtlasContentParser JSON 解析器
 * @since 1.2.0
 */
public record AtlasContent(
    ResourceLocation texturePath,  // 纹理资源路径
    int width,                     // 渲染宽度
    int height,                    // 渲染高度
    @Nullable LocalizedText labelText, // 可选的标签文本
    int offsetX,                   // X 轴偏移量
    int offsetY                    // Y 轴偏移量
) implements com.cooobird.datatip.api.layout.PreparedContent {

    public AtlasContent(
        ResourceLocation texturePath,
        int width,
        int height,
        @Nullable LocalizedText labelText,
        int offsetX,
        int offsetY
    ) {
        this.texturePath = java.util.Objects.requireNonNull(
            texturePath,
            "texturePath"
        );
        this.width = ContentBounds.dimension(width);
        this.height = ContentBounds.dimension(height);
        this.labelText = labelText;
        this.offsetX = ContentBounds.offset(offsetX);
        this.offsetY = ContentBounds.offset(offsetY);
    }

    public AtlasContent(
        ResourceLocation texturePath,
        int width,
        int height,
        @Nullable String label,
        int offsetX,
        int offsetY
    ) {
        this(texturePath, width, height,
            label != null ? LocalizedText.literal(label) : null, offsetX, offsetY);
    }

    @Nullable
    public String label() {
        return labelText != null ? labelText.getString() : null;
    }

    /**
     * 返回当前游戏语言对应的标签组件，并保留其文本样式。
     */
    @Nullable
    public Component labelComponent() {
        return labelText != null ? labelText.resolve() : null;
    }

    // 创建纹理内容
    public static AtlasContent of(ResourceLocation texturePath, int width, int height) {
        return new AtlasContent(texturePath, width, height, (LocalizedText) null, 0, 0);
    }

    // 创建正方形纹理内容
    public static AtlasContent of(ResourceLocation texturePath, int size) {
        return new AtlasContent(texturePath, size, size, (LocalizedText) null, 0, 0);
    }

    // 创建带标签的纹理内容
    public static AtlasContent withLabel(ResourceLocation texturePath, int width, int height, String label) {
        return new AtlasContent(texturePath, width, height, label, 0, 0);
    }

    // 从方块 ID 创建，自动转换路径
    public static AtlasContent fromBlock(ResourceLocation blockId, int size) {
        String path = blockId.getNamespace() + ":textures/block/" + blockId.getPath() + ".png";
        return new AtlasContent(ResourceLocation.parse(path), size, size, (LocalizedText) null, 0, 0);
    }

    // 从物品 ID 创建，自动转换路径
    public static AtlasContent fromItem(ResourceLocation itemId, int size) {
        String path = itemId.getNamespace() + ":textures/item/" + itemId.getPath() + ".png";
        return new AtlasContent(ResourceLocation.parse(path), size, size, (LocalizedText) null, 0, 0);
    }

    // 创建带偏移的纹理内容
    public static AtlasContent withOffset(ResourceLocation texturePath, int size, int offsetX, int offsetY) {
        return new AtlasContent(texturePath, size, size, (LocalizedText) null, offsetX, offsetY);
    }

    @Override
    public int getHeight(int maxWidth) {
        return getHeight(new TipLayoutContext(
            Minecraft.getInstance().font,
            net.minecraft.world.item.ItemStack.EMPTY,
            Math.max(0, maxWidth)
        ));
    }

    @Override
    public int getHeight(TipLayoutContext context) {
        return LabeledVisualBounds.height(
            visualHeight(),
            labelComponent(),
            context.font()
        );
    }

    @Override
    public int getWidth(int maxWidth) {
        return getWidth(new TipLayoutContext(
            Minecraft.getInstance().font,
            net.minecraft.world.item.ItemStack.EMPTY,
            Math.max(0, maxWidth)
        ));
    }

    @Override
    public int getWidth(TipLayoutContext context) {
        return LabeledVisualBounds.width(
            visualWidth(),
            labelComponent(),
            context.font()
        );
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0) return;

        int renderBaseX = x + ContentBounds.negativeInset(offsetX);
        int renderBaseY = y + ContentBounds.negativeInset(offsetY);
        int renderX = renderBaseX + offsetX;
        int renderY = renderBaseY + offsetY;

        boolean clipped = false;
        try {
            context.blit(texturePath, renderX, renderY, 0, 0, width, height, width, height);
            Component label = labelComponent();
            if (label != null) {
                int labelX = x + visualWidth() + 4;
                int rowHeight = LabeledVisualBounds.height(
                    visualHeight(),
                    label,
                    context.font()
                );
                int labelY = LabeledVisualBounds.labelY(y, rowHeight, context.font());
                context.drawString(label, labelX, labelY, 0xFFFFFF);
            }
        } finally {
            ContentBounds.endHorizontalClip(context, clipped);
        }
    }

    private int visualWidth() {
        return ContentBounds.extent(width, offsetX);
    }

    private int visualHeight() {
        return ContentBounds.extent(height, offsetY);
    }

    @Override
    public com.cooobird.datatip.api.layout.PreparedLayout prepare(
        com.cooobird.datatip.api.layout.TipPrepareContext context
    ) {
        Component frozenLabel = labelComponent();
        int naturalBodyWidth = visualWidth();
        int naturalBodyHeight = visualHeight();
        long rawX = ContentBounds.negativeInsetLong(offsetX) + offsetX;
        long rawY = ContentBounds.negativeInsetLong(offsetY) + offsetY;
        return PreparedLabeledVisualLayout.prepare(
            context,
            naturalBodyWidth,
            naturalBodyHeight,
            frozenLabel != null ? frozenLabel.copy() : null,
            0xFFFFFFFF,
            com.cooobird.datatip.api.render.RenderPhase.VISUAL_2D,
            "atlas",
            (renderContext, x, y, scale, alpha) -> {
                int drawX = ContentBounds.coordinate(
                    x,
                    scaled(rawX, scale)
                );
                int drawY = ContentBounds.coordinate(
                    y,
                    scaled(rawY, scale)
                );
                int drawWidth = scaled(width, scale);
                int drawHeight = scaled(height, scale);
                float[] shaderColor = RenderSystem.getShaderColor().clone();
                RenderSystem.setShaderColor(
                    shaderColor[0],
                    shaderColor[1],
                    shaderColor[2],
                    shaderColor[3] * alpha
                );
                try {
                    renderContext.blit(
                        texturePath,
                        drawX,
                        drawY,
                        drawWidth,
                        drawHeight,
                        0,
                        0,
                        width,
                        height,
                        width,
                        height
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
        return (int) Math.min(
            Integer.MAX_VALUE,
            Math.round(value * scale)
        );
    }
}
