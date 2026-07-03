package com.cooobird.datatip.api.util;

import com.cooobird.datatip.api.TipEventManager;
import com.cooobird.datatip.api.expression.ExpressionParser;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 变量解析器。
 * <p>
 * 支持在文本中使用变量占位符，如 {durability}、{count} 等。
 * 变量会在渲染时自动替换为实际值。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 简单变量替换
 * String text = "耐久: {durability}/{max_durability}";
 * String resolved = VariableResolver.resolve(text, itemStack);
 * // 结果: "耐久: 250/251"
 *
 * // 表达式求值
 * String expr = "{durability > 100 ? '良好' : '需要修复'}";
 * String result = VariableResolver.resolve(expr, itemStack);
 * // 结果: "良好"（当耐久 > 100 时）
 * }</pre>
 *
 * <h3>内置变量</h3>
 * <table border="1">
 *   <tr><th>变量名</th><th>说明</th><th>示例值</th></tr>
 *   <tr><td>durability</td><td>当前耐久</td><td>250</td></tr>
 *   <tr><td>max_durability</td><td>最大耐久</td><td>251</td></tr>
 *   <tr><td>damage</td><td>已损坏值</td><td>1</td></tr>
 *   <tr><td>durability_percent</td><td>耐久百分比</td><td>99</td></tr>
 *   <tr><td>count</td><td>物品数量</td><td>64</td></tr>
 *   <tr><td>item_name</td><td>物品名称</td><td>钻石剑</td></tr>
 *   <tr><td>item_id</td><td>物品 ID</td><td>minecraft:diamond_sword</td></tr>
 *   <tr><td>enchantment_count</td><td>附魔数量</td><td>3</td></tr>
 *   <tr><td>is_enchanted</td><td>是否附魔</td><td>true</td></tr>
 *   <tr><td>rarity</td><td>稀有度</td><td>rare</td></tr>
 *   <tr><td>max_stack_size</td><td>最大堆叠数</td><td>64</td></tr>
 *   <tr><td>is_stackable</td><td>是否可堆叠</td><td>true</td></tr>
 *   <tr><td>is_damageable</td><td>是否可损坏</td><td>true</td></tr>
 *   <tr><td>player_health</td><td>玩家生命值</td><td>20</td></tr>
 *   <tr><td>player_max_health</td><td>玩家最大生命值</td><td>20</td></tr>
 *   <tr><td>player_hunger</td><td>玩家饥饿值</td><td>20</td></tr>
 *   <tr><td>player_experience</td><td>玩家经验等级</td><td>30</td></tr>
 *   <tr><td>player_x</td><td>玩家 X 坐标</td><td>100</td></tr>
 *   <tr><td>player_y</td><td>玩家 Y 坐标</td><td>64</td></tr>
 *   <tr><td>player_z</td><td>玩家 Z 坐标</td><td>-200</td></tr>
 *   <tr><td>game_time</td><td>游戏时间</td><td>6000</td></tr>
 *   <tr><td>is_day</td><td>是否白天</td><td>true</td></tr>
 *   <tr><td>is_raining</td><td>是否下雨</td><td>false</td></tr>
 *   <tr><td>is_thundering</td><td>是否雷暴</td><td>false</td></tr>
 *   <tr><td>durability_bar</td><td>耐久条（可视化）</td><td>████████░░░░</td></tr>
 *   <tr><td>health_bar</td><td>生命条（可视化）</td><td>❤❤❤❤❤❤❤❤❤❤♡♡</td></tr>
 * </table>
 *
 * @author cooobird
 * @see ExpressionParser 表达式语法
 * @since 1.2.0
 */
public class VariableResolver {

    /**
     * 内置变量映射（变量名 → 解析函数）
     */
    private static final Map<String, Function<ItemStack, String>> BUILT_IN_VARS = new HashMap<>();

    /**
     * 变量解析缓存
     */
    private static final Map<String, CachedResult> CACHE = new ConcurrentHashMap<>();

    /**
     * 缓存过期时间配置（毫秒）
     * - 物品属性：200ms（不常变化）
     * - 玩家状态：50ms（频繁变化）
     * - 坐标/游戏状态：100ms（中等频率）
     */
    private static final long CACHE_EXPIRY_ITEM = 200;      // 物品属性
    private static final long CACHE_EXPIRY_PLAYER = 50;     // 玩家状态
    private static final long CACHE_EXPIRY_POSITION = 100;  // 坐标/游戏状态

    /**
     * 缓存的解析结果。
     */
    private record CachedResult(String result, long timestamp, long expiryMs) {
        /**
         * 检查缓存是否已过期。
         */
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > expiryMs;
        }
    }

    /**
     * 获取变量的缓存过期时间。
     */
    private static long getCacheExpiry(String varName) {
        if (varName.startsWith("player_") || varName.equals("health_bar")) {
            return CACHE_EXPIRY_PLAYER;
        }
        if (varName.equals("player_x") || varName.equals("player_y") || varName.equals("player_z") ||
            varName.equals("game_time") || varName.equals("is_day") || varName.equals("is_raining") || varName.equals("is_thundering")) {
            return CACHE_EXPIRY_POSITION;
        }
        return CACHE_EXPIRY_ITEM;
    }

    // 注册内置变量
    static {
        // 耐久相关
        registerVariable("durability", stack ->
            String.valueOf(stack.getMaxDamage() - stack.getDamageValue()));
        registerVariable("max_durability", stack ->
            String.valueOf(stack.getMaxDamage()));
        registerVariable("damage", stack ->
            String.valueOf(stack.getDamageValue()));

        // 数量
        registerVariable("count", stack ->
            String.valueOf(stack.getCount()));

        // 物品信息
        registerVariable("item_name", stack ->
            stack.getHoverName().getString());
        registerVariable("item_id", stack ->
            stack.getItem().toString());

        // 耐久百分比
        registerVariable("durability_percent", stack -> {
            if (!stack.isDamageableItem()) return "100";
            int max = stack.getMaxDamage();
            int current = max - stack.getDamageValue();
            return String.valueOf((int) ((current * 100.0) / max));
        });

        // 附魔相关
        registerVariable("enchantment_count", stack -> {
            ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            return String.valueOf(enchants.size());
        });
        registerVariable("is_enchanted", stack ->
            String.valueOf(stack.isEnchanted()));

        // 物品属性
        registerVariable("rarity", stack ->
            stack.getRarity().name().toLowerCase());
        registerVariable("max_stack_size", stack ->
            String.valueOf(stack.getMaxStackSize()));
        registerVariable("is_stackable", stack ->
            String.valueOf(stack.getMaxStackSize() > 1));
        registerVariable("is_damageable", stack ->
            String.valueOf(stack.isDamageableItem()));

        // 玩家相关（需要上下文）
        registerVariable("player_health", stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf((int) player.getHealth()) : "0";
        });
        registerVariable("player_max_health", stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf((int) player.getMaxHealth()) : "0";
        });
        registerVariable("player_hunger", stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf(player.getFoodData().getFoodLevel()) : "0";
        });
        registerVariable("player_experience", stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf(player.experienceLevel) : "0";
        });

        // 游戏状态
        registerVariable("game_time", stack -> {
            var level = Minecraft.getInstance().level;
            return level != null ? String.valueOf(level.getDayTime()) : "0";
        });
        registerVariable("is_day", stack -> {
            var level = Minecraft.getInstance().level;
            if (level == null) return "false";
            long time = level.getDayTime() % 24000;
            return String.valueOf(time >= 0 && time < 12000);
        });
        registerVariable("is_raining", stack -> {
            var level = Minecraft.getInstance().level;
            return level != null ? String.valueOf(level.isRaining()) : "false";
        });
        registerVariable("is_thundering", stack -> {
            var level = Minecraft.getInstance().level;
            return level != null ? String.valueOf(level.isThundering()) : "false";
        });

        // 玩家位置
        registerVariable("player_x", stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf((int) player.getX()) : "0";
        });
        registerVariable("player_y", stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf((int) player.getY()) : "0";
        });
        registerVariable("player_z", stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf((int) player.getZ()) : "0";
        });

        // 格式化显示
        registerVariable("durability_bar", stack -> {
            if (!stack.isDamageableItem()) return "████████████";
            int max = stack.getMaxDamage();
            int current = max - stack.getDamageValue();
            int bars = (int) ((current * 12.0) / max);
            return "█".repeat(bars) + "░".repeat(12 - bars);
        });
        registerVariable("health_bar", stack -> {
            var player = Minecraft.getInstance().player;
            if (player == null) return "░░░░░░░░░░░░";
            float health = player.getHealth();
            float maxHealth = player.getMaxHealth();
            int bars = (int) ((health / maxHealth) * 12);
            return "❤".repeat(bars) + "♡".repeat(12 - bars);
        });
    }

    /**
     * 解析文本中的变量（带缓存）。
     * 支持简单变量 {var} 和表达式 {var > 10 ? 'high' : 'low'}。
     * 支持 NBT 变量 {nbt:path} 读取物品 NBT 数据。
     *
     * @param text  包含变量/表达式的文本
     * @param stack 物品栈
     * @return 替换后的文本
     */
    public static String resolve(String text, ItemStack stack) {
        if (text == null || !text.contains("{")) {
            return text;
        }

        // 生成缓存键
        String cacheKey = text + ":" + stack.hashCode();

        // 检查缓存
        CachedResult cached = CACHE.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.result();
        }

        // 先替换 NBT 变量 {nbt:path}
        String result = resolveNbtVariables(text, stack);

        // 再替换简单变量
        Map<String, String> variables = new HashMap<>();
        for (Map.Entry<String, Function<ItemStack, String>> entry : BUILT_IN_VARS.entrySet()) {
            String varName = entry.getKey();
            String value = entry.getValue().apply(stack);
            variables.put(varName, value != null ? value : "");

            // 替换简单变量 {var}
            String placeholder = "{" + varName + "}";
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, value != null ? value : "");
            }
        }

        // 触发变量解析事件（让 mod 提供自定义变量值）
        result = resolveEventVariables(result, stack);

        // 检查是否有表达式
        if (result.contains("{") && result.contains("}") &&
            (result.contains("?") || result.contains(">") || result.contains("<") ||
                result.contains("==") || result.contains("!="))) {
            result = evaluateExpressions(result, variables, stack);
        }

        // 缓存结果
        long expiry = getCacheExpiry(cacheKey);
        CACHE.put(cacheKey, new CachedResult(result, System.currentTimeMillis(), expiry));

        return result;
    }

    /**
     * 解析组件变量 {component:path}。
     * NeoForge 1.21.1 使用组件系统。
     *
     * @param text  包含变量的文本
     * @param stack 物品栈
     * @return 替换后的文本
     */
    private static String resolveNbtVariables(String text, ItemStack stack) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            if (text.startsWith("{nbt:", i)) {
                int end = text.indexOf('}', i);
                if (end > i) {
                    String path = text.substring(i + 5, end);
                    String value = getComponentValue(stack, path);
                    result.append(value);
                    i = end + 1;
                    continue;
                }
            }
            result.append(text.charAt(i));
            i++;
        }
        return result.toString();
    }

    /**
     * 从物品组件中获取指定路径的值。
     *
     * @param stack 物品栈
     * @param path  组件路径（如 "custom_name", "lore"）
     * @return 组件值，未找到返回空字符串
     */
    private static String getComponentValue(ItemStack stack, String path) {
        return switch (path) {
            case "custom_name" -> {
                var name = stack.get(DataComponents.CUSTOM_NAME);
                yield name != null ? name.getString() : "";
            }
            case "item_name" -> {
                var name = stack.get(DataComponents.ITEM_NAME);
                yield name != null ? name.getString() : "";
            }
            case "lore" -> {
                var lore = stack.get(DataComponents.LORE);
                yield lore != null ? String.join(", ", lore.lines().stream().map(Object::toString).toList()) : "";
            }
            case "damage" -> String.valueOf(stack.getDamageValue());
            case "max_damage" -> String.valueOf(stack.getMaxDamage());
            case "enchantments" -> {
                var enchants = stack.getEnchantments();
                yield enchants.toString();
            }
            default -> "";
        };
    }

    /**
     * 求值文本中的表达式。
     *
     * @param text      包含表达式的文本
     * @param variables 变量映射
     * @param stack     物品栈
     * @return 求值后的文本
     */
    private static String evaluateExpressions(String text, Map<String, String> variables, ItemStack stack) {
        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < text.length()) {
            if (text.charAt(i) == '{') {
                // 找到表达式结束位置
                int end = findClosingBrace(text, i);
                if (end > i) {
                    String expr = text.substring(i + 1, end);

                    // 检查是否是表达式（包含运算符）
                    if (isExpression(expr)) {
                        // 求值表达式
                        Object value = ExpressionParser.evaluate(expr, variables);
                        result.append(value != null ? value.toString() : "");
                    } else {
                        // 简单变量，保留原样（应该已经被替换了）
                        result.append("{").append(expr).append("}");
                    }

                    i = end + 1;
                    continue;
                }
            }
            result.append(text.charAt(i));
            i++;
        }

        return result.toString();
    }

    /**
     * 查找匹配的右括号。
     *
     * @param text  文本
     * @param start 起始位置（左括号位置）
     * @return 右括号位置，未找到返回 -1
     */
    private static int findClosingBrace(String text, int start) {
        int depth = 0;
        for (int i = start; i < text.length(); i++) {
            if (text.charAt(i) == '{') depth++;
            if (text.charAt(i) == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    /**
     * 检查是否是表达式（包含运算符）。
     *
     * @param text 文本
     * @return true 如果是表达式
     */
    private static boolean isExpression(String text) {
        return text.contains("?") ||
            text.contains(">") || text.contains("<") ||
            text.contains("==") || text.contains("!=") ||
            text.contains("&&") || text.contains("||");
    }

    /**
     * 对文本中剩余未解析的 {变量} 触发变量解析事件。
     */
    private static String resolveEventVariables(String text, ItemStack stack) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) == '{') {
                int end = findClosingBrace(text, i);
                if (end > i + 1) {
                    String varName = text.substring(i + 1, end);
                    // 跳过已解析的变量和表达式
                    if (!varName.contains("?") && !varName.contains(">") &&
                        !varName.contains("<") && !varName.contains("nbt:")) {
                        var event = TipEventManager.fireVariableResolve(varName, stack);
                        if (event.isResolved() && event.getValue() != null) {
                            result.append(event.getValue());
                            i = end + 1;
                            continue;
                        }
                    }
                }
            }
            result.append(text.charAt(i));
            i++;
        }
        return result.toString();
    }

    /**
     * 注册自定义变量。
     *
     * @param name     变量名（不含大括号）
     * @param resolver 解析函数（接收 ItemStack，返回字符串）
     */
    public static void registerVariable(String name, Function<ItemStack, String> resolver) {
        BUILT_IN_VARS.put(name, resolver);
    }
}
