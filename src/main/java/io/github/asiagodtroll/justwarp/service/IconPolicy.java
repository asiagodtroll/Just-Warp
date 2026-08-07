package io.github.asiagodtroll.justwarp.service;

import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import io.github.asiagodtroll.justwarp.domain.CustomIcon;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;

final class IconPolicy {
    private IconPolicy() {}

    static String normalizeReference(String value, Collection<CustomIcon> customIcons) throws WarpException {
        return customIcons.stream().anyMatch(icon -> icon.name().equals(value)) ? value : normalizeItem(value);
    }

    static String normalizeItem(String icon) throws WarpException {
        Identifier id = Identifier.tryParse(icon.contains(":") ? icon : "minecraft:" + icon);
        if (id == null || BuiltInRegistries.ITEM.getOptional(id).isEmpty()) {
            throw new WarpException("error.icon", icon);
        }
        return id.toString();
    }

    static void validateTexture(String base64) throws WarpException {
        if (base64 == null || base64.isEmpty() || base64.length() > 32768) {
            throw new WarpException("error.base64");
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
            var root = JsonParser.parseString(decoded).getAsJsonObject();
            String url = root.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                throw new IllegalArgumentException();
            }
        } catch (RuntimeException e) {
            throw new WarpException("error.base64");
        }
    }
}
