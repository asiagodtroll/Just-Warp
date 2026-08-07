package io.github.asiagodtroll.justwarp.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import io.github.asiagodtroll.justwarp.service.WarpException;
import io.github.asiagodtroll.justwarp.service.JustWarpService;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

final class CommandSupport {
    private final JustWarpService manager;

    CommandSupport(JustWarpService manager) {
        this.manager = manager;
    }

    int execute(CommandSourceStack source, Action action, String successKey, Object... arguments) {
        try {
            action.run();
            return success(source, successKey, arguments);
        } catch (WarpException e) {
            return fail(source, e.translationKey(), e.arguments());
        } catch (IOException e) {
            return fail(source, "error.save", e.getMessage());
        }
    }

    int success(CommandSourceStack source, String key, Object... arguments) {
        source.sendSuccess(() -> manager.translations().message(key, arguments), false);
        return 1;
    }

    int fail(CommandSourceStack source, String key, Object... arguments) {
        source.sendFailure(manager.translations().message(key, arguments));
        return 0;
    }

    int openGui(CommandContext<CommandSourceStack> context, Consumer<ServerPlayer> opener) {
        return withPlayer(context, player -> {
            if (!manager.available()) {
                return fail(context.getSource(), "error.storage_unavailable", manager.unavailableReason());
            }
            opener.accept(player);
            return 1;
        });
    }

    int withPlayer(CommandContext<CommandSourceStack> context, ToIntFunction<ServerPlayer> action) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            return fail(context.getSource(), "error.player_only");
        }
        return action.applyAsInt(player);
    }

    boolean hasAdminPermission(CommandSourceStack source) {
        return AdminPermissionPolicy.allows(source, manager.config().adminPermissionLevel());
    }

    @FunctionalInterface
    interface Action { void run() throws WarpException, IOException; }
}
