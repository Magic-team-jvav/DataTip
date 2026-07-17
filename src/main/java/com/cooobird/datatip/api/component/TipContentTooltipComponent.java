package com.cooobird.datatip.api.component;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.content.TipAnimationTraversal;
import com.cooobird.datatip.api.layout.PreparedLayout;
import com.cooobird.datatip.api.layout.TipMeasureSpec;
import com.cooobird.datatip.api.layout.TipPrepareContext;
import com.cooobird.datatip.api.layout.TipRect;
import com.cooobird.datatip.api.render.RenderCommandPipeline;
import com.cooobird.datatip.api.session.TooltipHit;
import com.cooobird.datatip.api.session.TooltipInvalidation;
import com.cooobird.datatip.api.session.TooltipScrollState;
import com.cooobird.datatip.api.session.TooltipSessionContext;
import com.cooobird.datatip.client.TooltipSessionRuntime;
import com.cooobird.datatip.config.DatatipConfig;
import com.cooobird.datatip.internal.condition.TipConditionTraversal;
import com.cooobird.datatip.internal.render.PreparedPlanRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Objects;

/**
 * 将 DataTip 内容接入原版 TooltipComponent 调用链。
 * <p>
 * 原版会在一次绘制中多次查询宽高，因此本组件缓存同一个 PreparedLayout，
 * 文字阶段和图片阶段只消费快照，不再重新测量内容。
 * </p>
 */
public final class TipContentTooltipComponent
    implements TooltipComponent, ClientTooltipComponent {
    private final TipContent content;
    private final ItemStack itemStack;
    @Nullable
    private final TooltipHit hit;
    @Nullable
    private final TooltipViewportBudget viewportBudget;
    private final Object scrollKey;

    @Nullable
    private PreparedLayout cachedLayout;
    @Nullable
    private Font cachedFont;
    @Nullable
    private ItemStack cachedStack;
    @Nullable
    private TipMeasureSpec cachedSpec;
    private long cachedLayoutGeneration = -1;
    @Nullable
    private RenderCommandPipeline cachedVisiblePlan;
    private long cachedVisibleLayoutId = -1;
    private int cachedVisibleScrollOffset = Integer.MIN_VALUE;
    private int cachedVisibleWidth = -1;
    private int cachedVisibleHeight = -1;
    private long cachedAnimationLayoutId = -1;
    private boolean cachedAnimated;
    private boolean conditionScanComplete;
    private boolean hasNodeConditions;
    private int cachedConditionTick = Integer.MIN_VALUE;
    private long currentConditionFingerprint;
    private long cachedLayoutConditionFingerprint = Long.MIN_VALUE;

    public TipContentTooltipComponent(TipContent content, @Nullable ItemStack itemStack) {
        this(content, itemStack, null, null, content);
    }

    public TipContentTooltipComponent(
        TipContent content,
        @Nullable ItemStack itemStack,
        @Nullable TooltipHit hit
    ) {
        this(content, itemStack, hit, null, content);
    }

    public TipContentTooltipComponent(
        TipContent content,
        @Nullable ItemStack itemStack,
        @Nullable TooltipHit hit,
        @Nullable TooltipViewportBudget viewportBudget
    ) {
        this(content, itemStack, hit, viewportBudget, content);
    }

    public TipContentTooltipComponent(
        TipContent content,
        @Nullable ItemStack itemStack,
        @Nullable TooltipHit hit,
        @Nullable TooltipViewportBudget viewportBudget,
        Object scrollKey
    ) {
        this.content = Objects.requireNonNull(content, "content");
        this.itemStack = itemStack != null ? itemStack : ItemStack.EMPTY;
        this.hit = hit;
        this.viewportBudget = viewportBudget;
        this.scrollKey = Objects.requireNonNull(scrollKey, "scrollKey");
        if (viewportBudget != null) viewportBudget.register(this);
    }

    public TipContent content() {
        return content;
    }

    public ItemStack itemStack() {
        return itemStack;
    }

    @Nullable
    public TooltipViewportBudget viewportBudget() {
        return viewportBudget;
    }

    @Override
    public int getHeight() {
        Minecraft minecraft = Minecraft.getInstance();
        PreparedLayout prepared = layout(minecraft.font);
        return viewportHeight(prepared);
    }

    @Override
    public int getWidth(Font font) {
        return toMinecraftDimension(layout(font).allocatedBounds().width());
    }

    @Override
    public void renderText(
        Font font,
        int x,
        int y,
        Matrix4f matrix,
        MultiBufferSource.BufferSource bufferSource
    ) {
        // Prepared 文字与图片命令统一在图片阶段执行，才能共享滚动和裁剪。
        layout(font);
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemStack renderStack = normalizedStack();
        PreparedLayout prepared = layout(font);
        TipRenderContext context = new TipRenderContext(
            graphics,
            font,
            minecraft.gui.getGuiTicks(),
            minecraft.getTimer().getGameTimeDeltaPartialTick(true),
            renderStack
        );

        Runnable render = () -> {
            if (isAnimated(prepared)) {
                TipAnimationTraversal.tick(
                    content,
                    minecraft.gui.getGuiTicks()
                );
            }

            int contentHeight = toMinecraftDimension(
                prepared.allocatedBounds().height()
            );
            int viewportHeight = viewportHeight(prepared);
            if (viewportHeight <= 0) return;
            int scrollOffset = 0;
            if (hit != null) {
                TooltipScrollState scroll = TooltipSessionRuntime.updateViewport(
                    hit,
                    scrollKey,
                    contentHeight,
                    viewportHeight
                );
                scrollOffset = scroll.offset();
            }
            int contentWidth = toMinecraftDimension(
                prepared.allocatedBounds().width()
            );
            int shiftedY = saturatedCoordinate(y, -(long) scrollOffset);
            RenderCommandPipeline visiblePlan = visiblePlan(
                prepared,
                scrollOffset,
                contentWidth,
                viewportHeight
            );
            PreparedPlanRenderer.renderPlan(
                visiblePlan,
                context,
                x,
                shiftedY,
                x,
                y,
                contentWidth,
                viewportHeight,
                1.0f,
                true
            );
        };
        if (hit != null) {
            TooltipSessionContext.run(hit.session(), render);
        } else {
            render.run();
        }
    }

    private PreparedLayout layout(Font font) {
        ItemStack stack = normalizedStack();
        TipMeasureSpec spec = measureSpec();
        long conditionFingerprint = conditionFingerprint(stack);
        long layoutGeneration = hit != null
            ? hit.session().generation(TooltipInvalidation.LAYOUT)
            : 0;
        if (cachedLayout == null
            || cachedLayoutGeneration != layoutGeneration
            || cachedFont != font
            || !spec.equals(cachedSpec)
            || cachedStack == null
            || cachedLayoutConditionFingerprint != conditionFingerprint
            || !sameStack(cachedStack, stack)) {
            TipLayoutContext layoutContext = TipLayoutContext.bounded(
                font,
                stack,
                legacyWidthLimit(spec)
            );
            cachedLayout = prepareLayout(
                font,
                spec,
                layoutContext,
                conditionFingerprint
            );
            cachedFont = font;
            cachedStack = stack.copy();
            cachedSpec = spec;
            cachedLayoutGeneration = layoutGeneration;
            cachedLayoutConditionFingerprint = conditionFingerprint;
        }
        return cachedLayout;
    }

    private long conditionFingerprint(ItemStack stack) {
        if (conditionScanComplete && !hasNodeConditions) return 0L;
        int tick = Minecraft.getInstance().gui.getGuiTicks();
        if (conditionScanComplete && cachedConditionTick == tick) {
            return currentConditionFingerprint;
        }
        TipConditionTraversal.State state = hit != null
            ? TooltipSessionContext.call(
            hit.session(),
            () -> TipConditionTraversal.evaluate(content, stack)
        )
            : TipConditionTraversal.evaluate(content, stack);
        conditionScanComplete = true;
        hasNodeConditions = state.hasConditions();
        cachedConditionTick = tick;
        currentConditionFingerprint = state.fingerprint();
        return currentConditionFingerprint;
    }

    private TipMeasureSpec measureSpec() {
        Minecraft minecraft = Minecraft.getInstance();
        long hardWidth = viewportBudget != null
            ? viewportBudget.availableWidth()
            : Math.max(
            1,
            minecraft.getWindow().getGuiScaledWidth()
                - TooltipViewportBudget.BACKGROUND_HORIZONTAL_EXPANSION
        );
        // 宽度按原版可用空间布局；高度保持自然尺寸，最后由共享视口负责滚动。
        long hardHeight = Integer.MAX_VALUE;
        int configuredWidth = DatatipConfig.MAX_WIDTH.get();
        long softWidth = configuredWidth > 0 ? configuredWidth : 0;
        return new TipMeasureSpec(softWidth, hardWidth, hardHeight);
    }

    private ItemStack normalizedStack() {
        return hit != null ? hit.stackSnapshot() : itemStack;
    }

    private static int legacyWidthLimit(TipMeasureSpec spec) {
        long width = spec.softMaxWidth() > 0
            ? Math.min(spec.softMaxWidth(), spec.hardMaxWidth())
            : spec.hardMaxWidth();
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1, width));
    }

    private static int toMinecraftDimension(long dimension) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, dimension));
    }

    private static int saturatedCoordinate(int origin, long offset) {
        long value = com.cooobird.datatip.api.layout.TipMath.add(
            origin,
            offset
        );
        return (int) Math.max(
            Integer.MIN_VALUE,
            Math.min(Integer.MAX_VALUE, value)
        );
    }

    private int viewportHeight(PreparedLayout prepared) {
        if (viewportBudget != null) {
            Font font = cachedFont != null
                ? cachedFont
                : Minecraft.getInstance().font;
            return viewportBudget.heightFor(this, font);
        }
        int contentHeight = toMinecraftDimension(
            prepared.allocatedBounds().height()
        );
        TipMeasureSpec spec = cachedSpec != null ? cachedSpec : measureSpec();
        int hardHeight = toMinecraftDimension(spec.hardMaxHeight());
        return Math.min(contentHeight, Math.max(1, hardHeight));
    }

    int intrinsicHeight(Font font) {
        return toMinecraftDimension(layout(font).allocatedBounds().height());
    }

    int intrinsicWidth(Font font) {
        return toMinecraftDimension(layout(font).allocatedBounds().width());
    }

    private RenderCommandPipeline visiblePlan(
        PreparedLayout prepared,
        int scrollOffset,
        int width,
        int height
    ) {
        if (cachedVisiblePlan == null
            || cachedVisibleLayoutId != prepared.layoutId()
            || cachedVisibleScrollOffset != scrollOffset
            || cachedVisibleWidth != width
            || cachedVisibleHeight != height) {
            cachedVisiblePlan = prepared.renderPlan().visibleWithin(
                new TipRect(0, scrollOffset, width, height)
            );
            cachedVisibleLayoutId = prepared.layoutId();
            cachedVisibleScrollOffset = scrollOffset;
            cachedVisibleWidth = width;
            cachedVisibleHeight = height;
        }
        return cachedVisiblePlan;
    }

    private boolean isAnimated(PreparedLayout prepared) {
        if (cachedAnimationLayoutId != prepared.layoutId()) {
            cachedAnimated = TipAnimationTraversal.isAnimated(content);
            cachedAnimationLayoutId = prepared.layoutId();
        }
        return cachedAnimated;
    }

    private static boolean sameStack(ItemStack first, ItemStack second) {
        return first.getCount() == second.getCount()
            && ItemStack.isSameItemSameComponents(first, second);
    }

    private PreparedLayout prepareLayout(
        Font font,
        TipMeasureSpec spec,
        TipLayoutContext layoutContext,
        long conditionFingerprint
    ) {
        if (hit == null) {
            return content.prepare(new TipPrepareContext(layoutContext, spec));
        }
        LayoutCacheKey key = new LayoutCacheKey(
            content,
            font,
            spec,
            hit.itemFingerprint(),
            conditionFingerprint
        );
        return TooltipSessionContext.call(hit.session(), () -> {
            PreparedLayout prepared = hit.session().cached(
                TooltipInvalidation.LAYOUT,
                key,
                PreparedLayout.class
            );
            if (prepared != null) return prepared;
            PreparedLayout created = content.prepare(
                new TipPrepareContext(layoutContext, spec)
            );
            hit.session().cache(TooltipInvalidation.LAYOUT, key, created);
            return created;
        });
    }

    private static final class LayoutCacheKey {
        private final TipContent content;
        private final Font font;
        private final TipMeasureSpec spec;
        private final com.cooobird.datatip.api.session.ItemStackFingerprint item;
        private final long conditionFingerprint;
        private final int hash;

        private LayoutCacheKey(
            TipContent content,
            Font font,
            TipMeasureSpec spec,
            com.cooobird.datatip.api.session.ItemStackFingerprint item,
            long conditionFingerprint
        ) {
            this.content = content;
            this.font = font;
            this.spec = spec;
            this.item = item;
            this.conditionFingerprint = conditionFingerprint;
            this.hash = 31 * (
                31 * (31 * System.identityHashCode(content)
                    + System.identityHashCode(font))
                    + spec.hashCode()
            ) + 31 * item.hashCode() + Long.hashCode(conditionFingerprint);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof LayoutCacheKey key
                && content == key.content
                && font == key.font
                && spec.equals(key.spec)
                && item.equals(key.item)
                && conditionFingerprint == key.conditionFingerprint;
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
