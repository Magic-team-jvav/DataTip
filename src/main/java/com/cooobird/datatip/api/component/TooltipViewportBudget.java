package com.cooobird.datatip.api.component;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 同一个原版提示框内所有 DataTip 组件共享的物理视口预算。
 */
public final class TooltipViewportBudget {
    private static final int VANILLA_MOUSE_Y_OFFSET = 12;
    private static final int BACKGROUND_BOTTOM_OFFSET = 3;
    static final int SCREEN_EDGE_MARGIN = 2;
    private static final int FIRST_COMPONENT_RENDER_GAP = 2;

    /**
     * 原版背景相对内容矩形向上、向下各外扩四个物理像素。
     */
    static final int BACKGROUND_VERTICAL_EXPANSION = 8;
    /**
     * 原版背景相对内容矩形向左、向右各外扩四个物理像素。
     */
    static final int BACKGROUND_HORIZONTAL_EXPANSION = 8;

    private int availableWidth;
    private int availableHeight;
    private boolean scrollHintVisible;
    private final List<TipContentTooltipComponent> components =
        new ArrayList<>();

    private TooltipViewportBudget(int availableWidth, int availableHeight) {
        this.availableWidth = Math.max(1, availableWidth);
        this.availableHeight = Math.max(0, availableHeight);
    }

    /**
     * 在原版最终组件列表尚未形成时，按当前屏幕创建初始测量预算。
     */
    public static TooltipViewportBudget forScreen(
        int screenWidth,
        int screenHeight
    ) {
        return new TooltipViewportBudget(
            Math.max(1, screenWidth - BACKGROUND_HORIZONTAL_EXPANSION),
            Math.max(
                0,
                screenHeight
                    - BACKGROUND_VERTICAL_EXPANSION
                    - SCREEN_EDGE_MARGIN * 2
            )
        );
    }

    /**
     * 调整传给原版定位器的鼠标锚点，使完整背景仍位于屏幕内部。
     */
    public static int adjustAnchorYForVisibleBackground(
        int anchorY,
        int screenHeight,
        long tooltipHeight
    ) {
        long normalizedHeight = Math.max(0L, tooltipHeight);
        long contentY = (long) anchorY - VANILLA_MOUSE_Y_OFFSET;
        long outerBottom = contentY
            + normalizedHeight
            + BACKGROUND_BOTTOM_OFFSET;
        long maximumOuterBottom = (long) screenHeight
            - 1L
            - SCREEN_EDGE_MARGIN;
        if (outerBottom <= maximumOuterBottom) return anchorY;

        long adjusted = maximumOuterBottom
            - normalizedHeight
            - BACKGROUND_BOTTOM_OFFSET
            + VANILLA_MOUSE_Y_OFFSET;
        return (int) Math.max(
            Integer.MIN_VALUE,
            Math.min(Integer.MAX_VALUE, adjusted)
        );
    }

    /**
     * 原版完成文本换行后，用最终组件列表重新计算真正可用的物理高度。
     */
    public void updateFromFinalComponents(
        int screenHeight,
        List<ClientTooltipComponent> finalComponents,
        Font font
    ) {
        updateHeightFromFinalComponents(screenHeight, finalComponents, font);
    }

    /**
     * 原版完成文本换行后，先按鼠标左右空间确定宽度，再重新测量高度。
     */
    public void updateFromFinalComponents(
        int mouseX,
        int screenWidth,
        int screenHeight,
        List<ClientTooltipComponent> finalComponents,
        Font font,
        int configuredWidth
    ) {
        Objects.requireNonNull(finalComponents, "finalComponents");
        Objects.requireNonNull(font, "font");
        long provisionalWidth = 1L;
        for (ClientTooltipComponent component : finalComponents) {
            provisionalWidth = Math.max(
                provisionalWidth,
                component instanceof TipContentTooltipComponent tip
                    && tip.viewportBudget() == this
                    ? tip.intrinsicWidth(font)
                    : component.getWidth(font)
            );
        }
        int allocatedWidth = allocateWidth(
            mouseX,
            screenWidth,
            provisionalWidth,
            configuredWidth
        );
        // 内容本来就能放下时保留初始硬边界，避免同一帧无意义地准备两次布局。
        if (allocatedWidth < provisionalWidth) {
            availableWidth = allocatedWidth;
        }
        updateHeightFromFinalComponents(screenHeight, finalComponents, font);
    }

    private void updateHeightFromFinalComponents(
        int screenHeight,
        List<ClientTooltipComponent> finalComponents,
        Font font
    ) {
        Objects.requireNonNull(finalComponents, "finalComponents");
        Objects.requireNonNull(font, "font");
        // 原版仅在 Tooltip 只有一个组件时从总高度中减去两个像素。
        long fixedHeight = finalComponents.size() == 1 ? -2L : 0L;
        if (finalComponents.size() > 1) {
            // 原版绘制循环会在首组件后额外下移两像素，但总高度并未包含它。
            fixedHeight += FIRST_COMPONENT_RENDER_GAP;
        }
        for (ClientTooltipComponent component : finalComponents) {
            if (component instanceof TipContentTooltipComponent tip
                && tip.viewportBudget() == this) {
                continue;
            }
            if (component instanceof ScrollHintTooltipComponent hint
                && hint.viewportBudget() == this) {
                continue;
            }
            fixedHeight += component.getHeight();
        }
        long naturalHeight = 0;
        for (TipContentTooltipComponent component : components) {
            naturalHeight += component.intrinsicHeight(font);
        }
        HeightAllocation allocation = allocateHeight(
            screenHeight,
            fixedHeight,
            naturalHeight
        );
        scrollHintVisible = allocation.scrollHintVisible();
        availableHeight = allocation.availableHeight();
    }

    boolean scrollHintVisible() {
        return scrollHintVisible;
    }

    void register(TipContentTooltipComponent component) {
        components.add(Objects.requireNonNull(component, "component"));
    }

    int availableHeight() {
        return availableHeight;
    }

    int availableWidth() {
        return availableWidth;
    }

    int heightFor(TipContentTooltipComponent component, Font font) {
        int index = components.indexOf(component);
        if (index < 0) {
            throw new IllegalStateException(
                "Tooltip component is not registered with its viewport budget"
            );
        }
        int[] naturalHeights = new int[components.size()];
        for (int current = 0; current < components.size(); current++) {
            naturalHeights[current] = components.get(current)
                .intrinsicHeight(font);
        }
        return distribute(availableHeight, naturalHeights)[index];
    }

    static int[] distribute(int availableHeight, int[] naturalHeights) {
        Objects.requireNonNull(naturalHeights, "naturalHeights");
        int[] result = new int[naturalHeights.length];
        long total = 0;
        for (int height : naturalHeights) {
            if (height < 0) {
                throw new IllegalArgumentException(
                    "Natural tooltip height must not be negative"
                );
            }
            total += height;
        }
        int available = Math.max(0, availableHeight);
        if (total <= available) {
            System.arraycopy(
                naturalHeights,
                0,
                result,
                0,
                naturalHeights.length
            );
            return result;
        }
        if (total == 0 || available == 0) return result;

        long prefix = 0;
        long previousBoundary = 0;
        for (int index = 0; index < naturalHeights.length; index++) {
            prefix += naturalHeights[index];
            long boundary = (long) available * prefix / total;
            result[index] = (int) (boundary - previousBoundary);
            previousBoundary = boundary;
        }
        return result;
    }

    /**
     * 按原版内容矩形与背景装饰矩形的尺寸关系计算 DataTip 共享高度。
     */
    static HeightAllocation allocateHeight(
        int screenHeight,
        long fixedHeight,
        long naturalHeight
    ) {
        long physicalContentLimit = Math.max(
            0L,
            (long) screenHeight
                - BACKGROUND_VERTICAL_EXPANSION
                - SCREEN_EDGE_MARGIN * 2L
        );
        long remainingWithoutHint = Math.max(
            0L,
            physicalContentLimit - fixedHeight
        );
        boolean overflow = Math.max(0L, naturalHeight)
            > remainingWithoutHint;
        boolean hintVisible = overflow
            && remainingWithoutHint >= ScrollHintTooltipComponent.HEIGHT;
        long remaining = remainingWithoutHint
            - (hintVisible ? ScrollHintTooltipComponent.HEIGHT : 0L);
        return new HeightAllocation(
            (int) Math.min(Integer.MAX_VALUE, remaining),
            hintVisible
        );
    }

    /**
     * 复用原版 Gather 阶段的鼠标左右空间计算规则。
     */
    static int allocateWidth(
        int mouseX,
        int screenWidth,
        long naturalWidth,
        int configuredWidth
    ) {
        long width = Math.max(1L, naturalWidth);
        if (configuredWidth > 0) {
            width = Math.min(width, configuredWidth);
        }
        long tooltipX = (long) mouseX + 12L;
        if (tooltipX + width + 4L > screenWidth) {
            tooltipX = (long) mouseX - 16L - width;
            if (tooltipX < 4L) {
                long sideLimit = mouseX > screenWidth / 2
                    ? (long) mouseX - 20L
                    : (long) screenWidth - 16L - mouseX;
                width = Math.min(width, Math.max(1L, sideLimit));
            }
        }
        return (int) Math.min(Integer.MAX_VALUE, width);
    }

    record HeightAllocation(int availableHeight, boolean scrollHintVisible) {
    }
}
