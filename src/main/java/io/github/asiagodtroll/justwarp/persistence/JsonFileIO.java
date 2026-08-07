package io.github.asiagodtroll.justwarp.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class JsonFileIO {
    private static final Type MAP_TYPE = new TypeToken<LinkedHashMap<String, Object>>() {}.getType();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    Map<String, Object> read(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return parse(reader, path.getFileName().toString());
        }
    }

    Map<String, Object> read(InputStream input, String source) throws IOException {
        try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            return parse(reader, source);
        }
    }

    void write(Path target, Object value) throws IOException {
        writeTransaction(Map.of(target, value));
    }

    void writeTransaction(Map<Path, Object> values) throws IOException {
        Map<Path, JsonElement> serialized = new LinkedHashMap<>();
        for (var entry : values.entrySet()) {
            serialized.put(entry.getKey(), serialize(entry.getValue(), entry.getKey().getFileName().toString()));
        }

        Map<Path, Path> staged = new LinkedHashMap<>();
        Map<Path, Path> backups = new LinkedHashMap<>();
        Set<Path> replaced = new LinkedHashSet<>();
        IOException failure = null;
        try {
            for (var entry : serialized.entrySet()) {
                Path target = entry.getKey();
                Path temporary = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".staged");
                try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                    gson.toJson(entry.getValue(), writer);
                }
                staged.put(target, temporary);
                if (Files.exists(target)) {
                    Path backup = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".backup");
                    backups.put(target, backup);
                    Files.copy(target, backup, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            for (var entry : staged.entrySet()) {
                replace(entry.getValue(), entry.getKey());
                replaced.add(entry.getKey());
            }
        } catch (IOException caught) {
            failure = caught;
            rollback(replaced, backups, caught);
            throw caught;
        } finally {
            cleanup(staged.values(), backups.values(), failure);
        }
    }

    private Map<String, Object> parse(Reader reader, String source) throws IOException {
        try {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) {
                throw new IOException(source + " must contain a JSON object");
            }
            return gson.fromJson(root, MAP_TYPE);
        } catch (JsonParseException e) {
            throw new IOException("Could not parse " + source + ": " + e.getMessage(), e);
        }
    }

    private JsonElement serialize(Object value, String source) throws IOException {
        try {
            JsonElement serialized = gson.toJsonTree(value);
            if (!serialized.isJsonObject()) {
                throw new IOException(source + " must serialize to a JSON object");
            }
            gson.fromJson(serialized, MAP_TYPE);
            return serialized;
        } catch (RuntimeException e) {
            throw new IOException("Could not serialize " + source + ": " + e.getMessage(), e);
        }
    }

    private void rollback(Set<Path> replaced, Map<Path, Path> backups, IOException failure) {
        for (Path target : replaced) {
            try {
                Path backup = backups.get(target);
                if (backup == null) {
                    Files.deleteIfExists(target);
                } else {
                    Files.copy(backup, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
            }
        }
    }

    private static void cleanup(Iterable<Path> staged, Iterable<Path> backups, IOException failure) throws IOException {
        IOException cleanupFailure = null;
        for (Path path : concat(staged, backups)) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException caught) {
                if (cleanupFailure == null) {
                    cleanupFailure = caught;
                } else {
                    cleanupFailure.addSuppressed(caught);
                }
            }
        }
        if (cleanupFailure == null) {
            return;
        }
        if (failure != null) {
            failure.addSuppressed(cleanupFailure);
        } else {
            throw cleanupFailure;
        }
    }

    private static Iterable<Path> concat(Iterable<Path> first, Iterable<Path> second) {
        List<Path> paths = new ArrayList<>();
        first.forEach(paths::add);
        second.forEach(paths::add);
        return paths;
    }

    private void replace(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
