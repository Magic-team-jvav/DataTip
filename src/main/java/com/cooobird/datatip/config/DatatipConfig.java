package com.cooobird.datatip.config;

import net.neoforged.neoforge.common.ModConfigSpec;

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
 *   <tr><td>default_color</td><td>int</td><td>0xFFAAAAAA</td><td>默认文本颜色（ARGB）</td></tr>
 *   <tr><td>default_line_height</td><td>int</td><td>0</td><td>默认行高（0=使用原版字体行高）</td></tr>
 *   <tr><td>max_width</td><td>int</td><td>0</td><td>DataTip 内容宽度覆盖值（0=自然宽度）</td></tr>
 * </table>
 *
 * <h3>配置文件示例</h3>
 * <pre>{@code
 * # DataTip 配置文件
 *
 * # 启用 DataTip
 * enabled = true
 *
 * # 默认文本颜色（ARGB 格式）
 * default_color = -5592406  # 0xFFAAAAAA
 *
 * # 默认行高（0=使用原版字体行高）
 * default_line_height = 0
 *
 * # Tooltip 宽度覆盖值（0=按内容自然宽度）
 * max_width = 0  # 0=按内容自然宽度
 * }</pre>
 *
 * @author cooobird
 * @since 1.2.0
 */
public class DatatipConfig {
    /**
     * 配置构建器
     */
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /**
     * 启用/禁用 DataTip。
     * <p>
     * 设置为 false 可禁用所有自定义 tooltip，但保留原版 tooltip。
     * </p>
     */
    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER
        .comment("Enable DataTip tooltips. Set to false to disable all custom tooltips.")
        .define("enabled", true);

    /**
     * 默认文本颜色。
     * <p>
     * 使用 ARGB 格式。例如：
     * <ul>
     *   <li>0xFFFFFFFF = 白色</li>
     *   <li>0xFFFF5555 = 红色</li>
     *   <li>0xFF55FF55 = 绿色</li>
     *   <li>0xFF5555FF = 蓝色</li>
     * </ul>
     * </p>
     */
    public static final ModConfigSpec.IntValue DEFAULT_COLOR = BUILDER
        .comment("Default text color (ARGB format).")
        .defineInRange("default_color", 0xFFAAAAAA, Integer.MIN_VALUE, Integer.MAX_VALUE);

    /**
     * 默认行高。
     * <p>
     * 文本行与行之间的间距（像素）。0 表示使用原版字体行高。
     * </p>
     */
    public static final ModConfigSpec.IntValue DEFAULT_LINE_HEIGHT = BUILDER
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
    public static final ModConfigSpec.IntValue MAX_WIDTH = BUILDER
        .comment("Tooltip width override in pixels. Set to 0 to use the content's natural width.")
        .defineInRange("max_width", 0, 0, Integer.MAX_VALUE);

    /**
     * Shift 提示文字颜色。
     * <p>
     * 按住 Shift 显示详情提示的文字颜色（ARGB 格式）。
     * </p>
     */
    public static final ModConfigSpec.IntValue SHIFT_HINT_COLOR = BUILDER
        .comment("Shift hint text color (ARGB format).")
        .defineInRange("shift_hint_color", 0xFF888888, Integer.MIN_VALUE, Integer.MAX_VALUE);

    /**
     * 配置规范（用于注册）
     */
    public static final ModConfigSpec SPEC = BUILDER.build();
}
