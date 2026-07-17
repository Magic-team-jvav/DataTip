package com.cooobird.datatip.api.render;

import java.util.function.Consumer;

/**
 * 单条绘制命令或一个局部叠放上下文。
 */
public sealed interface RenderCommandNode
    permits RenderCommand, RenderCommandGroup {
    long offsetZ();

    int sourceIndex();

    void execute(RenderPass pass, Consumer<String> sink);
}
