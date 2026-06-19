package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipRenderContext;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
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

    // 图表类型
    public enum ChartType {
        BAR,   // 柱状图
        PIE,   // 饼图
        LINE   // 折线图
    }

    // 图表条目，支持静态值和变量表达式
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
        if (title != null) h += 12;
        if (showLabels) {
            // 根据图表类型计算标签高度
            switch (type) {
                case BAR, LINE -> h += 12; // 柱状图和折线图只有一行标签
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
        int renderWidth = Math.min(width, maxWidth);

        // 渲染标题
        if (title != null) {
            context.drawCenteredString(title, x + renderWidth / 2, y, titleColor);
            y += 14;
        }

        switch (type) {
            case BAR -> renderBarChart(context, x, y, maxWidth);
            case PIE -> renderPieChart(context, x, y, maxWidth);
            case LINE -> renderLineChart(context, x, y, maxWidth);
        }
    }

    // 渲染柱状图
    private void renderBarChart(TipRenderContext context, int x, int y, int maxWidth) {
        // 限制宽度
        int renderWidth = Math.min(width, maxWidth);

        // 先解析所有值
        double[] values = entries.stream()
            .mapToDouble(e -> e.resolveValue(context))
            .toArray();

        double maxValue = Arrays.stream(values).max().orElse(1);
        double minValue = Arrays.stream(values).min().orElse(0);

        // 处理负值：确保 minValue <= 0
        if (minValue > 0) minValue = 0;

        double range = maxValue - minValue;
        if (range == 0) range = 1;

        // 计算零线位置
        int zeroLineY = y + height - (int) (((0 - minValue) / range) * height);

        int barWidth = Math.max(4, (renderWidth - 4) / entries.size() - 2);
        int barX = x;

        for (int i = 0; i < entries.size(); i++) {
            ChartEntry entry = entries.get(i);
            double value = values[i];

            // 计算柱子位置和高度
            int barHeight = (int) (Math.abs(value) / range * height);
            int barY;
            if (value >= 0) {
                barY = zeroLineY - barHeight;
            } else {
                barY = zeroLineY;
            }

            // 确保柱子在边界内
            barY = Math.max(y, Math.min(y + height, barY));
            barHeight = Math.min(barHeight, y + height - barY);

            // 绘制柱子
            context.fill(barX, barY, barX + barWidth, barY + barHeight, entry.color());

            // 绘制标签
            if (showLabels) {
                int labelWidth = context.getStringWidth(entry.label());
                context.drawString(entry.label(), barX + (barWidth - labelWidth) / 2, y + height + 2, labelColor);
            }

            // 绘制数值
            if (showValues) {
                String valueStr = String.format("%.0f", value);
                int valueWidth = context.getStringWidth(valueStr);
                int valueY = value >= 0 ? barY - 12 : barY + 2;
                context.drawString(valueStr, barX + (barWidth - valueWidth) / 2, valueY, valueColor);
            }

            barX += barWidth + 2;
        }

        // 绘制零线
        if (minValue < 0) {
            context.hLine(x, x + renderWidth, zeroLineY, zeroLineColor);
        }
    }

    // 渲染饼图
    private void renderPieChart(TipRenderContext context, int x, int y, int maxWidth) {
        // 先解析所有值
        double[] values = entries.stream()
            .mapToDouble(e -> Math.abs(e.resolveValue(context)))
            .toArray();
        double total = Arrays.stream(values).sum();
        if (total == 0) return;

        // 限制尺寸不超过 maxWidth
        int renderWidth = Math.min(width, maxWidth);
        int renderHeight = Math.min(height, maxWidth);

        int centerX = x + renderWidth / 2;
        int centerY = y + renderHeight / 2;
        int radius = Math.min(renderWidth, renderHeight) / 2 - 4;

        double startAngle = -90; // 从顶部开始
        int labelY = y + renderHeight + 4;

        for (int i = 0; i < entries.size(); i++) {
            ChartEntry entry = entries.get(i);
            double value = values[i];
            double sweepAngle = (value / total) * 360;
            double endAngle = startAngle + sweepAngle;

            // 绘制扇形
            for (int angle = (int) startAngle; angle < endAngle; angle++) {
                double rad = Math.toRadians(angle);
                double nextRad = Math.toRadians(angle + 1);

                // 扇形的三角形顶点
                int x1 = centerX + (int) (radius * Math.cos(rad));
                int y1 = centerY + (int) (radius * Math.sin(rad));
                int x2 = centerX + (int) (radius * Math.cos(nextRad));
                int y2 = centerY + (int) (radius * Math.sin(nextRad));

                // 绘制三角形
                drawTriangle(context, centerX, centerY, x1, y1, x2, y2, entry.color());
            }

            // 绘制标签
            if (showLabels) {
                context.drawString(entry.label(), x, labelY, entry.color());
                labelY += 10;
            }

            startAngle = endAngle;
        }
    }

    // 绘制填充三角形
    private void drawTriangle(TipRenderContext context, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        // 计算边界框
        int minX = Math.min(x1, Math.min(x2, x3));
        int maxX = Math.max(x1, Math.max(x2, x3));
        int minY = Math.min(y1, Math.min(y2, y3));
        int maxY = Math.max(y1, Math.max(y2, y3));

        // 遍历边界框内的每个像素
        for (int px = minX; px <= maxX; px++) {
            for (int py = minY; py <= maxY; py++) {
                // 使用重心坐标判断点是否在三角形内
                if (isPointInTriangle(px, py, x1, y1, x2, y2, x3, y3)) {
                    context.fill(px, py, px + 1, py + 1, color);
                }
            }
        }
    }

    // 判断点是否在三角形内
    private boolean isPointInTriangle(int px, int py, int x1, int y1, int x2, int y2, int x3, int y3) {
        double d1 = sign(px, py, x1, y1, x2, y2);
        double d2 = sign(px, py, x2, y2, x3, y3);
        double d3 = sign(px, py, x3, y3, x1, y1);

        boolean hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(hasNeg && hasPos);
    }

    private double sign(int px, int py, int x1, int y1, int x2, int y2) {
        return (px - x2) * (y1 - y2) - (x1 - x2) * (py - y2);
    }

    // 渲染折线图
    private void renderLineChart(TipRenderContext context, int x, int y, int maxWidth) {
        if (entries.size() < 2) return;

        // 限制宽度
        int renderWidth = Math.min(width, maxWidth);

        // 先解析所有值
        double[] values = entries.stream()
            .mapToDouble(e -> e.resolveValue(context))
            .toArray();
        double maxValue = Arrays.stream(values).max().orElse(1);
        double minValue = Arrays.stream(values).min().orElse(0);

        // 处理负值：确保 minValue <= 0
        if (minValue > 0) minValue = 0;

        double range = maxValue - minValue;
        if (range == 0) range = 1;

        int stepX = renderWidth / (entries.size() - 1);

        // 绘制线条
        for (int i = 0; i < entries.size() - 1; i++) {
            ChartEntry current = entries.get(i);
            ChartEntry next = entries.get(i + 1);
            double currentValue = values[i];
            double nextValue = values[i + 1];

            int x1 = x + i * stepX;
            int y1 = y + height - (int) (((currentValue - minValue) / range) * height);
            int x2 = x + (i + 1) * stepX;
            int y2 = y + height - (int) (((nextValue - minValue) / range) * height);

            // 绘制线段
            drawLine(context, x1, y1, x2, y2, current.color());

            // 绘制点
            context.fill(x1 - 2, y1 - 2, x1 + 2, y1 + 2, current.color());
        }

        // 绘制最后一个点
        ChartEntry last = entries.get(entries.size() - 1);
        double lastValue = values[entries.size() - 1];
        int lastX = x + (entries.size() - 1) * stepX;
        int lastY = y + height - (int) (((lastValue - minValue) / range) * height);
        context.fill(lastX - 2, lastY - 2, lastX + 2, lastY + 2, last.color());

        // 绘制标签
        if (showLabels) {
            for (int i = 0; i < entries.size(); i++) {
                ChartEntry entry = entries.get(i);
                int labelX = x + i * stepX;
                context.drawCenteredString(entry.label(), labelX, y + height + 2, labelColor);
            }
        }
    }

    // 绘制线段
    private void drawLine(TipRenderContext context, int x1, int y1, int x2, int y2, int color) {
        // 简单的线段绘制
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            context.fill(x1, y1, x1 + 1, y1 + 1, color);

            if (x1 == x2 && y1 == y2) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }
}
