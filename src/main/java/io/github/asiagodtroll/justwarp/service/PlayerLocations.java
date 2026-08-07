package io.github.asiagodtroll.justwarp.service;

import net.minecraft.server.level.ServerPlayer;
import io.github.asiagodtroll.justwarp.domain.WarpLocation;

public final class PlayerLocations {
    private PlayerLocations() {}

    public static WarpLocation capture(ServerPlayer player) {
        return new WarpLocation(player.level().dimension().identifier().toString(), player.getX(), player.getY(),
                player.getZ(), player.getYRot(), player.getXRot());
    }
}
