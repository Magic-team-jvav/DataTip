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
    ChartType type,
    List<ChartEntry> entries,
    int width,
    int height,
    @Nullable Component title,
    boolean showLabels,
    boolean showValues
) implements TipContent {

    /**
     * 图表类型。
     */
    public enum ChartType {
        BAR,        // 柱状图
        PIE,        // 饼图
        LINE        // 折线图
    }

    /**
     * 图表条目。
     */
    public record ChartEntry(
        String label,
        double value,
        int color
    ) {
    }

    /**
     * 创建柱状图。
     */
    public static ChartContent bar(int width, int height) {
        return new ChartContent(ChartType.BAR, new ArrayList<>(), width, height, null, true, true);
    }

    /**
     * 创建饼图。
     */
    public static ChartContent pie(int size) {
        return new ChartContent(ChartType.PIE, new ArrayList<>(), size, size, null, true, true);
    }

    /**
     * 创建折线图。
     */
    public static ChartContent line(int width, int height) {
        return new ChartContent(ChartType.LINE, new ArrayList<>(), width, height, null, true, true);
    }

    /**
     * 添加数据条目。
     */
    public ChartContent addEntry(String label, double value, int color) {
        entries.add(new ChartEntry(label, value, color));
        return this;
    }

    /**
     * 设置标题。
     */
    public ChartContent title(Component title) {
        return new ChartContent(type, entries, width, height, title, showLabels, showValues);
    }

    @Override
    public int getHeight(int maxWidth) {
        int h = height;
        if (title != null) h += 12;
        if (showLabels) h += 12;
        return h;
    }

    @Override
    public int getWidth(int maxWidth) {
        return width;
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0 || entries.isEmpty()) return;

        // 渲染标题
        if (title != null) {
            context.drawCenteredString(title, x + width / 2, y, 0xFFFFFF);
            y += 14;
        }

        switch (type) {
            case BAR -> renderBarChart(context, x, y);
            case PIE -> renderPieChart(context, x, y);
            case LINE -> renderLineChart(context, x, y);
        }
    }

    /**
     * 渲染柱状图。
     */
    private void renderBarChart(TipRenderContext context, int x, int y) {
        double maxValue = entries.stream().mapToDouble(ChartEntry::value).max().orElse(1);
        int barWidth = Math.max(4, (width - 4) / entries.size() - 2);
        int barX = x;

        for (ChartEntry entry : entries) {
            int barHeight = (int) ((entry.value / maxValue) * height);
            int barY = y + height - barHeight;

            // 绘制柱子
            context.fill(barX, barY, barX + barWidth, y + height, entry.color);

            // 绘制标签
            if (showLabels) {
                int labelWidth = context.getStringWidth(entry.label);
                context.drawString(entry.label, barX + (barWidth - labelWidth) / 2, y + height + 2, 0xAAAAAA);
            }

            // 绘制数值
            if (showValues) {
                String valueStr = String.format("%.1f", entry.value);
                int valueWidth = context.getStringWidth(valueStr);
                context.drawString(valueStr, barX + (barWidth - valueWidth) / 2, barY - 10, 0xFFFFFF);
            }

            barX += barWidth + 2;
        }
    }

    /**
     * 渲染饼图。
     */
    private void renderPieChart(TipRenderContext context, int x, int y) {
        double total = entries.stream().mapToDouble(ChartEntry::value).sum();
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int radius = Math.min(width, height) / 2 - 4;

        double startAngle = 0;
        int labelY = y + height + 4;

        for (ChartEntry entry : entries) {
            double angle = (entry.value / total) * 360;

            // 绘制扇形（简化：绘制矩形条）
            int barWidth = (int) ((entry.value / total) * width);
            context.fill(x, y, x + barWidth, y + height, entry.color);

            // 绘制标签
            if (showLabels) {
                context.drawString(entry.label, x, labelY, entry.color);
                labelY += 10;
            }

            x += barWidth;
            startAngle += angle;
        }
    }

    /**
     * 渲染折线图。
     */
    private void renderLineChart(TipRenderContext context, int x, int y) {
        if (entries.size() < 2) return;

        double maxValue = entries.stream().mapToDouble(ChartEntry::value).max().orElse(1);
        double minValue = entries.stream().mapToDouble(ChartEntry::value).min().orElse(0);
        double range = maxValue - minValue;
        if (range == 0) range = 1;

        int stepX = width / (entries.size() - 1);

        // 绘制线条
        for (int i = 0; i < entries.size() - 1; i++) {
            ChartEntry current = entries.get(i);
            ChartEntry next = entries.get(i + 1);

            int x1 = x + i * stepX;
            int y1 = y + height - (int) (((current.value - minValue) / range) * height);
            int x2 = x + (i + 1) * stepX;
            int y2 = y + height - (int) (((next.value - minValue) / range) * height);

            // 绘制线段
            drawLine(context, x1, y1, x2, y2, current.color);

            // 绘制点
            context.fill(x1 - 2, y1 - 2, x1 + 2, y1 + 2, current.color);
        }

        // 绘制最后一个点
        ChartEntry last = entries.get(entries.size() - 1);
        int lastX = x + (entries.size() - 1) * stepX;
        int lastY = y + height - (int) (((last.value - minValue) / range) * height);
        context.fill(lastX - 2, lastY - 2, lastX + 2, lastY + 2, last.color);

        // 绘制标签
        if (showLabels) {
            for (int i = 0; i < entries.size(); i++) {
                ChartEntry entry = entries.get(i);
                int labelX = x + i * stepX;
                context.drawCenteredString(entry.label, labelX, y + height + 2, 0xAAAAAA);
            }
        }
    }

    /**
     * 绘制线段。
     */
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
