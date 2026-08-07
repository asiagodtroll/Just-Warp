package io.github.asiagodtroll.justwarp.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonFileIOTest {
    @TempDir Path temporary;

    @Test
    void rejectsMalformedAndNonObjectJsonWithSourceContext() throws Exception {
        JsonFileIO fileIO = new JsonFileIO();
        Path malformed = temporary.resolve("malformed.json");
        Path array = temporary.resolve("array.json");
        Files.writeString(malformed, "{");
        Files.writeString(array, "[]");

        IOException malformedError = assertThrows(IOException.class, () -> fileIO.read(malformed));
        IOException arrayError = assertThrows(IOException.class, () -> fileIO.read(array));

        assertTrue(malformedError.getMessage().contains("malformed.json"));
        assertTrue(arrayError.getMessage().contains("array.json must contain a JSON object"));
    }

    @Test
    void preparationFailurePreservesExistingFilesAndRemovesTemporaryFiles() throws Exception {
        JsonFileIO fileIO = new JsonFileIO();
        Path existing = temporary.resolve("existing.json");
        Files.writeString(existing, "original");
        Map<Path, Object> documents = new LinkedHashMap<>();
        documents.put(existing, Map.of("changed", true));
        documents.put(temporary.resolve("missing").resolve("second.json"), Map.of("value", 2));

        assertThrows(IOException.class, () -> fileIO.writeTransaction(documents));

        assertEquals("original", Files.readString(existing));
        try (var files = Files.list(temporary)) {
            assertTrue(files.noneMatch(path -> path.getFileName().toString().endsWith(".staged")
                    || path.getFileName().toString().endsWith(".backup")));
        }
    }

    @Test
    void serializationFailureDoesNotTouchExistingFile() throws Exception {
        JsonFileIO fileIO = new JsonFileIO();
        Path existing = temporary.resolve("data.json");
        Files.writeString(existing, "original");

        IOException error = assertThrows(IOException.class,
                () -> fileIO.write(existing, Map.of("coordinate", Double.NaN)));

        assertTrue(error.getMessage().contains("Could not serialize data.json"));
        assertEquals("original", Files.readString(existing));
    }

    @Test
    void rejectsSerializedDocumentsWithoutObjectRoots() {
        JsonFileIO fileIO = new JsonFileIO();
        Path target = temporary.resolve("array.json");

        IOException error = assertThrows(IOException.class, () -> fileIO.write(target, List.of(1, 2)));

        assertTrue(error.getMessage().contains("must serialize to a JSON object"));
    }
}
