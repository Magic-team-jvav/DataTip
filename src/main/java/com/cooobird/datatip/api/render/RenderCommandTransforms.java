package com.cooobird.datatip.api.render;

import com.cooobird.datatip.api.layout.TipMath;
import com.cooobird.datatip.api.layout.TipRect;

import java.util.ArrayList;
import java.util.List;

/**
 * 在不修改绘制载荷的前提下平移命令树。
 */
public final class RenderCommandTransforms {
    private RenderCommandTransforms() {
    }

    public static RenderCommandGroup translate(
        RenderCommandGroup group,
        long offsetX,
        long offsetY
    ) {
        return RenderCommandGroup.translatedLayer(
            group.offsetZ(),
            group.sourceIndex(),
            offsetX,
            offsetY,
            List.of(group)
        );
    }

    public static List<RenderCommand> translate(
        List<RenderCommand> commands,
        long offsetX,
        long offsetY
    ) {
        ArrayList<RenderCommand> translated = new ArrayList<>(commands.size());
        for (RenderCommand command : commands) {
            translated.add(translate(command, offsetX, offsetY));
        }
        return List.copyOf(translated);
    }

    public static List<RenderCommand> translateInPaintOrder(
        List<RenderCommand> commands,
        long offsetX,
        long offsetY
    ) {
        ArrayList<RenderCommand> translated = new ArrayList<>(commands.size());
        for (int index = 0; index < commands.size(); index++) {
            RenderCommand command = commands.get(index);
            TipRect clip = command.clipBounds() != null
                ? translate(command.clipBounds(), offsetX, offsetY)
                : null;
            translated.add(RenderCommand.positioned(
                command.phase(),
                0,
                index,
                translate(command.bounds(), offsetX, offsetY),
                clip,
                command.payload()
            ));
        }
        return List.copyOf(translated);
    }

    private static RenderCommand translate(
        RenderCommand command,
        long offsetX,
        long offsetY
    ) {
        TipRect clip = command.clipBounds() != null
            ? translate(command.clipBounds(), offsetX, offsetY)
            : null;
        return RenderCommand.positioned(
            command.phase(),
            command.offsetZ(),
            command.sourceIndex(),
            translate(command.bounds(), offsetX, offsetY),
            clip,
            command.payload()
        );
    }

    public static TipRect translate(
        TipRect bounds,
        long offsetX,
        long offsetY
    ) {
        return new TipRect(
            TipMath.add(bounds.x(), offsetX),
            TipMath.add(bounds.y(), offsetY),
            bounds.width(),
            bounds.height()
        );
    }
}
