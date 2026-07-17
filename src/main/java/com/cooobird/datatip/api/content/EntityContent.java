package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.layout.PreparedContent;
import com.cooobird.datatip.api.session.TooltipInvalidation;
import com.cooobird.datatip.api.session.TooltipSession;
import com.cooobird.datatip.api.session.TooltipSessionContext;
import com.cooobird.datatip.internal.layout.LabeledVisualBounds;
import com.cooobird.datatip.internal.layout.PreparedLabeledVisualLayout;
import com.cooobird.datatip.internal.layout.RotatingModelBounds;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
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
public class EntityContent implements PreparedContent {
    private static final float ENTITY_BOX_PADDING = 0.9f;

    private final EntityType<?> entityType;  // 实体类型
    private final int size;                  // 渲染大小
    private final float rotationSpeed;       // 旋转速度
    private final boolean autoRotate;        // 是否自动旋转
    private final int offsetX;               // X 轴偏移量
    private final int offsetY;               // Y 轴偏移量
    @Nullable
    private final Component label;           // 可选的标签文本

    private float currentRotation;
    private int lastTick = -1;

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
        return getHeight(new TipLayoutContext(
            Minecraft.getInstance().font,
            net.minecraft.world.item.ItemStack.EMPTY,
            Math.max(0, maxWidth)
        ));
    }

    @Override
    public int getHeight(TipLayoutContext context) {
        return LabeledVisualBounds.height(visualHeight(), label, context.font());
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
        return LabeledVisualBounds.width(visualWidth(), label, context.font());
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

        EntityLease lease = acquireEntityLease(mc.level);
        if (lease == null) return;
        Entity entity = lease.entity();

        // 获取旋转角度，使用 partialTick 进行插值让旋转更平滑
        float rotation = autoRotate
            ? Mth.wrapDegrees(currentRotation + rotationSpeed * context.partialTick())
            : 0;

        int renderBaseX = x + ContentBounds.negativeInset(offsetX);
        int renderBaseY = y + ContentBounds.negativeInset(offsetY);
        int horizontalInset = RotatingModelBounds.inset(size);
        int verticalInset = RotatingModelBounds.entityVerticalInset(size);

        boolean clipped = false;
        try {
            renderEntity(
                context.graphics(), entity,
                renderBaseX + horizontalInset + size / 2 + offsetX,
                renderBaseY + verticalInset + size + offsetY,
                size, rotation
            );
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
            if (TooltipSessionContext.current() == null) {
                lease.close();
            }
        }
    }

    private int visualWidth() {
        return ContentBounds.extent(RotatingModelBounds.boxSize(size), offsetX);
    }

    private int visualHeight() {
        return ContentBounds.extent(RotatingModelBounds.entityHeight(size), offsetY);
    }

    // 渲染实体到 GUI
    private static void renderEntity(GuiGraphics graphics, Entity entity, int x, int y, int size, float rotation) {
        // 在修改实体阴影和 GUI 光照前提交原版 Tooltip 缓冲，避免共享状态交叉影响。
        graphics.flush();
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        boolean previousShadow = dispatcher.shouldRenderShadow;
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
            dispatcher.setRenderShadow(previousShadow);
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
    private EntityLease acquireEntityLease(Level level) {
        TooltipSession session = TooltipSessionContext.current();
        if (session == null) {
            Entity entity = entityType.create(level);
            return entity != null ? new EntityLease(entity) : null;
        }

        EntityLease cached = session.cached(
            TooltipInvalidation.ENTITY,
            this,
            EntityLease.class
        );
        if (cached != null && !cached.entity().isRemoved()) {
            return cached;
        }
        if (cached != null) {
            cached.close();
        }

        Entity entity = entityType.create(level);
        if (entity == null) return null;
        EntityLease created = new EntityLease(entity);
        session.cache(TooltipInvalidation.ENTITY, this, created);
        session.own(TooltipInvalidation.ENTITY, created);
        return created;
    }

    private static final class EntityLease implements AutoCloseable {
        private final Entity entity;
        private boolean closed;

        private EntityLease(Entity entity) {
            this.entity = entity;
        }

        private Entity entity() {
            return entity;
        }

        @Override
        public void close() {
            if (closed) return;
            closed = true;
            entity.discard();
        }
    }

    @Override
    public com.cooobird.datatip.api.layout.PreparedLayout prepare(
        com.cooobird.datatip.api.layout.TipPrepareContext context
    ) {
        return PreparedLabeledVisualLayout.prepare(
            context,
            visualWidth(),
            visualHeight(),
            label != null ? label.copy() : null,
            0xFFFFFFFF,
            com.cooobird.datatip.api.render.RenderPhase.ISOLATED_MODEL,
            "entity",
            this::renderPreparedModel
        );
    }

    private void renderPreparedModel(
        TipRenderContext context,
        int x,
        int y,
        double scale,
        float alpha
    ) {
        if (alpha <= 0) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        EntityLease lease = acquireEntityLease(minecraft.level);
        if (lease == null) return;
        try {
            float rotation = autoRotate
                ? Mth.wrapDegrees(
                currentRotation + rotationSpeed * context.partialTick()
            )
                : 0;
            int preparedSize = scaled(size, scale);
            int preparedOffsetX = scaledSigned(offsetX, scale);
            int preparedOffsetY = scaledSigned(offsetY, scale);
            int horizontalInset = RotatingModelBounds.inset(preparedSize);
            int verticalInset = RotatingModelBounds.entityVerticalInset(
                preparedSize
            );
            renderEntity(
                context.graphics(),
                lease.entity(),
                ContentBounds.coordinate(
                    x,
                    ContentBounds.negativeInsetLong(preparedOffsetX),
                    horizontalInset,
                    preparedSize / 2L,
                    preparedOffsetX
                ),
                ContentBounds.coordinate(
                    y,
                    ContentBounds.negativeInsetLong(preparedOffsetY),
                    verticalInset,
                    preparedSize,
                    preparedOffsetY
                ),
                preparedSize,
                rotation
            );
        } finally {
            if (TooltipSessionContext.current() == null) {
                lease.close();
            }
        }
    }

    private static int scaled(int value, double scale) {
        return (int) Math.max(
            1,
            Math.min(Integer.MAX_VALUE, Math.round(value * scale))
        );
    }

    private static int scaledSigned(int value, double scale) {
        return (int) Math.max(
            Integer.MIN_VALUE,
            Math.min(Integer.MAX_VALUE, Math.round(value * scale))
        );
    }
}
