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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * 实体渲染内容类。
 * 在 tooltip 中渲染 3D 实体模型。
 */
public class EntityContent implements TipContent {
    private static final float ENTITY_BOX_PADDING = 0.9f;

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
    @Nullable
    private Entity cachedEntity;
    @Nullable
    private Level cachedLevel;

    public EntityContent(EntityType<?> entityType, int size, float rotationSpeed,
                         boolean autoRotate, int offsetX, int offsetY, @Nullable Component label) {
        this.entityType = java.util.Objects.requireNonNull(entityType, "entityType");
        this.size = ContentBounds.dimension(size);
        this.rotationSpeed = Float.isFinite(rotationSpeed) ? Mth.clamp(rotationSpeed, -360.0f, 360.0f) : 0.0f;
        this.autoRotate = autoRotate;
        this.offsetX = ContentBounds.offset(offsetX);
        this.offsetY = ContentBounds.offset(offsetY);
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
        return visualHeight();
    }

    @Override
    public int getWidth(int maxWidth) {
        int width = visualWidth();
        if (label != null) {
            Font font = Minecraft.getInstance().font;
            width += 4 + font.width(label);
        }
        return width;
    }

    @Override
    public boolean isAnimated() {
        return autoRotate;
    }

    @Override
    public void tick(int tickCount) {
        if (autoRotate && tickCount != lastTick) {
            lastTick = tickCount;
            currentRotation = Mth.wrapDegrees(currentRotation + rotationSpeed);
        }
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = getOrCreateEntity(mc.level);
        if (entity == null) return;

        // 获取旋转角度，使用 partialTick 进行插值让旋转更平滑
        float rotation = autoRotate
            ? Mth.wrapDegrees(currentRotation + rotationSpeed * context.partialTick())
            : 0;

        int renderBaseX = x + Math.max(0, -offsetX);
        int renderBaseY = y + Math.max(0, -offsetY);

        boolean clipped = ContentBounds.beginHorizontalClip(
            context, x, y, maxWidth, visualHeight(), getWidth(Integer.MAX_VALUE));
        try {
            renderEntity(
                context.graphics(), entity,
                renderBaseX + size / 2 + offsetX,
                renderBaseY + size + offsetY,
                size, rotation
            );
            if (label != null) {
                int labelX = x + visualWidth() + 4;
                int labelY = y + (visualHeight() - 8) / 2;
                context.drawString(label, labelX, labelY, 0xFFFFFF);
            }
        } finally {
            ContentBounds.endHorizontalClip(context, clipped);
        }
    }

    private int visualWidth() {
        return ContentBounds.extent(size, offsetX);
    }

    private int visualHeight() {
        return ContentBounds.extent(size, offsetY);
    }

    // 渲染实体到 GUI
    private static void renderEntity(GuiGraphics graphics, Entity entity, int x, int y, int size, float rotation) {
        // 在修改实体阴影和 GUI 光照前提交原版 Tooltip 缓冲，避免共享状态交叉影响。
        graphics.flush();
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        try {
            poseStack.translate(x, y, 100);
            float scale = calculateEntityScale(entity, size);
            poseStack.scale(scale, -scale, scale);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

            dispatcher.setRenderShadow(false);
            Lighting.setupForFlatItems();

            MultiBufferSource.BufferSource bufferSource = graphics.bufferSource();
            dispatcher.render(entity, 0, 0, 0, 0, 1.0f, poseStack, bufferSource, 15728880);
            bufferSource.endBatch();
        } finally {
            dispatcher.setRenderShadow(true);
            Lighting.setupFor3DItems();
            poseStack.popPose();
        }
    }

    private static float calculateEntityScale(Entity entity, int size) {
        float width = Math.max(entity.getBbWidth(), 0.1f);
        float height = Math.max(entity.getBbHeight(), 0.1f);
        float rotatedWidth = width * 1.4142136f;
        float maxDimension = Math.max(rotatedWidth, height);
        return size * ENTITY_BOX_PADDING / maxDimension;
    }

    @Nullable
    private Entity getOrCreateEntity(Level level) {
        if (cachedEntity == null || cachedLevel != level || cachedEntity.isRemoved()) {
            cachedEntity = entityType.create(level);
            cachedLevel = level;
        }
        return cachedEntity;
    }
}
