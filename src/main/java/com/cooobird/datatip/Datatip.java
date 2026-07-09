package com.cooobird.datatip;

import com.cooobird.datatip.api.TipContentRegistry;
import com.cooobird.datatip.api.component.TipContentTooltipComponent;
import com.cooobird.datatip.api.parser.*;
import com.cooobird.datatip.config.DatatipConfig;
import com.cooobird.datatip.event.TipRenderEventHandler;
import com.cooobird.datatip.internal.loader.TipContentLoader;
import com.cooobird.datatip.internal.util.SchemaExporter;
import com.mojang.logging.LogUtils;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Datatip.MODID)
public class Datatip {
    public static final String MODID = "datatip";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final TipContentLoader CONTENT_LOADER = new TipContentLoader();

    @SuppressWarnings("removal")
    public Datatip() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DatatipConfig.SPEC);
        registerContentParsers();
        SchemaExporter.exportDefaultSchema();
        TipRenderEventHandler.setContentSource(CONTENT_LOADER);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(TipRenderEventHandler::registerKeyMapping);
        modEventBus.<RegisterClientTooltipComponentFactoriesEvent>addListener(event ->
            event.register(TipContentTooltipComponent.class, tip -> tip));
        modEventBus.<RegisterClientReloadListenersEvent>addListener(event -> {
            event.registerReloadListener(CONTENT_LOADER);
            LOGGER.info("TipContentLoader registered for client reload");
        });

        LOGGER.info("DataTip loaded");
    }

    public static TipContentLoader getContentLoader() {
        return CONTENT_LOADER;
    }

    private static void registerContentParsers() {
        TipContentRegistry.registerParser("text", new TextContentParser());
        TipContentRegistry.registerParser("spacer", new SpacerContentParser());
        TipContentRegistry.registerParser("divider", new DividerContentParser());
        TipContentRegistry.registerParser("item", new ItemContentParser());
        TipContentRegistry.registerParser("atlas", new AtlasContentParser());
        TipContentRegistry.registerParser("block", new BlockContentParser());
        TipContentRegistry.registerParser("entity", new EntityContentParser());
        TipContentRegistry.registerParser("progress", new ProgressContentParser());
        TipContentRegistry.registerParser("vbox", new VBoxContentParser());
        TipContentRegistry.registerParser("hbox", new HBoxContentParser());
        TipContentRegistry.registerParser("carousel", new CarouselContentParser());
        TipContentRegistry.registerParser("typewriter", new TypewriterContentParser());
        TipContentRegistry.registerParser("image", new ImageContentParser());
        TipContentRegistry.registerParser("chart", new ChartContentParser());

        LOGGER.info("Registered {} content parsers", TipContentRegistry.getRegisteredTypes().size());
    }
}
