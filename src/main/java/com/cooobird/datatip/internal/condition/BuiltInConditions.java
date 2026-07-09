package com.cooobird.datatip.internal.condition;

import com.cooobird.datatip.api.condition.ConditionChecker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Map;

/**
 * 内置条件判断入口。
 */
public final class BuiltInConditions {
    private BuiltInConditions() {
    }

    public static void registerTo(Map<String, ConditionChecker.CustomCondition> conditions) {
        BuiltInEnvironmentConditions.registerTo(conditions);
        BuiltInPlayerConditions.registerTo(conditions);
        BuiltInItemConditions.registerTo(conditions);
    }

    public static Boolean check(ConditionChecker.Condition condition, ItemStack stack, Player player, Level level) {
        Boolean environment = BuiltInEnvironmentConditions.check(condition, player, level);
        if (environment != null) {
            return environment;
        }

        Boolean playerResult = BuiltInPlayerConditions.check(condition, player);
        if (playerResult != null) {
            return playerResult;
        }

        return BuiltInItemConditions.check(condition, stack);
    }
}
