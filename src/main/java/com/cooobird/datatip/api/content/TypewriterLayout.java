package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.util.VariableResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 打字机完整动画包络的尺寸计算。
 */
final class TypewriterLayout {
    private TypewriterLayout() {
    }

    static int getHeight(TypewriterContent content) {
        return getHeight(content, TipLayoutContext.unbounded(
            Minecraft.getInstance().font,
            ItemStack.EMPTY
        ));
    }

    static int getHeight(
        TypewriterContent content,
        TipLayoutContext context
    ) {
        List<String> lines = resolvedLines(content, context.itemStack());
        if (lines.isEmpty()) return 0;
        int segments = 0;
        for (int index = 0; index < lines.size(); index++) {
            TypewriterRenderer.RenderLine styled = TypewriterRenderer.styledLine(
                content,
                lines.get(index) + "▌",
                index,
                content.color
            );
            segments += Math.max(
                1,
                context.font().split(
                    Component.literal(styled.text()).withStyle(styled.style()),
                    Math.max(1, context.availableWidth())
                ).size()
            );
        }
        int flowHeight = (int) Math.min(
            Integer.MAX_VALUE,
            (long) segments * content.lineHeight
        );
        int visualBottom = ContentBounds.add(
            (int) Math.min(
                Integer.MAX_VALUE,
                (long) Math.max(0, segments - 1) * content.lineHeight
            ),
            context.font().lineHeight,
            content.shadow ? 1 : 0
        );
        return Math.max(flowHeight, visualBottom);
    }

    static boolean hasContent(TypewriterContent content) {
        return !TypewriterTextSource.currentLines(content).isEmpty();
    }

    static int getWidth(TypewriterContent content, int maxWidth) {
        TipLayoutContext context = maxWidth > 0
            ? TipLayoutContext.bounded(
            Minecraft.getInstance().font,
            ItemStack.EMPTY,
            maxWidth
        )
            : TipLayoutContext.unbounded(
            Minecraft.getInstance().font,
            ItemStack.EMPTY
        );
        return getWidth(content, context);
    }

    static int getWidth(
        TypewriterContent content,
        TipLayoutContext context
    ) {
        List<String> lines = resolvedLines(content, context.itemStack());
        int maximum = 0;
        for (int index = 0; index < lines.size(); index++) {
            TypewriterRenderer.RenderLine styled = TypewriterRenderer.styledLine(
                content,
                lines.get(index) + "▌",
                index,
                content.color
            );
            var split = context.font().split(
                Component.literal(styled.text()).withStyle(styled.style()),
                Math.max(1, context.availableWidth())
            );
            for (var segment : split) {
                maximum = Math.max(maximum, context.font().width(segment));
            }
        }
        return ContentBounds.add(maximum, content.shadow ? 1 : 0);
    }

    static List<String> resolvedLines(
        TypewriterContent content,
        ItemStack stack
    ) {
        ArrayList<String> resolved = new ArrayList<>();
        for (String line : TypewriterTextSource.currentLines(content)) {
            String value = stack != null && !stack.isEmpty()
                ? VariableResolver.resolve(line, stack)
                : line;
            resolved.add(value != null ? value : "");
        }
        return List.copyOf(resolved);
    }
}
