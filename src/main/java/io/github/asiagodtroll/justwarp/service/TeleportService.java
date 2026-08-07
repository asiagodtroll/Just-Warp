package io.github.asiagodtroll.justwarp.service;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import io.github.asiagodtroll.justwarp.domain.Warp;
import io.github.asiagodtroll.justwarp.domain.WarpConfig;
import io.github.asiagodtroll.justwarp.domain.WarpLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

final class TeleportService {
    void teleport(ServerPlayer player, Warp warp, WarpConfig config) throws WarpException {
        Identifier worldId = Identifier.tryParse(warp.location().world());
        ServerLevel level = worldId == null ? null : player.level().getServer()
                .getLevel(ResourceKey.create(Registries.DIMENSION, worldId));
        if (level == null) {
            throw new WarpException("error.world_missing", warp.name());
        }
        WarpLocation target = warp.location();
        if (config.teleportSafety() == 3
                && !isSafe(level, BlockPos.containing(target.x(), target.y(), target.z()))) {
            throw new WarpException("error.unsafe", warp.name());
        }
        if (config.teleportSafety() == 2) {
            BlockPos safe = findSafe(level, BlockPos.containing(target.x(), target.y(), target.z()), config);
            if (safe == null) {
                throw new WarpException("error.no_safe_position", warp.name());
            }
            player.teleportTo(level, safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5,
                    Set.of(), target.yaw(), target.pitch(), false);
        } else {
            player.teleportTo(level, target.x(), target.y(), target.z(), Set.of(), target.yaw(), target.pitch(), false);
        }
    }

    private BlockPos findSafe(ServerLevel level, BlockPos origin, WarpConfig config) {
        if (isSafe(level, origin)) {
            return origin;
        }
        List<BlockPos> candidates = new ArrayList<>();
        for (int dx = -config.safeSearchRadius(); dx <= config.safeSearchRadius(); dx++) {
            for (int dz = -config.safeSearchRadius(); dz <= config.safeSearchRadius(); dz++) {
                for (int dy = -config.safeSearchVerticalRange(); dy <= config.safeSearchVerticalRange(); dy++) {
                    candidates.add(origin.offset(dx, dy, dz));
                }
            }
        }
        candidates.sort(Comparator.comparingDouble(pos -> pos.distSqr(origin)));
        return candidates.stream().filter(pos -> isSafe(level, pos)).findFirst().orElse(null);
    }

    private boolean isSafe(ServerLevel level, BlockPos feet) {
        BlockPos head = feet.above();
        BlockPos below = feet.below();
        if (!level.getBlockState(feet).isAir() || !level.getBlockState(head).isAir()) {
            return false;
        }
        if (!level.getFluidState(feet).isEmpty() || !level.getFluidState(head).isEmpty()) {
            return false;
        }
        var support = level.getBlockState(below);
        if (!support.isFaceSturdy(level, below, Direction.UP)) {
            return false;
        }
        return !support.is(Blocks.CACTUS) && !support.is(Blocks.MAGMA_BLOCK)
                && !support.is(Blocks.CAMPFIRE) && !support.is(Blocks.SOUL_CAMPFIRE)
                && !support.is(Blocks.POWDER_SNOW) && !support.is(Blocks.SWEET_BERRY_BUSH);
    }
}
