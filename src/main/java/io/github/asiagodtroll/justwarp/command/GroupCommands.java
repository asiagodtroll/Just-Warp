package io.github.asiagodtroll.justwarp.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import io.github.asiagodtroll.justwarp.gui.WarpGui;
import io.github.asiagodtroll.justwarp.service.JustWarpService;

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
                        .then(CommandArguments.text("name")
                                .then(CommandArguments.text("icon")
                                        .suggests(suggestions::iconReferences).executes(this::addTyped))))
                .then(literal("del").requires(support::hasAdminPermission)
                        .then(nameArgument().executes(this::delete)))
                .then(literal("set").requires(support::hasAdminPermission)
                        .then(CommandArguments.text("name").suggests(suggestions::groups)
                                .then(literal("name").then(CommandArguments.text("value")
                                        .executes(this::setNameTyped)))
                                .then(literal("icon").then(CommandArguments.text("value")
                                        .suggests(suggestions::iconReferences).executes(this::setIconTyped))))));
    }

    private com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> nameArgument() {
        return CommandArguments.text("name").suggests(suggestions::groups);
    }

    private int open(CommandContext<CommandSourceStack> context) {
        return support.openGui(context, player -> gui.openGroups(player, 0));
    }

    private int addTyped(CommandContext<CommandSourceStack> context) {
        return add(context, CommandArguments.value(context, "name"), CommandArguments.value(context, "icon"));
    }

    private int add(CommandContext<CommandSourceStack> context, String name, String icon) {
        return support.execute(context.getSource(), () -> manager.addGroup(name, icon),
                "success.group_added", name);
    }

    private int setNameTyped(CommandContext<CommandSourceStack> context) {
        return setName(context, CommandArguments.value(context, "name"), CommandArguments.value(context, "value"));
    }

    private int setIconTyped(CommandContext<CommandSourceStack> context) {
        return setIcon(context, CommandArguments.value(context, "name"), CommandArguments.value(context, "value"));
    }

    private int delete(CommandContext<CommandSourceStack> context) {
        String name = CommandArguments.value(context, "name");
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
