package com.cooobird.datatip.event;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipContentEntry;
import com.cooobird.datatip.api.TipEventManager;
import com.cooobird.datatip.api.component.TipContentTooltipComponent;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Tooltip 元素插入工具。
 */
final class TipTooltipElements {
    private TipTooltipElements() {
    }

    static void insertContentEntries(
        RenderTooltipEvent.GatherComponents event,
        List<TipContentEntry> entries,
        ItemStack stack,
        boolean shiftDown,
        KeyMapping showTipKey
    ) {
        if (hasShiftContent(entries) && !shiftDown) {
            addShiftHint(event, showTipKey);
        }

        List<TipContent> prependContents = new ArrayList<>();
        List<TipContent> normalContents = new ArrayList<>();
        collectVisibleContents(entries, shiftDown, prependContents, normalContents);
        insertPrependContents(event, prependContents, stack);
        appendNormalContents(event, normalContents, stack);
    }

    private static boolean hasShiftContent(List<TipContentEntry> entries) {
        return entries.stream().anyMatch(TipContentEntry::shift);
    }

    private static void addShiftHint(RenderTooltipEvent.GatherComponents event, KeyMapping showTipKey) {
        Component hint = Component.translatable("tooltip.datatip.hold_shift", showTipKey.getTranslatedKeyMessage())
            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
        event.getTooltipElements().add(Either.left(hint));
    }

    private static void collectVisibleContents(
        List<TipContentEntry> entries,
        boolean shiftDown,
        List<TipContent> prependContents,
        List<TipContent> normalContents
    ) {
        for (TipContentEntry entry : entries) {
            if (entry.shift() && !shiftDown) continue;

            if (entry.prepend()) {
                prependContents.add(entry.content());
            } else {
                normalContents.add(entry.content());
            }
        }
    }

    private static void insertPrependContents(
        RenderTooltipEvent.GatherComponents event,
        List<TipContent> prependContents,
        ItemStack stack
    ) {
        int insertIndex = 1;
        for (TipContent content : prependContents) {
            event.getTooltipElements().add(insertIndex, Either.right(new TipContentTooltipComponent(content, stack)));
            insertIndex++;
        }
    }

    private static void appendNormalContents(
        RenderTooltipEvent.GatherComponents event,
        List<TipContent> normalContents,
        ItemStack stack
    ) {
        for (TipContent content : normalContents) {
            event.getTooltipElements().add(Either.right(new TipContentTooltipComponent(content, stack)));
        }
    }

    static void appendExtraLines(RenderTooltipEvent.GatherComponents event, ItemStack stack) {
        TipEventManager.AppendLinesEvent appendEvent = TipEventManager.fireAppendLines(stack);
        for (String line : appendEvent.getLines()) {
            event.getTooltipElements().add(Either.left(Component.literal(line)));
        }
    }
}
