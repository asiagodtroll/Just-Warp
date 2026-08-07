package io.github.asiagodtroll.justwarp.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

final class CommandInput {
    private CommandInput() {}

    static String[] tokens(CommandContext<CommandSourceStack> context, String key, int limit) {
        return tokens(CommandSupport.value(context, key), limit);
    }

    static String[] tokens(String value, int limit) {
        String input = value.trim();
        return input.isEmpty() ? new String[0] : input.split("\\s+", limit);
    }
}
