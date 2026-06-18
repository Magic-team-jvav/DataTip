package com.cooobird.datatip.tooltip;

import com.cooobird.datatip.data.TooltipLine;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class TooltipRenderer {

    public static Component render(TooltipLine line, ItemStack stack, @Nullable TextColor fallbackColor) {
        String text = line.text() != null ? line.text() : "";
        text = expandVars(text, stack);

        TextColor c = parseColor(line.color());
        if (c == null) c = fallbackColor;
        if (c == null) c = TextColor.fromRgb(0xAAAAAA);
        Style style = applyFont(Style.EMPTY.withColor(c), line);

        return Component.literal(text).withStyle(style);
    }

    private static String expandVars(String text, ItemStack stack) {
        if (!text.contains("{")) return text;
        String s = text;
        if (stack.isDamageableItem()) {
            s = s.replace("{durability}", String.valueOf(stack.getMaxDamage() - stack.getDamageValue()));
            s = s.replace("{max_durability}", String.valueOf(stack.getMaxDamage()));
        }
        s = s.replace("{count}", String.valueOf(stack.getCount()));
        return s;
    }

    /**
     * 鏀寔鍗佸叚杩涘埗 #FF6600 鍜屽懡鍚嶈壊 gold/red/...
     * 1.20.1: TextColor.parseColor() 鐩存帴杩斿洖 @Nullable TextColor
     */
    @Nullable
    public static TextColor parseColor(@Nullable String name) {
        if (name == null || name.isEmpty()) return null;
        // 鍗佸叚杩涘埗
        TextColor tc = TextColor.parseColor(name);
        if (tc != null) return tc;
        // 鍛藉悕鑹?
        return TextColor.fromLegacyFormat(switch (name.toLowerCase()) {
            case "black" -> net.minecraft.ChatFormatting.BLACK;
            case "dark_blue" -> net.minecraft.ChatFormatting.DARK_BLUE;
            case "dark_green" -> net.minecraft.ChatFormatting.DARK_GREEN;
            case "dark_aqua" -> net.minecraft.ChatFormatting.DARK_AQUA;
            case "dark_red" -> net.minecraft.ChatFormatting.DARK_RED;
            case "dark_purple" -> net.minecraft.ChatFormatting.DARK_PURPLE;
            case "gold" -> net.minecraft.ChatFormatting.GOLD;
            case "gray", "grey" -> net.minecraft.ChatFormatting.GRAY;
            case "dark_gray", "dark_grey" -> net.minecraft.ChatFormatting.DARK_GRAY;
            case "blue" -> net.minecraft.ChatFormatting.BLUE;
            case "green" -> net.minecraft.ChatFormatting.GREEN;
            case "aqua" -> net.minecraft.ChatFormatting.AQUA;
            case "red" -> net.minecraft.ChatFormatting.RED;
            case "light_purple" -> net.minecraft.ChatFormatting.LIGHT_PURPLE;
            case "yellow" -> net.minecraft.ChatFormatting.YELLOW;
            case "white" -> net.minecraft.ChatFormatting.WHITE;
            default -> null;
        });
    }

    private static Style applyFont(Style s, TooltipLine line) {
        if (line.bold() != null && line.bold()) s = s.withBold(true);
        if (line.italic() != null && line.italic()) s = s.withItalic(true);
        if (line.underlined() != null && line.underlined()) s = s.withUnderlined(true);
        if (line.strikethrough() != null && line.strikethrough()) s = s.withStrikethrough(true);
        if (line.font() != null) {
            ResourceLocation fontId = ResourceLocation.tryParse(line.font());
            if (fontId != null) s = s.withFont(fontId);
        }
        return s;
    }
}
