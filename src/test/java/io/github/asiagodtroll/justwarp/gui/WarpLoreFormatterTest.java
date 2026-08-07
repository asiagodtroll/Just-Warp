package io.github.asiagodtroll.justwarp.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import io.github.asiagodtroll.justwarp.domain.Warp;
import io.github.asiagodtroll.justwarp.domain.WarpLocation;
import io.github.asiagodtroll.justwarp.persistence.JsonStore;
import io.github.asiagodtroll.justwarp.service.Translations;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarpLoreFormatterTest {
    @TempDir Path temporary;

    @Test
    void formatsDescriptionCoordinatesAuthorAndAction() throws Exception {
        Translations translations = translations();
        Warp warp = new Warp("home", "Steve", "My home", "minecraft:red_bed",
                new WarpLocation("minecraft:overworld", 1.25, 64, -3.5, 0, 0));

        List<Component> lore = WarpLoreFormatter.format(translations, warp);

        assertEquals(List.of(
                "說明: My home",
                "維度: minecraft:overworld",
                "X: 1.3",
                "Y: 64.0",
                "Z: -3.5",
                "作者: Steve",
                "點擊傳送"), lore.stream().map(Component::getString).toList());
        Component description = lore.getFirst();
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY), description.getStyle().getColor());
        assertTrue(description.getStyle().isBold());
        Component content = description.getSiblings().getFirst();
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.WHITE), content.getStyle().getColor());
        assertFalse(content.getStyle().isBold());
        assertFalse(content.getStyle().isItalic());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.DARK_PURPLE),
                lore.getLast().getStyle().getColor());
        assertTrue(lore.getLast().getStyle().isBold());
        assertFalse(lore.getLast().getStyle().isItalic());
    }

    @Test
    void keepsDescriptionLabelWhenDescriptionIsEmpty() throws Exception {
        Warp warp = new Warp("home", "Steve", "", "minecraft:red_bed",
                new WarpLocation("minecraft:overworld", 0, 64, 0, 0, 0));

        assertEquals("說明: ", WarpLoreFormatter.format(translations(), warp).getFirst().getString());
    }

    private Translations translations() throws Exception {
        JsonStore store = new JsonStore(temporary.resolve("justwarp"));
        store.ensureDefaults();
        Translations translations = new Translations();
        translations.reload(store, "zh_TW");
        return translations;
    }
}
