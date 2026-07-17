package com.cooobird.datatip.internal.condition;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.condition.ConditionChecker;
import com.cooobird.datatip.api.content.AlignedContent;
import com.cooobird.datatip.api.content.CarouselContent;
import com.cooobird.datatip.api.content.ContainerContent;
import com.cooobird.datatip.api.node.TipNode;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayDeque;
import java.util.List;
import java.util.function.Predicate;

/**
 * 迭代计算节点条件的当前可见性指纹，供布局缓存判断是否失效。
 */
public final class TipConditionTraversal {
    private static final long FNV_OFFSET = 0xcbf29ce484222325L;
    private static final long FNV_PRIME = 0x100000001b3L;
    private static final ThreadLocal<ArrayDeque<TipContent>> WORK =
        ThreadLocal.withInitial(ArrayDeque::new);

    private TipConditionTraversal() {
    }

    public static State evaluate(TipContent root, ItemStack stack) {
        return evaluate(
            root,
            conditions -> ConditionChecker.checkAll(conditions, stack)
        );
    }

    static State evaluate(
        TipContent root,
        Predicate<List<ConditionChecker.Condition>> evaluator
    ) {
        ArrayDeque<TipContent> work = WORK.get();
        work.clear();
        work.push(root);
        long fingerprint = FNV_OFFSET;
        int conditionalNodes = 0;
        try {
            while (!work.isEmpty()) {
                TipContent content = work.pop();
                if (content instanceof TipNode node) {
                    List<ConditionChecker.Condition> conditions =
                        node.modifiers().conditions();
                    if (!conditions.isEmpty()) {
                        conditionalNodes++;
                        boolean visible = evaluator.test(conditions);
                        fingerprint ^= visible ? 1L : 0L;
                        fingerprint *= FNV_PRIME;
                        if (!visible) continue;
                    }
                    work.push(node.inner());
                    continue;
                }
                if (content instanceof AlignedContent aligned) {
                    work.push(aligned.inner());
                    continue;
                }
                if (content instanceof CarouselContent carousel) {
                    pushAll(work, carousel.getFrames());
                    continue;
                }
                if (content instanceof ContainerContent container) {
                    pushAll(work, container.children());
                }
            }
            return conditionalNodes == 0
                ? State.NONE
                : new State(true, fingerprint);
        } finally {
            work.clear();
        }
    }

    private static void pushAll(
        ArrayDeque<TipContent> work,
        List<TipContent> children
    ) {
        for (int index = children.size() - 1; index >= 0; index--) {
            work.push(children.get(index));
        }
    }

    public record State(boolean hasConditions, long fingerprint) {
        public static final State NONE = new State(false, 0L);
    }
}
