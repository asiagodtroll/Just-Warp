package io.github.asiagodtroll.justwarp.domain;

import java.util.List;

public record WarpData(List<WarpGroup> groups, List<Warp> warps, List<CustomIcon> icons) {
    public WarpData {
        groups = List.copyOf(groups);
        warps = List.copyOf(warps);
        icons = List.copyOf(icons);
    }
}
