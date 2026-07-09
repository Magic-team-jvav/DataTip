package com.cooobird.datatip.client;

import com.cooobird.datatip.Datatip;
import com.cooobird.datatip.api.component.TipContentTooltipComponent;
import com.cooobird.datatip.event.TipRenderEventHandler;
import com.cooobird.datatip.internal.loader.TipContentLoader;
import com.cooobird.datatip.internal.util.SchemaExporter;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

@Mod(value = Datatip.MODID, dist = Dist.CLIENT)
public class DatatipClient {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final TipContentLoader CONTENT_LOADER = new TipContentLoader();

    public DatatipClient(IEventBus modEventBus, ModContainer modContainer) {
        SchemaExporter.exportDefaultSchema();
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(TipRenderEventHandler::registerKeyMapping);
        modEventBus.addListener(RegisterClientTooltipComponentFactoriesEvent.class, event ->
            event.register(TipContentTooltipComponent.class, tip -> tip));
        modEventBus.addListener(RegisterClientReloadListenersEvent.class, this::registerReloadListeners);
    }

    private void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(CONTENT_LOADER);
        event.registerReloadListener((stage, rm, prepProf, reloadProf, bgExec, gameExec) ->
            CompletableFuture
                .supplyAsync(() -> null, bgExec)
                .thenCompose(stage::wait)
                .thenRunAsync(() -> {
                    TipRenderEventHandler.setContentSource(CONTENT_LOADER);
                    LOGGER.info("TipContentLoader updated with {} entries",
                        CONTENT_LOADER.getExactItemIds().size());
                }, gameExec));
    }
}
