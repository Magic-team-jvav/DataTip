package com.cooobird.datatip.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * DataTip 配置类。
 * <p>
 * 定义了 DataTip 模组的所有配置选项。
 * 配置文件位于 {@code config/datatip-common.toml}。
 * </p>
 *
 * <h3>配置选项</h3>
 * <table border="1">
 *   <tr><th>选项</th><th>类型</th><th>默认值</th><th>说明</th></tr>
 *   <tr><td>enabled</td><td>boolean</td><td>true</td><td>启用/禁用 DataTip</td></tr>
 *   <tr><td>default_color</td><td>enum</td><td>GRAY</td><td>默认文本颜色预设</td></tr>
 *   <tr><td>default_color_value</td><td>string</td><td>#FFAAAAAA</td><td>自定义默认文本颜色</td></tr>
 *   <tr><td>default_line_height</td><td>int</td><td>0</td><td>默认行高（0=使用原版字体行高）</td></tr>
 *   <tr><td>max_width</td><td>int</td><td>0</td><td>DataTip 内容宽度覆盖值（0=自然宽度）</td></tr>
 *   <tr><td>shift_hint_color</td><td>enum</td><td>CUSTOM</td><td>Shift 提示颜色预设</td></tr>
 *   <tr><td>shift_hint_color_value</td><td>string</td><td>#FF888888</td><td>自定义 Shift 提示颜色</td></tr>
 *   <tr><td>scroll_hint_color</td><td>enum</td><td>CUSTOM</td><td>滚动提示颜色预设</td></tr>
 *   <tr><td>scroll_hint_color_value</td><td>string</td><td>#FF888888</td><td>自定义滚动提示颜色</td></tr>
 * </table>
 *
 * <h3>配置文件示例</h3>
 * <pre>{@code
 * # DataTip 配置文件
 *
 * # 启用 DataTip
 * enabled = true
 *
 * # 默认文本颜色预设
 * default_color = "GRAY"
 *
 * # 选择 CUSTOM 时使用的自定义颜色
 * default_color_value = "#FFAAAAAA"
 *
 * # 默认行高（0=使用原版字体行高）
 * default_line_height = 0
 *
 * # Tooltip 宽度覆盖值（0=按内容自然宽度）
 * max_width = 0  # 0=按内容自然宽度
 *
 * # Shift 提示颜色
 * shift_hint_color = "CUSTOM"
 * shift_hint_color_value = "#FF888888"
 *
 * # 按住滚动键时显示的滚动提示颜色
 * scroll_hint_color = "CUSTOM"
 * scroll_hint_color_value = "#FF888888"
 * }</pre>
 *
 * @author cooobird
 * @since 1.2.0
 */
public class DatatipConfig {
    private static final int DEFAULT_TEXT_COLOR = 0xFFAAAAAA;
    private static final int DEFAULT_SHIFT_HINT_COLOR = 0xFF888888;
    private static final int DEFAULT_SCROLL_HINT_COLOR = 0xFF888888;

    /**
     * 配置构建器
     */
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    /**
     * 启用/禁用 DataTip。
     * <p>
     * 设置为 false 可禁用所有自定义 tooltip，但保留原版 tooltip。
     * </p>
     */
    public static final ForgeConfigSpec.BooleanValue ENABLED = BUILDER
        .comment("Enable DataTip tooltips. Set to false to disable all custom tooltips.")
        .define("enabled", true);

    /**
     * 默认文本颜色预设。
     * <p>
     * 可选择原版聊天颜色；选择 CUSTOM 时读取 default_color_value。
     * </p>
     */
    public static final ForgeConfigSpec.EnumValue<ColorPreset> DEFAULT_COLOR = BUILDER
        .comment("Default text color preset. Select CUSTOM to use default_color_value.")
        .defineEnum("default_color", ColorPreset.GRAY);

    /**
     * 自定义默认文本颜色。
     */
    public static final ForgeConfigSpec.ConfigValue<String> DEFAULT_COLOR_VALUE = BUILDER
        .comment("Custom default text color. Supports #RRGGBB, #AARRGGBB, 0xAARRGGBB, or signed decimal ARGB.")
        .define("default_color_value", "#FFAAAAAA", DatatipConfig::isValidColorValue);

    /**
     * 默认行高。
     * <p>
     * 文本行与行之间的间距（像素）。0 表示使用原版字体行高。
     * </p>
     */
    public static final ForgeConfigSpec.IntValue DEFAULT_LINE_HEIGHT = BUILDER
        .comment("Default line height in pixels. Set to 0 to use the vanilla font line height.")
        .defineInRange(
            "default_line_height",
            0,
            0,
            Integer.MAX_VALUE
        );

    /**
     * 宽度覆盖值。
     * <p>
     * Tooltip 的宽度覆盖值（像素）。0 表示按内容自然宽度交给原版定位逻辑处理。
     * </p>
     */
    public static final ForgeConfigSpec.IntValue MAX_WIDTH = BUILDER
        .comment("Tooltip width override in pixels. Set to 0 to use the content's natural width.")
        .defineInRange("max_width", 0, 0, Integer.MAX_VALUE);

    /**
     * Shift 提示文字颜色预设。
     * <p>
     * 可选择原版聊天颜色；选择 CUSTOM 时读取 shift_hint_color_value。
     * </p>
     */
    public static final ForgeConfigSpec.EnumValue<ColorPreset> SHIFT_HINT_COLOR = BUILDER
        .comment("Shift hint color preset. Select CUSTOM to use shift_hint_color_value.")
        .defineEnum("shift_hint_color", ColorPreset.CUSTOM);

    /**
     * 自定义 Shift 提示文字颜色。
     */
    public static final ForgeConfigSpec.ConfigValue<String> SHIFT_HINT_COLOR_VALUE = BUILDER
        .comment("Custom Shift hint color. Supports #RRGGBB, #AARRGGBB, 0xAARRGGBB, or signed decimal ARGB.")
        .define("shift_hint_color_value", "#FF888888", DatatipConfig::isValidColorValue);

    /**
     * 滚动提示文字颜色预设。
     * <p>
     * 可选择原版聊天颜色；选择 CUSTOM 时读取 scroll_hint_color_value。
     * </p>
     */
    public static final ForgeConfigSpec.EnumValue<ColorPreset> SCROLL_HINT_COLOR = BUILDER
        .comment("Scroll hint color preset. Select CUSTOM to use scroll_hint_color_value.")
        .defineEnum("scroll_hint_color", ColorPreset.CUSTOM);

    /**
     * 自定义滚动提示文字颜色。
     */
    public static final ForgeConfigSpec.ConfigValue<String> SCROLL_HINT_COLOR_VALUE = BUILDER
        .comment("Custom scroll hint color. Supports #RRGGBB, #AARRGGBB, 0xAARRGGBB, or signed decimal ARGB.")
        .define("scroll_hint_color_value", "#FF888888", DatatipConfig::isValidColorValue);

    /**
     * 获取解析后的默认文本颜色。
     */
    public static int defaultColor() {
        return resolveColor(
            DEFAULT_COLOR,
            DEFAULT_COLOR_VALUE,
            DEFAULT_TEXT_COLOR
        );
    }

    /**
     * 获取解析后的 Shift 提示颜色。
     */
    public static int shiftHintColor() {
        return resolveColor(
            SHIFT_HINT_COLOR,
            SHIFT_HINT_COLOR_VALUE,
            DEFAULT_SHIFT_HINT_COLOR
        );
    }

    /**
     * 获取解析后的滚动提示颜色。
     */
    public static int scrollHintColor() {
        return resolveColor(
            SCROLL_HINT_COLOR,
            SCROLL_HINT_COLOR_VALUE,
            DEFAULT_SCROLL_HINT_COLOR
        );
    }

    private static int resolveColor(
        ForgeConfigSpec.EnumValue<ColorPreset> preset,
        ForgeConfigSpec.ConfigValue<String> customValue,
        int fallback
    ) {
        try {
            ColorPreset selected = preset.get();
            if (selected != ColorPreset.CUSTOM) {
                return selected.argb();
            }
            Integer parsed = parseColorValue(customValue.get());
            return parsed != null ? parsed : fallback;
        } catch (IllegalStateException exception) {
            return fallback;
        }
    }

    private static boolean isValidColorValue(Object value) {
        return value instanceof String string && parseColorValue(string) != null;
    }

    private static Integer parseColorValue(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        if (normalized.isEmpty()) return null;

        String hex = normalized;
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        } else if (hex.startsWith("0x") || hex.startsWith("0X")) {
            hex = hex.substring(2);
        } else {
            try {
                long decimal = Long.parseLong(normalized);
                if (decimal < Integer.MIN_VALUE || decimal > 0xFFFFFFFFL) {
                    return null;
                }
                return (int) decimal;
            } catch (NumberFormatException exception) {
                return null;
            }
        }

        if (hex.length() != 6 && hex.length() != 8) return null;
        try {
            long parsed = Long.parseUnsignedLong(hex, 16);
            return hex.length() == 6
                ? 0xFF000000 | (int) parsed
                : (int) parsed;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * 可供配置界面选择的原版聊天颜色。
     */
    public enum ColorPreset {
        BLACK(0xFF000000),
        DARK_BLUE(0xFF0000AA),
        DARK_GREEN(0xFF00AA00),
        DARK_AQUA(0xFF00AAAA),
        DARK_RED(0xFFAA0000),
        DARK_PURPLE(0xFFAA00AA),
        GOLD(0xFFFFAA00),
        GRAY(0xFFAAAAAA),
        DARK_GRAY(0xFF555555),
        BLUE(0xFF5555FF),
        GREEN(0xFF55FF55),
        AQUA(0xFF55FFFF),
        RED(0xFFFF5555),
        LIGHT_PURPLE(0xFFFF55FF),
        YELLOW(0xFFFFFF55),
        WHITE(0xFFFFFFFF),
        CUSTOM(0);

        private final int argb;

        ColorPreset(int argb) {
            this.argb = argb;
        }

        public int argb() {
            return argb;
        }
    }

    /**
     * 配置规范（用于注册）
     */
    public static final ForgeConfigSpec SPEC = BUILDER.build();
}
