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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体渲染内容类。
 * <p>
 * 在 tooltip 中渲染 3D 实体模型。每个实体类型维护独立的旋转状态，
 * 避免多个实体同时渲染时出现抖动。
 * </p>
 *
 * <h3>渲染原理</h3>
 * <p>
 * 使用 {@link EntityRenderDispatcher} 渲染实体，内部使用 {@link ConcurrentHashMap}
 * 存储每个实体类型的旋转状态，确保多实例渲染时的稳定性。
 * </p>
 *
 * <h3>JSON 示例</h3>
 * <pre>{@code
 * // 基础实体渲染
 * {"type": "entity", "entity": "minecraft:wolf", "size": 48}
 *
 * // 带旋转动画
 * {"type": "entity", "entity": "minecraft:wolf", "size": 48, "rotationSpeed": 1.0, "autoRotate": true}
 *
 * // 带标签
 * {"type": "entity", "entity": "minecraft:creeper", "size": 48, "label": "苦力怕"}
 * }</pre>
 *
 * @author cooobird
 * @see com.cooobird.datatip.api.parser.EntityContentParser JSON 解析器
 * @since 1.2.0
 */
public record EntityContent(
    EntityType<?> entityType,  // 实体类型
    int size,                  // 渲染大小
    float rotationSpeed,       // 旋转速度
    boolean autoRotate,        // 是否自动旋转
    int offsetX,               // X 轴偏移量
    int offsetY,               // Y 轴偏移量
    @Nullable Component label  // 可选的标签文本
) implements TipContent {

    /**
     * 旋转状态存储。
     * 使用 ConcurrentHashMap 按实体类型存储，避免多实例渲染时的抖动问题。
     */
    private static final Map<EntityType<?>, RotationState> ROTATION_STATES = new ConcurrentHashMap<>();

    // 旋转状态内部类
    private static class RotationState {
        float rotation = 0;  // 当前旋转角度
        int lastTick = -1;   // 上次更新的 tick 计数
    }

    // 获取或创建指定实体类型的旋转状态
    private static RotationState getRotationState(EntityType<?> type) {
        return ROTATION_STATES.computeIfAbsent(type, k -> new RotationState());
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
        // 更新旋转状态
        if (autoRotate) {
            RotationState state = getRotationState(entityType);
            if (tickCount != state.lastTick) {
                state.lastTick = tickCount;
                state.rotation += rotationSpeed;
                if (state.rotation >= 360) {
                    state.rotation -= 360;
                }
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
        RotationState state = getRotationState(entityType);
        float rotation = autoRotate ? state.rotation + rotationSpeed * context.partialTick() : 0;

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
        dispatcher.setRenderShadow(false);  // 禁用阴影

        // 设置全局光照
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
