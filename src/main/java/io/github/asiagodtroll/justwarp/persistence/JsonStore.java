package io.github.asiagodtroll.justwarp.persistence;

import io.github.asiagodtroll.justwarp.domain.CustomIcon;
import io.github.asiagodtroll.justwarp.domain.Warp;
import io.github.asiagodtroll.justwarp.domain.WarpConfig;
import io.github.asiagodtroll.justwarp.domain.WarpData;
import io.github.asiagodtroll.justwarp.domain.WarpGroup;
import io.github.asiagodtroll.justwarp.domain.WarpLocation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonStore {
    private static final int SCHEMA_VERSION = 2;
    private final Path directory;
    private final JsonFileIO fileIO = new JsonFileIO();

    public JsonStore(Path directory) {
        this.directory = directory;
    }

    public void ensureDefaults() throws IOException {
        copyDefault("config.json");
        copyDefault("groups.json");
        copyDefault("warps.json");
        copyDefault("icons.json");
    }

    private void copyDefault(String relative) throws IOException {
        Path target = directory.resolve(relative);
        if (Files.exists(target)) {
            return;
        }
        Files.createDirectories(target.getParent());
        try (InputStream in = JsonStore.class.getResourceAsStream("/defaults/" + relative)) {
            if (in == null) {
                throw new IOException("Missing bundled default: " + relative);
            }
            Files.copy(in, target);
        }
    }

    public WarpConfig loadConfig() throws IOException {
        Map<String, Object> root = fileIO.read(directory.resolve("config.json"));
        boolean sectioned = root.containsKey("plugin") || root.containsKey("warp");
        Map<String, Object> plugin = sectioned ? object(root, "plugin") : root;
        Map<String, Object> warp = sectioned ? object(root, "warp") : root;
        try {
            return new WarpConfig(string(plugin, "locale"), integer(plugin, "admin-permission-level"),
                    integer(warp, "teleport-safety"),
                    integer(warp, "safe-search-radius"), integer(warp, "safe-search-vertical-range"));
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid value in config.json: " + e.getMessage(), e);
        }
    }

    public WarpData loadData() throws IOException {
        Map<String, Object> groupRoot = fileIO.read(directory.resolve("groups.json"));
        Map<String, Object> warpRoot = fileIO.read(directory.resolve("warps.json"));
        Map<String, Object> iconRoot = fileIO.read(directory.resolve("icons.json"));
        requireSchema(groupRoot, "groups.json");
        requireSchema(warpRoot, "warps.json");
        requireSchema(iconRoot, "icons.json");

        List<WarpGroup> groups = new ArrayList<>();
        for (Map<String, Object> row : mapList(groupRoot, "groups")) {
            groups.add(new WarpGroup(string(row, "name"), string(row, "icon"), stringList(row, "warps")));
        }
        List<Warp> warps = new ArrayList<>();
        for (Map<String, Object> row : mapList(warpRoot, "warps")) {
            WarpLocation location = location(row, string(row, "name"));
            warps.add(new Warp(string(row, "name"), optionalString(row, "author", "unknown"),
                    optionalString(row, "description", ""), string(row, "icon"), location));
        }
        List<CustomIcon> icons = new ArrayList<>();
        for (Map<String, Object> row : mapList(iconRoot, "icons")) {
            icons.add(new CustomIcon(string(row, "name"), string(row, "base64")));
        }
        return new WarpData(groups, warps, icons);
    }

    public Map<String, String> loadLanguage(String locale) throws IOException {
        Map<String, Object> raw = readBundledMap("lang/" + locale + ".json");
        Map<String, String> result = new LinkedHashMap<>();
        for (var entry : raw.entrySet()) {
            result.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return Map.copyOf(result);
    }

    private Map<String, Object> readBundledMap(String relative) throws IOException {
        try (InputStream input = JsonStore.class.getResourceAsStream("/defaults/" + relative)) {
            if (input == null) {
                throw new IOException("Missing bundled default: " + relative);
            }
            return fileIO.read(input, "bundled " + relative);
        }
    }

    public void saveGroups(List<WarpGroup> groups) throws IOException {
        fileIO.write(directory.resolve("groups.json"), groupsDocument(groups));
    }

    public void saveWarps(List<Warp> warps) throws IOException {
        fileIO.write(directory.resolve("warps.json"), warpsDocument(warps));
    }

    public void saveWarpState(List<WarpGroup> groups, List<Warp> warps) throws IOException {
        Map<Path, Object> documents = new LinkedHashMap<>();
        documents.put(directory.resolve("groups.json"), groupsDocument(groups));
        documents.put(directory.resolve("warps.json"), warpsDocument(warps));
        fileIO.writeTransaction(documents);
    }

    private Map<String, Object> groupsDocument(List<WarpGroup> groups) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("schema-version", SCHEMA_VERSION);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (WarpGroup group : groups) {
            rows.add(linked("name", group.name(), "icon", group.icon(), "warps", group.warps()));
        }
        root.put("groups", rows);
        return root;
    }

    private Map<String, Object> warpsDocument(List<Warp> warps) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("schema-version", SCHEMA_VERSION);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Warp warp : warps) {
            WarpLocation p = warp.location();
            rows.add(linked("name", warp.name(), "author", warp.author(), "description", warp.description(),
                    "icon", warp.icon(), "world", p.world(),
                    "x", p.x(), "y", p.y(), "z", p.z(), "yaw", p.yaw(), "pitch", p.pitch()));
        }
        root.put("warps", rows);
        return root;
    }

    public void saveIcons(List<CustomIcon> icons) throws IOException {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("schema-version", SCHEMA_VERSION);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (CustomIcon icon : icons) {
            rows.add(linked("name", icon.name(), "base64", icon.base64()));
        }
        root.put("icons", rows);
        fileIO.write(directory.resolve("icons.json"), root);
    }

    private static void requireSchema(Map<String, Object> root, String file) throws IOException {
        if (integer(root, "schema-version") != SCHEMA_VERSION) {
            throw new IOException("Unsupported schema-version in " + file);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Map<String, Object> root, String key) throws IOException {
        Object value = root.get(key);
        if (!(value instanceof Map<?, ?> map)) {
            throw new IOException("'" + key + "' must be an object");
        }
        return (Map<String, Object>) map;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> mapList(Map<String, Object> root, String key) throws IOException {
        Object value = root.get(key);
        if (!(value instanceof List<?> list)) {
            throw new IOException("'" + key + "' must be a list");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object row : list) {
            if (!(row instanceof Map<?, ?> map)) {
                throw new IOException("Every '" + key + "' entry must be an object");
            }
            result.add((Map<String, Object>) map);
        }
        return result;
    }

    private static String string(Map<String, Object> map, String key) throws IOException {
        Object value = map.get(key);
        if (!(value instanceof String text) || text.isEmpty()) {
            throw new IOException("'" + key + "' must be a non-empty string");
        }
        return text;
    }

    private static String optionalString(Map<String, Object> map, String key, String fallback) throws IOException {
        Object value = map.get(key);
        if (value == null) {
            return fallback;
        }
        if (value instanceof String text) {
            return text;
        }
        throw new IOException("'" + key + "' must be a string");
    }

    private static List<String> stringList(Map<String, Object> map, String key) throws IOException {
        Object value = map.get(key);
        if (!(value instanceof List<?> list)) {
            throw new IOException("'" + key + "' must be a list");
        }
        List<String> result = new ArrayList<>();
        for (Object entry : list) {
            if (!(entry instanceof String text) || text.isEmpty()) {
                throw new IOException("Every '" + key + "' entry must be a non-empty string");
            }
            result.add(text);
        }
        return List.copyOf(result);
    }

    private static int integer(Map<String, Object> map, String key) throws IOException {
        Object value = map.get(key);
        if (!(value instanceof Number number) || number.doubleValue() != number.intValue()) {
            throw new IOException("'" + key + "' must be an integer");
        }
        return number.intValue();
    }

    private static double decimal(Map<String, Object> map, String key) throws IOException {
        Object value = map.get(key);
        if (!(value instanceof Number number)) {
            throw new IOException("'" + key + "' must be a number");
        }
        return number.doubleValue();
    }

    private static WarpLocation location(Map<String, Object> row, String warpName) throws IOException {
        try {
            return new WarpLocation(string(row, "world"), decimal(row, "x"), decimal(row, "y"),
                    decimal(row, "z"), (float) decimal(row, "yaw"), (float) decimal(row, "pitch"));
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid location for warp '" + warpName + "': " + e.getMessage(), e);
        }
    }

    private static Map<String, Object> linked(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put((String) values[i], values[i + 1]);
        }
        return map;
    }
}
