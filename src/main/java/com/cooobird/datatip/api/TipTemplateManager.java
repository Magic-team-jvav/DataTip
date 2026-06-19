package com.cooobird.datatip.api;

import com.google.gson.JsonObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模板管理器。
 * <p>
 * 允许注册和使用可复用的 tooltip 模板，简化常用布局的配置。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 注册模板
 * TipTemplateManager.registerTemplate("item_card", variables -> {
 *     JsonObject json = new JsonObject();
 *     json.addProperty("type", "vbox");
 *     json.addProperty("gap", 4);
 *     JsonArray children = new JsonArray();
 *
 *     // 标题
 *     JsonObject title = new JsonObject();
 *     title.addProperty("type", "text");
 *     title.addProperty("text", variables.getOrDefault("name", "Unknown"));
 *     title.addProperty("color", "#55FFFF");
 *     title.addProperty("bold", true);
 *     children.add(title);
 *
 *     // 描述
 *     JsonObject desc = new JsonObject();
 *     desc.addProperty("type", "text");
 *     desc.addProperty("text", variables.getOrDefault("desc", ""));
 *     desc.addProperty("color", "gray");
 *     children.add(desc);
 *
 *     json.add("children", children);
 *     return json;
 * });
 *
 * // 在 JSON 中使用
 * {
 *   "template": "item_card",
 *   "variables": {
 *     "name": "钻石剑",
 *     "desc": "一把锋利的剑"
 *   }
 * }
 * }</pre>
 *
 * @author cooobird
 * @since 1.2.0
 */
public class TipTemplateManager {

    /**
     * 模板函数接口
     */
    @FunctionalInterface
    public interface TemplateFunction {
        /**
         * 生成 tooltip JSON。
         *
         * @param variables 模板变量
         * @return tooltip JSON
         */
        JsonObject generate(Map<String, String> variables);
    }

    /**
     * 模板注册表
     */
    private static final Map<String, TemplateFunction> TEMPLATES = new ConcurrentHashMap<>();

    /**
     * 注册模板。
     *
     * @param name     模板名
     * @param template 模板函数
     */
    public static void registerTemplate(String name, TemplateFunction template) {
        TEMPLATES.put(name, template);
    }

    /**
     * 获取模板。
     *
     * @param name 模板名
     * @return 模板函数，未找到返回 null
     */
    public static TemplateFunction getTemplate(String name) {
        return TEMPLATES.get(name);
    }

    /**
     * 检查模板是否存在。
     *
     * @param name 模板名
     * @return true 如果模板存在
     */
    public static boolean hasTemplate(String name) {
        return TEMPLATES.containsKey(name);
    }

    /**
     * 获取所有注册的模板名。
     *
     * @return 模板名集合
     */
    public static java.util.Set<String> getTemplateNames() {
        return TEMPLATES.keySet();
    }
}
