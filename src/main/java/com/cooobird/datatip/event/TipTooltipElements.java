package com.cooobird.datatip.event;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipContentEntry;
import com.cooobird.datatip.api.TipEventManager;
import com.cooobird.datatip.api.component.ScrollHintTooltipComponent;
import com.cooobird.datatip.api.component.TipContentTooltipComponent;
import com.cooobird.datatip.api.component.TooltipViewportBudget;
import com.cooobird.datatip.api.content.CarouselContent;
import com.cooobird.datatip.api.content.ContainerContent;
import com.cooobird.datatip.api.content.VBoxContent;
import com.cooobird.datatip.api.node.TipNode;
import com.cooobird.datatip.api.session.TooltipHit;
import com.cooobird.datatip.config.DatatipConfig;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Tooltip 元素插入工具。
 */
final class TipTooltipElements {
    private static final Object PREPEND_SCROLL_KEY = new Object();
    private static final Object NORMAL_SCROLL_KEY = new Object();

    private TipTooltipElements() {
    }

    @Nullable
    static TooltipViewportBudget insertContentEntries(
        RenderTooltipEvent.GatherComponents event,
        List<TipContentEntry> entries,
        TooltipHit hit,
        boolean shiftDown
    ) {
        List<TipContent> prependContents = new ArrayList<>();
        List<TipContent> normalContents = new ArrayList<>();
        collectVisibleContents(entries, shiftDown, prependContents, normalContents);
        // Gather 阶段原版尚未换行，先按原版背景外扩保留边界；
        // 最终预算会在 Pre 阶段按真实组件高度覆盖。
        TooltipViewportBudget budget = TooltipViewportBudget.forScreen(
            event.getScreenWidth(),
            event.getScreenHeight()
        );
        insertPrependContents(event, prependContents, hit, budget);
        appendNormalContents(event, normalContents, hit, budget);
        return prependContents.isEmpty() && normalContents.isEmpty()
            ? null
            : budget;
    }

    static void appendShiftHint(
        RenderTooltipEvent.GatherComponents event,
        List<TipContentEntry> entries,
        boolean shiftDown,
        KeyMapping showTipKey,
        TooltipHit hit
    ) {
        if (hasShiftContent(entries) && !shiftDown) {
            addShiftHint(event, entries, showTipKey, hit);
        }
    }

    private static boolean hasShiftContent(List<TipContentEntry> entries) {
        for (TipContentEntry entry : entries) {
            if (entry.shift() || containsShiftNode(entry.content())) return true;
        }
        return false;
    }

    private static boolean containsShiftNode(TipContent content) {
        ArrayDeque<TipContent> work = new ArrayDeque<>();
        work.push(content);
        while (!work.isEmpty()) {
            TipContent current = work.pop();
            if (current instanceof TipNode node) {
                if (node.modifiers().shift()) return true;
                work.push(node.inner());
                continue;
            }
            if (current instanceof ContainerContent container) {
                for (TipContent child : container.children()) {
                    work.push(child);
                }
                continue;
            }
            if (current instanceof CarouselContent carousel) {
                for (TipContent frame : carousel.getFrames()) {
                    work.push(frame);
                }
            }
        }
        return false;
    }

    private static void addShiftHint(
        RenderTooltipEvent.GatherComponents event,
        List<TipContentEntry> entries,
        KeyMapping showTipKey,
        TooltipHit hit
    ) {
        TipContent custom = firstHint(entries, true);
        if (custom != null) {
            event.getTooltipElements().add(Either.right(
                new TipContentTooltipComponent(
                    custom,
                    hit.stackSnapshot(),
                    hit
                )
            ));
            return;
        }
        Component hint = Component.translatable(
            "tooltip.datatip.hold_shift",
            showTipKey.getTranslatedKeyMessage()
        ).withStyle(style -> style
            .withColor(DatatipConfig.shiftHintColor())
            .withItalic(true));
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
        TooltipHit hit,
        TooltipViewportBudget budget
    ) {
        if (prependContents.isEmpty()) return;
        TipContent content = prependContents.size() == 1
            ? prependContents.get(0)
            : new VBoxContent(
            prependContents,
            0,
            0,
            VBoxContent.HorizontalAlign.LEFT
        );
        event.getTooltipElements().add(
            Math.min(1, event.getTooltipElements().size()),
            Either.right(prepareComponent(
                content,
                hit,
                budget,
                PREPEND_SCROLL_KEY
            ))
        );
    }

    private static void appendNormalContents(
        RenderTooltipEvent.GatherComponents event,
        List<TipContent> normalContents,
        TooltipHit hit,
        TooltipViewportBudget budget
    ) {
        if (normalContents.isEmpty()) return;
        TipContent content = normalContents.size() == 1
            ? normalContents.get(0)
            : new VBoxContent(
            normalContents,
            0,
            0,
            VBoxContent.HorizontalAlign.LEFT
        );
        event.getTooltipElements().add(
            Either.right(prepareComponent(
                content,
                hit,
                budget,
                NORMAL_SCROLL_KEY
            ))
        );
    }

    private static TipContentTooltipComponent prepareComponent(
        TipContent content,
        TooltipHit hit,
        TooltipViewportBudget budget,
        Object scrollKey
    ) {
        return new TipContentTooltipComponent(
            content,
            hit.stackSnapshot(),
            hit,
            budget,
            scrollKey
        );
    }

    static List<Component> collectExtraLines(ItemStack stack) {
        TipEventManager.AppendLinesEvent appendEvent = TipEventManager.fireAppendLines(stack);
        ArrayList<Component> result = new ArrayList<>();
        for (String line : appendEvent.getLines()) {
            result.add(Component.literal(line));
        }
        return List.copyOf(result);
    }

    static void appendExtraLines(
        RenderTooltipEvent.GatherComponents event,
        List<Component> lines
    ) {
        for (Component line : lines) {
            event.getTooltipElements().add(Either.left(line));
        }
    }

    static void appendScrollHint(
        RenderTooltipEvent.GatherComponents event,
        @Nullable TooltipViewportBudget budget,
        KeyMapping scrollKey,
        List<TipContentEntry> entries,
        TooltipHit hit
    ) {
        if (budget == null) return;
        TipContent custom = firstHint(entries, false);
        event.getTooltipElements().add(
            Either.right(custom != null
                ? new ScrollHintTooltipComponent(budget, custom, hit)
                : new ScrollHintTooltipComponent(
                budget,
                scrollKey.getTranslatedKeyMessage()
            ))
        );
    }

    @Nullable
    private static TipContent firstHint(
        List<TipContentEntry> entries,
        boolean shiftHint
    ) {
        for (TipContentEntry entry : entries) {
            TipContent hint = shiftHint
                ? entry.shiftHint()
                : entry.scrollHint();
            if (hint != null) return hint;
        }
        return null;
    }

}
