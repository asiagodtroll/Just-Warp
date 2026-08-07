package io.github.asiagodtroll.justwarp.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WarpDomainTest {
    @Test
    void setSettingsKeepsExactLocation() {
        WarpLocation location = new WarpLocation("minecraft:overworld", 1.125, 64.25, -3.5, 22.5f, -10f);
        Warp changed = new Warp("home", "Steve", "Spawn point", "minecraft:stone", location)
                .withIcon("minecraft:diamond");
        assertSame(location, changed.location());
        assertEquals("minecraft:diamond", changed.icon());
    }

    @Test
    void renamingKeepsWarpAndGroupSettings() {
        WarpLocation location = new WarpLocation("minecraft:overworld", 1, 64, 2, 30, 5);
        Warp warp = new Warp("before", "Steve", "Description", "minecraft:stone", location).withName("after");
        WarpGroup group = new WarpGroup("town", "minecraft:bricks", List.of("before")).withName("city");

        assertEquals("after", warp.name());
        assertEquals("minecraft:stone", warp.icon());
        assertEquals("Steve", warp.author());
        assertEquals("Description", warp.description());
        assertSame(location, warp.location());
        assertEquals(new WarpGroup("city", "minecraft:bricks", List.of("before")), group);
    }

    @Test
    void changingPositionKeepsWarpMetadataAndIcon() {
        WarpLocation before = new WarpLocation("minecraft:overworld", 0, 64, 0, 0, 0);
        WarpLocation after = new WarpLocation("minecraft:the_nether", 10.5, 70, -4.5, 90, 15);
        Warp changed = new Warp("home", "Steve", "My home", "minecraft:red_bed", before)
                .withLocation(after);

        assertEquals("home", changed.name());
        assertEquals("Steve", changed.author());
        assertEquals("My home", changed.description());
        assertEquals("minecraft:red_bed", changed.icon());
        assertSame(after, changed.location());
    }

    @Test
    void changingCustomIconTextureKeepsItsName() {
        CustomIcon changed = new CustomIcon("castle", "before").withBase64("after");

        assertEquals(new CustomIcon("castle", "after"), changed);
    }

    @Test
    void dataAndGroupDefensivelyCopyOrderedLists() {
        List<String> warpNames = new ArrayList<>(List.of("home"));
        WarpGroup group = new WarpGroup("town", "minecraft:bricks", warpNames);
        List<WarpGroup> groups = new ArrayList<>(List.of(group));
        WarpData data = new WarpData(groups, List.of(), List.of());

        warpNames.add("spawn");
        groups.clear();

        assertEquals(List.of("home"), group.warps());
        assertEquals(List.of(group), data.groups());
        assertThrows(UnsupportedOperationException.class, () -> group.warps().add("mine"));
        assertThrows(UnsupportedOperationException.class, () -> data.groups().clear());
    }

    @Test
    void rejectsNonFiniteCoordinatesAndInvalidConfigRanges() {
        assertThrows(IllegalArgumentException.class,
                () -> new WarpLocation("minecraft:overworld", Double.NaN, 0, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new WarpConfig("zh_TW", 0, 1, 5, 3));
        assertThrows(IllegalArgumentException.class, () -> new WarpConfig("zh_TW", 2, 4, 5, 3));
        assertThrows(IllegalArgumentException.class, () -> new WarpConfig("zh_TW", 2, 1, 65, 3));
    }
}
