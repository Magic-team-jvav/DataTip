package com.cooobird.datatip.internal.text;

import java.util.List;

/**
 * 调色板循环文本的颜色计算。
 */
public final class CyclingTextColor {
    private CyclingTextColor() {
    }

    public static int at(List<Integer> colors, double progress, boolean smooth) {
        if (colors == null || colors.isEmpty()) {
            throw new IllegalArgumentException("Cycling text requires at least one color");
        }
        if (colors.size() == 1) return colors.get(0);

        double wrapped = progress - Math.floor(progress);
        double scaled = wrapped * colors.size();
        int currentIndex = Math.min(colors.size() - 1, (int) Math.floor(scaled));
        int current = colors.get(currentIndex);
        if (!smooth) return current;

        int next = colors.get((currentIndex + 1) % colors.size());
        return interpolate(current, next, scaled - Math.floor(scaled));
    }

    private static int interpolate(int start, int end, double amount) {
        return channel(start, end, amount, 24) << 24
            | channel(start, end, amount, 16) << 16
            | channel(start, end, amount, 8) << 8
            | channel(start, end, amount, 0);
    }

    private static int channel(int start, int end, double amount, int shift) {
        int first = start >>> shift & 0xFF;
        int second = end >>> shift & 0xFF;
        return (int) Math.round(first + (second - first) * amount);
    }
}

