package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
    @Nullable String label,        // 可选的标签文本
    int offsetY                    // Y 轴偏移量
) implements TipContent {

    // 创建纹理内容
    public static AtlasContent of(ResourceLocation texturePath, int width, int height) {
        return new AtlasContent(texturePath, width, height, null, 0);
    }

    // 创建正方形纹理内容
    public static AtlasContent of(ResourceLocation texturePath, int size) {
        return new AtlasContent(texturePath, size, size, null, 0);
    }

    // 创建带标签的纹理内容
    public static AtlasContent withLabel(ResourceLocation texturePath, int width, int height, String label) {
        return new AtlasContent(texturePath, width, height, label, 0);
    }

    // 从方块 ID 创建，自动转换路径
    public static AtlasContent fromBlock(ResourceLocation blockId, int size) {
        String path = blockId.getNamespace() + ":textures/block/" + blockId.getPath() + ".png";
        return new AtlasContent(ResourceLocation.parse(path), size, size, null, 0);
    }

    // 从物品 ID 创建，自动转换路径
    public static AtlasContent fromItem(ResourceLocation itemId, int size) {
        String path = itemId.getNamespace() + ":textures/item/" + itemId.getPath() + ".png";
        return new AtlasContent(ResourceLocation.parse(path), size, size, null, 0);
    }

    // 创建带偏移的纹理内容
    public static AtlasContent withOffset(ResourceLocation texturePath, int size, int offsetY) {
        return new AtlasContent(texturePath, size, size, null, offsetY);
    }

    @Override
    public int getHeight(int maxWidth) {
        return height + (label != null ? 12 : 0);
    }

    @Override
    public int getWidth(int maxWidth) {
        int spriteWidth = width;
        if (label != null) {
            Font font = Minecraft.getInstance().font;
            spriteWidth += 4 + font.width(label);
        }
        return Math.min(spriteWidth, maxWidth);
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0) return;

        int renderY = y + offsetY;

        // 使用 blit 渲染纹理
        context.blit(texturePath, x, renderY, 0, 0, width, height, width, height);

        // 渲染标签
        if (label != null) {
            int labelX = x + width + 4;
            int labelY = renderY + (height - 8) / 2;
            context.drawString(label, labelX, labelY, 0xFFFFFF);
        }
    }
}
