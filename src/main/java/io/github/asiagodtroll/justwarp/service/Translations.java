package io.github.asiagodtroll.justwarp.service;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import io.github.asiagodtroll.justwarp.persistence.JsonStore;

import java.io.IOException;
import java.util.Map;

public final class Translations {
    private Map<String, String> selected = Map.of();
    private Map<String, String> english = Map.of();

    public void reload(JsonStore store, String locale) throws IOException {
        Map<String, String> newEnglish = store.loadLanguage("en_US");
        Map<String, String> newSelected = locale.equals("en_US") ? newEnglish : store.loadLanguage(locale);
        english = newEnglish;
        selected = newSelected;
    }

    public String text(String key, Object... arguments) {
        String pattern = pattern(key);
        try {
            return String.format(pattern, arguments);
        } catch (RuntimeException ignored) {
            return pattern;
        }
    }

    public Component message(String key, Object... arguments) {
        String pattern = pattern(key);
        int offset = 0;
        MutableComponent content = Component.empty().withStyle(ChatFormatting.WHITE)
                .withStyle(style -> style.withItalic(false));
        for (Object argument : arguments) {
            int placeholder = pattern.indexOf("%s", offset);
            if (placeholder < 0) {
                return plainMessage(key, arguments);
            }
            content.append(Component.literal(pattern.substring(offset, placeholder)));
            content.append(Component.literal(String.valueOf(argument))
                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
                    .withStyle(style -> style.withItalic(false)));
            offset = placeholder + 2;
        }
        content.append(Component.literal(pattern.substring(offset)));
        return prefixed(content);
    }

    public Component prefixed(Component content) {
        return Component.literal("[").withStyle(ChatFormatting.DARK_GRAY)
                .withStyle(style -> style.withItalic(false))
                .append(Component.literal(text("mod.name")).withStyle(ChatFormatting.LIGHT_PURPLE)
                        .withStyle(style -> style.withItalic(false)))
                .append(Component.literal("] ").withStyle(ChatFormatting.DARK_GRAY)
                        .withStyle(style -> style.withItalic(false)))
                .append(content);
    }

    private Component plainMessage(String key, Object... arguments) {
        return prefixed(Component.literal(text(key, arguments))
                .withStyle(ChatFormatting.WHITE)
                .withStyle(style -> style.withItalic(false)));
    }

    private String pattern(String key) {
        return selected.getOrDefault(key, english.getOrDefault(key, key));
    }
}
