package io.github.asiagodtroll.justwarp.gui;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import io.github.asiagodtroll.justwarp.domain.CustomIcon;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

final class PlayerHeadProfileFactory {
    private PlayerHeadProfileFactory() {}

    static GameProfile create(CustomIcon icon) {
        UUID id = UUID.nameUUIDFromBytes(icon.base64().getBytes(StandardCharsets.UTF_8));
        String name = icon.name().substring(0, Math.min(16, icon.name().length()));
        Property texture = new Property("textures", icon.base64());
        PropertyMap properties = new PropertyMap(ImmutableMultimap.of("textures", texture));
        return new GameProfile(id, name, properties);
    }
}
