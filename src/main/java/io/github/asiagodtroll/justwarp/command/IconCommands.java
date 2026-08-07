package io.github.asiagodtroll.justwarp.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import io.github.asiagodtroll.justwarp.gui.IconGui;
import io.github.asiagodtroll.justwarp.service.JustWarpService;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

final class IconCommands {
    private final JustWarpService manager;
    private final IconGui gui;
    private final CommandSupport support;
    private final CommandSuggestions suggestions;

    IconCommands(JustWarpService manager, IconGui gui, CommandSupport support, CommandSuggestions suggestions) {
        this.manager = manager;
        this.gui = gui;
        this.support = support;
        this.suggestions = suggestions;
    }

    void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(literal("icon").executes(this::open)
                .then(literal("add").requires(support::hasAdminPermission)
                        .then(argument("name", StringArgumentType.string())
                                .then(argument("base64", StringArgumentType.greedyString())
                                        .executes(this::addTyped)))
                        .then(argument("arguments", StringArgumentType.greedyString()).executes(this::add)))
                .then(literal("del").requires(support::hasAdminPermission)
                        .then(iconNameArgument().executes(this::delete)))
                .then(literal("set").requires(support::hasAdminPermission)
                        .then(argument("name", StringArgumentType.string()).suggests(suggestions::customIcons)
                                .then(argument("new_base64", StringArgumentType.greedyString())
                                        .executes(this::setTyped)))
                        .then(argument("arguments", StringArgumentType.greedyString()).executes(this::set))));
    }

    private int open(CommandContext<CommandSourceStack> context) {
        return support.openGui(context, player -> gui.open(player, 0));
    }

    private int add(CommandContext<CommandSourceStack> context) {
        String[] arguments = CommandInput.tokens(context, "arguments", 2);
        if (arguments.length != 2) {
            return support.fail(context.getSource(), "error.arguments");
        }
        return add(context, arguments[0], arguments[1]);
    }

    private int addTyped(CommandContext<CommandSourceStack> context) {
        return add(context, CommandSupport.value(context, "name"), CommandSupport.value(context, "base64"));
    }

    private int add(CommandContext<CommandSourceStack> context, String name, String base64) {
        return support.execute(context.getSource(), () -> manager.addIcon(name, base64),
                "success.custom_icon_added", name);
    }

    private int delete(CommandContext<CommandSourceStack> context) {
        String name = CommandSupport.value(context, "name");
        return support.execute(context.getSource(), () -> manager.deleteIcon(name),
                "success.custom_icon_deleted", name);
    }

    private int set(CommandContext<CommandSourceStack> context) {
        String[] arguments = CommandInput.tokens(context, "arguments", 2);
        if (arguments.length != 2) {
            return support.fail(context.getSource(), "error.arguments");
        }
        return set(context, arguments[0], arguments[1]);
    }

    private int setTyped(CommandContext<CommandSourceStack> context) {
        return set(context, CommandSupport.value(context, "name"), CommandSupport.value(context, "new_base64"));
    }

    private int set(CommandContext<CommandSourceStack> context, String name, String base64) {
        return support.execute(context.getSource(), () -> manager.setIconBase64(name, base64),
                "success.custom_icon_updated", name);
    }

    private com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> iconNameArgument() {
        return argument("name", StringArgumentType.greedyString()).suggests(suggestions::customIcons);
    }
}
