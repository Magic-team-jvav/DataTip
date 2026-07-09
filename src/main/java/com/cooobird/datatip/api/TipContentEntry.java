package com.cooobird.datatip.api;

import com.cooobird.datatip.api.condition.ConditionChecker;

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
 */
public record TipContentEntry(
    TipContent content,
    List<ConditionChecker.Condition> conditions,
    boolean shift,
    boolean prepend
) {
    public TipContentEntry {
        conditions = conditions == null ? List.of() : List.copyOf(conditions);
    }

    public static TipContentEntry of(TipContent content) {
        return new TipContentEntry(content, List.of(), false, false);
    }

    public static TipContentEntry shifted(TipContent content) {
        return new TipContentEntry(content, List.of(), true, false);
    }

    public static TipContentEntry prepended(TipContent content) {
        return new TipContentEntry(content, List.of(), false, true);
    }
}
