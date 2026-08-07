package io.github.asiagodtroll.justwarp.domain;

public record WarpConfig(String locale, int adminPermissionLevel, int teleportSafety,
                         int safeSearchRadius, int safeSearchVerticalRange) {
    public WarpConfig {
        if (adminPermissionLevel < 1 || adminPermissionLevel > 4) {
            throw new IllegalArgumentException("admin-permission-level must be 1..4");
        }
        if (teleportSafety < 1 || teleportSafety > 3) {
            throw new IllegalArgumentException("teleport-safety must be 1..3");
        }
        if (safeSearchRadius < 0 || safeSearchRadius > 64) {
            throw new IllegalArgumentException("safe-search-radius must be 0..64");
        }
        if (safeSearchVerticalRange < 0 || safeSearchVerticalRange > 32) {
            throw new IllegalArgumentException("safe-search-vertical-range must be 0..32");
        }
    }
}
