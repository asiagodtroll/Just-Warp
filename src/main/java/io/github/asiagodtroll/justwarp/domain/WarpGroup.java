package io.github.asiagodtroll.justwarp.domain;

import java.util.List;

public record WarpGroup(String name, String icon, List<String> warps) {
    public WarpGroup {
        warps = List.copyOf(warps);
    }

    public WarpGroup withName(String newName) {
        return new WarpGroup(newName, icon, warps);
    }

    public WarpGroup withIcon(String newIcon) {
        return new WarpGroup(name, newIcon, warps);
    }

    public WarpGroup withWarps(List<String> newWarps) {
        return new WarpGroup(name, icon, newWarps);
    }
}
