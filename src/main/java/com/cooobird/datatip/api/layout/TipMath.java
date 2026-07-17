package com.cooobird.datatip.api.layout;

/**
 * Tooltip 几何使用的饱和算术。
 */
public final class TipMath {
    private TipMath() {
    }

    public static long add(long left, long right) {
        if (right > 0 && left > Long.MAX_VALUE - right) return Long.MAX_VALUE;
        if (right < 0 && left < Long.MIN_VALUE - right) return Long.MIN_VALUE;
        return left + right;
    }

    public static long subtract(long left, long right) {
        if (right > 0 && left < Long.MIN_VALUE + right) return Long.MIN_VALUE;
        if (right < 0 && left > Long.MAX_VALUE + right) return Long.MAX_VALUE;
        return left - right;
    }

    public static long distance(long high, long low) {
        if (high <= low) return 0;
        long distance = subtract(high, low);
        return distance < 0 ? Long.MAX_VALUE : distance;
    }
}
