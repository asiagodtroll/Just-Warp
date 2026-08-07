package io.github.asiagodtroll.justwarp.command;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

final class HelpRenderer {
    private HelpRenderer() {}

    static List<Component> section(String title, String content, ChatFormatting accent) {
        List<Component> lines = new ArrayList<>();
        lines.add(nonItalic("──── ").withStyle(ChatFormatting.DARK_GRAY)
                .append(nonItalic(title).withStyle(accent, ChatFormatting.BOLD))
                .append(nonItalic(" ────").withStyle(ChatFormatting.DARK_GRAY)));
        for (String line : content.lines().toList()) {
            lines.add(commandLine(line, accent));
        }
        return List.copyOf(lines);
    }

    private static Component commandLine(String line, ChatFormatting accent) {
        int separator = line.indexOf(": ");
        String command = separator < 0 ? line : line.substring(0, separator);
        MutableComponent rendered = nonItalic(" • ").withStyle(ChatFormatting.DARK_GRAY)
                .append(nonItalic(command).withStyle(accent));
        if (separator >= 0) {
            rendered = rendered
                    .append(nonItalic(": ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(nonItalic(line.substring(separator + 2)).withStyle(ChatFormatting.WHITE));
        }
        return rendered;
    }

    private static MutableComponent nonItalic(String text) {
        return Component.literal(text).withStyle(style -> style.withItalic(false));
    }
}
