package io.github.asiagodtroll.justwarp.service;

import org.junit.jupiter.api.Test;
import io.github.asiagodtroll.justwarp.domain.WarpLocation;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackHistoryTest {
    @Test
    void keepsIndependentLatestLocationsPerPlayer() {
        BackHistory history = new BackHistory();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        WarpLocation firstLocation = new WarpLocation("minecraft:overworld", 1, 2, 3, 4, 5);
        WarpLocation replacement = new WarpLocation("minecraft:the_nether", 6, 7, 8, 9, 10);

        assertTrue(history.find(first).isEmpty());
        history.remember(first, firstLocation);
        history.remember(second, replacement);
        history.remember(first, replacement);

        assertEquals(replacement, history.find(first).orElseThrow());
        assertEquals(replacement, history.find(second).orElseThrow());
    }
}
