package com.cooobird.datatip.api.condition;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * NBT 匹配器。
 * 检查物品的 NBT 数据是否匹配指定条件。
 *
 * @author cooobird
 * @since 1.2.0
 */
public class NbtMatcher {

    /**
     * 检查物品 NBT 是否匹配。
     *
     * @param stack         物品栈
     * @param nbtConditions NBT 条件（键值对）
     * @return 是否匹配
     */
    public static boolean matches(ItemStack stack, @Nullable Map<String, Object> nbtConditions) {
        if (nbtConditions == null || nbtConditions.isEmpty()) {
            return true;
        }

        for (var entry : nbtConditions.entrySet()) {
            String key = entry.getKey();
            Object expected = entry.getValue();

            // 获取实际值
            Object actual = getNbtValue(stack, key);

            // 比较
            if (!matchesValue(actual, expected)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取物品的 NBT 值。
     */
    @Nullable
    private static Object getNbtValue(ItemStack stack, String key) {
        // 处理特殊键
        switch (key) {
            case "Damage":
                return stack.getDamageValue();
            case "Count":
                return stack.getCount();
            case "RepairCost":
                return stack.getOrDefault(DataComponents.REPAIR_COST, 0);
            default:
                // 从自定义数据获取
                CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                if (customData.isEmpty()) {
                    return null;
                }
                CompoundTag tag = customData.copyTag();
                if (tag.contains(key)) {
                    return tag.get(key).getAsString();
                }
                return null;
        }
    }

    /**
     * 比较值是否匹配。
     */
    private static boolean matchesValue(@Nullable Object actual, Object expected) {
        if (actual == null) {
            return expected == null;
        }

        // 字符串比较
        String actualStr = String.valueOf(actual);
        String expectedStr = String.valueOf(expected);

        return actualStr.equals(expectedStr);
    }
}
