package com.cooobird.datatip.client;

import com.cooobird.datatip.Datatip;
import com.cooobird.datatip.api.component.ScrollHintTooltipComponent;
import com.cooobird.datatip.api.component.TipContentTooltipComponent;
import com.cooobird.datatip.event.TipRenderEventHandler;
import com.cooobird.datatip.internal.loader.TipContentLoader;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * 只在客户端装载的注册入口，避免服务端解析客户端类。
 */
@Mod.EventBusSubscriber(
    modid = Datatip.MODID,
    bus = Mod.EventBusSubscriber.Bus.MOD,
    value = Dist.CLIENT
)
public final class DatatipClient {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final TipContentLoader CONTENT_LOADER = new TipContentLoader();

    private DatatipClient() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        TipRenderEventHandler.registerKeyMapping(event);
    }

    @SubscribeEvent
    public static void registerTooltipComponents(
        RegisterClientTooltipComponentFactoriesEvent event
    ) {
        event.register(TipContentTooltipComponent.class, tip -> tip);
        event.register(ScrollHintTooltipComponent.class, hint -> hint);
    }

    @SubscribeEvent
    public static void registerReloadListeners(
        RegisterClientReloadListenersEvent event
    ) {
        TipRenderEventHandler.setContentSource(CONTENT_LOADER);
        event.registerReloadListener(CONTENT_LOADER);
        event.registerReloadListener((stage, resourceManager, preparationsProfiler,
                                      reloadProfiler, backgroundExecutor, gameExecutor) ->
            CompletableFuture
                .supplyAsync(() -> null, backgroundExecutor)
                .thenCompose(stage::wait)
                .thenRunAsync(() -> {
                    TooltipSessionRuntime.resourcesReloaded();
                    TipRenderEventHandler.setContentSource(CONTENT_LOADER);
                    LOGGER.info("TipContentLoader updated with {} entries",
                        CONTENT_LOADER.getExactItemIds().size());
                }, gameExecutor));
        LOGGER.info("TipContentLoader registered for client reload");
    }
}
