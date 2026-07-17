package com.cooobird.datatip.api.util;

import java.util.Locale;

/**
 * 通用颜色解析工具。
 */
public final class ColorParser {
    public static final int WHITE = 0xFFFFFFFF;
    public static final int DEFAULT_TEXT = 0xFFAAAAAA;

    private ColorParser() {
    }

    public static int parse(String colorStr, int defaultValue) {
        if (colorStr == null || colorStr.isEmpty()) {
            return defaultValue;
        }
        if (colorStr.startsWith("#")) {
            return parseHex(colorStr, defaultValue);
        }

        return switch (colorStr.toLowerCase(Locale.ROOT)) {
            case "black" -> 0xFF000000;
            case "dark_blue" -> 0xFF0000AA;
            case "dark_green" -> 0xFF00AA00;
            case "dark_aqua" -> 0xFF00AAAA;
            case "dark_red" -> 0xFFAA0000;
            case "dark_purple" -> 0xFFAA00AA;
            case "gold", "orange" -> 0xFFFFAA00;
            case "gray", "grey" -> 0xFFAAAAAA;
            case "dark_gray", "dark_grey" -> 0xFF555555;
            case "blue", "light_blue" -> 0xFF5555FF;
            case "green", "light_green" -> 0xFF55FF55;
            case "aqua" -> 0xFF55FFFF;
            case "red", "light_red" -> 0xFFFF5555;
            case "light_purple" -> 0xFFFF55FF;
            case "yellow" -> 0xFFFFFF55;
            case "white" -> WHITE;
            case "pink" -> 0xFFFFAACC;
            case "cyan" -> 0xFF00FFFF;
            case "magenta" -> 0xFFFF00FF;
            case "lime" -> 0xFFAAFF00;
            case "brown" -> 0xFFAA5500;
            default -> defaultValue;
        };
    }

    public static String toHex(int argb) {
        return String.format("#%06X", argb & 0xFFFFFF);
    }

    private static int parseHex(String colorStr, int defaultValue) {
        try {
            return (int) Long.parseLong(colorStr.substring(1), 16) | 0xFF000000;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
