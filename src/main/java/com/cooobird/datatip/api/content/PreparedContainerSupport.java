package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.layout.*;
import com.cooobird.datatip.api.node.TipNode;
import com.cooobird.datatip.api.render.RenderCommandGroup;
import com.cooobird.datatip.api.render.RenderCommandNode;
import com.cooobird.datatip.api.render.RenderCommandPipeline;
import com.cooobird.datatip.internal.layout.PreparedLeafSupport;
import com.cooobird.datatip.internal.layout.TipModifierLayout;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * VBox、HBox 和 Stack 共用的单次准备与命令树合成器。
 */
final class PreparedContainerSupport {
    private PreparedContainerSupport() {
    }

    static PreparedLayout prepareTree(
        TipContent root,
        TipPrepareContext context
    ) {
        Frame rootFrame = new Frame(root, context);
        ArrayDeque<Frame> work = new ArrayDeque<>();
        work.push(rootFrame);
        while (!work.isEmpty()) {
            Frame frame = work.peek();
            if (!frame.initialized) frame.initialize();
            if (frame.result != null) {
                work.pop();
                continue;
            }
            if (frame.nextChild < frame.children.size()) {
                work.push(frame.children.get(frame.nextChild++));
                continue;
            }
            frame.finish();
            work.pop();
        }
        return rootFrame.result;
    }

    static PreparedLayout prepareVBox(
        VBoxContent container,
        TipPrepareContext context
    ) {
        int padding = effectivePadding(context, container.padding());
        ChildContext childContext = childContext(context, padding);
        ArrayList<ChildLayout> children = prepareChildren(
            container.children(),
            childContext
        );
        long contentWidth = 0;
        for (ChildLayout child : children) {
            contentWidth = Math.max(
                contentWidth,
                flowWidth(child.layout())
            );
        }
        stretchFillDividers(children, contentWidth);

        long y = padding;
        boolean first = true;
        ArrayList<PlacedChild> placed = new ArrayList<>();
        for (ChildLayout child : children) {
            long childHeight = flowHeight(child.layout());
            long childWidth = flowWidth(child.layout());
            if (!first) y = TipMath.add(y, container.gap());
            long x = TipMath.add(
                padding,
                horizontalOffset(
                    child.content(),
                    container.horizontalAlign(),
                    contentWidth,
                    childWidth
                )
            );
            placed.add(new PlacedChild(child, x, y));
            y = TipMath.add(y, childHeight);
            first = false;
        }

        long width = TipMath.add(
            contentWidth,
            (long) padding * 2
        );
        long height = children.isEmpty()
            ? (long) padding * 2
            : TipMath.add(y, padding);
        return compose(width, height, placed);
    }

    static PreparedLayout prepareHBox(
        HBoxContent container,
        TipPrepareContext context
    ) {
        int padding = effectivePadding(context, container.padding());
        ChildContext childContext = childContext(context, padding);
        ArrayList<ChildLayout> children = prepareChildren(
            container.children(),
            childContext
        );
        long rowLimit = childContext.hardWidth();
        ArrayList<Row> rows = new ArrayList<>();
        Row current = new Row();
        for (ChildLayout child : children) {
            long childWidth = flowWidth(child.layout());
            long required = current.children.isEmpty()
                ? childWidth
                : TipMath.add(
                TipMath.add(current.width, container.gap()),
                childWidth
            );
            if (!current.children.isEmpty() && required > rowLimit) {
                rows.add(current);
                current = new Row();
            }
            current.add(child, container.gap());
        }
        if (!current.children.isEmpty()) rows.add(current);

        long y = padding;
        long contentWidth = 0;
        ArrayList<PlacedChild> placed = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Row row = rows.get(rowIndex);
            long x = padding;
            for (int childIndex = 0; childIndex < row.children.size(); childIndex++) {
                ChildLayout child = row.children.get(childIndex);
                long childHeight = flowHeight(child.layout());
                HBoxContent.VerticalAlign alignment = verticalAlignment(
                    child.content(),
                    container.verticalAlign()
                );
                long verticalOffset = switch (alignment) {
                    case TOP -> 0;
                    case CENTER -> (row.height - childHeight) / 2;
                    case BOTTOM -> row.height - childHeight;
                };
                placed.add(new PlacedChild(
                    child,
                    x,
                    TipMath.add(y, verticalOffset)
                ));
                x = TipMath.add(
                    x,
                    TipMath.add(
                        flowWidth(child.layout()),
                        container.gap()
                    )
                );
            }
            contentWidth = Math.max(contentWidth, row.width);
            y = TipMath.add(y, row.height);
            if (rowIndex + 1 < rows.size()) {
                y = TipMath.add(y, container.gap());
            }
        }

        long width = TipMath.add(
            contentWidth,
            (long) padding * 2
        );
        long height = rows.isEmpty()
            ? (long) padding * 2
            : TipMath.add(y, padding);
        return compose(width, height, placed);
    }

    static PreparedLayout prepareStack(
        StackContent container,
        TipPrepareContext context
    ) {
        int padding = effectivePadding(context, container.padding());
        ChildContext childContext = childContext(context, padding);
        ArrayList<ChildLayout> children = prepareChildren(
            container.children(),
            childContext
        );
        long contentWidth = 0;
        long contentHeight = 0;
        for (ChildLayout child : children) {
            contentWidth = Math.max(
                contentWidth,
                flowWidth(child.layout())
            );
            contentHeight = Math.max(
                contentHeight,
                flowHeight(child.layout())
            );
        }
        stretchFillDividers(children, contentWidth);

        ArrayList<PlacedChild> placed = new ArrayList<>();
        for (ChildLayout child : children) {
            long childWidth = flowWidth(child.layout());
            long childHeight = flowHeight(child.layout());
            StackContent.HorizontalAlign horizontal = stackHorizontalAlignment(
                child.content(),
                container.horizontalAlign()
            );
            StackContent.VerticalAlign vertical = stackVerticalAlignment(
                child.content(),
                container.verticalAlign()
            );
            long x = TipMath.add(
                padding,
                switch (horizontal) {
                    case LEFT -> 0;
                    case CENTER -> (contentWidth - childWidth) / 2;
                    case RIGHT -> contentWidth - childWidth;
                }
            );
            long y = TipMath.add(
                padding,
                switch (vertical) {
                    case TOP -> 0;
                    case CENTER -> (contentHeight - childHeight) / 2;
                    case BOTTOM -> contentHeight - childHeight;
                }
            );
            placed.add(new PlacedChild(child, x, y));
        }

        long width = children.isEmpty()
            ? 0
            : TipMath.add(contentWidth, (long) padding * 2);
        long height = children.isEmpty()
            ? 0
            : TipMath.add(contentHeight, (long) padding * 2);
        return compose(width, height, placed);
    }

    private static ArrayList<ChildLayout> prepareChildren(
        List<TipContent> source,
        ChildContext context
    ) {
        ArrayList<ChildLayout> result = new ArrayList<>();
        for (int index = 0; index < source.size(); index++) {
            TipContent child = source.get(index);
            if (!child.hasContent()) continue;
            PreparedLayout prepared = child.prepare(context.prepareContext());
            if (prepared.allocatedBounds().width() == 0
                && prepared.allocatedBounds().height() == 0) {
                continue;
            }
            result.add(new ChildLayout(child, prepared, index));
        }
        return result;
    }

    private static void stretchFillDividers(
        List<ChildLayout> children,
        long contentWidth
    ) {
        int allocatedWidth = (int) Math.min(Integer.MAX_VALUE, contentWidth);
        for (int index = 0; index < children.size(); index++) {
            ChildLayout child = children.get(index);
            DividerContent divider = fillDivider(child.content());
            if (divider == null) continue;
            children.set(index, new ChildLayout(
                child.content(),
                divider.prepareAllocated(allocatedWidth),
                child.sourceIndex()
            ));
        }
    }

    private static DividerContent fillDivider(TipContent content) {
        TipContent current = originalContent(content);
        while (true) {
            if (current instanceof TipNode node) {
                current = node.inner();
                continue;
            }
            if (current instanceof AlignedContent aligned) {
                current = aligned.inner();
                continue;
            }
            break;
        }
        return current instanceof DividerContent divider
            && divider.widthMode() == DividerContent.WidthMode.FILL
            ? divider
            : null;
    }

    private static long horizontalOffset(
        TipContent content,
        VBoxContent.HorizontalAlign inherited,
        long available,
        long childWidth
    ) {
        content = originalContent(content);
        VBoxContent.HorizontalAlign alignment = inherited;
        if (content instanceof TipNode node) {
            alignment = switch (node.modifiers().selfAlignment()) {
                case INHERIT -> inherited;
                case LEFT -> VBoxContent.HorizontalAlign.LEFT;
                case CENTER -> VBoxContent.HorizontalAlign.CENTER;
                case RIGHT -> VBoxContent.HorizontalAlign.RIGHT;
            };
        } else if (content instanceof AlignedContent aligned) {
            alignment = aligned.align();
        }
        return switch (alignment) {
            case LEFT -> 0;
            case CENTER -> (available - childWidth) / 2;
            case RIGHT -> available - childWidth;
        };
    }

    private static HBoxContent.VerticalAlign verticalAlignment(
        TipContent content,
        HBoxContent.VerticalAlign inherited
    ) {
        content = originalContent(content);
        if (!(content instanceof TipNode node)) return inherited;
        return switch (node.modifiers().selfAlignY()) {
            case INHERIT -> inherited;
            case TOP -> HBoxContent.VerticalAlign.TOP;
            case CENTER -> HBoxContent.VerticalAlign.CENTER;
            case BOTTOM -> HBoxContent.VerticalAlign.BOTTOM;
        };
    }

    private static StackContent.HorizontalAlign stackHorizontalAlignment(
        TipContent content,
        StackContent.HorizontalAlign inherited
    ) {
        content = originalContent(content);
        if (!(content instanceof TipNode node)) return inherited;
        return switch (node.modifiers().selfAlignX()) {
            case INHERIT -> inherited;
            case LEFT -> StackContent.HorizontalAlign.LEFT;
            case CENTER -> StackContent.HorizontalAlign.CENTER;
            case RIGHT -> StackContent.HorizontalAlign.RIGHT;
        };
    }

    private static StackContent.VerticalAlign stackVerticalAlignment(
        TipContent content,
        StackContent.VerticalAlign inherited
    ) {
        content = originalContent(content);
        if (!(content instanceof TipNode node)) return inherited;
        return switch (node.modifiers().selfAlignY()) {
            case INHERIT -> inherited;
            case TOP -> StackContent.VerticalAlign.TOP;
            case CENTER -> StackContent.VerticalAlign.CENTER;
            case BOTTOM -> StackContent.VerticalAlign.BOTTOM;
        };
    }

    private static PreparedLayout compose(
        long width,
        long height,
        List<PlacedChild> children
    ) {
        ArrayList<RenderCommandNode> text = new ArrayList<>();
        ArrayList<RenderCommandNode> image = new ArrayList<>();
        TipRect flowBounds = new TipRect(0, 0, width, height);
        TipRect outerBounds = flowBounds;
        for (PlacedChild placed : children) {
            PreparedLayout childLayout = placed.child().layout();
            long childX = TipMath.subtract(
                placed.x(),
                childLayout.flowBounds().x()
            );
            long childY = TipMath.subtract(
                placed.y(),
                childLayout.flowBounds().y()
            );
            TipRect childOuter = translate(
                childLayout.allocatedBounds(),
                childX,
                childY
            );
            outerBounds = outerBounds.union(childOuter);
            if (childLayout.renderPlan().textRoot().containsPhase(
                com.cooobird.datatip.api.render.RenderPhase.ORDINARY_TEXT
            )) {
                text.add(RenderCommandGroup.translatedFlowLayer(
                    placed.child().sourceIndex(),
                    childX,
                    childY,
                    List.of(childLayout.renderPlan().textRoot())
                ));
            }
            image.add(RenderCommandGroup.translatedLayer(
                placed.child().content().offsetZ(),
                placed.child().sourceIndex(),
                childX,
                childY,
                List.of(childLayout.renderPlan().imageRoot())
            ));
        }
        long normalizeX = TipMath.subtract(0, outerBounds.x());
        long normalizeY = TipMath.subtract(0, outerBounds.y());
        TipRect bounds = new TipRect(
            0,
            0,
            outerBounds.width(),
            outerBounds.height()
        );
        TipRect normalizedFlow = translate(
            flowBounds,
            normalizeX,
            normalizeY
        );
        return PreparedLayout.create(
            bounds,
            new TipSize(width, height),
            bounds,
            bounds,
            normalizedFlow,
            null,
            OverflowPolicy.NONE,
            new RenderCommandPipeline(
                RenderCommandGroup.translatedFlowLayer(
                    0,
                    normalizeX,
                    normalizeY,
                    text
                ),
                RenderCommandGroup.translatedLayer(
                    0,
                    0,
                    normalizeX,
                    normalizeY,
                    image
                )
            )
        );
    }

    private static long flowWidth(PreparedLayout layout) {
        return layout.flowBounds().width();
    }

    private static long flowHeight(PreparedLayout layout) {
        return layout.flowBounds().height();
    }

    private static TipRect translate(TipRect bounds, long x, long y) {
        return new TipRect(
            TipMath.add(bounds.x(), x),
            TipMath.add(bounds.y(), y),
            bounds.width(),
            bounds.height()
        );
    }

    private static ChildContext childContext(
        TipPrepareContext parent,
        int padding
    ) {
        TipMeasureSpec spec = parent.measureSpec();
        long horizontalPadding = (long) padding * 2;
        long hardWidth = Math.max(1, spec.hardMaxWidth() - horizontalPadding);
        long softWidth = spec.softMaxWidth() > 0
            ? Math.max(1, spec.softMaxWidth() - horizontalPadding)
            : 0;
        TipMeasureSpec childSpec = new TipMeasureSpec(
            softWidth,
            hardWidth,
            spec.hardMaxHeight()
        );
        TipLayoutContext layout = parent.layoutContext() != null
            ? parent.layoutContext().withMaxWidth(
            (int) Math.min(Integer.MAX_VALUE, hardWidth)
        )
            : null;
        return new ChildContext(
            new TipPrepareContext(layout, childSpec),
            hardWidth
        );
    }

    private static int effectivePadding(
        TipPrepareContext context,
        int requested
    ) {
        long maximum = Math.max(
            0,
            (context.measureSpec().hardMaxWidth() - 1) / 2
        );
        return (int) Math.min(requested, maximum);
    }

    private record ChildContext(
        TipPrepareContext prepareContext,
        long hardWidth
    ) {
    }

    private record ChildLayout(
        TipContent content,
        PreparedLayout layout,
        int sourceIndex
    ) {
    }

    private record PlacedChild(
        ChildLayout child,
        long x,
        long y
    ) {
    }

    private static final class Row {
        private final ArrayList<ChildLayout> children = new ArrayList<>();
        private long width;
        private long height;

        private void add(ChildLayout child, int gap) {
            if (!children.isEmpty()) width = TipMath.add(width, gap);
            children.add(child);
            width = TipMath.add(
                width,
                flowWidth(child.layout())
            );
            height = Math.max(
                height,
                flowHeight(child.layout())
            );
        }
    }

    private static TipContent originalContent(TipContent content) {
        return content instanceof FrozenContent frozen
            ? frozen.original
            : content;
    }

    private enum FrameKind {
        WRAPPER,
        VBOX,
        HBOX,
        STACK,
        CAROUSEL,
        LEAF
    }

    private static final class Frame {
        private final TipContent content;
        private final TipPrepareContext context;
        private final ArrayList<Frame> children = new ArrayList<>();
        private FrameKind kind;
        private boolean initialized;
        private int nextChild;
        private PreparedLayout result;

        private Frame(TipContent content, TipPrepareContext context) {
            this.content = content;
            this.context = context;
        }

        private void initialize() {
            initialized = true;
            if (content instanceof TipNode node) {
                if (!node.modifiers().visible()
                    || (node.modifiers().shift()
                    && !com.cooobird.datatip.event.TipRenderEventHandler
                    .isShowTipDown())) {
                    result = PreparedLeafSupport.empty(0, 0);
                    return;
                }
                kind = FrameKind.WRAPPER;
                children.add(new Frame(
                    node.inner(),
                    TipModifierLayout.childContext(context, node.modifiers())
                ));
                return;
            }
            if (content instanceof AlignedContent aligned) {
                kind = FrameKind.WRAPPER;
                children.add(new Frame(aligned.inner(), context));
                return;
            }
            if (content instanceof VBoxContent vbox) {
                kind = FrameKind.VBOX;
                addContainerChildren(
                    vbox.children(),
                    childContext(
                        context,
                        effectivePadding(context, vbox.padding())
                    ).prepareContext()
                );
                return;
            }
            if (content instanceof HBoxContent hbox) {
                kind = FrameKind.HBOX;
                addContainerChildren(
                    hbox.children(),
                    childContext(
                        context,
                        effectivePadding(context, hbox.padding())
                    ).prepareContext()
                );
                return;
            }
            if (content instanceof StackContent stack) {
                kind = FrameKind.STACK;
                addContainerChildren(
                    stack.children(),
                    childContext(
                        context,
                        effectivePadding(context, stack.padding())
                    ).prepareContext()
                );
                return;
            }
            if (content instanceof CarouselContent carousel) {
                kind = FrameKind.CAROUSEL;
                addContainerChildren(carousel.getFrames(), context);
                return;
            }
            kind = FrameKind.LEAF;
            result = content.prepare(context);
        }

        private void addContainerChildren(
            List<TipContent> contents,
            TipPrepareContext childContext
        ) {
            for (TipContent child : contents) {
                children.add(new Frame(child, childContext));
            }
        }

        private void finish() {
            if (kind == FrameKind.WRAPPER) {
                PreparedLayout inner = children.isEmpty()
                    ? PreparedLeafSupport.empty(0, 0)
                    : children.get(0).result;
                result = content instanceof TipNode node
                    ? TipModifierLayout.apply(inner, node.modifiers())
                    : inner;
                return;
            }
            ArrayList<TipContent> frozen = new ArrayList<>(children.size());
            for (int index = 0; index < children.size(); index++) {
                Frame child = children.get(index);
                frozen.add(new FrozenContent(child.content, child.result));
            }
            result = switch (kind) {
                case VBOX -> {
                    VBoxContent vbox = (VBoxContent) content;
                    yield prepareVBox(
                        new VBoxContent(
                            frozen,
                            vbox.gap(),
                            vbox.padding(),
                            vbox.horizontalAlign()
                        ),
                        context
                    );
                }
                case HBOX -> {
                    HBoxContent hbox = (HBoxContent) content;
                    yield prepareHBox(
                        new HBoxContent(
                            frozen,
                            hbox.gap(),
                            hbox.padding(),
                            hbox.verticalAlign()
                        ),
                        context
                    );
                }
                case STACK -> {
                    StackContent stack = (StackContent) content;
                    yield prepareStack(
                        new StackContent(
                            frozen,
                            stack.padding(),
                            stack.horizontalAlign(),
                            stack.verticalAlign()
                        ),
                        context
                    );
                }
                case CAROUSEL -> {
                    CarouselContent carousel = (CarouselContent) content;
                    yield new CarouselContent(
                        frozen,
                        carousel.getIntervalSeconds(),
                        carousel.getTransition()
                    ).prepareDirect(context);
                }
                case LEAF, WRAPPER -> throw new IllegalStateException(
                    "Invalid prepared container frame state"
                );
            };
        }
    }

    private record FrozenContent(TipContent original, PreparedLayout prepared)
        implements PreparedContent {

        @Override
        public PreparedLayout prepare(TipPrepareContext context) {
            return prepared;
        }

        @Override
        public int getHeight(int maxWidth) {
            return (int) Math.min(
                Integer.MAX_VALUE,
                prepared.allocatedBounds().height()
            );
        }

        @Override
        public int getWidth(int maxWidth) {
            return (int) Math.min(
                Integer.MAX_VALUE,
                prepared.allocatedBounds().width()
            );
        }

        @Override
        public void render(
            TipRenderContext context,
            int x,
            int y,
            int maxWidth,
            float alpha
        ) {
        }

        @Override
        public boolean hasContent() {
            return original.hasContent();
        }

        @Override
        public boolean isShiftCollapsed() {
            return original.isShiftCollapsed();
        }

        @Override
        public long offsetZ() {
            return original.offsetZ();
        }
    }
}
