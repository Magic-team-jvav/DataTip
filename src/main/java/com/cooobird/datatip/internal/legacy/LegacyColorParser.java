package com.cooobird.datatip.internal.legacy;

import com.cooobird.datatip.api.util.ColorParser;
import com.cooobird.datatip.config.DatatipConfig;

/**
 * 旧格式颜色解析工具。
 */
final class LegacyColorParser {
    private static final int FALLBACK_COLOR = 0xFFAAAAAA;

    private LegacyColorParser() {
    }

    static int defaultColor() {
        try {
            return DatatipConfig.DEFAULT_COLOR.get();
        } catch (IllegalStateException ignored) {
            return FALLBACK_COLOR;
        }
    }

    static int parse(String colorStr) {
        return ColorParser.parse(colorStr, ColorParser.WHITE);
    }
}
