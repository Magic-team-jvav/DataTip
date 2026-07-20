package com.cooobird.datatip.event;

import com.cooobird.datatip.api.TipContentEntry;
import com.cooobird.datatip.api.TipContentSource;
import com.cooobird.datatip.api.TipEventManager;
import com.cooobird.datatip.api.component.TipContentTooltipComponent;
import com.cooobird.datatip.api.component.TooltipViewportBudget;
import com.cooobird.datatip.api.session.TooltipSessionContext;
import com.cooobird.datatip.client.DatatipKeyMappings;
import com.cooobird.datatip.client.TooltipSessionRuntime;
import com.cooobird.datatip.config.DatatipConfig;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Tooltip 渲染事件处理器。
 *
 * @author cooobird
 * @since 1.2.0
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class TipRenderEventHandler {
    private static TipContentSource contentSource;

    /**
     * 设置内容加载器。
     *
     * @param source 内容来源实例
     */
    public static void setContentSource(TipContentSource source) {
        contentSource = source;
    }

    /**
     * 获取内容加载器。
     *
     * @return 内容加载器实例，未初始化时返回 null
     */
    @Nullable
    public static TipContentSource getContentSource() {
        return contentSource;
    }

    /**
     * 注册按键绑定。
     *
     * @param event 按键映射注册事件
     */
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        DatatipKeyMappings.register(event);
    }

    /**
     * 收集 Tooltip 组件事件处理。
     *
     * @param event 收集组件事件
     */
    @SubscribeEvent
    public static void onGatherComponents(RenderTooltipEvent.GatherComponents event) {
        if (!DatatipConfig.ENABLED.get()) return;

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        TipEventManager.PreRenderEvent preRender = TipEventManager.firePreRender(stack);
        if (preRender.isCanceled()) return;
        ItemStack effectiveStack = preRender.getItemStack() != null
            ? preRender.getItemStack()
            : ItemStack.EMPTY;
        boolean shiftDown = isShowTipDown();
        var hit = TooltipSessionRuntime.acquire(stack, effectiveStack, shiftDown);
        List<TipContentEntry> entries = TooltipSessionContext.call(
            hit.session(),
            () -> TipRenderContentLookup.find(contentSource, hit.stackSnapshot())
        );
        TooltipViewportBudget viewportBudget = null;
        if (!entries.isEmpty()) {
            applyConfiguredWidth(event);
            TooltipSessionRuntime.activate(hit);
            viewportBudget = TipTooltipElements.insertContentEntries(
                event,
                entries,
                hit,
                shiftDown
            );
        }

        TipTooltipElements.appendExtraLines(
            event,
            TipTooltipElements.collectExtraLines(effectiveStack)
        );
        TipTooltipElements.appendShiftHint(
            event,
            entries,
            shiftDown,
            DatatipKeyMappings.SHOW_TIP,
            hit
        );
        TipTooltipElements.appendScrollHint(
            event,
            viewportBudget,
            DatatipKeyMappings.SCROLL_TOOLTIP,
            entries,
            hit
        );
    }

    /**
     * 原版完成文本换行后，按最终组件宽高为 DataTip 分配物理视口。
     */
    @SubscribeEvent
    public static void onRenderTooltipPre(RenderTooltipEvent.Pre event) {
        Set<TooltipViewportBudget> budgets = Collections.newSetFromMap(
            new IdentityHashMap<>()
        );
        for (var component : event.getComponents()) {
            if (component instanceof TipContentTooltipComponent tip
                && tip.viewportBudget() != null) {
                budgets.add(tip.viewportBudget());
            }
        }
        for (TooltipViewportBudget budget : budgets) {
            budget.updateFromFinalComponents(
                event.getX(),
                event.getScreenWidth(),
                event.getScreenHeight(),
                event.getComponents(),
                event.getFont(),
                DatatipConfig.MAX_WIDTH.get()
            );
        }
        if (!budgets.isEmpty()
            && event.getTooltipPositioner()
            == DefaultTooltipPositioner.INSTANCE) {
            long tooltipHeight = event.getComponents().size() == 1
                ? -2L
                : 0L;
            for (var component : event.getComponents()) {
                tooltipHeight += component.getHeight();
            }
            event.setY(
                TooltipViewportBudget.adjustAnchorYForVisibleBackground(
                    event.getY(),
                    event.getScreenHeight(),
                    tooltipHeight
                )
            );
        }
    }

    public static boolean isShowTipDown() {
        return DatatipKeyMappings.isShowTipDown();
    }

    public static boolean isScrollTooltipDown() {
        return DatatipKeyMappings.isScrollTooltipDown();
    }

    /**
     * 显式配置宽度时，同时交给 NeoForge 的原版文本换行阶段处理。
     */
    private static void applyConfiguredWidth(
        RenderTooltipEvent.GatherComponents event
    ) {
        int configuredWidth = DatatipConfig.MAX_WIDTH.get();
        if (configuredWidth <= 0) return;
        int existingWidth = event.getMaxWidth();
        event.setMaxWidth(
            existingWidth > 0
                ? Math.min(existingWidth, configuredWidth)
                : configuredWidth
        );
    }
}
