package io.github.asiagodtroll.justwarp.service;

import io.github.asiagodtroll.justwarp.domain.WarpLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

final class BackHistory {
    private final Map<UUID, WarpLocation> locations = new HashMap<>();

    Optional<WarpLocation> find(UUID playerId) {
        return Optional.ofNullable(locations.get(playerId));
    }

    void remember(UUID playerId, WarpLocation location) {
        locations.put(playerId, location);
    }
}
