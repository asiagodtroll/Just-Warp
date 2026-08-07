package io.github.asiagodtroll.justwarp.service;

import io.github.asiagodtroll.justwarp.domain.CustomIcon;
import io.github.asiagodtroll.justwarp.domain.Warp;
import io.github.asiagodtroll.justwarp.domain.WarpGroup;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

record WarpState(List<WarpGroup> groups, List<Warp> warps, List<CustomIcon> icons) {
    WarpState {
        groups = List.copyOf(groups);
        warps = List.copyOf(warps);
        icons = List.copyOf(icons);
    }

    Optional<Warp> warp(String name) {
        return warps.stream().filter(warp -> warp.name().equals(name)).findFirst();
    }

    Optional<WarpGroup> group(String name) {
        return groups.stream().filter(group -> group.name().equals(name)).findFirst();
    }

    Optional<CustomIcon> icon(String name) {
        return icons.stream().filter(icon -> icon.name().equals(name)).findFirst();
    }

    Warp requireWarp(String name) throws WarpException {
        return warp(name).orElseThrow(() -> new WarpException("error.warp_missing", name));
    }

    WarpGroup requireGroup(String name) throws WarpException {
        return group(name).orElseThrow(() -> new WarpException("error.group_missing", name));
    }

    CustomIcon requireIcon(String name) throws WarpException {
        return icon(name).orElseThrow(() -> new WarpException("error.custom_icon_missing", name));
    }

    List<Warp> ungroupedWarps() {
        Set<String> grouped = groups.stream().flatMap(group -> group.warps().stream()).collect(Collectors.toSet());
        return warps.stream().filter(warp -> !grouped.contains(warp.name())).toList();
    }

    List<Warp> warpsIn(String groupName) {
        return group(groupName).stream().flatMap(group -> group.warps().stream())
                .map(this::warp).flatMap(Optional::stream).toList();
    }

    WarpState withWarpData(List<WarpGroup> newGroups, List<Warp> newWarps) {
        return new WarpState(newGroups, newWarps, icons);
    }

    WarpState withGroups(List<WarpGroup> newGroups) {
        return new WarpState(newGroups, warps, icons);
    }

    WarpState withWarps(List<Warp> newWarps) {
        return new WarpState(groups, newWarps, icons);
    }

    WarpState withIcons(List<CustomIcon> newIcons) {
        return new WarpState(groups, warps, newIcons);
    }
}
