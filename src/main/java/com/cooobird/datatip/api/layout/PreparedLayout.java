package com.cooobird.datatip.api.layout;

import com.cooobird.datatip.api.render.RenderCommandPipeline;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 单次求值后由宽度、高度和所有渲染阶段共同消费的布局快照。
 */
public final class PreparedLayout {
    private static final AtomicLong NEXT_ID = new AtomicLong();

    private final long layoutId;
    private final TipRect naturalBounds;
    private final TipSize preferredSize;
    private final TipRect allocatedBounds;
    private final TipRect visualBounds;
    private final TipRect flowBounds;
    @Nullable
    private final TipRect clipBounds;
    private final OverflowPolicy overflowPolicy;
    private final RenderCommandPipeline renderPlan;

    private PreparedLayout(
        long layoutId,
        TipRect naturalBounds,
        TipSize preferredSize,
        TipRect allocatedBounds,
        TipRect visualBounds,
        TipRect flowBounds,
        @Nullable TipRect clipBounds,
        OverflowPolicy overflowPolicy,
        RenderCommandPipeline renderPlan
    ) {
        this.layoutId = layoutId;
        this.naturalBounds = naturalBounds;
        this.preferredSize = preferredSize;
        this.allocatedBounds = allocatedBounds;
        this.visualBounds = visualBounds;
        this.flowBounds = flowBounds;
        this.clipBounds = clipBounds;
        this.overflowPolicy = overflowPolicy;
        this.renderPlan = renderPlan;
    }

    public static PreparedLayout create(
        TipRect naturalBounds,
        TipSize preferredSize,
        TipRect allocatedBounds,
        TipRect visualBounds,
        @Nullable TipRect clipBounds,
        OverflowPolicy overflowPolicy
    ) {
        return create(
            naturalBounds,
            preferredSize,
            allocatedBounds,
            visualBounds,
            allocatedBounds,
            clipBounds,
            overflowPolicy,
            RenderCommandPipeline.empty()
        );
    }

    public static PreparedLayout create(
        TipRect naturalBounds,
        TipSize preferredSize,
        TipRect allocatedBounds,
        TipRect visualBounds,
        @Nullable TipRect clipBounds,
        OverflowPolicy overflowPolicy,
        RenderCommandPipeline renderPlan
    ) {
        return create(
            naturalBounds,
            preferredSize,
            allocatedBounds,
            visualBounds,
            allocatedBounds,
            clipBounds,
            overflowPolicy,
            renderPlan
        );
    }

    public static PreparedLayout create(
        TipRect naturalBounds,
        TipSize preferredSize,
        TipRect allocatedBounds,
        TipRect visualBounds,
        TipRect flowBounds,
        @Nullable TipRect clipBounds,
        OverflowPolicy overflowPolicy,
        RenderCommandPipeline renderPlan
    ) {
        Objects.requireNonNull(naturalBounds, "naturalBounds");
        Objects.requireNonNull(preferredSize, "preferredSize");
        Objects.requireNonNull(allocatedBounds, "allocatedBounds");
        Objects.requireNonNull(visualBounds, "visualBounds");
        Objects.requireNonNull(flowBounds, "flowBounds");
        Objects.requireNonNull(overflowPolicy, "overflowPolicy");
        Objects.requireNonNull(renderPlan, "renderPlan");
        if (clipBounds == null && !allocatedBounds.contains(visualBounds)) {
            throw new IllegalArgumentException(
                "Allocated bounds must contain visual bounds when no clip is present"
            );
        }
        if (clipBounds != null && !allocatedBounds.contains(clipBounds)) {
            throw new IllegalArgumentException(
                "Clip bounds must be contained by allocated bounds"
            );
        }
        return new PreparedLayout(
            NEXT_ID.incrementAndGet(),
            naturalBounds,
            preferredSize,
            allocatedBounds,
            visualBounds,
            flowBounds,
            clipBounds,
            overflowPolicy,
            renderPlan
        );
    }

    public static PreparedLayout constrain(
        TipSize naturalSize,
        TipMeasureSpec spec,
        OverflowPolicy overflowPolicy
    ) {
        Objects.requireNonNull(naturalSize, "naturalSize");
        Objects.requireNonNull(spec, "spec");
        Objects.requireNonNull(overflowPolicy, "overflowPolicy");

        long preferredWidth = spec.softMaxWidth() > 0
            ? Math.min(naturalSize.width(), spec.softMaxWidth())
            : naturalSize.width();
        TipSize preferred = new TipSize(preferredWidth, naturalSize.height());
        TipRect allocated = new TipRect(
            0,
            0,
            Math.min(preferred.width(), spec.hardMaxWidth()),
            Math.min(preferred.height(), spec.hardMaxHeight())
        );
        TipRect natural = TipRect.ofSize(naturalSize);
        boolean constrained = allocated.width() < natural.width()
            || allocated.height() < natural.height();
        if (constrained && overflowPolicy == OverflowPolicy.NONE) {
            throw new IllegalArgumentException(
                "Overflow policy NONE cannot allocate less than natural size"
            );
        }

        TipRect visual = switch (overflowPolicy) {
            case SCALE_DOWN, WRAP -> allocated;
            case NONE, CLIP -> natural;
        };
        TipRect clip = constrained && overflowPolicy == OverflowPolicy.CLIP ? allocated : null;
        return create(natural, preferred, allocated, visual, clip, overflowPolicy);
    }

    public long layoutId() {
        return layoutId;
    }

    public TipRect naturalBounds() {
        return naturalBounds;
    }

    public TipSize preferredSize() {
        return preferredSize;
    }

    public TipRect allocatedBounds() {
        return allocatedBounds;
    }

    public TipRect visualBounds() {
        return visualBounds;
    }

    /**
     * 父布局用于排列和对齐的逻辑占位框；视觉变换不会改写其锚点。
     */
    public TipRect flowBounds() {
        return flowBounds;
    }

    public Optional<TipRect> clipBounds() {
        return Optional.ofNullable(clipBounds);
    }

    public TipRect effectiveVisualBounds() {
        return clipBounds != null ? visualBounds.intersection(clipBounds) : visualBounds;
    }

    public OverflowPolicy overflowPolicy() {
        return overflowPolicy;
    }

    public RenderCommandPipeline renderPlan() {
        return renderPlan;
    }

    public PreparedLayout withRenderPlan(RenderCommandPipeline replacement) {
        return create(
            naturalBounds,
            preferredSize,
            allocatedBounds,
            visualBounds,
            flowBounds,
            clipBounds,
            overflowPolicy,
            replacement
        );
    }
}
