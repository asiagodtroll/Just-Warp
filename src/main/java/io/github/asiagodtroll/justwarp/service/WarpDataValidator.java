package io.github.asiagodtroll.justwarp.service;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import io.github.asiagodtroll.justwarp.domain.CustomIcon;
import io.github.asiagodtroll.justwarp.domain.Warp;
import io.github.asiagodtroll.justwarp.domain.WarpData;
import io.github.asiagodtroll.justwarp.domain.WarpGroup;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

final class WarpDataValidator {
    void validate(WarpData data, MinecraftServer server) throws IOException, WarpException {
        Set<String> iconNames = validateIcons(data);
        Set<String> warpNames = validateWarps(data, server, iconNames);
        validateGroups(data, warpNames, iconNames);
    }

    private Set<String> validateIcons(WarpData data) throws IOException, WarpException {
        Set<String> names = new HashSet<>();
        for (CustomIcon icon : data.icons()) {
            NameValidator.customIcon(icon.name());
            if (!names.add(icon.name())) {
                throw new IOException("Duplicate custom icon: " + icon.name());
            }
            IconPolicy.validateTexture(icon.base64());
        }
        return names;
    }

    private Set<String> validateWarps(WarpData data, MinecraftServer server, Set<String> iconNames)
            throws IOException, WarpException {
        Set<String> names = new HashSet<>();
        for (Warp warp : data.warps()) {
            NameValidator.warp(warp.name());
            WarpMetadataValidator.author(warp.author());
            WarpMetadataValidator.description(warp.description());
            if (!names.add(warp.name())) {
                throw new IOException("Duplicate warp: " + warp.name());
            }
            validateIconReference(warp.icon(), iconNames);
            Identifier id = Identifier.tryParse(warp.location().world());
            if (id == null || server.getLevel(ResourceKey.create(Registries.DIMENSION, id)) == null) {
                throw new IOException("Unknown world for warp " + warp.name());
            }
        }
        return names;
    }

    private void validateGroups(WarpData data, Set<String> warpNames, Set<String> iconNames)
            throws IOException, WarpException {
        Set<String> groupNames = new HashSet<>();
        Set<String> groupedWarps = new HashSet<>();
        for (WarpGroup group : data.groups()) {
            NameValidator.group(group.name());
            if (!groupNames.add(group.name())) {
                throw new IOException("Duplicate group: " + group.name());
            }
            validateIconReference(group.icon(), iconNames);
            for (String warpName : group.warps()) {
                if (!warpNames.contains(warpName)) {
                    throw new IOException("Unknown warp in group " + group.name() + ": " + warpName);
                }
                if (!groupedWarps.add(warpName)) {
                    throw new IOException("Warp belongs to multiple groups: " + warpName);
                }
            }
        }
    }

    private void validateIconReference(String value, Set<String> customIcons) throws WarpException {
        if (!customIcons.contains(value)) {
            IconPolicy.normalizeItem(value);
        }
    }
}
