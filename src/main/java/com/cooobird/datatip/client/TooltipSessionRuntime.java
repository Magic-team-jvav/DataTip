package com.cooobird.datatip.client;

import com.cooobird.datatip.api.TipContentRegistry;
import com.cooobird.datatip.api.TipRuntimeContentRegistry;
import com.cooobird.datatip.api.condition.ComponentReaderRegistry;
import com.cooobird.datatip.api.condition.ConditionChecker;
import com.cooobird.datatip.api.session.*;
import com.cooobird.datatip.config.DatatipConfig;
import com.cooobird.datatip.internal.variable.VariableRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 客户端当前悬停会话及其帧、世界和资源生命周期。
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public final class TooltipSessionRuntime {
    private static final Object NO_WORLD = new Object();
    private static final TooltipSessionManager SESSIONS = new TooltipSessionManager();
    private static final AtomicLong RESOURCE_REVISION = new AtomicLong();
    private static final AtomicLong TAG_REVISION = new AtomicLong();
    private static final AtomicLong LANGUAGE_REVISION = new AtomicLong();
    private static final AtomicLong FONT_REVISION = new AtomicLong();
    private static volatile TooltipHit activeHit;
    private static final Set<TooltipScrollState> ACTIVE_SCROLL_STATES =
        Collections.newSetFromMap(new IdentityHashMap<>());

    private TooltipSessionRuntime() {
    }

    public static TooltipHit acquire(
        ItemStack hoverStack,
        ItemStack effectiveStack,
        boolean shiftDown
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        Object screenOwner = minecraft.screen != null ? minecraft.screen : minecraft.gui;
        Object hoverTarget = hoverStack;
        if (minecraft.screen instanceof AbstractContainerScreen<?> container
            && container.getSlotUnderMouse() != null) {
            hoverTarget = container.getSlotUnderMouse();
        }
        HoverIdentity identity = new HoverIdentity(screenOwner, hoverTarget);
        return SESSIONS.acquireHit(
            identity,
            effectiveStack,
            fingerprint -> dependencies(minecraft, fingerprint, shiftDown)
        );
    }

    public static void activate(TooltipHit hit) {
        if (hit != null && !hit.session().isClosed()) {
            activeHit = hit;
        }
    }

    public static TooltipScrollState updateViewport(
        TooltipHit hit,
        Object scrollKey,
        int contentHeight,
        int viewportHeight
    ) {
        TooltipScrollState state = hit.session().scrollState(scrollKey);
        state.update(contentHeight, viewportHeight);
        if (state.scrollable()) ACTIVE_SCROLL_STATES.add(state);
        return state;
    }

    public static void resourcesReloaded() {
        RESOURCE_REVISION.incrementAndGet();
        LANGUAGE_REVISION.incrementAndGet();
        FONT_REVISION.incrementAndGet();
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            activeHit = null;
            ACTIVE_SCROLL_STATES.clear();
            SESSIONS.beginFrame();
            return;
        }

        SESSIONS.endFrame();
        TooltipHit hit = activeHit;
        if (hit != null
            && (hit.session().isClosed()
            || SESSIONS.active() != hit.session())) {
            activeHit = null;
        }
    }

    @SubscribeEvent
    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (!DatatipKeyMappings.isScrollTooltipDown()) return;
        TooltipHit hit = activeHit;
        if (hit == null || hit.session().isClosed()) return;
        boolean scrolled = false;
        for (TooltipScrollState state : ACTIVE_SCROLL_STATES) {
            scrolled |= state.scrollBy(event.getScrollDelta());
        }
        if (scrolled) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        TAG_REVISION.incrementAndGet();
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        activeHit = null;
        SESSIONS.onWorldChanged();
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ClientLevel) {
            activeHit = null;
            SESSIONS.onWorldChanged();
        }
    }

    @SubscribeEvent
    public static void onShutdown(GameShuttingDownEvent event) {
        activeHit = null;
        SESSIONS.close();
    }

    private static TooltipDependencies dependencies(
        Minecraft minecraft,
        ItemStackFingerprint fingerprint,
        boolean shiftDown
    ) {
        int guiScale = Math.max(1, (int) Math.round(minecraft.getWindow().getGuiScale()));
        Object world = minecraft.level != null ? minecraft.level : NO_WORLD;
        return new TooltipDependencies(
            RESOURCE_REVISION.get(),
            TAG_REVISION.get(),
            TipRuntimeContentRegistry.getRevision(),
            TipContentRegistry.getRevision(),
            VariableRegistry.getRevision(),
            ConditionChecker.getRevision(),
            ComponentReaderRegistry.getRevision(),
            fingerprint,
            minecraft.getLanguageManager().getSelected(),
            LANGUAGE_REVISION.get(),
            minecraft.font,
            FONT_REVISION.get(),
            guiScale,
            minecraft.getWindow().getGuiScaledWidth(),
            minecraft.getWindow().getGuiScaledHeight(),
            new TooltipConfigSnapshot(
                DatatipConfig.defaultColor(),
                DatatipConfig.DEFAULT_LINE_HEIGHT.get(),
                DatatipConfig.MAX_WIDTH.get(),
                DatatipConfig.shiftHintColor(),
                DatatipConfig.scrollHintColor()
            ),
            shiftDown,
            world
        );
    }
}
