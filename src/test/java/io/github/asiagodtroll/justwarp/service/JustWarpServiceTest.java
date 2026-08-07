package io.github.asiagodtroll.justwarp.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import io.github.asiagodtroll.justwarp.domain.WarpLocation;
import io.github.asiagodtroll.justwarp.persistence.JsonStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JustWarpServiceTest {
    @TempDir Path temporary;

    @Test
    void rejectsMutationsBeforeInitializationAtTheSharedGateway() {
        JustWarpService service = new JustWarpService(new JsonStore(temporary.resolve("not-initialized")),
                new Translations());

        WarpException failure = assertThrows(WarpException.class,
                () -> service.addGroup("town", "minecraft:stone"));

        assertEquals("error.storage_unavailable", failure.translationKey());
    }

    @Test
    void persistsIconUpdatesInPlaceAndProtectsIconsUsedByGroups() throws Exception {
        JsonStore store = new JsonStore(temporary.resolve("justwarp"));
        JustWarpService service = new JustWarpService(store, new Translations());
        service.initialize(null);
        String firstTexture = texture("first");
        String secondTexture = texture("second");

        service.addIcon("castle", firstTexture);
        List<String> orderBefore = service.icons().stream().map(icon -> icon.name()).toList();
        service.setIconBase64("castle", secondTexture);
        service.addGroup("town", "castle");

        WarpException used = assertThrows(WarpException.class, () -> service.deleteIcon("castle"));
        assertEquals("error.custom_icon_used", used.translationKey());

        service.reload(null);
        assertEquals(orderBefore, service.icons().stream().map(icon -> icon.name()).toList());
        assertEquals(secondTexture, service.icon("castle").orElseThrow().base64());
        assertEquals("castle", service.group("town").orElseThrow().icon());

        service.deleteGroup("town");
        service.deleteIcon("castle");
        assertEquals(orderBefore.size() - 1, service.icons().size());
    }

    @Test
    void exposesMalformedJsonReasonWhileStorageIsUnavailable() throws Exception {
        Path directory = temporary.resolve("broken");
        JsonStore store = new JsonStore(directory);
        store.ensureDefaults();
        Files.writeString(directory.resolve("config.json"), "{");
        JustWarpService service = new JustWarpService(store, new Translations());

        assertThrows(IOException.class, () -> service.initialize(null));

        assertFalse(service.available());
        assertTrue(service.unavailableReason().contains("config.json"));
        String message = service.translations().text("error.storage_unavailable", service.unavailableReason());
        assertTrue(message.contains("JustWarp data is unavailable"));
        assertTrue(message.contains("config.json"));
    }

    @Test
    void preservesMutationOrderAndUpdatesGroupReferences() throws Exception {
        JsonStore store = new JsonStore(temporary.resolve("mutations"));
        JustWarpService service = new JustWarpService(store, new Translations());
        service.initialize(null);
        WarpLocation location = new WarpLocation("minecraft:overworld", 1, 2, 3, 4, 5);
        service.addIcon("marker", texture("marker"));

        service.addGroup("town", "marker");
        service.addWarp("first", "tester", "town", "marker", location);
        service.addWarp("second", "tester", "town", "marker", location);
        service.renameWarp("first", "renamed");

        assertEquals(List.of("renamed", "second"), service.group("town").orElseThrow().warps());
        assertEquals(List.of("renamed", "second"), service.warps().stream().map(warp -> warp.name()).toList());

        service.deleteWarp("renamed");
        assertEquals(List.of("second"), service.group("town").orElseThrow().warps());
    }

    @Test
    void keepsLiveStateWhenPersistenceFails() throws Exception {
        Path directory = temporary.resolve("failure");
        JsonStore store = new JsonStore(directory);
        JustWarpService service = new JustWarpService(store, new Translations());
        service.initialize(null);
        service.addIcon("marker", texture("marker"));
        Files.move(directory, temporary.resolve("moved-data"));
        Files.writeString(directory, "blocks directory recreation");

        assertThrows(IOException.class, () -> service.addGroup("town", "marker"));
        assertTrue(service.groups().isEmpty());
    }

    private static String texture(String id) {
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"https://textures.minecraft.net/texture/" + id + "\"}}}";
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
}
