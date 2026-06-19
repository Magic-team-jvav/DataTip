package com.cooobird.datatip.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主题管理器。
 * <p>
 * 允许注册和使用自定义主题，统一管理 tooltip 的样式。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 注册主题
 * TipThemeManager.registerTheme("dark", new TipTheme(
 *     0xFF1E1E1E,  // 背景色
 *     0xFF333333,  // 边框色
 *     0xFFFFFFFF,  // 文本色
 *     12,          // 行高
 *     200          // 最大宽度
 * ));
 *
 * // 在 JSON 中使用
 * {
 *   "theme": "dark",
 *   "type": "text",
 *   "text": "Hello World"
 * }
 * }</pre>
 *
 * @author cooobird
 * @since 1.2.0
 */
public class TipThemeManager {

    /**
     * 主题记录。
     */
    public record TipTheme(
        int backgroundColor,  // 背景色（ARGB）
        int borderColor,      // 边框色（ARGB）
        int textColor,        // 默认文本色（ARGB）
        int lineHeight,       // 行高
        int maxWidth          // 最大宽度
    ) {
    }

    /**
     * 主题注册表
     */
    private static final Map<String, TipTheme> THEMES = new ConcurrentHashMap<>();

    /**
     * 注册自定义主题。
     *
     * @param name  主题名
     * @param theme 主题配置
     */
    public static void registerTheme(String name, TipTheme theme) {
        THEMES.put(name, theme);
    }

    /**
     * 获取主题。
     *
     * @param name 主题名
     * @return 主题配置，未找到返回 null
     */
    public static TipTheme getTheme(String name) {
        return THEMES.get(name);
    }

    /**
     * 检查主题是否存在。
     *
     * @param name 主题名
     * @return true 如果主题存在
     */
    public static boolean hasTheme(String name) {
        return THEMES.containsKey(name);
    }

    /**
     * 获取所有注册的主题名。
     *
     * @return 主题名集合
     */
    public static java.util.Set<String> getThemeNames() {
        return THEMES.keySet();
    }
}
