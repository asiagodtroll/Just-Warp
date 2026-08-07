package io.github.asiagodtroll.justwarp.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class CommandInputTest {
    @Test
    void splitsChineseNamesAndPreservesTheFinalFreeTextValue() {
        assertArrayEquals(new String[]{"\u50b3\u9001\u9ede", "minecraft:stone", "\u57ce\u93ae"},
                CommandInput.tokens("\u50b3\u9001\u9ede minecraft:stone \u57ce\u93ae", 4));
        assertArrayEquals(new String[]{"\u50b3\u9001\u9ede", "description", "\u4e2d\u6587 \u8aaa\u660e"},
                CommandInput.tokens("\u50b3\u9001\u9ede description \u4e2d\u6587 \u8aaa\u660e", 3));
    }
}
