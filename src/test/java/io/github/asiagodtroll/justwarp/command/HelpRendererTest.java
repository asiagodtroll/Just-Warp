package io.github.asiagodtroll.justwarp.command;

import net.minecraft.ChatFormatting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HelpRendererTest {
    @Test
    void rendersTitleAndOneComponentPerHelpLine() {
        var rendered = HelpRenderer.section("Commands", "/jw: Open menu\n/jw back: Return", ChatFormatting.AQUA);

        assertEquals(3, rendered.size());
        assertFalse(rendered.get(0).getStyle().isItalic());
        assertFalse(rendered.get(1).getStyle().isItalic());
        assertTrue(rendered.get(0).getString().contains("Commands"));
        assertTrue(rendered.get(1).getString().contains("/jw: Open menu"));
        assertTrue(rendered.get(2).getString().contains("/jw back: Return"));
    }
}
