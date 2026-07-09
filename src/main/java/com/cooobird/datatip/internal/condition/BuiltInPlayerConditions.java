package com.cooobird.datatip.internal.condition;

import com.cooobird.datatip.api.condition.ConditionChecker;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * 玩家相关内置条件。
 */
final class BuiltInPlayerConditions {
    private BuiltInPlayerConditions() {
    }

    static void registerTo(Map<String, ConditionChecker.CustomCondition> conditions) {
        conditions.put("holding", (value, stack, player, level) -> checkHolding(value, player));
        conditions.put("sneaking", (value, stack, player, level) -> player.isShiftKeyDown());
        conditions.put("creative", (value, stack, player, level) -> player.isCreative());
        conditions.put("survival", (value, stack, player, level) -> !player.isCreative() && !player.isSpectator());
        conditions.put("health", (value, stack, player, level) -> checkHealth(value, player));
        conditions.put("hunger", (value, stack, player, level) -> checkHunger(value, player));
        conditions.put("experience", (value, stack, player, level) -> checkExperience(value, player));
        conditions.put("level", (value, stack, player, level) -> checkExperience(value, player));
    }

    static Boolean check(ConditionChecker.Condition condition, Player player) {
        return switch (condition.type()) {
            case "holding" -> checkHolding(condition.value(), player);
            case "sneaking" -> player.isShiftKeyDown();
            case "creative" -> player.isCreative();
            case "survival" -> !player.isCreative() && !player.isSpectator();
            case "health" -> checkHealth(condition.value(), player);
            case "hunger" -> checkHunger(condition.value(), player);
            case "experience", "level" -> checkExperience(condition.value(), player);
            default -> null;
        };
    }

    private static boolean checkHolding(Object value, Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        if (value instanceof String itemStr) {
            return isHeld(itemStr, mainHand, offHand);
        } else if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof String str && isHeld(str, mainHand, offHand)) return true;
            }
        }
        return false;
    }

    private static boolean isHeld(String itemStr, ItemStack mainHand, ItemStack offHand) {
        ResourceLocation itemId = ResourceLocation.tryParse(itemStr);
        if (itemId == null) {
            return false;
        }

        Item item = BuiltInRegistries.ITEM.get(itemId);
        return mainHand.is(item) || offHand.is(item);
    }

    private static boolean checkHealth(Object value, Player player) {
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        if (value instanceof Number num) return health >= num.floatValue();
        if (value instanceof String str) {
            if (str.endsWith("%")) {
                Float percent = ConditionNumbers.parseFloatOrNull(str.substring(0, str.length() - 1));
                if (percent == null) return false;
                return (health / maxHealth * 100) >= percent;
            }
            Float threshold = ConditionNumbers.parseFloatOrNull(str);
            return threshold != null && health >= threshold;
        }
        return false;
    }

    private static boolean checkHunger(Object value, Player player) {
        int foodLevel = player.getFoodData().getFoodLevel();
        if (value instanceof Number num) return foodLevel >= num.intValue();
        if (value instanceof String str) {
            Integer threshold = ConditionNumbers.parseIntOrNull(str);
            return threshold != null && foodLevel >= threshold;
        }
        return false;
    }

    private static boolean checkExperience(Object value, Player player) {
        int level = player.experienceLevel;
        if (value instanceof Number num) return level >= num.intValue();
        if (value instanceof String str) {
            Integer threshold = ConditionNumbers.parseIntOrNull(str);
            return threshold != null && level >= threshold;
        }
        return false;
    }
}
