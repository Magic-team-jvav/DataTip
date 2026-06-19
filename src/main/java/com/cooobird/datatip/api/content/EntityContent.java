package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipRenderContext;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

/**
 * 实体渲染内容类。
 * 在 tooltip 中渲染 3D 实体模型。
 */
public class EntityContent implements TipContent {

    private final EntityType<?> entityType;  // 实体类型
    private final int size;                  // 渲染大小
    private final float rotationSpeed;       // 旋转速度
    private final boolean autoRotate;        // 是否自动旋转
    private final int offsetX;               // X 轴偏移量
    private final int offsetY;               // Y 轴偏移量
    @Nullable
    private final Component label;           // 可选的标签文本

    private float currentRotation = 0;
    private int lastTick = -1;

    public EntityContent(EntityType<?> entityType, int size, float rotationSpeed,
                         boolean autoRotate, int offsetX, int offsetY, @Nullable Component label) {
        this.entityType = entityType;
        this.size = size;
        this.rotationSpeed = rotationSpeed;
        this.autoRotate = autoRotate;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.label = label;
    }

    // Getter 方法
    public EntityType<?> entityType() {
        return entityType;
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

    public int offsetX() {
        return offsetX;
    }

    public int offsetY() {
        return offsetY;
    }

    @Nullable
    public Component label() {
        return label;
    }

    // 创建实体内容
    public static EntityContent of(EntityType<?> entityType) {
        return new EntityContent(entityType, 48, 1.0f, true, 0, 0, null);
    }

    // 创建带标签的实体内容
    public static EntityContent withLabel(EntityType<?> entityType, Component label) {
        return new EntityContent(entityType, 48, 1.0f, true, 0, 0, label);
    }

    // 创建指定尺寸的实体内容
    public static EntityContent of(EntityType<?> entityType, int size) {
        return new EntityContent(entityType, size, 1.0f, true, 0, 0, null);
    }

    // 创建带偏移的实体内容
    public static EntityContent withOffset(EntityType<?> entityType, int size, int offsetX, int offsetY) {
        return new EntityContent(entityType, size, 1.0f, true, offsetX, offsetY, null);
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

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // 创建实体实例
        Entity entity = entityType.create(mc.level);
        if (entity == null) return;

        // 获取旋转角度，使用 partialTick 进行插值让旋转更平滑
        float rotation = autoRotate ? currentRotation + rotationSpeed * context.partialTick() : 0;

        // 渲染实体
        renderEntity(context.graphics(), entity, x + size / 2 + offsetX, y + size + offsetY, size, rotation);

        // 渲染标签
        if (label != null) {
            int labelX = x + size + 4;
            int labelY = y + (size - 8) / 2;
            context.drawString(label, labelX, labelY, 0xFFFFFF);
        }
    }

    // 渲染实体到 GUI
    private static void renderEntity(GuiGraphics graphics, Entity entity, int x, int y, int size, float rotation) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        // 移动到指定位置
        poseStack.translate(x, y, 100);
        poseStack.scale(size, -size, size);

        // 绕 Y 轴旋转
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        // 渲染实体
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);

        // 设置全局光照，无阴影
        Lighting.setupForFlatItems();

        MultiBufferSource.BufferSource bufferSource = graphics.bufferSource();
        dispatcher.render(entity, 0, 0, 0, 0, 1.0f, poseStack, bufferSource, 15728880);
        bufferSource.endBatch();

        // 恢复设置
        dispatcher.setRenderShadow(true);
        Lighting.setupFor3DItems();

        poseStack.popPose();
    }
}
