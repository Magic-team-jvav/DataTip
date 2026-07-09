package com.cooobird.datatip.internal.condition;

import com.cooobird.datatip.api.condition.ConditionChecker;
import com.cooobird.datatip.api.condition.ItemComponentMatcher;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * 物品相关内置条件。
 */
final class BuiltInItemConditions {
    private BuiltInItemConditions() {
    }

    static void registerTo(Map<String, ConditionChecker.CustomCondition> conditions) {
        conditions.put("enchanted", (value, stack, player, level) -> checkEnchanted(value, stack));
        conditions.put("damage", (value, stack, player, level) -> checkDamage(value, stack));
        conditions.put("count", (value, stack, player, level) -> checkCount(value, stack));
        conditions.put("component", (value, stack, player, level) -> checkComponent(value, stack));
        conditions.put("custom_data", (value, stack, player, level) -> checkCustomData(value, stack));
        conditions.put("item_tag", (value, stack, player, level) -> checkItemTag(value, stack));
    }

    static Boolean check(ConditionChecker.Condition condition, ItemStack stack) {
        return switch (condition.type()) {
            case "enchanted" -> checkEnchanted(condition.value(), stack);
            case "damage" -> checkDamage(condition.value(), stack);
            case "count" -> checkCount(condition.value(), stack);
            case "component" -> checkComponent(condition.value(), stack);
            case "custom_data" -> checkCustomData(condition.value(), stack);
            case "item_tag" -> checkItemTag(condition.value(), stack);
            default -> null;
        };
    }

    private static boolean checkEnchanted(Object value, ItemStack stack) {
        boolean enchanted = stack.isEnchanted();
        if (value instanceof Boolean bool) return enchanted == bool;
        return enchanted;
    }

    private static boolean checkDamage(Object value, ItemStack stack) {
        if (value instanceof Number num) return stack.getDamageValue() <= num.intValue();
        return false;
    }

    private static boolean checkCount(Object value, ItemStack stack) {
        if (value instanceof Number num) return stack.getCount() >= num.intValue();
        return false;
    }

    private static boolean checkComponent(Object value, ItemStack stack) {
        if (!(value instanceof String path)) return false;
        return ItemComponentMatcher.hasComponent(stack, path);
    }

    @SuppressWarnings("unchecked")
    private static boolean checkCustomData(Object value, ItemStack stack) {
        if (value instanceof String path) {
            return ItemComponentMatcher.hasCustomData(stack, path);
        }
        if (value instanceof Map<?, ?> map) {
            return ItemComponentMatcher.matchesCustomData(stack, (Map<String, Object>) map);
        }
        return false;
    }

    private static boolean checkItemTag(Object value, ItemStack stack) {
        if (!(value instanceof String tagStr)) return false;

        ResourceLocation tagId = ResourceLocation.tryParse(tagStr);
        if (tagId == null) return false;

        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagId);
        return stack.is(tagKey);
    }
}
