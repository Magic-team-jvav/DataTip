package com.cooobird.datatip.api.content;

import com.cooobird.datatip.config.DatatipConfig;

/**
 * TextContent 默认配置读取。
 */
public final class TextContentDefaults {
    private static final int FALLBACK_COLOR = 0xFFAAAAAA;
    private static final int FALLBACK_LINE_HEIGHT = 9;

    private TextContentDefaults() {
    }

    public static int color() {
        try {
            return DatatipConfig.defaultColor();
        } catch (IllegalStateException e) {
            return FALLBACK_COLOR;
        }
    }

    public static int lineHeight() {
        try {
            int configuredLineHeight = DatatipConfig.DEFAULT_LINE_HEIGHT.get();
            return configuredLineHeight > 0 ? configuredLineHeight : FALLBACK_LINE_HEIGHT;
        } catch (IllegalStateException e) {
            return FALLBACK_LINE_HEIGHT;
        }
    }
}
