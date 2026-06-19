package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipRenderContext;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * 方块渲染内容类。
 * <p>
 * 在 tooltip 中渲染 3D 方块模型。使用物品渲染管线，避免 blockColors 颜色问题。
 * </p>
 *
 * <h3>渲染原理</h3>
 * <p>
 * 使用 {@link ItemDisplayContext#GUI} 上下文渲染方块，内部根据 size 自动计算缩放。
 * 旋转顺序：先绕 X 轴倾斜，再绕 Y 轴水平旋转。
 * </p>
 *
 * <h3>JSON 示例</h3>
 * <pre>{@code
 * // 基础方块渲染
 * {"type": "block", "block": "minecraft:stone", "size": 32}
 *
 * // 带旋转动画
 * {"type": "block", "block": "minecraft:crafting_table", "size": 48, "rotationSpeed": 0.5, "autoRotate": true}
 *
 * // 带标签
 * {"type": "block", "block": "minecraft:diamond_block", "size": 32, "label": "钻石块"}
 * }</pre>
 *
 * @author cooobird
 * @see com.cooobird.datatip.api.parser.BlockContentParser JSON 解析器
 * @since 1.2.0
 */
public record BlockContent(
    Block block,                // 方块实例
    int size,                   // 渲染大小
    float rotationSpeed,        // 旋转速度，默认 0.5
    boolean autoRotate,         // 是否自动旋转
    @Nullable Component label,  // 可选的标签文本
    int offsetY                 // Y 轴偏移量
) implements TipContent {

    private static float rotationAngle = 0;  // 当前旋转角度
    private static int lastTickCount = 0;    // 上次更新的 tick 计数

    // 创建方块内容
    public static BlockContent of(Block block) {
        return new BlockContent(block, 32, 0.5f, true, null, 0);
    }

    // 创建带标签的方块内容
    public static BlockContent withLabel(Block block, Component label) {
        return new BlockContent(block, 32, 0.5f, true, label, 0);
    }

    // 创建指定尺寸的方块内容
    public static BlockContent of(Block block, int size) {
        return new BlockContent(block, size, 0.5f, true, null, 0);
    }

    // 创建带偏移的方块内容
    public static BlockContent withOffset(Block block, int size, int offsetY) {
        return new BlockContent(block, size, 0.5f, true, null, offsetY);
    }

    @Override
    public int getHeight(int maxWidth) {
        return size + (label != null ? 12 : 0);
    }

    @Override
    public int getWidth(int maxWidth) {
        int width = size;
        if (label != null) {
            Font font = Minecraft.getInstance().font;
            width += 4 + font.width(label.getString());
        }
        return Math.min(width, maxWidth);
    }

    @Override
    public boolean isAnimated() {
        return autoRotate;
    }

    @Override
    public void tick(int tickCount) {
        // 每 tick 更新旋转角度
        if (autoRotate && tickCount != lastTickCount) {
            lastTickCount = tickCount;
            rotationAngle += rotationSpeed;
            if (rotationAngle >= 360) {
                rotationAngle -= 360;
            }
        }
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0) return;

        // 应用 Y 轴偏移
        int renderY = y + offsetY;

        // 创建物品栈用于渲染
        ItemStack stack = new ItemStack(block);
        renderBlockAsItem(context.graphics(), stack, x + size / 2, renderY + size / 2, size, rotationAngle);

        // 渲染标签
        if (label != null) {
            int labelX = x + size + 4;
            int labelY = renderY + (size - 8) / 2;
            context.drawString(label, labelX, labelY, 0xFFFFFF);
        }
    }

    /**
     * 使用物品渲染方式渲染方块。
     * <p>
     * 渲染流程：
     * <ol>
     *   <li>移动到指定位置</li>
     *   <li>根据 size 自动缩放</li>
     *   <li>绕 X 轴倾斜</li>
     *   <li>绕 Y 轴水平旋转</li>
     *   <li>使用 ItemDisplayContext.GUI 渲染</li>
     * </ol>
     * </p>
     *
     * @param graphics GuiGraphics 实例
     * @param stack    物品栈
     * @param x        中心 X 坐标
     * @param y        中心 Y 坐标
     * @param size     目标渲染大小
     * @param rotation 当前旋转角度
     */
    private static void renderBlockAsItem(GuiGraphics graphics, ItemStack stack, int x, int y, int size, float rotation) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        // 移动到指定位置
        poseStack.translate(x, y, 100);

        // 根据 size 自动缩放
        // Y 轴负值用于翻转
        poseStack.scale((float) size, -(float) size, (float) size);

        poseStack.mulPose(Axis.YP.rotationDegrees(45 + rotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(-35));


        // 设置平面光照
        Lighting.setupForFlatItems();

        // 使用物品渲染器渲染
        Minecraft mc = Minecraft.getInstance();
        mc.getItemRenderer().renderStatic(stack,
            ItemDisplayContext.GUI,
            15728880,  // 全亮光照
            OverlayTexture.NO_OVERLAY,
            poseStack,
            graphics.bufferSource(),
            mc.level,
            0);
        graphics.bufferSource().endBatch();

        // 恢复光照设置
        Lighting.setupFor3DItems();

        poseStack.popPose();
    }
}
