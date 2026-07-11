package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.text.LocalizedText;
import com.cooobird.datatip.api.util.VariableResolver;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 图表内容。
 * 渲染简单的柱状图、饼图等。
 *
 * @author cooobird
 * @since 1.2.0
 */
public record ChartContent(
    ChartType type,           // 图表类型
    List<ChartEntry> entries, // 数据条目
    int width,                // 宽度
    int height,               // 高度
    @Nullable Component title, // 标题
    boolean showLabels,       // 显示标签
    boolean showValues,       // 显示数值
    int titleColor,           // 标题颜色
    int labelColor,           // 标签颜色
    int valueColor,           // 数值颜色
    int zeroLineColor         // 零线颜色
) implements TipContent {
    private static final int TITLE_HEIGHT = 14;
    private static final int VALUE_LABEL_HEIGHT = 12;
    private static final int AXIS_LABEL_HEIGHT = 12;

    // 图表类型
    public enum ChartType {
        BAR,   // 柱状图
        PIE,   // 饼图
        LINE   // 折线图
    }

    // 图表条目，支持静态值和变量表达式
    public ChartContent {
        type = type != null ? type : ChartType.BAR;
        entries = entries != null
            ? entries.stream()
            .filter(Objects::nonNull)
            .limit(ContentBounds.MAX_CHART_ENTRIES)
            .collect(java.util.stream.Collectors.toCollection(ArrayList::new))
            : new ArrayList<>();
        width = type == ChartType.PIE ? ContentBounds.pieDimension(width) : ContentBounds.dimension(width);
        height = type == ChartType.PIE ? ContentBounds.pieDimension(height) : ContentBounds.dimension(height);
    }

    public record ChartEntry(
        LocalizedText labelText, // 标签
        String valueExpr,  // 值表达式
        int color          // 颜色
    ) {
        public ChartEntry {
            labelText = labelText != null ? labelText : LocalizedText.empty();
            valueExpr = valueExpr != null ? valueExpr : "0";
        }

        public ChartEntry(@Nullable String label, @Nullable String valueExpr, int color) {
            this(LocalizedText.literal(label), valueExpr, color);
        }

        public String label() {
            return labelText.getString();
        }

        public Component labelComponent() {
            return labelText.resolve();
        }

        // 获取解析后的数值
        public double resolveValue(TipRenderContext context) {
            try {
                // 尝试直接解析为数字
                return Double.parseDouble(valueExpr);
            } catch (NumberFormatException e) {
                // 不是数字，尝试解析变量
                String resolved = context.resolveVariables(valueExpr);
                if (resolved == null || resolved.isBlank()) return 0;
                try {
                    return Double.parseDouble(resolved);
                } catch (NumberFormatException e2) {
                    // 解析失败，返回 0
                    return 0;
                }
            }
        }
    }

    // 创建柱状图
    public static ChartContent bar(int width, int height) {
        return new ChartContent(ChartType.BAR, new ArrayList<>(), width, height, null, true, true,
            0xFFFFFF, 0xAAAAAA, 0xFFFFFF, 0x888888);
    }

    // 创建饼图
    public static ChartContent pie(int size) {
        return new ChartContent(ChartType.PIE, new ArrayList<>(), size, size, null, true, true,
            0xFFFFFF, 0xAAAAAA, 0xFFFFFF, 0x888888);
    }

    // 创建折线图
    public static ChartContent line(int width, int height) {
        return new ChartContent(ChartType.LINE, new ArrayList<>(), width, height, null, true, true,
            0xFFFFFF, 0xAAAAAA, 0xFFFFFF, 0x888888);
    }

    // 创建带自定义颜色的图表
    public static ChartContent withColors(ChartType type, int width, int height, int titleColor, int labelColor, int valueColor, int zeroLineColor) {
        return new ChartContent(type, new ArrayList<>(), width, height, null, true, true,
            titleColor, labelColor, valueColor, zeroLineColor);
    }

    // 添加数据条目
    public ChartContent addEntry(@Nullable String label, @Nullable String valueExpr, int color) {
        return addEntry(LocalizedText.literal(label), valueExpr, color);
    }

    public ChartContent addEntry(@Nullable LocalizedText label, @Nullable String valueExpr, int color) {
        if (entries.size() < ContentBounds.MAX_CHART_ENTRIES) {
            entries.add(new ChartEntry(label, valueExpr, color));
        }
        return this;
    }

    // 添加数据条目
    public ChartContent addEntry(String label, double value, int color) {
        return addEntry(LocalizedText.literal(label), String.valueOf(value), color);
    }

    // 设置标题
    public ChartContent title(@Nullable Component title) {
        return new ChartContent(type, entries, width, height, title, showLabels, showValues,
            titleColor, labelColor, valueColor, zeroLineColor);
    }

    public ChartContent displayOptions(boolean showLabels, boolean showValues) {
        return new ChartContent(type, entries, width, height, title, showLabels, showValues,
            titleColor, labelColor, valueColor, zeroLineColor);
    }

    @Override
    public int getHeight(int maxWidth) {
        int h = height;
        if (title != null) h += TITLE_HEIGHT;
        if ((type == ChartType.BAR || type == ChartType.LINE) && showValues) h += VALUE_LABEL_HEIGHT;
        if (showLabels || (type == ChartType.PIE && showValues)) {
            // 根据图表类型计算标签高度
            switch (type) {
                case BAR, LINE -> h += AXIS_LABEL_HEIGHT; // 柱状图和折线图只有一行标签
                case PIE -> h += 4 + entries.size() * 12; // 饼图每个条目一行标签
            }
        }
        return h;
    }

    @Override
    public int getWidth(int maxWidth) {
        return Math.min(width, maxWidth);
    }

    @Override
    public int getWidth(TipLayoutContext context) {
        int measuredWidth = width;
        if (title != null) measuredWidth = Math.max(measuredWidth, context.font().width(title));
        if (type == ChartType.PIE && (showLabels || showValues)) {
            for (ChartEntry entry : entries) {
                Component labelText = showLabels ? entry.labelComponent() : Component.empty();
                String valueText = showValues
                    ? displayValueForLayout(entry, context)
                    : "";
                Component legend = composeLegend(labelText, valueText);
                measuredWidth = Math.max(measuredWidth, context.font().width(legend));
            }
        }
        return context.constrainWidth(measuredWidth);
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0 || entries.isEmpty()) return;

        // 限制标题宽度
        int renderWidth = Math.max(1, Math.min(width, maxWidth));
        int renderX = type == ChartType.PIE
            ? x
            : x + Math.max(0, (maxWidth - renderWidth) / 2);

        // 渲染标题
        if (title != null) {
            var titleLines = context.font().split(title, maxWidth);
            if (!titleLines.isEmpty()) {
                var visibleTitle = titleLines.getFirst();
                int titleX = x + (maxWidth - context.font().width(visibleTitle)) / 2;
                context.drawString(visibleTitle, titleX, y, titleColor);
            }
            y += TITLE_HEIGHT;
        }

        if ((type == ChartType.BAR || type == ChartType.LINE) && showValues) {
            y += VALUE_LABEL_HEIGHT;
        }

        switch (type) {
            case BAR, PIE, LINE -> ChartRenderers.render(this, context, renderX, y, renderWidth);
        }
    }

    Component legendText(ChartEntry entry, @Nullable TipRenderContext context) {
        Component labelText = showLabels ? entry.labelComponent() : Component.empty();
        String valueText = showValues
            ? formatValue(context != null ? entry.resolveValue(context) : 0)
            : "";
        return composeLegend(labelText, valueText);
    }

    private static Component composeLegend(Component label, String value) {
        label = label != null ? label : Component.empty();
        value = value != null ? value : "";
        if (!label.getString().isEmpty() && !value.isEmpty()) {
            return Component.empty().append(label).append(": ").append(value);
        }
        return !label.getString().isEmpty() ? label : Component.literal(value);
    }

    static String formatValue(double value) {
        return String.format(java.util.Locale.ROOT, "%.0f", value);
    }

    private static String displayValueForLayout(ChartEntry entry, TipLayoutContext context) {
        String resolved = VariableResolver.resolve(entry.valueExpr(), context.itemStack());
        if (resolved == null || resolved.isBlank()) return "";
        try {
            return formatValue(Double.parseDouble(resolved));
        } catch (NumberFormatException ignored) {
            return resolved;
        }
    }
}
