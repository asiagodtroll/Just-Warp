package io.github.asiagodtroll.justwarp.service;

import io.github.asiagodtroll.justwarp.domain.CustomIcon;
import io.github.asiagodtroll.justwarp.persistence.JsonStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class IconMutationService {
    private final JsonStore store;

    IconMutationService(JsonStore store) {
        this.store = store;
    }

    WarpState add(WarpState state, String name, String base64) throws WarpException, IOException {
        NameValidator.customIcon(name);
        if (state.icon(name).isPresent()) {
            throw new WarpException("error.custom_icon_exists", name);
        }
        IconPolicy.validateTexture(base64);
        List<CustomIcon> icons = new ArrayList<>(state.icons());
        icons.add(new CustomIcon(name, base64));
        return save(state, icons);
    }

    WarpState delete(WarpState state, String name) throws WarpException, IOException {
        state.requireIcon(name);
        boolean used = state.warps().stream().anyMatch(warp -> warp.icon().equals(name))
                || state.groups().stream().anyMatch(group -> group.icon().equals(name));
        if (used) {
            throw new WarpException("error.custom_icon_used", name);
        }
        return save(state, state.icons().stream().filter(icon -> !icon.name().equals(name)).toList());
    }

    WarpState setBase64(WarpState state, String name, String base64) throws WarpException, IOException {
        CustomIcon existing = state.requireIcon(name);
        IconPolicy.validateTexture(base64);
        return save(state, state.icons().stream()
                .map(icon -> icon == existing ? icon.withBase64(base64) : icon).toList());
    }

    private WarpState save(WarpState state, List<CustomIcon> icons) throws IOException {
        store.saveIcons(icons);
        return state.withIcons(icons);
    }
}
