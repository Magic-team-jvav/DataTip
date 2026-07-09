package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipRenderContext;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
        entries = entries != null ? new ArrayList<>(entries) : new ArrayList<>();
        width = Math.max(1, width);
        height = Math.max(1, height);
    }

    public record ChartEntry(
        String label,      // 标签
        String valueExpr,  // 值表达式
        int color          // 颜色
    ) {
        // 获取解析后的数值
        public double resolveValue(TipRenderContext context) {
            try {
                // 尝试直接解析为数字
                return Double.parseDouble(valueExpr);
            } catch (NumberFormatException e) {
                // 不是数字，尝试解析变量
                String resolved = context.resolveVariables(valueExpr);
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
    public ChartContent addEntry(String label, String valueExpr, int color) {
        entries.add(new ChartEntry(label, valueExpr, color));
        return this;
    }

    // 添加数据条目
    public ChartContent addEntry(String label, double value, int color) {
        entries.add(new ChartEntry(label, String.valueOf(value), color));
        return this;
    }

    // 设置标题
    public ChartContent title(Component title) {
        return new ChartContent(type, entries, width, height, title, showLabels, showValues,
            titleColor, labelColor, valueColor, zeroLineColor);
    }

    @Override
    public int getHeight(int maxWidth) {
        int h = height;
        if (title != null) h += TITLE_HEIGHT;
        if (type == ChartType.BAR && showValues) h += VALUE_LABEL_HEIGHT;
        if (showLabels) {
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
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0 || entries.isEmpty()) return;

        // 限制标题宽度
        int renderWidth = Math.max(1, Math.min(width, maxWidth));

        // 渲染标题
        if (title != null) {
            context.drawCenteredString(title, x + renderWidth / 2, y, titleColor);
            y += TITLE_HEIGHT;
        }

        if (type == ChartType.BAR && showValues) {
            y += VALUE_LABEL_HEIGHT;
        }

        switch (type) {
            case BAR, PIE, LINE -> ChartRenderers.render(this, context, x, y, maxWidth);
        }
    }
}
