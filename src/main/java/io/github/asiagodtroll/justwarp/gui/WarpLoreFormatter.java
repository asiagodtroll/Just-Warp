package io.github.asiagodtroll.justwarp.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import io.github.asiagodtroll.justwarp.domain.Warp;
import io.github.asiagodtroll.justwarp.service.Translations;

import java.util.List;
import java.util.Locale;

final class WarpLoreFormatter {
    private WarpLoreFormatter() {}

    static List<Component> format(Translations translations, Warp warp) {
        return List.of(
                field(translations.text("gui.warp.description"), warp.description()),
                field(translations.text("gui.warp.dimension"), warp.location().world()),
                field(translations.text("gui.warp.coordinate_x"), decimal(warp.location().x())),
                field(translations.text("gui.warp.coordinate_y"), decimal(warp.location().y())),
                field(translations.text("gui.warp.coordinate_z"), decimal(warp.location().z())),
                field(translations.text("gui.warp.author"), warp.author()),
                Component.literal(translations.text("gui.warp.lore"))
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD)
                        .withStyle(style -> style.withItalic(false)));
    }

    private static Component field(String heading, String content) {
        return Component.literal(heading).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)
                .withStyle(style -> style.withItalic(false))
                .append(Component.literal(": " + content).withStyle(ChatFormatting.WHITE)
                        .withStyle(style -> style.withBold(false).withItalic(false)));
    }

    private static String decimal(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
