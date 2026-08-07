package io.github.asiagodtroll.justwarp.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import io.github.asiagodtroll.justwarp.domain.CustomIcon;
import io.github.asiagodtroll.justwarp.domain.Warp;
import io.github.asiagodtroll.justwarp.domain.WarpConfig;
import io.github.asiagodtroll.justwarp.domain.WarpData;
import io.github.asiagodtroll.justwarp.domain.WarpGroup;
import io.github.asiagodtroll.justwarp.domain.WarpLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonStoreTest {
    @TempDir Path temporary;

    @Test
    void createsDefaultsAndLoadsConfiguration() throws Exception {
        Path directory = temporary.resolve("justwarp");
        JsonStore store = new JsonStore(directory);
        store.ensureDefaults();

        WarpConfig config = store.loadConfig();
        assertEquals("zh_TW", config.locale());
        assertEquals(2, config.adminPermissionLevel());
        assertEquals(1, config.teleportSafety());
        assertTrue(store.loadLanguage("en_US").containsKey("gui.root.title"));
        assertEquals(List.of("arrow_left", "arrow_right", "back"),
                store.loadData().icons().stream().map(CustomIcon::name).toList());
        assertFalse(Files.exists(directory.resolve("lang")));
        assertFalse(Files.exists(directory.resolve("config.yml")));
    }

    @Test
    void roundTripPreservesOrderUnicodeNullGroupAndCoordinates() throws Exception {
        Path directory = temporary.resolve("justwarp");
        JsonStore store = new JsonStore(directory);
        store.ensureDefaults();
        List<WarpGroup> groups = List.of(
                new WarpGroup("撱箇?", "minecraft:bricks", List.of("?")),
                new WarpGroup("farm", "minecraft:wheat", List.of()));
        List<Warp> warps = List.of(
                new Warp("\u50b3\u9001\u9ede", "Player", "\u4e2d\u6587\u50b3\u9001\u9ede", "minecraft:stone",
                        new WarpLocation("minecraft:overworld", 1.25, 64.5, -8.75, 90, 10)),
                new Warp("home", "Player", "", "minecraft:red_bed",
                        new WarpLocation("minecraft:the_nether", 2, 70, 3, -45, 0)));
        List<CustomIcon> icons = List.of(new CustomIcon("castle", "texture-base64"));

        store.saveWarpState(groups, warps);
        store.saveIcons(icons);
        WarpData loaded = store.loadData();

        assertEquals(groups, loaded.groups());
        assertEquals(warps, loaded.warps());
        assertEquals(icons, loaded.icons());
        try (var files = Files.list(directory)) {
            assertTrue(files.noneMatch(path -> path.getFileName().toString().endsWith(".staged")
                    || path.getFileName().toString().endsWith(".backup")));
        }
    }

    @Test
    void rejectsUnsupportedSchema() throws Exception {
        Path directory = temporary.resolve("justwarp");
        JsonStore store = new JsonStore(directory);
        store.ensureDefaults();
        Files.writeString(directory.resolve("groups.json"), "{\"schema-version\":99,\"groups\":[]}");
        assertThrows(IOException.class, store::loadData);
    }

    @Test
    void reportsInvalidConfigRangesAsIoErrors() throws Exception {
        Path directory = temporary.resolve("justwarp");
        JsonStore store = new JsonStore(directory);
        store.ensureDefaults();
        Files.writeString(directory.resolve("config.json"), """
                {"plugin":{"locale":"zh_TW","admin-permission-level":2},
                "warp":{"teleport-safety":9,
                "safe-search-radius":5,"safe-search-vertical-range":3}}
                """);

        IOException error = assertThrows(IOException.class, store::loadConfig);
        assertTrue(error.getMessage().contains("config.json"));
        assertInstanceOf(IllegalArgumentException.class, error.getCause());
    }

    @Test
    void ignoresRemovedPortalSettingsInExistingConfigs() throws Exception {
        Path directory = temporary.resolve("justwarp");
        JsonStore store = new JsonStore(directory);
        store.ensureDefaults();
        Path configPath = directory.resolve("config.json");
        String existing = """
                {"plugin":{"locale":"zh_TW","admin-permission-level":2},
                "warp":{"check-nearby-nether-portals":"legacy-value","nether-portal-check-radius":999,
                "teleport-safety":1,
                "safe-search-radius":5,"safe-search-vertical-range":3}}
                """;
        Files.writeString(configPath, existing);

        assertEquals(1, store.loadConfig().teleportSafety());
        assertEquals(existing, Files.readString(configPath));
    }

    @Test
    void rejectsMissingConfigSections() throws Exception {
        Path directory = temporary.resolve("justwarp");
        JsonStore store = new JsonStore(directory);
        store.ensureDefaults();
        Files.writeString(directory.resolve("config.json"), """
                {"plugin":{"locale":"zh_TW","admin-permission-level":2}}
                """);

        IOException error = assertThrows(IOException.class, store::loadConfig);
        assertTrue(error.getMessage().contains("'warp' must be an object"));
    }

    @Test
    void loadsLegacyFlatConfigWithoutOverwritingIt() throws Exception {
        Path directory = temporary.resolve("justwarp");
        JsonStore store = new JsonStore(directory);
        store.ensureDefaults();
        Path configPath = directory.resolve("config.json");
        String legacy = """
                {"locale":"en_US","admin-permission-level":3,"teleport-safety":2,
                "safe-search-radius":7,"safe-search-vertical-range":4}
                """;
        Files.writeString(configPath, legacy);

        WarpConfig config = store.loadConfig();

        assertEquals("en_US", config.locale());
        assertEquals(3, config.adminPermissionLevel());
        assertEquals(2, config.teleportSafety());
        assertEquals(7, config.safeSearchRadius());
        assertEquals(4, config.safeSearchVerticalRange());
        assertEquals(legacy, Files.readString(configPath));
    }

    @Test
    void reportsNonFiniteWarpLocationsAsIoErrors() throws Exception {
        Path directory = temporary.resolve("justwarp");
        JsonStore store = new JsonStore(directory);
        store.ensureDefaults();
        Files.writeString(directory.resolve("warps.json"), """
                {"schema-version":2,"warps":[{"name":"broken","icon":"minecraft:stone",
                "world":"minecraft:overworld","x":1e400,"y":64,"z":0,"yaw":0,"pitch":0}]}
                """);

        IOException error = assertThrows(IOException.class, store::loadData);
        assertTrue(error.getMessage().contains("broken"));
    }

    @Test
    void loadsSchemaTwoWarpsWithoutMetadataUsingSafeDefaults() throws Exception {
        Path directory = temporary.resolve("justwarp");
        JsonStore store = new JsonStore(directory);
        store.ensureDefaults();
        Files.writeString(directory.resolve("warps.json"), """
                {"schema-version":2,"warps":[{"name":"legacy","icon":"minecraft:stone",
                "world":"minecraft:overworld","x":0,"y":64,"z":0,"yaw":0,"pitch":0}]}
                """);

        Warp legacy = store.loadData().warps().getFirst();
        assertEquals("unknown", legacy.author());
        assertEquals("", legacy.description());
    }
}
