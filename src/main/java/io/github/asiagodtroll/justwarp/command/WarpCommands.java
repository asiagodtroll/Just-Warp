package io.github.asiagodtroll.justwarp.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import io.github.asiagodtroll.justwarp.domain.WarpLocation;
import io.github.asiagodtroll.justwarp.gui.WarpGui;
import io.github.asiagodtroll.justwarp.service.JustWarpService;

import static net.minecraft.commands.Commands.argument;
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
                        .then(argument("name", StringArgumentType.string())
                                .then(argument("icon", StringArgumentType.string())
                                        .suggests(suggestions::iconReferences)
                                        .executes(context -> addTyped(context, null))
                                        .then(argument("group", StringArgumentType.string())
                                                .suggests(suggestions::groupsOrNone)
                                                .executes(context -> addTyped(context, CommandSupport.nullableGroup(
                                                        CommandSupport.value(context, "group")))))))
                        .then(argument("arguments", StringArgumentType.greedyString()).executes(this::add)))
                .then(literal("del").requires(support::hasAdminPermission).then(nameArgument().executes(this::delete)))
                .then(literal("set").requires(support::hasAdminPermission)
                        .then(argument("name", StringArgumentType.string()).suggests(suggestions::warps)
                                .then(literal("name").then(argument("value", StringArgumentType.string())
                                        .executes(this::setNameTyped)))
                                .then(literal("group").then(argument("value", StringArgumentType.string())
                                        .suggests(suggestions::groupsOrNone).executes(this::setGroupTyped)))
                                .then(literal("icon").then(argument("value", StringArgumentType.string())
                                        .suggests(suggestions::iconReferences).executes(this::setIconTyped)))
                                .then(literal("author").then(argument("value", StringArgumentType.greedyString())
                                        .executes(this::setAuthorTyped)))
                                .then(literal("description")
                                        .then(argument("value", StringArgumentType.greedyString())
                                                .executes(this::setDescriptionTyped)))
                                .then(literal("position").executes(this::setPositionTyped)))
                        .then(argument("arguments", StringArgumentType.greedyString()).executes(this::set))));
    }

    private com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> nameArgument() {
        return argument("name", StringArgumentType.greedyString()).suggests(suggestions::warps);
    }

    private int open(CommandContext<CommandSourceStack> context) {
        return support.openGui(context, player -> gui.openWarps(player, 0));
    }

    private int add(CommandContext<CommandSourceStack> context) {
        String[] arguments = CommandInput.tokens(context, "arguments", 4);
        if (arguments.length < 2 || arguments.length > 3) {
            return support.fail(context.getSource(), "error.arguments");
        }
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            return support.fail(context.getSource(), "error.player_only");
        }
        String name = arguments[0];
        String icon = arguments[1];
        String group = arguments.length == 3 ? CommandSupport.nullableGroup(arguments[2]) : null;
        return add(context, player, name, icon, group);
    }

    private int addTyped(CommandContext<CommandSourceStack> context, String group) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            return support.fail(context.getSource(), "error.player_only");
        }
        return add(context, player, CommandSupport.value(context, "name"),
                CommandSupport.value(context, "icon"), group);
    }

    private int add(CommandContext<CommandSourceStack> context, ServerPlayer player, String name, String icon,
                    String group) {
        String author = player.getGameProfile().name();
        WarpLocation location = playerLocation(player);
        return support.execute(context.getSource(), () -> manager.addWarp(name, author, group, icon, location),
                "success.warp_added", name);
    }

    private int setNameTyped(CommandContext<CommandSourceStack> context) {
        return setName(context, CommandSupport.value(context, "name"), CommandSupport.value(context, "value"));
    }

    private int setGroupTyped(CommandContext<CommandSourceStack> context) {
        return setGroup(context, CommandSupport.value(context, "name"), CommandSupport.value(context, "value"));
    }

    private int setIconTyped(CommandContext<CommandSourceStack> context) {
        return setIcon(context, CommandSupport.value(context, "name"), CommandSupport.value(context, "value"));
    }

    private int setAuthorTyped(CommandContext<CommandSourceStack> context) {
        return setAuthor(context, CommandSupport.value(context, "name"), CommandSupport.value(context, "value"));
    }

    private int setDescriptionTyped(CommandContext<CommandSourceStack> context) {
        return setDescription(context, CommandSupport.value(context, "name"), CommandSupport.value(context, "value"));
    }

    private int setPositionTyped(CommandContext<CommandSourceStack> context) {
        return setPosition(context, CommandSupport.value(context, "name"));
    }

    private int set(CommandContext<CommandSourceStack> context) {
        String[] arguments = CommandInput.tokens(context, "arguments", 3);
        if (arguments.length == 2 && arguments[1].equals("position")) {
            return setPosition(context, arguments[0]);
        }
        if (arguments.length != 3) {
            return support.fail(context.getSource(), "error.arguments");
        }
        return switch (arguments[1]) {
            case "name" -> setName(context, arguments[0], arguments[2]);
            case "group" -> setGroup(context, arguments[0], arguments[2]);
            case "icon" -> setIcon(context, arguments[0], arguments[2]);
            case "author" -> setAuthor(context, arguments[0], arguments[2]);
            case "description" -> setDescription(context, arguments[0], arguments[2]);
            default -> support.fail(context.getSource(), "error.arguments");
        };
    }

    private int delete(CommandContext<CommandSourceStack> context) {
        String name = CommandSupport.value(context, "name");
        return support.execute(context.getSource(), () -> manager.deleteWarp(name), "success.warp_deleted", name);
    }

    private int setName(CommandContext<CommandSourceStack> context, String name, String value) {
        return support.execute(context.getSource(), () -> manager.renameWarp(name, value),
                "success.warp_updated", name);
    }

    private int setGroup(CommandContext<CommandSourceStack> context, String name, String group) {
        String value = CommandSupport.nullableGroup(group);
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
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            return support.fail(context.getSource(), "error.player_only");
        }
        WarpLocation location = playerLocation(player);
        return support.execute(context.getSource(), () -> manager.setWarpPosition(name, location),
                "success.warp_position_updated", name);
    }

    private static WarpLocation playerLocation(ServerPlayer player) {
        return new WarpLocation(player.level().dimension().identifier().toString(),
                player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
    }
}
