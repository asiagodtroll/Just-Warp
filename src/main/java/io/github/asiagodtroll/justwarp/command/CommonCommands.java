package io.github.asiagodtroll.justwarp.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import io.github.asiagodtroll.justwarp.gui.WarpGui;
import io.github.asiagodtroll.justwarp.service.JustWarpService;

import java.io.IOException;

import static net.minecraft.commands.Commands.literal;

final class CommonCommands {
    private final JustWarpService manager;
    private final WarpGui gui;
    private final CommandSupport support;

    CommonCommands(JustWarpService manager, WarpGui gui, CommandSupport support) {
        this.manager = manager;
        this.gui = gui;
        this.support = support;
    }

    void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.executes(this::open)
                .then(literal("help").executes(this::help))
                .then(literal("back").executes(this::back))
                .then(literal("reload").requires(support::hasAdminPermission).executes(this::reload));
    }

    private int open(CommandContext<CommandSourceStack> context) {
        return support.openGui(context, player -> gui.openRoot(player, 0));
    }

    private int help(CommandContext<CommandSourceStack> context) {
        HelpRenderer.section(manager.translations().text("help.user.title"),
                        manager.translations().text("help.user"), ChatFormatting.AQUA)
                .stream().map(manager.translations()::prefixed)
                .forEach(context.getSource()::sendSystemMessage);
        if (support.hasAdminPermission(context.getSource())) {
            HelpRenderer.section(manager.translations().text("help.admin.title"),
                            manager.translations().text("help.admin"), ChatFormatting.GOLD)
                    .stream().map(manager.translations()::prefixed)
                    .forEach(context.getSource()::sendSystemMessage);
        }
        return 1;
    }

    private int back(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            return support.fail(context.getSource(), "error.player_only");
        }
        return support.execute(context.getSource(), () -> manager.back(player), "success.back");
    }

    private int reload(CommandContext<CommandSourceStack> context) {
        try {
            manager.reload(context.getSource().getServer());
            return support.success(context.getSource(), "success.reloaded");
        } catch (IOException e) {
            return support.fail(context.getSource(), "error.reload", e.getMessage());
        }
    }
}
