package io.github.asiagodtroll.justwarp.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.argument;

final class CommandArguments {
    private CommandArguments() {}

    static RequiredArgumentBuilder<CommandSourceStack, String> text(String name) {
        return argument(name, StringArgumentType.string());
    }

    static RequiredArgumentBuilder<CommandSourceStack, String> opaqueTail(String name) {
        return argument(name, StringArgumentType.greedyString());
    }

    static String value(CommandContext<CommandSourceStack> context, String name) {
        return StringArgumentType.getString(context, name);
    }

    static String nullableGroup(String value) {
        return value.equalsIgnoreCase("none") ? null : value;
    }
}
