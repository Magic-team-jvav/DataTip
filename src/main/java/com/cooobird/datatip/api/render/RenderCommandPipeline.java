package com.cooobird.datatip.api.render;

import com.cooobird.datatip.api.layout.TipRect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 将普通流文本与需要保持局部叠放顺序的图片阶段命令分开。
 */
public final class RenderCommandPipeline {
    private static final RenderCommandPipeline EMPTY = new RenderCommandPipeline(
        List.of(),
        RenderCommandGroup.root(List.of())
    );

    private final RenderCommandGroup textRoot;
    private volatile List<RenderCommand> ordinaryText;
    private final RenderCommandGroup imageRoot;
    private volatile List<RenderCommand> imageCommands;

    public RenderCommandPipeline(
        List<RenderCommand> ordinaryText,
        RenderCommandGroup imageRoot
    ) {
        this(ordinaryText, imageRoot, null);
    }

    private RenderCommandPipeline(
        List<RenderCommand> ordinaryText,
        RenderCommandGroup imageRoot,
        List<RenderCommand> preparedImageCommands
    ) {
        ArrayList<RenderCommand> orderedText = new ArrayList<>(
            Objects.requireNonNull(ordinaryText, "ordinaryText")
        );
        for (RenderCommand command : orderedText) {
            if (command.phase() != RenderPhase.ORDINARY_TEXT) {
                throw new IllegalArgumentException(
                    "Text pass may only contain ORDINARY_TEXT commands"
                );
            }
        }
        orderedText.sort(Comparator.comparingInt(RenderCommand::sourceIndex));
        this.ordinaryText = List.copyOf(orderedText);
        this.textRoot = RenderCommandGroup.flowRoot(this.ordinaryText);
        this.imageRoot = Objects.requireNonNull(imageRoot, "imageRoot");
        if (imageRoot.containsPhase(RenderPhase.ORDINARY_TEXT)) {
            throw new IllegalArgumentException(
                "Image stacking contexts cannot contain ORDINARY_TEXT commands"
            );
        }
        this.imageCommands = preparedImageCommands != null
            ? List.copyOf(preparedImageCommands)
            : null;
    }

    public RenderCommandPipeline(
        RenderCommandGroup textRoot,
        RenderCommandGroup imageRoot
    ) {
        this.textRoot = Objects.requireNonNull(textRoot, "textRoot");
        if (!textRoot.containsOnlyPhase(RenderPhase.ORDINARY_TEXT)) {
            throw new IllegalArgumentException(
                "Text flow may only contain ORDINARY_TEXT commands"
            );
        }
        this.ordinaryText = null;
        this.imageRoot = Objects.requireNonNull(imageRoot, "imageRoot");
        if (imageRoot.containsPhase(RenderPhase.ORDINARY_TEXT)) {
            throw new IllegalArgumentException(
                "Image stacking contexts cannot contain ORDINARY_TEXT commands"
            );
        }
        this.imageCommands = null;
    }

    public static RenderCommandPipeline empty() {
        return EMPTY;
    }

    public List<RenderCommand> ordinaryTextCommands() {
        List<RenderCommand> commands = ordinaryText;
        if (commands != null) return commands;
        synchronized (this) {
            commands = ordinaryText;
            if (commands == null) {
                commands = textRoot.commands();
                ordinaryText = commands;
            }
            return commands;
        }
    }

    public List<RenderCommand> imageCommands() {
        List<RenderCommand> commands = imageCommands;
        if (commands != null) return commands;
        synchronized (this) {
            commands = imageCommands;
            if (commands == null) {
                commands = imageRoot.commands();
                imageCommands = commands;
            }
            return commands;
        }
    }

    public RenderCommandGroup imageRoot() {
        return imageRoot;
    }

    public RenderCommandGroup textRoot() {
        return textRoot;
    }

    public RenderCommandPipeline visibleWithin(TipRect viewport) {
        Objects.requireNonNull(viewport, "viewport");
        RenderCommandGroup visibleText = textRoot.visibleWithin(viewport);
        RenderCommandGroup visibleImage = imageRoot.visibleWithin(viewport);
        if (visibleText == textRoot && visibleImage == imageRoot) return this;
        return new RenderCommandPipeline(visibleText, visibleImage);
    }

    public void executeText(Consumer<String> sink) {
        Objects.requireNonNull(sink, "sink");
        for (RenderCommand command : ordinaryTextCommands()) {
            command.execute(RenderPass.TEXT, sink);
        }
    }

    public void executeImage(Consumer<String> sink) {
        Objects.requireNonNull(sink, "sink");
        for (RenderCommand command : imageCommands()) {
            command.execute(RenderPass.IMAGE, sink);
        }
    }

}
