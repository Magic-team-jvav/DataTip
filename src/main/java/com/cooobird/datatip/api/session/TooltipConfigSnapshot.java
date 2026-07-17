package com.cooobird.datatip.api.session;

/**
 * 会影响解析、布局或提示颜色的配置值快照。
 */
public record TooltipConfigSnapshot(
    int defaultColor,
    int defaultLineHeight,
    int maxWidth,
    int shiftHintColor
) {
}
