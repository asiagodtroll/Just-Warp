package io.github.asiagodtroll.justwarp.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import io.github.asiagodtroll.justwarp.gui.WarpGui;
import io.github.asiagodtroll.justwarp.service.JustWarpService;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

final class GroupCommands {
    private final JustWarpService manager;
    private final CommandSupport support;
    private final CommandSuggestions suggestions;
    private final WarpGui gui;

    GroupCommands(JustWarpService manager, WarpGui gui, CommandSupport support, CommandSuggestions suggestions) {
        this.manager = manager;
        this.gui = gui;
        this.support = support;
        this.suggestions = suggestions;
    }

    void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(literal("group").executes(this::open)
                .then(literal("add").requires(support::hasAdminPermission)
                        .then(argument("name", StringArgumentType.string())
                                .then(argument("icon", StringArgumentType.string())
                                        .suggests(suggestions::iconReferences).executes(this::addTyped)))
                        .then(argument("arguments", StringArgumentType.greedyString()).executes(this::add)))
                .then(literal("del").requires(support::hasAdminPermission).then(nameArgument().executes(this::delete)))
                .then(literal("set").requires(support::hasAdminPermission)
                        .then(argument("name", StringArgumentType.string()).suggests(suggestions::groups)
                                .then(literal("name").then(argument("value", StringArgumentType.string())
                                        .executes(this::setNameTyped)))
                                .then(literal("icon").then(argument("value", StringArgumentType.string())
                                        .suggests(suggestions::iconReferences).executes(this::setIconTyped))))
                        .then(argument("arguments", StringArgumentType.greedyString()).executes(this::set))));
    }

    private com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> nameArgument() {
        return argument("name", StringArgumentType.greedyString()).suggests(suggestions::groups);
    }

    private int open(CommandContext<CommandSourceStack> context) {
        return support.openGui(context, player -> gui.openGroups(player, 0));
    }

    private int add(CommandContext<CommandSourceStack> context) {
        String[] arguments = CommandInput.tokens(context, "arguments", 3);
        if (arguments.length != 2) {
            return support.fail(context.getSource(), "error.arguments");
        }
        return add(context, arguments[0], arguments[1]);
    }

    private int addTyped(CommandContext<CommandSourceStack> context) {
        return add(context, CommandSupport.value(context, "name"), CommandSupport.value(context, "icon"));
    }

    private int add(CommandContext<CommandSourceStack> context, String name, String icon) {
        return support.execute(context.getSource(), () -> manager.addGroup(name, icon),
                "success.group_added", name);
    }

    private int setNameTyped(CommandContext<CommandSourceStack> context) {
        return setName(context, CommandSupport.value(context, "name"), CommandSupport.value(context, "value"));
    }

    private int setIconTyped(CommandContext<CommandSourceStack> context) {
        return setIcon(context, CommandSupport.value(context, "name"), CommandSupport.value(context, "value"));
    }

    private int set(CommandContext<CommandSourceStack> context) {
        String[] arguments = CommandInput.tokens(context, "arguments", 3);
        if (arguments.length != 3) {
            return support.fail(context.getSource(), "error.arguments");
        }
        return switch (arguments[1]) {
            case "name" -> setName(context, arguments[0], arguments[2]);
            case "icon" -> setIcon(context, arguments[0], arguments[2]);
            default -> support.fail(context.getSource(), "error.arguments");
        };
    }

    private int delete(CommandContext<CommandSourceStack> context) {
        String name = CommandSupport.value(context, "name");
        return support.execute(context.getSource(), () -> manager.deleteGroup(name), "success.group_deleted", name);
    }

    private int setName(CommandContext<CommandSourceStack> context, String name, String value) {
        return support.execute(context.getSource(), () -> manager.renameGroup(name, value),
                "success.group_updated", name);
    }

    private int setIcon(CommandContext<CommandSourceStack> context, String name, String value) {
        return support.execute(context.getSource(), () -> manager.setGroupIcon(name, value),
                "success.group_updated", name);
    }
}
