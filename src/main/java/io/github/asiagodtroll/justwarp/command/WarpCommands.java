package io.github.asiagodtroll.justwarp.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import io.github.asiagodtroll.justwarp.domain.WarpLocation;
import io.github.asiagodtroll.justwarp.gui.WarpGui;
import io.github.asiagodtroll.justwarp.service.JustWarpService;
import io.github.asiagodtroll.justwarp.service.PlayerLocations;

import static net.minecraft.commands.Commands.literal;

final class WarpCommands {
    private final JustWarpService manager;
    private final CommandSupport support;
    private final CommandSuggestions suggestions;
    private final WarpGui gui;

    WarpCommands(JustWarpService manager, WarpGui gui, CommandSupport support, CommandSuggestions suggestions) {
        this.manager = manager;
        this.gui = gui;
        this.support = support;
        this.suggestions = suggestions;
    }

    void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(literal("warp").executes(this::open)
                .then(literal("add").requires(support::hasAdminPermission)
                        .then(CommandArguments.text("name")
                                .then(CommandArguments.text("icon")
                                        .suggests(suggestions::iconReferences)
                                        .executes(context -> addTyped(context, null))
                                        .then(CommandArguments.text("group")
                                                .suggests(suggestions::groupsOrNone)
                                                .executes(context -> addTyped(context, CommandArguments.nullableGroup(
                                                        CommandArguments.value(context, "group"))))))))
                .then(literal("del").requires(support::hasAdminPermission)
                        .then(nameArgument().executes(this::delete)))
                .then(literal("set").requires(support::hasAdminPermission)
                        .then(CommandArguments.text("name").suggests(suggestions::warps)
                                .then(literal("name").then(CommandArguments.text("value")
                                        .executes(this::setNameTyped)))
                                .then(literal("group").then(CommandArguments.text("value")
                                        .suggests(suggestions::groupsOrNone).executes(this::setGroupTyped)))
                                .then(literal("icon").then(CommandArguments.text("value")
                                        .suggests(suggestions::iconReferences).executes(this::setIconTyped)))
                                .then(literal("author").then(CommandArguments.text("value")
                                        .executes(this::setAuthorTyped)))
                                .then(literal("description")
                                        .then(CommandArguments.text("value")
                                                .executes(this::setDescriptionTyped)))
                                .then(literal("position").executes(this::setPositionTyped)))));
    }

    private com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> nameArgument() {
        return CommandArguments.text("name").suggests(suggestions::warps);
    }

    private int open(CommandContext<CommandSourceStack> context) {
        return support.openGui(context, player -> gui.openWarps(player, 0));
    }

    private int addTyped(CommandContext<CommandSourceStack> context, String group) {
        return support.withPlayer(context, player -> add(context, player, CommandArguments.value(context, "name"),
                CommandArguments.value(context, "icon"), group));
    }

    private int add(CommandContext<CommandSourceStack> context, ServerPlayer player, String name, String icon,
                    String group) {
        String author = player.getGameProfile().name();
        WarpLocation location = PlayerLocations.capture(player);
        return support.execute(context.getSource(), () -> manager.addWarp(name, author, group, icon, location),
                "success.warp_added", name);
    }

    private int setNameTyped(CommandContext<CommandSourceStack> context) {
        return setName(context, CommandArguments.value(context, "name"), CommandArguments.value(context, "value"));
    }

    private int setGroupTyped(CommandContext<CommandSourceStack> context) {
        return setGroup(context, CommandArguments.value(context, "name"), CommandArguments.value(context, "value"));
    }

    private int setIconTyped(CommandContext<CommandSourceStack> context) {
        return setIcon(context, CommandArguments.value(context, "name"), CommandArguments.value(context, "value"));
    }

    private int setAuthorTyped(CommandContext<CommandSourceStack> context) {
        return setAuthor(context, CommandArguments.value(context, "name"), CommandArguments.value(context, "value"));
    }

    private int setDescriptionTyped(CommandContext<CommandSourceStack> context) {
        return setDescription(context, CommandArguments.value(context, "name"),
                CommandArguments.value(context, "value"));
    }

    private int setPositionTyped(CommandContext<CommandSourceStack> context) {
        return setPosition(context, CommandArguments.value(context, "name"));
    }

    private int delete(CommandContext<CommandSourceStack> context) {
        String name = CommandArguments.value(context, "name");
        return support.execute(context.getSource(), () -> manager.deleteWarp(name), "success.warp_deleted", name);
    }

    private int setName(CommandContext<CommandSourceStack> context, String name, String value) {
        return support.execute(context.getSource(), () -> manager.renameWarp(name, value),
                "success.warp_updated", name);
    }

    private int setGroup(CommandContext<CommandSourceStack> context, String name, String group) {
        String value = CommandArguments.nullableGroup(group);
        return support.execute(context.getSource(), () -> manager.setWarpGroup(name, value),
                "success.warp_updated", name);
    }

    private int setIcon(CommandContext<CommandSourceStack> context, String name, String value) {
        return support.execute(context.getSource(), () -> manager.setWarpIcon(name, value),
                "success.warp_updated", name);
    }

    private int setAuthor(CommandContext<CommandSourceStack> context, String name, String value) {
        return support.execute(context.getSource(), () -> manager.setWarpAuthor(name, value),
                "success.warp_updated", name);
    }

    private int setDescription(CommandContext<CommandSourceStack> context, String name, String value) {
        return support.execute(context.getSource(), () -> manager.setWarpDescription(name, value),
                "success.warp_updated", name);
    }

    private int setPosition(CommandContext<CommandSourceStack> context, String name) {
        return support.withPlayer(context, player -> {
            WarpLocation location = PlayerLocations.capture(player);
            return support.execute(context.getSource(), () -> manager.setWarpPosition(name, location),
                    "success.warp_position_updated", name);
        });
    }

}
