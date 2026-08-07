package io.github.asiagodtroll.justwarp.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import io.github.asiagodtroll.justwarp.gui.IconGui;
import io.github.asiagodtroll.justwarp.gui.WarpGui;
import io.github.asiagodtroll.justwarp.service.JustWarpService;

import static net.minecraft.commands.Commands.literal;

public final class JustWarpCommands {
    private final CommonCommands common;
    private final WarpCommands warp;
    private final GroupCommands group;
    private final IconCommands icon;

    public JustWarpCommands(JustWarpService manager, WarpGui warpGui, IconGui iconGui) {
        CommandSupport support = new CommandSupport(manager);
        CommandSuggestions suggestions = new CommandSuggestions(manager);
        common = new CommonCommands(manager, warpGui, support);
        warp = new WarpCommands(manager, warpGui, support, suggestions);
        group = new GroupCommands(manager, warpGui, support, suggestions);
        icon = new IconCommands(manager, iconGui, support, suggestions);
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = literal("jw");
        common.register(root);
        warp.register(root);
        group.register(root);
        icon.register(root);
        dispatcher.register(root);
    }
}
