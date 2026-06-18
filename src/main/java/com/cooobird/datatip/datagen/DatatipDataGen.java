package com.cooobird.datatip.datagen;

import com.cooobird.datatip.Datatip;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 数据生成。
 * 默认不生成文件，取消下面注释即可激活示例。
 */
@Mod.EventBusSubscriber(modid = Datatip.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DatatipDataGen {
    @SubscribeEvent
    static void gatherData(GatherDataEvent event) {
        // 取消注释来激活示例生成
        // var output = event.getGenerator().getPackOutput();
        // event.getGenerator().addProvider(true, new ExampleTooltipProvider(output));
    }
}
