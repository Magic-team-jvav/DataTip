package com.cooobird.datatip.datagen;

import com.cooobird.datatip.Datatip;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * 其他模组的 datagen 参照这里的写法，核心就是调 {@link TipContentBuilder}。
 * 默认不生成文件，取消下面注释即可激活示例：
 */
@EventBusSubscriber(modid = Datatip.MODID)
public class DatatipDataGen {
    @SubscribeEvent
    static void gatherData(GatherDataEvent event) {
        // 取消注释来激活示例生成
        // var output = event.getGenerator().getPackOutput();
        // event.getGenerator().addProvider(true, new ExampleTooltipProvider(output));
    }
}
