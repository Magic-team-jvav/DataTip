package com.cooobird.datatip.api.condition;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.stream.Collectors;

/**
 * DataTip 内置支持的数据组件读取逻辑。
 */
final class BuiltInComponentReaders {
    private BuiltInComponentReaders() {
    }

    @Nullable
    static Boolean hasComponent(ItemStack stack, String componentName) {
        return switch (componentName) {
            case "custom_name" -> stack.has(DataComponents.CUSTOM_NAME);
            case "item_name" -> stack.has(DataComponents.ITEM_NAME);
            case "lore" -> stack.has(DataComponents.LORE);
            case "enchantments" -> stack.has(DataComponents.ENCHANTMENTS);
            case "damage" -> stack.has(DataComponents.DAMAGE);
            case "max_damage" -> stack.isDamageableItem();
            case "repair_cost" -> stack.has(DataComponents.REPAIR_COST);
            case "unbreakable" -> stack.has(DataComponents.UNBREAKABLE);
            case "color" -> stack.has(DataComponents.DYED_COLOR);
            case "trim" -> stack.has(DataComponents.TRIM);
            case "custom_data" -> !stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).isEmpty();
            default -> null;
        };
    }

    @Nullable
    static String getComponentValue(ItemStack stack, String componentName) {
        return switch (componentName) {
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
                yield lore != null
                    ? lore.lines().stream().map(line -> line.getString()).collect(Collectors.joining(", "))
                    : "";
            }
            case "damage" -> String.valueOf(stack.getDamageValue());
            case "max_damage" -> String.valueOf(stack.getMaxDamage());
            case "repair_cost" -> String.valueOf(stack.getOrDefault(DataComponents.REPAIR_COST, 0));
            case "enchantments" -> {
                var enchantments = stack.get(DataComponents.ENCHANTMENTS);
                yield enchantments != null
                    ? enchantments.entrySet().stream()
                    .map(entry -> Enchantment.getFullname(entry.getKey(), entry.getIntValue()).getString())
                    .collect(Collectors.joining(", "))
                    : "";
            }
            case "unbreakable" -> String.valueOf(stack.has(DataComponents.UNBREAKABLE));
            case "color" -> {
                var dyedColor = stack.get(DataComponents.DYED_COLOR);
                yield dyedColor != null ? String.format(Locale.ROOT, "#%06X", dyedColor.rgb()) : "";
            }
            case "trim" -> {
                var trim = stack.get(DataComponents.TRIM);
                yield trim != null
                    ? trim.pattern().value().description().getString() + " / "
                    + trim.material().value().description().getString()
                    : "";
            }
            case "custom_data" -> CustomDataPathReader.fullValue(stack);
            default -> null;
        };
    }
}
