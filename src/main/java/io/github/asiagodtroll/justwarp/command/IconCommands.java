package io.github.asiagodtroll.justwarp.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import io.github.asiagodtroll.justwarp.gui.IconGui;
import io.github.asiagodtroll.justwarp.service.JustWarpService;

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
                        .then(CommandArguments.text("name")
                                .then(CommandArguments.opaqueTail("base64")
                                        .executes(this::addTyped))))
                .then(literal("del").requires(support::hasAdminPermission)
                        .then(iconNameArgument().executes(this::delete)))
                .then(literal("set").requires(support::hasAdminPermission)
                        .then(CommandArguments.text("name").suggests(suggestions::customIcons)
                                .then(CommandArguments.opaqueTail("new_base64")
                                        .executes(this::setTyped)))));
    }

    private int open(CommandContext<CommandSourceStack> context) {
        return support.openGui(context, player -> gui.open(player, 0));
    }

    private int addTyped(CommandContext<CommandSourceStack> context) {
        return add(context, CommandArguments.value(context, "name"), CommandArguments.value(context, "base64"));
    }

    private int add(CommandContext<CommandSourceStack> context, String name, String base64) {
        return support.execute(context.getSource(), () -> manager.addIcon(name, base64),
                "success.custom_icon_added", name);
    }

    private int delete(CommandContext<CommandSourceStack> context) {
        String name = CommandArguments.value(context, "name");
        return support.execute(context.getSource(), () -> manager.deleteIcon(name),
                "success.custom_icon_deleted", name);
    }

    private int setTyped(CommandContext<CommandSourceStack> context) {
        return set(context, CommandArguments.value(context, "name"),
                CommandArguments.value(context, "new_base64"));
    }

    private int set(CommandContext<CommandSourceStack> context, String name, String base64) {
        return support.execute(context.getSource(), () -> manager.setIconBase64(name, base64),
                "success.custom_icon_updated", name);
    }

    private com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> iconNameArgument() {
        return CommandArguments.text("name").suggests(suggestions::customIcons);
    }

}
