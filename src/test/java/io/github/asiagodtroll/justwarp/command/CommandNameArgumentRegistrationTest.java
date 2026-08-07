package io.github.asiagodtroll.justwarp.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import io.github.asiagodtroll.justwarp.gui.IconGui;
import io.github.asiagodtroll.justwarp.gui.WarpGui;
import io.github.asiagodtroll.justwarp.domain.WarpGroup;
import io.github.asiagodtroll.justwarp.persistence.JsonStore;
import io.github.asiagodtroll.justwarp.service.JustWarpService;
import io.github.asiagodtroll.justwarp.service.Translations;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandNameArgumentRegistrationTest {
    @TempDir
    Path temporary;

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void registersOnlyArgumentTypesThatVanillaClientsCanSynchronize() {
        CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
        assertSynchronizable(dispatcher.getRoot());
    }

    @Test
    void parsesCompleteCommandsWithChineseReferences() {
        CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
        CommandSourceStack source = commandSource();

        assertParses(dispatcher, source, "jw warp add \"\u50b3\u9001\u9ede\" \"minecraft:stone\" \"\u57ce\u93ae\"");
        assertParses(dispatcher, source, "jw warp add \"\u50b3\u9001\u9ede\" \"\u5730\u6a19\"");
        assertParses(dispatcher, source, "jw group add \"\u57ce\u93ae\" \"\u5730\u6a19\"");
        assertUsesTypedValue(dispatcher, source,
                "jw warp set \"\u50b3\u9001\u9ede\" author \"\u4f5c\u8005\u540d\u7a31\"");
        assertUsesTypedValue(dispatcher, source,
                "jw warp set \"\u50b3\u9001\u9ede\" description \"\u4e2d\u6587 \u8aaa\u660e\"");
    }

    @Test
    void parsesDeleteCommandsWithChineseReferences() {
        CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
        CommandSourceStack source = commandSource();

        assertParses(dispatcher, source, "jw warp del \"\u50b3\u9001\u9ede\"");
        assertParses(dispatcher, source, "jw group del \"\u57ce\u93ae\"");
        assertParses(dispatcher, source, "jw icon del \"\u5730\u6a19\"");
    }

    @Test
    void suggestsNamespacedVanillaItemsForIconReferences() {
        JsonStore store = new JsonStore(temporary.resolve("justwarp"));
        JustWarpService service = new JustWarpService(store, new Translations());
        CommandSuggestions suggestions = new CommandSuggestions(service);

        var result = suggestions.iconReferences(null, new SuggestionsBuilder("\"minecraft:sto", 0)).join();

        assertEquals(1, result.getList().stream()
                .filter(suggestion -> suggestion.getText().equals("\"minecraft:stone\"")).count());
        var stone = result.getList().stream()
                .filter(suggestion -> suggestion.getText().equals("\"minecraft:stone\""))
                .findFirst().orElseThrow();
        assertEquals("suggest.item_icon", stone.getTooltip().getString());
    }

    @Test
    void suggestsQuotedChineseGroupsFromReloadedServiceState() throws Exception {
        JsonStore store = new JsonStore(temporary.resolve("justwarp"));
        store.ensureDefaults();
        store.saveGroups(List.of(new WarpGroup("\u57ce\u93ae", "minecraft:stone", List.of())));
        Translations translations = new Translations();
        JustWarpService service = new JustWarpService(store, translations);
        service.reload(null);

        CommandSuggestions suggestions = new CommandSuggestions(service);
        var result = suggestions.groups(null, new SuggestionsBuilder("\"\u57ce", 0)).join();
        var town = result.getList().stream().filter(suggestion -> suggestion.getText().equals("\"\u57ce\u93ae\""))
                .findFirst().orElseThrow();

        assertEquals("\u50b3\u9001\u9ede\u7fa4\u7d44", town.getTooltip().getString());
    }

    @Test
    void continuesSuggestingFieldsAfterQuotedChineseName() {
        CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
        String input = "jw warp set \"\u50b3\u9001\u9ede\" ";

        var suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse(input, commandSource())).join();
        List<String> values = suggestions.getList().stream().map(suggestion -> suggestion.getText()).toList();

        assertTrue(values.containsAll(List.of("name", "group", "icon", "author", "description", "position")));
    }

    @Test
    void locatesUnquotedChineseName() {
        CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
        String input = "jw warp del \u50b3\u9001\u9ede";

        CommandSyntaxException exception = assertThrows(CommandSyntaxException.class,
                () -> dispatcher.execute(input, commandSource()));

        assertEquals(input.indexOf('\u50b3'), exception.getCursor());
        assertTrue(exception.getContext().endsWith("<--[HERE]"));
    }

    private CommandDispatcher<CommandSourceStack> dispatcher() {
        JsonStore store = new JsonStore(temporary.resolve("justwarp"));
        Translations translations = new Translations();
        try {
            translations.reload(store, "zh_TW");
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }
        JustWarpService service = new JustWarpService(store, translations);
        WarpGui warpGui = new WarpGui(service);
        IconGui iconGui = new IconGui(service, warpGui);
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        new JustWarpCommands(service, warpGui, iconGui).register(dispatcher);
        return dispatcher;
    }

    private static void assertSynchronizable(CommandNode<CommandSourceStack> node) {
        if (node instanceof ArgumentCommandNode<CommandSourceStack, ?> argumentNode) {
            assertDoesNotThrow(() -> ArgumentTypeInfos.unpack(argumentNode.getType()));
        }
        node.getChildren().forEach(CommandNameArgumentRegistrationTest::assertSynchronizable);
    }

    private static CommandSourceStack commandSource() {
        return new CommandSourceStack(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null,
                PermissionSet.ALL_PERMISSIONS, "test", Component.literal("test"), null, null);
    }

    private static void assertParses(CommandDispatcher<CommandSourceStack> dispatcher, CommandSourceStack source,
                                     String command) {
        ParseResults<CommandSourceStack> result = dispatcher.parse(command, source);
        assertTrue(result.getExceptions().isEmpty(), () -> "Command parse exceptions: " + result.getExceptions());
        assertTrue(!result.getReader().canRead(), () -> "Unparsed command input: " + result.getReader().getRemaining());
    }

    private static void assertUsesTypedValue(CommandDispatcher<CommandSourceStack> dispatcher,
                                             CommandSourceStack source, String command) {
        ParseResults<CommandSourceStack> result = dispatcher.parse(command, source);
        assertParses(dispatcher, source, command);
        assertEquals("value", result.getContext().getNodes().getLast().getNode().getName());
    }
}
