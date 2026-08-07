package io.github.asiagodtroll.justwarp.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import io.github.asiagodtroll.justwarp.domain.CustomIcon;
import io.github.asiagodtroll.justwarp.domain.Warp;
import io.github.asiagodtroll.justwarp.domain.WarpConfig;
import io.github.asiagodtroll.justwarp.domain.WarpData;
import io.github.asiagodtroll.justwarp.domain.WarpGroup;
import io.github.asiagodtroll.justwarp.domain.WarpLocation;
import io.github.asiagodtroll.justwarp.persistence.JsonStore;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public final class JustWarpService {
    private final JsonStore store;
    private final Translations translations;
    private final WarpDataValidator validator = new WarpDataValidator();
    private final TeleportService teleportService = new TeleportService();
    private final BackHistory backHistory = new BackHistory();
    private final WarpMutationService warpMutations;
    private final GroupMutationService groupMutations;
    private final IconMutationService iconMutations;
    private WarpConfig config = new WarpConfig("zh_TW", 2, 1, 5, 3);
    private WarpState state = new WarpState(List.of(), List.of(), List.of());
    private boolean available;
    private String unavailableReason = "Initialization has not completed";

    public JustWarpService(JsonStore store, Translations translations) {
        this.store = store;
        this.translations = translations;
        warpMutations = new WarpMutationService(store);
        groupMutations = new GroupMutationService(store);
        iconMutations = new IconMutationService(store);
    }

    public synchronized void initialize(MinecraftServer server) throws IOException {
        try {
            translations.reload(store, "en_US");
            store.ensureDefaults();
            reload(server);
        } catch (IOException exception) {
            unavailableReason = message(exception);
            throw exception;
        }
    }

    public synchronized void reload(MinecraftServer server) throws IOException {
        try {
            WarpConfig candidateConfig = store.loadConfig();
            WarpData candidateData = store.loadData();
            try {
                validator.validate(candidateData, server);
            } catch (WarpException exception) {
                throw new IOException(translations.text(exception.translationKey(), exception.arguments()), exception);
            }
            translations.reload(store, candidateConfig.locale());
            config = candidateConfig;
            state = new WarpState(candidateData.groups(), candidateData.warps(), candidateData.icons());
            available = true;
            unavailableReason = null;
        } catch (IOException exception) {
            if (!available) {
                unavailableReason = message(exception);
            }
            throw exception;
        }
    }

    public boolean available() {
        return available;
    }

    public String unavailableReason() {
        return unavailableReason;
    }

    public WarpConfig config() {
        return config;
    }

    public List<WarpGroup> groups() {
        return state.groups();
    }

    public List<Warp> warps() {
        return state.warps();
    }

    public List<CustomIcon> icons() {
        return state.icons();
    }

    public Translations translations() {
        return translations;
    }

    public Optional<Warp> warp(String name) {
        return state.warp(name);
    }

    public Optional<WarpGroup> group(String name) {
        return state.group(name);
    }

    public Optional<CustomIcon> icon(String name) {
        return state.icon(name);
    }

    public List<Warp> ungroupedWarps() {
        return state.ungroupedWarps();
    }

    public List<Warp> warpsIn(String group) {
        return state.warpsIn(group);
    }

    public void addWarp(String name, String author, String group, String icon, WarpLocation location)
            throws WarpException, IOException {
        mutate(current -> warpMutations.add(current, name, author, group, icon, location));
    }

    public void deleteWarp(String name) throws WarpException, IOException {
        mutate(current -> warpMutations.delete(current, name));
    }

    public void renameWarp(String before, String after) throws WarpException, IOException {
        mutate(current -> warpMutations.rename(current, before, after));
    }

    public void setWarpGroup(String name, String group) throws WarpException, IOException {
        mutate(current -> warpMutations.setGroup(current, name, group));
    }

    public void setWarpIcon(String name, String icon) throws WarpException, IOException {
        mutate(current -> warpMutations.setIcon(current, name, icon));
    }

    public void setWarpAuthor(String name, String author) throws WarpException, IOException {
        mutate(current -> warpMutations.setAuthor(current, name, author));
    }

    public void setWarpDescription(String name, String description) throws WarpException, IOException {
        mutate(current -> warpMutations.setDescription(current, name, description));
    }

    public void setWarpPosition(String name, WarpLocation location) throws WarpException, IOException {
        mutate(current -> warpMutations.setPosition(current, name, location));
    }

    public void addGroup(String name, String icon) throws WarpException, IOException {
        mutate(current -> groupMutations.add(current, name, icon));
    }

    public void deleteGroup(String name) throws WarpException, IOException {
        mutate(current -> groupMutations.delete(current, name));
    }

    public void renameGroup(String before, String after) throws WarpException, IOException {
        mutate(current -> groupMutations.rename(current, before, after));
    }

    public void setGroupIcon(String name, String icon) throws WarpException, IOException {
        mutate(current -> groupMutations.setIcon(current, name, icon));
    }

    public void addIcon(String name, String base64) throws WarpException, IOException {
        mutate(current -> iconMutations.add(current, name, base64));
    }

    public void deleteIcon(String name) throws WarpException, IOException {
        mutate(current -> iconMutations.delete(current, name));
    }

    public void setIconBase64(String name, String base64) throws WarpException, IOException {
        mutate(current -> iconMutations.setBase64(current, name, base64));
    }

    public synchronized void teleport(ServerPlayer player, String warpName) throws WarpException {
        Warp warp = availableState().requireWarp(warpName);
        WarpLocation previous = PlayerLocations.capture(player);
        teleportService.teleport(player, warp, config);
        backHistory.remember(player.getUUID(), previous);
    }

    public synchronized void back(ServerPlayer player) throws WarpException {
        availableState();
        WarpLocation target = backHistory.find(player.getUUID())
                .orElseThrow(() -> new WarpException("error.back_missing"));
        WarpLocation previous = PlayerLocations.capture(player);
        teleportService.teleport(player, new Warp("back", "system", "", "minecraft:barrier", target), config);
        backHistory.remember(player.getUUID(), previous);
    }

    private synchronized void mutate(StateMutation mutation) throws WarpException, IOException {
        WarpState current = availableState();
        state = mutation.apply(current);
    }

    private WarpState availableState() throws WarpException {
        if (!available) {
            throw new WarpException("error.storage_unavailable", unavailableReason);
        }
        return state;
    }

    private static String message(IOException exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? exception.getClass().getSimpleName() : exception.getMessage();
    }

    @FunctionalInterface
    private interface StateMutation {
        WarpState apply(WarpState current) throws WarpException, IOException;
    }
}
