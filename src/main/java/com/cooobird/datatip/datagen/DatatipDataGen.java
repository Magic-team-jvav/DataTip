package com.cooobird.datatip.datagen;

import com.cooobird.datatip.Datatip;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * DataTip 客户端资源数据生成入口。
 * <p>
 * 示例内容全部通过 {@link TipContentBuilder} 构造，并分别生成完整内容展示与全部条件示例。
 * </p>
 */
@EventBusSubscriber(modid = Datatip.MODID)
public class DatatipDataGen {
    @SubscribeEvent
    static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        generator.addProvider(event.includeClient(), new ExampleTooltipProvider(output));
    }
}
