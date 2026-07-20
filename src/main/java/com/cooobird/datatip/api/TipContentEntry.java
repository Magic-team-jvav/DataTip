package com.cooobird.datatip.api;

import com.cooobird.datatip.api.condition.ConditionChecker;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Tooltip 内容条目。
 * <p>
 * 一个条目包含实际渲染内容、显示条件和展示位置选项。资源包加载与运行时注册都使用同一条目模型，
 * 这样渲染层不需要关心内容来自 JSON 还是来自代码注册。
 * </p>
 *
 * @param content    Tooltip 内容
 * @param conditions 显示条件
 * @param shift      是否需要按住 Shift 才显示
 * @param prepend    是否插入到原版 tooltip 标题后
 * @param shiftHint  折叠内容存在时替换默认 Shift 提示的内容
 * @param scrollHint 内容溢出时替换默认滚动提示的内容
 */
public record TipContentEntry(
    TipContent content,
    List<ConditionChecker.Condition> conditions,
    boolean shift,
    boolean prepend,
    @Nullable TipContent shiftHint,
    @Nullable TipContent scrollHint
) {
    public TipContentEntry(
        TipContent content,
        List<ConditionChecker.Condition> conditions,
        boolean shift,
        boolean prepend,
        @Nullable TipContent shiftHint,
        @Nullable TipContent scrollHint
    ) {
        this.content = java.util.Objects.requireNonNull(content, "content");
        this.conditions = conditions == null ? List.of() : List.copyOf(conditions);
        this.shift = shift;
        this.prepend = prepend;
        this.shiftHint = shiftHint;
        this.scrollHint = scrollHint;
    }

    public TipContentEntry(
        TipContent content,
        List<ConditionChecker.Condition> conditions,
        boolean shift,
        boolean prepend
    ) {
        this(content, conditions, shift, prepend, null, null);
    }

    public static TipContentEntry of(TipContent content) {
        return new TipContentEntry(content, List.of(), false, false, null, null);
    }

    public static TipContentEntry shifted(TipContent content) {
        return new TipContentEntry(content, List.of(), true, false, null, null);
    }

    public static TipContentEntry prepended(TipContent content) {
        return new TipContentEntry(content, List.of(), false, true, null, null);
    }
}
