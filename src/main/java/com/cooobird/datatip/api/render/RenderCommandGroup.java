package com.cooobird.datatip.api.render;

import com.cooobird.datatip.api.layout.TipRect;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * 建立局部 painter-order 的叠放上下文。
 * <p>
 * 子节点只在当前组内按 offsetZ 和源顺序排序，不会越过父级兄弟节点。
 * </p>
 */
public final class RenderCommandGroup implements RenderCommandNode {
    private static final Comparator<RenderCommandNode> PAINT_ORDER =
        Comparator.comparingLong(RenderCommandNode::offsetZ)
            .thenComparingInt(RenderCommandNode::sourceIndex);
    private static final Comparator<RenderCommandNode> FLOW_ORDER =
        Comparator.comparingInt(RenderCommandNode::sourceIndex);

    private final long offsetZ;
    private final int sourceIndex;
    private final long translationX;
    private final long translationY;
    private final RenderTransform transform;
    private final double opacity;
    @Nullable
    private final TipRect clipBounds;
    private final List<RenderCommandNode> children;
    private final int phaseMask;
    private final boolean flowOrder;
    private volatile List<RenderCommand> flattenedCommands;

    private RenderCommandGroup(
        long offsetZ,
        int sourceIndex,
        long translationX,
        long translationY,
        boolean flowOrder,
        List<? extends RenderCommandNode> children
    ) {
        this(
            offsetZ,
            sourceIndex,
            translationX,
            translationY,
            RenderTransform.IDENTITY,
            1.0,
            null,
            flowOrder,
            children
        );
    }

    private RenderCommandGroup(
        long offsetZ,
        int sourceIndex,
        long translationX,
        long translationY,
        RenderTransform transform,
        double opacity,
        @Nullable TipRect clipBounds,
        boolean flowOrder,
        List<? extends RenderCommandNode> children
    ) {
        if (sourceIndex < 0) {
            throw new IllegalArgumentException("Source index must be non-negative");
        }
        this.offsetZ = offsetZ;
        this.sourceIndex = sourceIndex;
        this.translationX = translationX;
        this.translationY = translationY;
        this.transform = Objects.requireNonNull(transform, "transform");
        if (!Double.isFinite(opacity) || opacity < 0.0 || opacity > 1.0) {
            throw new IllegalArgumentException(
                "Group opacity must be between 0 and 1"
            );
        }
        this.opacity = opacity;
        this.clipBounds = clipBounds;
        this.flowOrder = flowOrder;
        ArrayList<RenderCommandNode> ordered = new ArrayList<>(
            Objects.requireNonNull(children, "children")
        );
        ordered.sort(flowOrder ? FLOW_ORDER : PAINT_ORDER);
        this.children = List.copyOf(ordered);
        int phases = 0;
        for (RenderCommandNode child : this.children) {
            phases |= child instanceof RenderCommand command
                ? 1 << command.phase().ordinal()
                : ((RenderCommandGroup) child).phaseMask;
        }
        this.phaseMask = phases;
    }

    public static RenderCommandGroup root(List<? extends RenderCommandNode> children) {
        return new RenderCommandGroup(0, 0, 0, 0, false, children);
    }

    /**
     * 创建只按源顺序执行的普通流文本根节点。
     */
    public static RenderCommandGroup flowRoot(
        List<? extends RenderCommandNode> children
    ) {
        return new RenderCommandGroup(0, 0, 0, 0, true, children);
    }

    public static RenderCommandGroup layer(
        long offsetZ,
        int sourceIndex,
        List<? extends RenderCommandNode> children
    ) {
        return new RenderCommandGroup(
            offsetZ,
            sourceIndex,
            0,
            0,
            false,
            children
        );
    }

    /**
     * 创建带局部坐标平移的叠放层，不复制其后代命令。
     */
    public static RenderCommandGroup translatedLayer(
        long offsetZ,
        int sourceIndex,
        long translationX,
        long translationY,
        List<? extends RenderCommandNode> children
    ) {
        return new RenderCommandGroup(
            offsetZ,
            sourceIndex,
            translationX,
            translationY,
            false,
            children
        );
    }

    /**
     * 创建带局部平移的普通流文本层。
     */
    public static RenderCommandGroup translatedFlowLayer(
        int sourceIndex,
        long translationX,
        long translationY,
        List<? extends RenderCommandNode> children
    ) {
        return new RenderCommandGroup(
            0,
            sourceIndex,
            translationX,
            translationY,
            true,
            children
        );
    }

    /**
     * 创建惰性仿射变换层。变换、透明度与裁剪在遍历叶命令时组合。
     */
    public static RenderCommandGroup transformedLayer(
        long offsetZ,
        int sourceIndex,
        RenderTransform transform,
        double opacity,
        @Nullable TipRect clipBounds,
        List<? extends RenderCommandNode> children
    ) {
        return new RenderCommandGroup(
            offsetZ,
            sourceIndex,
            0,
            0,
            transform,
            opacity,
            clipBounds,
            false,
            children
        );
    }

    public List<RenderCommandNode> children() {
        return children;
    }

    public RenderTransform localTransform() {
        RenderTransform translation = RenderTransform.translation(
            translationX,
            translationY
        );
        return translation.compose(transform);
    }

    public double opacity() {
        return opacity;
    }

    @Nullable
    public TipRect clipBounds() {
        return clipBounds;
    }

    public List<RenderCommand> commands() {
        List<RenderCommand> cached = flattenedCommands;
        if (cached != null) return cached;
        synchronized (this) {
            cached = flattenedCommands;
            if (cached == null) {
                cached = flattenCommands();
                flattenedCommands = cached;
            }
            return cached;
        }
    }

    /**
     * 按最终视觉 AABB 裁剪命令树，同时保留每层局部变换和透明度。
     * 使用显式栈处理深层 JSON，避免递归溢出。
     */
    public RenderCommandGroup visibleWithin(TipRect viewport) {
        Objects.requireNonNull(viewport, "viewport");
        ArrayDeque<PruneFrame> work = new ArrayDeque<>();
        work.push(PruneFrame.root(this));
        RenderCommandGroup result = null;
        while (!work.isEmpty()) {
            PruneFrame frame = work.peek();
            if (frame.nextChild < frame.group.children.size()) {
                RenderCommandNode child = frame.group.children.get(
                    frame.nextChild++
                );
                if (child instanceof RenderCommand command) {
                    if (visible(command, frame.transform, frame.clip, viewport)) {
                        frame.visible.add(command);
                    } else {
                        frame.changed = true;
                    }
                    continue;
                }
                work.push(PruneFrame.child(
                    (RenderCommandGroup) child,
                    frame.transform,
                    frame.clip
                ));
                continue;
            }

            work.pop();
            RenderCommandGroup pruned = frame.changed
                ? frame.group.copyWith(frame.visible)
                : frame.group;
            if (work.isEmpty()) {
                result = pruned;
            } else {
                PruneFrame parent = work.peek();
                if (!pruned.children.isEmpty()) {
                    parent.visible.add(pruned);
                } else {
                    parent.changed = true;
                }
                if (pruned != frame.group) parent.changed = true;
            }
        }
        return Objects.requireNonNull(result, "result");
    }

    private RenderCommandGroup copyWith(
        List<? extends RenderCommandNode> replacement
    ) {
        return new RenderCommandGroup(
            offsetZ,
            sourceIndex,
            translationX,
            translationY,
            transform,
            opacity,
            clipBounds,
            flowOrder,
            replacement
        );
    }

    private static boolean visible(
        RenderCommand command,
        RenderTransform transform,
        @Nullable TipRect inheritedClip,
        TipRect viewport
    ) {
        TipRect bounds = transform.transformBounds(command.bounds());
        TipRect clip = inheritedClip;
        if (command.clipBounds() != null) {
            clip = intersectNullable(
                clip,
                transform.transformBounds(command.clipBounds())
            );
        }
        if (clip != null) bounds = bounds.intersection(clip);
        return bounds.right() > viewport.x()
            && bounds.bottom() > viewport.y()
            && bounds.x() < viewport.right()
            && bounds.y() < viewport.bottom();
    }

    private List<RenderCommand> flattenCommands() {
        ArrayList<RenderCommand> result = new ArrayList<>();
        ArrayDeque<Traversal> work = new ArrayDeque<>();
        work.push(new Traversal(
            this,
            RenderTransform.IDENTITY,
            1.0,
            null
        ));
        while (!work.isEmpty()) {
            Traversal traversal = work.pop();
            RenderCommandNode node = traversal.node();
            if (node instanceof RenderCommand command) {
                result.add(resolve(
                    command,
                    traversal.transform(),
                    traversal.clipBounds()
                ));
                continue;
            }
            RenderCommandGroup group = (RenderCommandGroup) node;
            RenderTransform resolved = traversal.transform().compose(
                group.localTransform()
            );
            TipRect clip = group.clipBounds != null
                ? resolved.transformBounds(group.clipBounds)
                : null;
            clip = intersectNullable(traversal.clipBounds(), clip);
            for (int index = group.children.size() - 1; index >= 0; index--) {
                work.push(new Traversal(
                    group.children.get(index),
                    resolved,
                    traversal.opacity() * group.opacity,
                    clip
                ));
            }
        }
        return List.copyOf(result);
    }

    public boolean containsPhase(RenderPhase phase) {
        Objects.requireNonNull(phase, "phase");
        return (phaseMask & (1 << phase.ordinal())) != 0;
    }

    public boolean containsOnlyPhase(RenderPhase phase) {
        Objects.requireNonNull(phase, "phase");
        int allowed = 1 << phase.ordinal();
        return (phaseMask & ~allowed) == 0;
    }

    @Override
    public long offsetZ() {
        return offsetZ;
    }

    @Override
    public int sourceIndex() {
        return sourceIndex;
    }

    @Override
    public void execute(RenderPass pass, Consumer<String> sink) {
        Objects.requireNonNull(pass, "pass");
        Objects.requireNonNull(sink, "sink");
        for (RenderCommand command : commands()) {
            command.execute(pass, sink);
        }
    }

    private static RenderCommand resolve(
        RenderCommand command,
        RenderTransform transform,
        @Nullable TipRect inheritedClip
    ) {
        TipRect clip = command.clipBounds() != null
            ? transform.transformBounds(command.clipBounds())
            : null;
        clip = intersectNullable(inheritedClip, clip);
        return RenderCommand.positioned(
            command.phase(),
            command.offsetZ(),
            command.sourceIndex(),
            transform.transformBounds(command.bounds()),
            clip,
            command.payload()
        );
    }

    @Nullable
    private static TipRect intersectNullable(
        @Nullable TipRect left,
        @Nullable TipRect right
    ) {
        if (left == null) return right;
        if (right == null) return left;
        return left.intersection(right);
    }

    private record Traversal(
        RenderCommandNode node,
        RenderTransform transform,
        double opacity,
        @Nullable TipRect clipBounds
    ) {
    }

    private static final class PruneFrame {
        private final RenderCommandGroup group;
        private final RenderTransform transform;
        @Nullable
        private final TipRect clip;
        private final ArrayList<RenderCommandNode> visible = new ArrayList<>();
        private int nextChild;
        private boolean changed;

        private PruneFrame(
            RenderCommandGroup group,
            RenderTransform parentTransform,
            @Nullable TipRect inheritedClip
        ) {
            this.group = group;
            this.transform = parentTransform.compose(group.localTransform());
            TipRect groupClip = group.clipBounds != null
                ? this.transform.transformBounds(group.clipBounds)
                : null;
            this.clip = intersectNullable(inheritedClip, groupClip);
        }

        private static PruneFrame root(RenderCommandGroup group) {
            return new PruneFrame(
                group,
                RenderTransform.IDENTITY,
                null
            );
        }

        private static PruneFrame child(
            RenderCommandGroup group,
            RenderTransform parentTransform,
            @Nullable TipRect inheritedClip
        ) {
            return new PruneFrame(group, parentTransform, inheritedClip);
        }
    }
}
