package com.cooobird.datatip.api;

/**
 * Tooltip 内容的基础接口。
 * <p>
 * 所有自定义内容类型（文本、物品、进度条等）都应实现此接口。
 * DataTip 通过此接口统一管理不同类型的内容渲染。
 * </p>
 *
 * <h3>实现示例</h3>
 * <pre>{@code
 * public record MyCustomContent(String text) implements TipContent {
 *     @Override
 *     public int getHeight(int maxWidth) { return 12; }
 *
 *     @Override
 *     public int getWidth(int maxWidth) { return maxWidth; }
 *
 *     @Override
 *     public void render(TipRenderContext ctx, int x, int y, int maxWidth, float alpha) {
 *         ctx.drawString(text, x, y, 0xFFFFFF);
 *     }
 * }
 * }</pre>
 *
 * <h3>内置实现</h3>
 * <ul>
 *   <li>{@link com.cooobird.datatip.api.content.TextContent} - 文本内容</li>
 *   <li>{@link com.cooobird.datatip.api.content.ItemContent} - 物品内容</li>
 *   <li>{@link com.cooobird.datatip.api.content.BlockContent} - 方块内容</li>
 *   <li>{@link com.cooobird.datatip.api.content.EntityContent} - 实体内容</li>
 *   <li>{@link com.cooobird.datatip.api.content.ProgressContent} - 进度条内容</li>
 *   <li>{@link com.cooobird.datatip.api.content.CarouselContent} - 轮播内容</li>
 *   <li>{@link com.cooobird.datatip.api.content.TypewriterContent} - 打字机内容</li>
 *   <li>{@link com.cooobird.datatip.api.content.AtlasContent} - 纹理内容</li>
 *   <li>{@link com.cooobird.datatip.api.content.ImageContent} - 图片内容</li>
 *   <li>{@link com.cooobird.datatip.api.content.ChartContent} - 图表内容</li>
 *   <li>{@link com.cooobird.datatip.api.content.VBoxContent} - 垂直布局</li>
 *   <li>{@link com.cooobird.datatip.api.content.HBoxContent} - 水平布局</li>
 *   <li>{@link com.cooobird.datatip.api.content.DividerContent} - 分割线</li>
 *   <li>{@link com.cooobird.datatip.api.content.SpacerContent} - 间距</li>
 * </ul>
 *
 * @author cooobird
 * @see TipRenderContext 渲染上下文
 * @see TipContentRegistry 内容注册表
 * @since 1.2.0
 */
public interface TipContent {

    /**
     * 使用完整布局上下文获取内容高度。
     * <p>
     * 默认实现委派给旧宽度接口，现有第三方内容无需修改即可继续工作。需要按物品变量或字体
     * 精确测量的内容可以覆盖此方法。
     * </p>
     */
    default int getHeight(TipLayoutContext context) {
        return Math.max(0, getHeight(context.compatibilityWidth()));
    }

    /**
     * 使用完整布局上下文获取内容宽度。
     */
    default int getWidth(TipLayoutContext context) {
        return context.constrainWidth(getWidth(context.compatibilityWidth()));
    }

    /**
     * 获取此内容的高度（像素）。
     *
     * @param maxWidth 可用最大宽度（像素）
     * @return 内容高度（像素）
     */
    int getHeight(int maxWidth);

    /**
     * 获取此内容的宽度（像素）。
     *
     * @param maxWidth 可用最大宽度（像素）
     * @return 内容宽度（像素）
     */
    int getWidth(int maxWidth);

    /**
     * 渲染此内容。
     *
     * @param context  渲染上下文，提供绘图工具和变量解析
     * @param x        起始 X 坐标
     * @param y        起始 Y 坐标
     * @param maxWidth 可用最大宽度（像素）
     * @param alpha    透明度（0.0-1.0）
     */
    void render(TipRenderContext context, int x, int y, int maxWidth, float alpha);

    /**
     * 是否因 Shift 折叠而显示为提示行（而非完整内容）。
     * <p>
     * 容器（如 VBox）可用此方法判断是否需要将多个折叠项
     * 合并为一条"按住 Shift 显示详情"提示。
     * </p>
     *
     * @return true 表示当前处于 Shift 折叠状态
     */
    default boolean isShiftCollapsed() {
        return false;
    }

    /**
     * 在当前语言环境下是否有可显示的内容。
     * <p>
     * 多语言文本若当前语言无对应翻译则返回 false。
     * 容器在判断折叠合并时应跳过无内容的子元素。
     * </p>
     *
     * @return true 表示当前语言有可显示内容
     */
    default boolean hasContent() {
        return true;
    }

    /**
     * 此内容是否需要每帧更新（动画）。
     * <p>
     * 返回 true 时，{@link #tick(int)} 方法会在每 tick 被调用。
     * 用于实现旋转、闪烁等动画效果。
     * </p>
     *
     * @return true 表示需要动画更新，false 表示静态内容
     */
    default boolean isAnimated() {
        return false;
    }

    /**
     * 每 tick 调用一次，用于动画更新。
     * 仅当 {@link #isAnimated()} 返回 true 时调用。
     *
     * @param tickCount 当前 tick 计数
     */
    default void tick(int tickCount) {
        // 默认空实现
    }

}
