package io.github.asiagodtroll.justwarp.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.Permissions;

final class AdminPermissionPolicy {
    private AdminPermissionPolicy() {}

    static boolean allows(CommandSourceStack source, int level) {
        return source.permissions().hasPermission(permission(level));
    }

    private static Permission permission(int level) {
        return switch (level) {
            case 1 -> Permissions.COMMANDS_MODERATOR;
            case 2 -> Permissions.COMMANDS_GAMEMASTER;
            case 3 -> Permissions.COMMANDS_ADMIN;
            default -> Permissions.COMMANDS_OWNER;
        };
    }
}
