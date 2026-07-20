package com.cooobird.datatip.internal.text;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

/**
 * 按当前客户端语言选择映射中的文本值。
 */
public final class LanguageTextSelector {
    private LanguageTextSelector() {
    }

    /**
     * 使用当前客户端语言选择文本值。
     */
    @Nullable
    public static <T> T selectCurrent(Map<String, T> values) {
        return select(values, currentLanguage());
    }

    /**
     * 使用指定语言选择文本值，供确定性测试和非渲染路径复用。
     */
    @Nullable
    static <T> T select(Map<String, T> values, @Nullable String language) {
        return values.get(normalize(language));
    }

    private static String currentLanguage() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getLanguageManager() == null) return "en_us";
        return normalize(minecraft.getLanguageManager().getSelected());
    }

    private static String normalize(@Nullable String language) {
        if (language == null || language.isBlank()) return "en_us";
        return language.trim().toLowerCase(Locale.ROOT);
    }
}

