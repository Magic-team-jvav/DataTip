package com.cooobird.datatip.event;

import com.cooobird.datatip.api.TipContentEntry;
import com.cooobird.datatip.api.TipContentSource;
import com.cooobird.datatip.config.DatatipConfig;
import com.cooobird.datatip.internal.input.ClientKeyState;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Tooltip 渲染事件处理器。
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class TipRenderEventHandler {
    public static final KeyMapping SHOW_TIP = new KeyMapping(
        "key.datatip.show_tip",
        InputConstants.KEY_LSHIFT,
        "key.categories.datatip"
    );

    private static TipContentSource contentSource;

    public static void setContentSource(TipContentSource source) {
        contentSource = source;
    }

    @Nullable
    public static TipContentSource getContentSource() {
        return contentSource;
    }

    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(SHOW_TIP);
    }

    @SubscribeEvent
    public static void onGatherComponents(RenderTooltipEvent.GatherComponents event) {
        if (!DatatipConfig.ENABLED.get()) return;

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        List<TipContentEntry> entries = TipRenderContentLookup.find(contentSource, stack);
        if (!entries.isEmpty()) {
            TipTooltipElements.insertContentEntries(event, entries, stack, isShowTipDown(), SHOW_TIP);
        }

        TipTooltipElements.appendExtraLines(event, stack);
    }

    public static boolean isShowTipDown() {
        return ClientKeyState.isDown(SHOW_TIP)
            || (SHOW_TIP.isDefault() && Screen.hasShiftDown());
    }
}
