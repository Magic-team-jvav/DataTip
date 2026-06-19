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
 * 在 tooltip 中渲染 3D 方块模型。
 */
public class BlockContent implements TipContent {

    private final Block block;               // 方块实例
    private final int size;                  // 渲染大小
    private final float rotationSpeed;       // 旋转速度
    private final boolean autoRotate;        // 是否自动旋转
    @Nullable
    private final Component label;           // 可选的标签文本
    private final int offsetX;               // X 轴偏移量
    private final int offsetY;               // Y 轴偏移量

    // 实例级别的旋转状态
    private float currentRotation = 0;
    private int lastTick = -1;

    public BlockContent(Block block, int size, float rotationSpeed,
                        boolean autoRotate, @Nullable Component label, int offsetX, int offsetY) {
        this.block = block;
        this.size = size;
        this.rotationSpeed = rotationSpeed;
        this.autoRotate = autoRotate;
        this.label = label;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    // Getter 方法
    public Block block() {
        return block;
    }

    public int size() {
        return size;
    }

    public float rotationSpeed() {
        return rotationSpeed;
    }

    public boolean autoRotate() {
        return autoRotate;
    }

    @Nullable
    public Component label() {
        return label;
    }

    public int offsetX() {
        return offsetX;
    }

    public int offsetY() {
        return offsetY;
    }

    // 创建方块内容
    public static BlockContent of(Block block) {
        return new BlockContent(block, 32, 0.5f, true, null, 0, 0);
    }

    // 创建带标签的方块内容
    public static BlockContent withLabel(Block block, Component label) {
        return new BlockContent(block, 32, 0.5f, true, label, 0, 0);
    }

    // 创建指定尺寸的方块内容
    public static BlockContent of(Block block, int size) {
        return new BlockContent(block, size, 0.5f, true, null, 0, 0);
    }

    // 创建带偏移的方块内容
    public static BlockContent withOffset(Block block, int size, int offsetX, int offsetY) {
        return new BlockContent(block, size, 0.5f, true, null, offsetX, offsetY);
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
        if (autoRotate && tickCount != lastTick) {
            lastTick = tickCount;
            currentRotation += rotationSpeed;
            if (currentRotation >= 360) {
                currentRotation -= 360;
            }
        }
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0) return;

        // 应用偏移
        int renderX = x + offsetX;
        int renderY = y + offsetY;

        // 使用 partialTick 进行插值，让旋转更平滑
        float smoothRotation = currentRotation;
        if (autoRotate) {
            smoothRotation += rotationSpeed * context.partialTick();
        }

        // 创建物品栈用于渲染
        ItemStack stack = new ItemStack(block);
        renderBlockAsItem(context.graphics(), stack, renderX + size / 2, renderY + size / 2, size, smoothRotation);

        // 渲染标签
        if (label != null) {
            int labelX = x + size + 4;
            int labelY = renderY + (size - 8) / 2;
            context.drawString(label, labelX, labelY, 0xFFFFFF);
        }
    }

    // 使用物品渲染方式渲染方块
    private static void renderBlockAsItem(GuiGraphics graphics, ItemStack stack, int x, int y, int size, float rotation) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        // 移动到指定位置
        poseStack.translate(x, y, 100);

        // 根据 size 自动缩放
        poseStack.scale((float) size, -(float) size, (float) size);

        poseStack.mulPose(Axis.YP.rotationDegrees(45 + rotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(-35));

        // 设置平面光照
        Lighting.setupForFlatItems();

        // 使用物品渲染器渲染
        Minecraft mc = Minecraft.getInstance();
        mc.getItemRenderer().renderStatic(stack,
            ItemDisplayContext.GUI,
            15728880,
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
