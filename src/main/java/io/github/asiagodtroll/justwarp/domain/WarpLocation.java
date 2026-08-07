package io.github.asiagodtroll.justwarp.domain;

public record WarpLocation(String world, double x, double y, double z, float yaw, float pitch) {
    public WarpLocation {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                || !Float.isFinite(yaw) || !Float.isFinite(pitch)) {
            throw new IllegalArgumentException("location values must be finite");
        }
    }
}
