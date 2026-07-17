package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;

import java.util.List;

/**
 * 容器内容接口。
 * 可以包含子元素的内容类型。
 * <p>
 * 提供默认的儿童迭代/过滤/动画传播逻辑，
 * {@link VBoxContent} 和 {@link HBoxContent} 继承后只需实现布局算法。
 * </p>
 *
 * @author cooobird
 * @since 1.2.0
 */
public interface ContainerContent
    extends com.cooobird.datatip.api.layout.PreparedContent {

    /**
     * 折叠提示行高度
     */
    int HINT_LINE_HEIGHT = 12;

    /**
     * 获取子元素列表。
     *
     * @return 子元素列表
     */
    List<TipContent> children();

    /**
     * 添加子元素。
     *
     * @param child 要添加的子元素
     */
    void addChild(TipContent child);

    /**
     * 是否有任意子元素处于 shift 折叠状态
     */
    default boolean anyShiftCollapsed() {
        for (TipContent c : children()) {
            if (c.hasContent() && c.isShiftCollapsed()) return true;
        }
        return false;
    }

    /**
     * 遍历有效子元素（跳过无内容项），对每个非折叠项调用 visitor。
     * visitor 第二参数表示是否需要加 gap（非首项）。
     *
     * @return 是否有折叠项、是否有非折叠项
     */
    default ChildIteration forEachVisibleContent(java.util.function.BiConsumer<TipContent, Boolean> visitor) {
        boolean hasCollapsed = false;
        boolean hasPrev = false;
        for (TipContent c : children()) {
            if (!c.hasContent()) continue;
            if (c.isShiftCollapsed()) {
                hasCollapsed = true;
            } else {
                visitor.accept(c, hasPrev);
                hasPrev = true;
            }
        }
        return new ChildIteration(hasCollapsed, hasPrev);
    }

    /**
     * {@link #forEachVisibleContent} 的返回值
     */
    record ChildIteration(boolean hasCollapsed, boolean hasNonCollapsed) {
    }

    @Override
    default boolean isAnimated() {
        return TipAnimationTraversal.isAnimated(this);
    }

    @Override
    default void tick(int tickCount) {
        TipAnimationTraversal.tick(this, tickCount);
    }

}
