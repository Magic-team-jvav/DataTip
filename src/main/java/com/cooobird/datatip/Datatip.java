package com.cooobird.datatip;

import com.cooobird.datatip.api.TipContentRegistry;
import com.cooobird.datatip.api.component.TipContentTooltipComponent;
import com.cooobird.datatip.api.loader.TipContentLoader;
import com.cooobird.datatip.api.parser.*;
import com.cooobird.datatip.config.DatatipConfig;
import com.cooobird.datatip.event.TipRenderEventHandler;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

@Mod(Datatip.MODID)
public class Datatip {
    public static final String MODID = "datatip";
    private static final Logger LOGGER = LogUtils.getLogger();

    // 内容加载器
    private static final TipContentLoader CONTENT_LOADER = new TipContentLoader();

    public Datatip(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, DatatipConfig.SPEC);
        registerContentParsers();
        modEventBus.addListener(RegisterClientTooltipComponentFactoriesEvent.class, event -> {
            event.register(TipContentTooltipComponent.class, tip -> tip);
        });

        modEventBus.addListener(RegisterClientReloadListenersEvent.class, event -> {
            event.registerReloadListener(CONTENT_LOADER);
            event.registerReloadListener((stage, rm, prepProf, reloadProf, bgExec, gameExec) ->
                CompletableFuture
                    .supplyAsync(() -> null, bgExec)
                    .thenCompose(stage::wait)
                    .thenRunAsync(() -> {
                        TipRenderEventHandler.setContentLoader(CONTENT_LOADER);
                        LOGGER.info("TipContentLoader updated with {} entries",
                            CONTENT_LOADER.getExactItemIds().size());
                    }, gameExec));
        });

        LOGGER.info("DataTip loaded");
    }

    /**
     * 注册所有内容解析器。
     */
    private static void registerContentParsers() {
        // 基础内容类型
        TipContentRegistry.registerParser("text", new TextContentParser());
        TipContentRegistry.registerParser("spacer", new SpacerContentParser());
        TipContentRegistry.registerParser("divider", new DividerContentParser());

        // 物品/方块/实体
        TipContentRegistry.registerParser("item", new ItemContentParser());
        TipContentRegistry.registerParser("atlas", new AtlasContentParser());
        TipContentRegistry.registerParser("block", new BlockContentParser());
        TipContentRegistry.registerParser("entity", new EntityContentParser());

        // 进度/数据
        TipContentRegistry.registerParser("progress", new ProgressContentParser());

        // 布局
        TipContentRegistry.registerParser("vbox", new VBoxContentParser());
        TipContentRegistry.registerParser("hbox", new HBoxContentParser());

        // 动画/轮播
        TipContentRegistry.registerParser("carousel", new CarouselContentParser());
        TipContentRegistry.registerParser("typewriter", new TypewriterContentParser());

        // 新增：图片和图表
        TipContentRegistry.registerParser("image", new ImageContentParser());
        TipContentRegistry.registerParser("chart", new ChartContentParser());

        LOGGER.info("Registered {} content parsers", TipContentRegistry.getRegisteredTypes().size());
    }
}
