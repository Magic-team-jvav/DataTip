package com.cooobird.datatip.api.condition;

import com.cooobird.datatip.internal.condition.BuiltInConditions;
import com.cooobird.datatip.internal.condition.ConditionCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 条件检查器。
 * <p>
 * 根据游戏状态决定是否显示 tooltip。入口保留在此类中，具体的缓存和内置条件判断拆到包内辅助类。
 * </p>
 *
 * @author cooobird
 * @see Condition 条件记录
 * @since 1.2.0
 */
public class ConditionChecker {
    /**
     * 自定义条件注册表。
     */
    private static final Map<String, CustomCondition> CUSTOM_CONDITIONS = new ConcurrentHashMap<>();

    static {
        BuiltInConditions.registerTo(CUSTOM_CONDITIONS);
    }

    /**
     * 自定义条件接口。
     * 其他 mod 可以实现此接口来注册自定义条件。
     */
    @FunctionalInterface
    public interface CustomCondition {
        /**
         * 检查条件是否满足。
         *
         * @param value  条件值
         * @param stack  物品栈
         * @param player 玩家
         * @param level  世界
         * @return true 如果条件满足
         */
        boolean check(Object value, ItemStack stack, Player player, Level level);
    }

    /**
     * 注册自定义条件。
     *
     * @param type      条件类型名
     * @param condition 条件检查实现
     */
    public static void registerCondition(String type, CustomCondition condition) {
        CUSTOM_CONDITIONS.put(type, condition);
    }

    /**
     * 检查所有条件是否满足。
     *
     * @param conditions 条件列表
     * @param stack      物品栈
     * @return true 如果所有条件都满足
     */
    public static boolean checkAll(List<Condition> conditions, ItemStack stack) {
        if (conditions.isEmpty()) return true;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.level;

        if (player == null || level == null) return false;

        for (Condition condition : conditions) {
            if (!check(condition, stack, player, level)) return false;
        }
        return true;
    }

    /**
     * 清空条件缓存。
     */
    public static void clearCache() {
        ConditionCache.clear();
    }

    private static boolean check(Condition condition, ItemStack stack, Player player, Level level) {
        Boolean cached = ConditionCache.get(condition, stack, player, level);
        if (cached != null) return cached;

        boolean result;
        try {
            result = checkCondition(condition, stack, player, level);
        } catch (RuntimeException ignored) {
            result = false;
        }

        ConditionCache.put(condition, stack, player, level, result);
        return result;
    }

    private static boolean checkCondition(Condition condition, ItemStack stack, Player player, Level level) {
        Boolean builtInResult = BuiltInConditions.check(condition, stack, player, level);
        if (builtInResult != null) return builtInResult;

        CustomCondition custom = CUSTOM_CONDITIONS.get(condition.type());
        return custom != null && custom.check(condition.value(), stack, player, level);
    }

    /**
     * 条件记录。
     *
     * @param type  条件类型，如 "dimension"、"health"
     * @param value 条件值
     */
    public record Condition(String type, Object value) {
    }
}
