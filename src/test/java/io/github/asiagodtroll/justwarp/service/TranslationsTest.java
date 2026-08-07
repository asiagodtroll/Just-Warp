package io.github.asiagodtroll.justwarp.service;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import io.github.asiagodtroll.justwarp.persistence.JsonStore;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TranslationsTest {
    @TempDir Path temporary;

    @Test
    void loadsBundledLocaleFallsBackToKeyAndFormatsArguments() throws Exception {
        JsonStore store = new JsonStore(temporary.resolve("justwarp"));
        store.ensureDefaults();
        Translations translations = new Translations();
        translations.reload(store, "zh_TW");

        assertEquals("傳送點", translations.text("gui.root.title"));
        assertEquals("已新增傳送點「home」", translations.text("success.warp_added", "home"));
        Component message = translations.message("success.warp_added", "home");
        assertEquals("[JustWarp] 已新增傳送點「home」", message.getString());
        assertFalse(message.getStyle().isItalic());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY), message.getStyle().getColor());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.LIGHT_PURPLE),
                message.getSiblings().get(0).getStyle().getColor());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.WHITE),
                message.getSiblings().get(2).getStyle().getColor());
        assertHighlightedArgument(message, "home");
        assertHighlightedArgument(translations.message("success.warp_deleted", "home"), "home");
        assertHighlightedArgument(translations.message("success.teleported", "home"), "home");
        assertTrue(translations.text("error.storage_unavailable", "config.json 格式錯誤")
                .contains("config.json 格式錯誤"));
        assertEquals("missing.key", translations.text("missing.key"));
    }

    private static void assertHighlightedArgument(Component message, String expected) {
        Component content = message.getSiblings().get(2);
        Component argument = content.getSiblings().get(1);
        assertEquals(expected, argument.getString());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.YELLOW), argument.getStyle().getColor());
        assertTrue(argument.getStyle().isBold());
        assertFalse(argument.getStyle().isItalic());
    }
}
