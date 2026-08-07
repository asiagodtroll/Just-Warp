package io.github.asiagodtroll.justwarp.service;

import io.github.asiagodtroll.justwarp.domain.Warp;
import io.github.asiagodtroll.justwarp.domain.WarpGroup;
import io.github.asiagodtroll.justwarp.domain.WarpLocation;
import io.github.asiagodtroll.justwarp.persistence.JsonStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class WarpMutationService {
    private final JsonStore store;

    WarpMutationService(JsonStore store) {
        this.store = store;
    }

    WarpState add(WarpState state, String name, String author, String group, String icon, WarpLocation location)
            throws WarpException, IOException {
        NameValidator.warp(name);
        if (group != null) {
            state.requireGroup(group);
        }
        WarpMetadataValidator.author(author);
        if (state.warp(name).isPresent()) {
            throw new WarpException("error.warp_exists", name);
        }
        String normalized = IconPolicy.normalizeReference(icon, state.icons());
        List<Warp> warps = new ArrayList<>(state.warps());
        warps.add(new Warp(name, author, "", normalized, location));
        List<WarpGroup> groups = group == null ? state.groups() : state.groups().stream()
                .map(value -> value.name().equals(group) ? append(value, name) : value).toList();
        if (group == null) {
            store.saveWarps(warps);
        } else {
            store.saveWarpState(groups, warps);
        }
        return state.withWarpData(groups, warps);
    }

    WarpState delete(WarpState state, String name) throws WarpException, IOException {
        state.requireWarp(name);
        List<Warp> warps = state.warps().stream().filter(warp -> !warp.name().equals(name)).toList();
        List<WarpGroup> groups = state.groups().stream().map(group -> remove(group, name)).toList();
        store.saveWarpState(groups, warps);
        return state.withWarpData(groups, warps);
    }

    WarpState rename(WarpState state, String before, String after) throws WarpException, IOException {
        Warp existing = state.requireWarp(before);
        NameValidator.warp(after);
        if (!before.equals(after) && state.warp(after).isPresent()) {
            throw new WarpException("error.warp_exists", after);
        }
        List<Warp> warps = state.warps().stream()
                .map(warp -> warp == existing ? warp.withName(after) : warp).toList();
        List<WarpGroup> groups = state.groups().stream().map(group -> group.withWarps(group.warps().stream()
                .map(name -> name.equals(before) ? after : name).toList())).toList();
        store.saveWarpState(groups, warps);
        return state.withWarpData(groups, warps);
    }

    WarpState setGroup(WarpState state, String name, String groupName) throws WarpException, IOException {
        if (groupName != null) {
            state.requireGroup(groupName);
        }
        state.requireWarp(name);
        List<WarpGroup> groups = state.groups().stream().map(group -> {
            if (Objects.equals(group.name(), groupName)) {
                return group.warps().contains(name) ? group : append(group, name);
            }
            return remove(group, name);
        }).toList();
        store.saveGroups(groups);
        return state.withGroups(groups);
    }

    WarpState setIcon(WarpState state, String name, String icon) throws WarpException, IOException {
        Warp existing = state.requireWarp(name);
        String normalized = IconPolicy.normalizeReference(icon, state.icons());
        return saveWarps(state, state.warps().stream()
                .map(warp -> warp == existing ? warp.withIcon(normalized) : warp).toList());
    }

    WarpState setAuthor(WarpState state, String name, String author) throws WarpException, IOException {
        Warp existing = state.requireWarp(name);
        WarpMetadataValidator.author(author);
        return saveWarps(state, state.warps().stream()
                .map(warp -> warp == existing ? warp.withAuthor(author) : warp).toList());
    }

    WarpState setDescription(WarpState state, String name, String description) throws WarpException, IOException {
        Warp existing = state.requireWarp(name);
        WarpMetadataValidator.description(description);
        return saveWarps(state, state.warps().stream()
                .map(warp -> warp == existing ? warp.withDescription(description) : warp).toList());
    }

    WarpState setPosition(WarpState state, String name, WarpLocation location) throws WarpException, IOException {
        Warp existing = state.requireWarp(name);
        return saveWarps(state, state.warps().stream()
                .map(warp -> warp == existing ? warp.withLocation(location) : warp).toList());
    }

    private WarpState saveWarps(WarpState state, List<Warp> warps) throws IOException {
        store.saveWarps(warps);
        return state.withWarps(warps);
    }

    private static WarpGroup append(WarpGroup group, String name) {
        List<String> warps = new ArrayList<>(group.warps());
        warps.add(name);
        return group.withWarps(warps);
    }

    private static WarpGroup remove(WarpGroup group, String name) {
        return group.withWarps(group.warps().stream().filter(value -> !value.equals(name)).toList());
    }
}
