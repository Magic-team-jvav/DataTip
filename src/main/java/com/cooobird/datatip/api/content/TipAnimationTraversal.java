package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.node.TipNode;

import java.util.ArrayDeque;
import java.util.List;

/**
 * 使用显式工作栈查询和推进可见动画节点。
 */
public final class TipAnimationTraversal {
    private static final ThreadLocal<TraversalWorkspace> WORKSPACE =
        ThreadLocal.withInitial(TraversalWorkspace::new);

    private TipAnimationTraversal() {
    }

    public static boolean isAnimated(TipContent root) {
        TraversalWorkspace workspace = WORKSPACE.get();
        ArrayDeque<TipContent> work = workspace.acquire();
        try {
            work.push(root);
            while (!work.isEmpty()) {
                TipContent content = work.pop();
                if (content instanceof TipNode node) {
                    if (!node.isShiftCollapsed()) work.push(node.inner());
                    continue;
                }
                if (content instanceof AlignedContent aligned) {
                    work.push(aligned.inner());
                    continue;
                }
                if (content instanceof CarouselContent carousel) {
                    List<TipContent> visible = carousel.visibleFrames();
                    if (visible.size() > 1) return true;
                    for (TipContent frame : visible) work.push(frame);
                    continue;
                }
                if (content instanceof ContainerContent container) {
                    for (TipContent child : container.children()) {
                        work.push(child);
                    }
                    continue;
                }
                if (content.isAnimated()) return true;
            }
            return false;
        } finally {
            workspace.release(work);
        }
    }

    public static void tick(TipContent root, int tickCount) {
        TraversalWorkspace workspace = WORKSPACE.get();
        ArrayDeque<TipContent> work = workspace.acquire();
        try {
            work.push(root);
            while (!work.isEmpty()) {
                TipContent content = work.pop();
                if (content instanceof TipNode node) {
                    if (!node.isShiftCollapsed()) work.push(node.inner());
                    continue;
                }
                if (content instanceof AlignedContent aligned) {
                    work.push(aligned.inner());
                    continue;
                }
                if (content instanceof CarouselContent carousel) {
                    carousel.enqueueTickTargets(tickCount, work);
                    continue;
                }
                if (content instanceof ContainerContent container) {
                    for (TipContent child : container.children()) {
                        work.push(child);
                    }
                    continue;
                }
                if (content.isAnimated()) content.tick(tickCount);
            }
        } finally {
            workspace.release(work);
        }
    }

    private static final class TraversalWorkspace {
        private final java.util.ArrayList<ArrayDeque<TipContent>> pool =
            new java.util.ArrayList<>();
        private int depth;

        private ArrayDeque<TipContent> acquire() {
            if (depth == pool.size()) {
                pool.add(new ArrayDeque<>());
            }
            ArrayDeque<TipContent> work = pool.get(depth++);
            work.clear();
            return work;
        }

        private void release(ArrayDeque<TipContent> work) {
            work.clear();
            depth--;
        }
    }
}
