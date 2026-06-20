package com.cooobird.datatip.api;

import com.cooobird.datatip.api.util.VariableResolver;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Tooltip 渲染上下文。
 * 封装了 {@link GuiGraphics} 和动画状态，提供便捷的绘图方法。
 *
 * @param graphics    GuiGraphics 实例
 * @param font        字体渲染器
 * @param tickCount   当前 tick 计数
 * @param partialTick 帧间插值 (0.0-1.0)
 * @param itemStack   物品栈（用于变量解析）
 * @author cooobird
 * @since 1.2.0
 */
public record TipRenderContext(GuiGraphics graphics, Font font, int tickCount, float partialTick, ItemStack itemStack) {

    /**
     * 创建渲染上下文
     *
     * @param graphics    GuiGraphics 实例
     * @param font        字体渲染器
     * @param tickCount   当前 tick 计数
     * @param partialTick 帧间插值 (0.0-1.0)
     * @param itemStack   物品栈（用于变量解析）
     */
    public TipRenderContext {
    }

    /**
     * 创建渲染上下文（无物品栈）
     */
    public TipRenderContext(GuiGraphics graphics, Font font, int tickCount, float partialTick) {
        this(graphics, font, tickCount, partialTick, ItemStack.EMPTY);
    }

    /**
     * 绘制文字（带阴影）
     *
     * @param text  文字内容
     * @param x     X 坐标
     * @param y     Y 坐标
     * @param color 颜色
     */
    public void drawString(String text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, true);
    }

    /**
     * 绘制文字（带变量替换）
     *
     * @param text  包含变量的文字
     * @param x     X 坐标
     * @param y     Y 坐标
     * @param color 颜色
     */
    public void drawStringWithVariables(String text, int x, int y, int color) {
        String resolved = resolveVariables(text);
        graphics.drawString(font, resolved, x, y, color, true);
    }

    /**
     * 绘制文字
     *
     * @param text   文字内容
     * @param x      X 坐标
     * @param y      Y 坐标
     * @param color  颜色
     * @param shadow 是否带阴影
     */
    public void drawString(String text, int x, int y, int color, boolean shadow) {
        graphics.drawString(font, text, x, y, color, shadow);
    }

    public void drawString(String text, int x, int y, int color, @Nullable ResourceLocation customFont) {
        if (customFont != null) {
            graphics.drawString(font, Component.literal(text).withStyle(Style.EMPTY.withFont(customFont)), x, y, color, true);
        } else {
            graphics.drawString(font, text, x, y, color, true);
        }
    }

    /**
     * 绘制居中文字（带阴影）
     *
     * @param text  文字内容
     * @param x     中心 X 坐标
     * @param y     Y 坐标
     * @param color 颜色
     */
    public void drawCenteredString(String text, int x, int y, int color) {
        int width = font.width(text);
        graphics.drawString(font, text, x - width / 2, y, color, true);
    }

    /**
     * 绘制右对齐文字（带阴影）
     *
     * @param text  文字内容
     * @param x     右边界 X 坐标
     * @param y     Y 坐标
     * @param color 颜色
     */
    public void drawRightAlignedString(String text, int x, int y, int color) {
        int width = font.width(text);
        graphics.drawString(font, text, x - width, y, color, true);
    }

    /**
     * 绘制 Component（带阴影）
     */
    public void drawString(Component text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, true);
    }

    /**
     * 绘制 Component（可选阴影）
     */
    public void drawString(Component text, int x, int y, int color, boolean shadow) {
        graphics.drawString(font, text, x, y, color, shadow);
    }

    /**
     * 绘制 FormattedCharSequence（带阴影）
     */
    public void drawString(FormattedCharSequence text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, true);
    }

    /**
     * 绘制 FormattedCharSequence（可选阴影）
     */
    public void drawString(FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        graphics.drawString(font, text, x, y, color, shadow);
    }

    /**
     * 绘制居中 Component（带阴影）
     */
    public void drawCenteredString(Component text, int x, int y, int color) {
        graphics.drawCenteredString(font, text, x, y, color);
    }

    /**
     * 获取文字宽度
     *
     * @param text 文字内容
     * @return 宽度（像素）
     */
    public int getStringWidth(String text) {
        return font.width(text);
    }

    /**
     * 获取 Component 宽度
     */
    public int getStringWidth(Component text) {
        return font.width(text);
    }

    /**
     * 填充矩形
     */
    public void fill(int x1, int y1, int x2, int y2, int color) {
        graphics.fill(x1, y1, x2, y2, color);
    }

    /**
     * 填充渐变矩形（垂直）
     */
    public void fillGradientV(int x1, int y1, int x2, int y2, int colorTop, int colorBottom) {
        graphics.fillGradient(x1, y1, x2, y2, colorTop, colorBottom);
    }

    /**
     * 绘制水平线
     */
    public void hLine(int x1, int x2, int y, int color) {
        graphics.fill(x1, y, x2, y + 1, color);
    }

    /**
     * 绘制纹理区域
     */
    public void blit(ResourceLocation texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    /**
     * 绘制纹理（自动计算 UV）
     */
    public void blit(ResourceLocation texture, int x, int y, int width, int height,
                     float u1, float v1, int u2, int v2, int textureWidth, int textureHeight) {
        graphics.blit(texture, x, y, width, height, u1, v1, u2, v2, textureWidth, textureHeight);
    }

    /**
     * 绘制精灵（从 TextureAtlas）
     */
    public void blitSprite(ResourceLocation sprite, int x, int y, int width, int height) {
        graphics.blitSprite(sprite, x, y, width, height);
    }

    /**
     * 渲染物品图标（16x16）
     */
    public void renderItem(ItemStack stack, int x, int y) {
        graphics.renderItem(stack, x, y);
    }

    /**
     * 渲染物品图标（指定尺寸，通过缩放）
     */
    public void renderItemScaled(ItemStack stack, int x, int y, int size) {
        graphics.pose().pushPose();
        float scale = size / 16.0f;
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }

    /**
     * 渲染物品装饰（数量、耐久条等）
     * 注意：必须在 renderItem 之后调用
     */
    public void renderItemDecorations(ItemStack stack, int x, int y) {
        graphics.renderItemDecorations(font, stack, x, y);
    }

    /**
     * 渲染物品装饰（数量、耐久条等，自定义数量文字）
     */
    public void renderItemDecorations(ItemStack stack, int x, int y, String countText) {
        graphics.renderItemDecorations(font, stack, x, y, countText);
    }

    /**
     * 渲染物品图标 + 装饰（数量、耐久条等）
     */
    public void renderItemWithDecorations(ItemStack stack, int x, int y) {
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(font, stack, x, y);
    }

    /**
     * 获取当前 tick 计数
     */
    @Override
    public int tickCount() {
        return tickCount;
    }

    /**
     * 获取帧间插值
     */
    @Override
    public float partialTick() {
        return partialTick;
    }

    /**
     * 获取底层 GuiGraphics
     */
    @Override
    public GuiGraphics graphics() {
        return graphics;
    }

    /**
     * 获取字体渲染器
     */
    @Override
    public Font font() {
        return font;
    }

    /**
     * 获取 PoseStack
     */
    public PoseStack pose() {
        return graphics.pose();
    }

    /**
     * 解析文本中的变量
     */
    public String resolveVariables(String text) {
        if (text == null || itemStack.isEmpty()) {
            return text;
        }
        return VariableResolver.resolve(text, itemStack);
    }
}
