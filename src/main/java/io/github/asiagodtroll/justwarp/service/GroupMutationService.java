package io.github.asiagodtroll.justwarp.service;

import io.github.asiagodtroll.justwarp.domain.WarpGroup;
import io.github.asiagodtroll.justwarp.persistence.JsonStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class GroupMutationService {
    private final JsonStore store;

    GroupMutationService(JsonStore store) {
        this.store = store;
    }

    WarpState add(WarpState state, String name, String icon) throws WarpException, IOException {
        NameValidator.group(name);
        if (state.group(name).isPresent()) {
            throw new WarpException("error.group_exists", name);
        }
        List<WarpGroup> groups = new ArrayList<>(state.groups());
        groups.add(new WarpGroup(name, IconPolicy.normalizeReference(icon, state.icons()), List.of()));
        return save(state, groups);
    }

    WarpState delete(WarpState state, String name) throws WarpException, IOException {
        state.requireGroup(name);
        return save(state, state.groups().stream().filter(group -> !group.name().equals(name)).toList());
    }

    WarpState rename(WarpState state, String before, String after) throws WarpException, IOException {
        WarpGroup existing = state.requireGroup(before);
        NameValidator.group(after);
        if (!before.equals(after) && state.group(after).isPresent()) {
            throw new WarpException("error.group_exists", after);
        }
        return save(state, state.groups().stream()
                .map(group -> group == existing ? group.withName(after) : group).toList());
    }

    WarpState setIcon(WarpState state, String name, String icon) throws WarpException, IOException {
        WarpGroup existing = state.requireGroup(name);
        String normalized = IconPolicy.normalizeReference(icon, state.icons());
        return save(state, state.groups().stream()
                .map(group -> group == existing ? group.withIcon(normalized) : group).toList());
    }

    private WarpState save(WarpState state, List<WarpGroup> groups) throws IOException {
        store.saveGroups(groups);
        return state.withGroups(groups);
    }
}
