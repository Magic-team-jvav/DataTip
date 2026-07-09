package com.cooobird.datatip.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * DataTip 配置。
 *
 * @author cooobird
 * @since 1.2.0
 */
public class DatatipConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    /**
     * 启用或禁用 DataTip。
     */
    public static final ForgeConfigSpec.BooleanValue ENABLED = BUILDER
        .comment("Enable DataTip tooltips. Set to false to disable all custom tooltips.")
        .define("enabled", true);

    /**
     * 默认文本颜色，ARGB 格式。
     */
    public static final ForgeConfigSpec.IntValue DEFAULT_COLOR = BUILDER
        .comment("Default text color (ARGB format).")
        .defineInRange("default_color", 0xFFAAAAAA, Integer.MIN_VALUE, Integer.MAX_VALUE);

    /**
     * 默认文本行高。0 表示跟随原版字体行高。
     */
    public static final ForgeConfigSpec.IntValue DEFAULT_LINE_HEIGHT = BUILDER
        .comment("Default line height in pixels. Set to 0 to use vanilla font line height.")
        .defineInRange("default_line_height", 0, 0, 32);

    /**
     * Tooltip 最大宽度。0 表示不额外限制，交给原版 tooltip 布局处理。
     */
    public static final ForgeConfigSpec.IntValue MAX_WIDTH = BUILDER
        .comment("Maximum tooltip width in pixels. Set to 0 to let vanilla tooltip layout decide.")
        .defineInRange("max_width", 0, 0, 10000);

    /**
     * Shift 提示文字颜色，ARGB 格式。
     */
    public static final ForgeConfigSpec.IntValue SHIFT_HINT_COLOR = BUILDER
        .comment("Shift hint text color (ARGB format).")
        .defineInRange("shift_hint_color", 0xFF888888, Integer.MIN_VALUE, Integer.MAX_VALUE);

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}
