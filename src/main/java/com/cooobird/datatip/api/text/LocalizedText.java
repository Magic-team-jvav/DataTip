package com.cooobird.datatip.api.text;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 可按当前客户端语言解析的文本值。
 */
public final class LocalizedText implements Component {
    private final Component fallback;
    private final Map<String, Component> translations;

    private LocalizedText(Component fallback, Map<String, Component> translations) {
        this.fallback = Objects.requireNonNull(fallback, "fallback");
        this.translations = Collections.unmodifiableMap(new LinkedHashMap<>(translations));
    }

    public static LocalizedText empty() {
        return literal("");
    }

    public static LocalizedText literal(String text) {
        return component(Component.literal(text != null ? text : ""));
    }

    public static LocalizedText component(Component component) {
        return new LocalizedText(Objects.requireNonNull(component, "component"), Map.of());
    }

    public static LocalizedText translatable(String translationKey) {
        return component(Component.translatable(Objects.requireNonNull(translationKey, "translationKey")));
    }

    public static LocalizedText languages(Map<String, Component> translations) {
        Objects.requireNonNull(translations, "translations");
        LinkedHashMap<String, Component> normalized = new LinkedHashMap<>();
        translations.forEach((language, text) -> {
            if (language != null && text != null) {
                normalized.put(normalizeLanguage(language), text);
            }
        });

        Component fallback = normalized.get("en_us");
        if (fallback == null && !normalized.isEmpty()) fallback = normalized.values().iterator().next();
        return new LocalizedText(fallback != null ? fallback : Component.empty(), normalized);
    }

    public Component resolve() {
        if (translations.isEmpty()) return fallback;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getLanguageManager() == null) return fallback;
        String language = normalizeLanguage(minecraft.getLanguageManager().getSelected());
        return translations.getOrDefault(language, fallback);
    }

    @Override
    public String getString() {
        return resolve().getString();
    }

    @Override
    public Style getStyle() {
        return resolve().getStyle();
    }

    @Override
    public ComponentContents getContents() {
        return resolve().getContents();
    }

    @Override
    public java.util.List<Component> getSiblings() {
        return resolve().getSiblings();
    }

    @Override
    public FormattedCharSequence getVisualOrderText() {
        return resolve().getVisualOrderText();
    }

    public boolean isEmpty() {
        return getString().isEmpty();
    }

    public Map<String, Component> translations() {
        return translations;
    }

    public Component fallback() {
        return fallback;
    }

    public static LocalizedText ofNullable(@Nullable Component component) {
        return component != null ? component(component) : empty();
    }

    private static String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) return "en_us";
        return language.trim().toLowerCase(Locale.ROOT);
    }
}
