package com.cooobird.datatip;

import com.cooobird.datatip.api.TipContentRegistry;
import com.cooobird.datatip.api.parser.*;
import com.cooobird.datatip.config.DatatipConfig;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(Datatip.MODID)
public class Datatip {
    public static final String MODID = "datatip";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Datatip(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, DatatipConfig.SPEC);
        registerContentParsers();

        LOGGER.info("DataTip loaded");
    }

    /**
     * 注册所有内容解析器。
     */
    static void registerContentParsers() {
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
        TipContentRegistry.registerParser("stack", new StackContentParser());

        // 动画/轮播
        TipContentRegistry.registerParser("carousel", new CarouselContentParser());
        TipContentRegistry.registerParser("typewriter", new TypewriterContentParser());

        // 新增：图片和图表
        TipContentRegistry.registerParser("image", new ImageContentParser());
        TipContentRegistry.registerParser("chart", new ChartContentParser());

        LOGGER.info("Registered {} content parsers", TipContentRegistry.getRegisteredTypes().size());
    }
}
