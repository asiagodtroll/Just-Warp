package io.github.asiagodtroll.justwarp.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
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
import io.github.asiagodtroll.justwarp.persistence.JsonStore;
import io.github.asiagodtroll.justwarp.service.JustWarpService;
import io.github.asiagodtroll.justwarp.service.Translations;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

        assertParses(dispatcher, source, "jw warp add \u50b3\u9001\u9ede minecraft:stone \u57ce\u93ae");
        assertParses(dispatcher, source, "jw warp add \u50b3\u9001\u9ede \u5730\u6a19");
        assertParses(dispatcher, source, "jw group add \u57ce\u93ae \u5730\u6a19");
    }

    @Test
    void parsesDeleteCommandsWithChineseReferences() {
        CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
        CommandSourceStack source = commandSource();

        assertParses(dispatcher, source, "jw warp del \u50b3\u9001\u9ede");
        assertParses(dispatcher, source, "jw group del \u57ce\u93ae");
        assertParses(dispatcher, source, "jw icon del \u5730\u6a19");
    }

    @Test
    void suggestsNamespacedVanillaItemsForIconReferences() {
        JsonStore store = new JsonStore(temporary.resolve("justwarp"));
        JustWarpService service = new JustWarpService(store, new Translations());
        CommandSuggestions suggestions = new CommandSuggestions(service);

        var result = suggestions.iconReferences(null, new SuggestionsBuilder("minecraft:sto", 0)).join();

        assertEquals(1, result.getList().stream()
                .filter(suggestion -> suggestion.getText().equals("minecraft:stone")).count());
    }

    private CommandDispatcher<CommandSourceStack> dispatcher() {
        JsonStore store = new JsonStore(temporary.resolve("justwarp"));
        JustWarpService service = new JustWarpService(store, new Translations());
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
}
