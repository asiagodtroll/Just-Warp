package io.github.asiagodtroll.justwarp.gui;

import org.junit.jupiter.api.Test;
import io.github.asiagodtroll.justwarp.domain.CustomIcon;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerHeadProfileFactoryTest {
    @Test
    void createsProfileWithTextureWithoutMutatingImmutablePropertyMap() {
        var profile = PlayerHeadProfileFactory.create(new CustomIcon("back", "texture-base64"));

        assertEquals("back", profile.name());
        assertEquals("texture-base64", profile.properties().get("textures").iterator().next().value());
    }
}
