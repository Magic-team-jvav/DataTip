package com.cooobird.datatip.api.util;

import com.cooobird.datatip.api.loader.TipContentLoader;
import com.cooobird.datatip.config.DatatipConfig;
import com.cooobird.datatip.event.TipRenderEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * DataTip 调试工具。
 * 显示匹配信息、渲染边界等调试信息。
 *
 * @author cooobird
 * @since 1.2.0
 */
public class TipDebugger {

    private static final int debugX = 10;
    private static final int debugY = 10;

    /**
     * 渲染调试信息。
     */
    public static void render(GuiGraphics graphics, Font font, ItemStack stack, int mouseX, int mouseY) {
        if (!DatatipConfig.DEBUG_MODE.get()) return;

        int x = debugX;
        int y = debugY;
        int lineHeight = 10;
        int backgroundColor = 0x80000000;
        int textColor = 0xFFFFFF;
        int highlightColor = 0xFFFF55;

        // 背景
        graphics.fill(x - 2, y - 2, x + 200, y + 12 * lineHeight, backgroundColor);

        // 标题
        graphics.drawString(font, "§lDataTip Debug Mode", x, y, highlightColor);
        y += lineHeight;

        // 物品信息
        graphics.drawString(font, "§eItem: §f" + stack.getHoverName().getString(), x, y, textColor);
        y += lineHeight;

        graphics.drawString(font, "§eID: §f" + stack.getItem(), x, y, textColor);
        y += lineHeight;

        graphics.drawString(font, "§eCount: §f" + stack.getCount(), x, y, textColor);
        y += lineHeight;

        if (stack.isDamageableItem()) {
            graphics.drawString(font, "§eDurability: §f" + (stack.getMaxDamage() - stack.getDamageValue()) + "/" + stack.getMaxDamage(), x, y, textColor);
            y += lineHeight;
        }

        // 匹配信息
        TipContentLoader loader = getLoader();
        if (loader != null) {
            String itemId = stack.getItem().toString();
            List<TipContentLoader.ContentEntry> entries = loader.getEntries(itemId, stack);

            graphics.drawString(font, "§eMatches: §f" + entries.size(), x, y, textColor);
            y += lineHeight;

            if (!entries.isEmpty()) {
                graphics.drawString(font, "§eFirst Match:", x, y, textColor);
                y += lineHeight;

                TipContentLoader.ContentEntry first = entries.get(0);
                graphics.drawString(font, "  §7shift: §f" + first.shift(), x, y, textColor);
                y += lineHeight;

                graphics.drawString(font, "  §7prepend: §f" + first.prepend(), x, y, textColor);
                y += lineHeight;

                graphics.drawString(font, "  §7conditions: §f" + first.conditions().size(), x, y, textColor);
                y += lineHeight;
            }
        }

        // 鼠标位置
        graphics.drawString(font, "§eMouse: §f" + mouseX + ", " + mouseY, x, y, textColor);
        y += lineHeight;

        // 性能信息
        graphics.drawString(font, "§eFPS: §f" + Minecraft.getInstance().getFps(), x, y, textColor);
    }

    /**
     * 渲染渲染边界。
     */
    public static void renderBounds(GuiGraphics graphics, int x, int y, int width, int height) {
        if (!DatatipConfig.DEBUG_MODE.get()) return;

        // 绘制边界框
        int color = 0xFFFF0000; // 红色
        graphics.fill(x, y, x + width, y + 1, color); // 上
        graphics.fill(x, y + height - 1, x + width, y + height, color); // 下
        graphics.fill(x, y, x + 1, y + height, color); // 左
        graphics.fill(x + width - 1, y, x + width, y + height, color); // 右

        // 绘制尺寸信息
        Minecraft mc = Minecraft.getInstance();
        if (mc.font != null) {
            String sizeText = width + "x" + height;
            graphics.drawString(mc.font, sizeText, x + 2, y + 2, 0xFFFF00);
        }
    }

    /**
     * 获取加载器实例。
     */
    private static TipContentLoader getLoader() {
        return TipRenderEventHandler.getContentLoader();
    }
}
