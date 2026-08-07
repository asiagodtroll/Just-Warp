package io.github.asiagodtroll.justwarp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.asiagodtroll.justwarp.command.JustWarpCommands;
import io.github.asiagodtroll.justwarp.gui.IconGui;
import io.github.asiagodtroll.justwarp.gui.WarpGui;
import io.github.asiagodtroll.justwarp.persistence.JsonStore;
import io.github.asiagodtroll.justwarp.service.JustWarpService;
import io.github.asiagodtroll.justwarp.service.Translations;

import java.io.IOException;

public final class JustWarpMod implements ModInitializer {
    public static final String MOD_ID = "justwarp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        JsonStore store = new JsonStore(FabricLoader.getInstance().getConfigDir().resolve(MOD_ID));
        Translations translations = new Translations();
        JustWarpService manager = new JustWarpService(store, translations);
        WarpGui gui = new WarpGui(manager);
        IconGui iconGui = new IconGui(manager, gui);
        JustWarpCommands commands = new JustWarpCommands(manager, gui, iconGui);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                commands.register(dispatcher));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            try {
                manager.initialize(server);
                LOGGER.info("Loaded {} groups and {} warps", manager.groups().size(), manager.warps().size());
            } catch (IOException exception) {
                LOGGER.error("JustWarp data could not be loaded. Fix the JSON and run /jw reload.", exception);
            }
        });
    }
}
