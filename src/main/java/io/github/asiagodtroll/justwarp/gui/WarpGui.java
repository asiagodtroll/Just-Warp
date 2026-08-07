package io.github.asiagodtroll.justwarp.gui;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import io.github.asiagodtroll.justwarp.domain.Warp;
import io.github.asiagodtroll.justwarp.domain.WarpGroup;
import io.github.asiagodtroll.justwarp.service.WarpException;
import io.github.asiagodtroll.justwarp.service.JustWarpService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class WarpGui {
    private static final int BACK_SLOT = 51;
    private final JustWarpService manager;

    public WarpGui(JustWarpService manager) {
        this.manager = manager;
    }

    public void openRoot(ServerPlayer player, int page) {
        List<Entry> entries = new ArrayList<>();
        manager.groups().forEach(group -> entries.add(new GroupEntry(group, false)));
        manager.ungroupedWarps().forEach(warp -> entries.add(new WarpEntry(warp)));
        PagedChestView.Button backButton = new PagedChestView.Button(
                IconStackFactory.create(manager, "back", manager.translations().text("gui.back"), null), this::back);
        open(player, page, manager.translations().text("gui.root.title"), entries, null,
                Map.of(BACK_SLOT, backButton));
    }

    public void openWarps(ServerPlayer player, int page) {
        List<Entry> entries = manager.warps().stream().map(WarpEntry::new).map(Entry.class::cast).toList();
        open(player, page, manager.translations().text("gui.warps.title"), entries, null, Map.of());
    }

    public void openGroups(ServerPlayer player, int page) {
        List<Entry> entries = manager.groups().stream()
                .map(group -> new GroupEntry(group, true)).map(Entry.class::cast).toList();
        open(player, page, manager.translations().text("gui.groups.title"), entries, null, Map.of());
    }

    public void openIcon(ServerPlayer player, String icon, int page, Consumer<ServerPlayer> back) {
        List<Entry> entries = manager.warps().stream().filter(warp -> warp.icon().equals(icon))
                .map(WarpEntry::new).map(Entry.class::cast).toList();
        open(player, page, manager.translations().text("gui.icon.title", icon), entries, back, Map.of());
    }

    private void openGroup(ServerPlayer player, String group, int page, boolean backToGroups) {
        List<Entry> entries = manager.warpsIn(group).stream()
                .map(WarpEntry::new).map(Entry.class::cast).toList();
        open(player, page, manager.translations().text("gui.group.title", group), entries,
                clickingPlayer -> {
                    if (backToGroups) {
                        openGroups(clickingPlayer, 0);
                    } else {
                        openRoot(clickingPlayer, 0);
                    }
                }, Map.of());
    }

    private void open(ServerPlayer player, int page, String title, List<Entry> entries,
                      Consumer<ServerPlayer> back, Map<Integer, PagedChestView.Button> buttons) {
        List<ItemStack> items = entries.stream().map(this::stack).toList();
        PagedChestView.open(manager, player, page, title, items, manager.translations(),
                (clickingPlayer, index) -> activate(clickingPlayer, entries.get(index)),
                back, buttons);
    }

    private void activate(ServerPlayer player, Entry entry) {
        if (entry instanceof GroupEntry group) {
            openGroup(player, group.value().name(), 0, group.backToGroups());
            return;
        }
        Warp warp = ((WarpEntry) entry).value();
        player.closeContainer();
        try {
            manager.teleport(player, warp.name());
            player.sendSystemMessage(manager.translations().message("success.teleported", warp.name()));
        } catch (WarpException e) {
            player.sendSystemMessage(manager.translations().message(e.translationKey(), e.arguments()));
        }
    }

    private void back(ServerPlayer player) {
        player.closeContainer();
        try {
            manager.back(player);
            player.sendSystemMessage(manager.translations().message("success.back"));
        } catch (WarpException e) {
            player.sendSystemMessage(manager.translations().message(e.translationKey(), e.arguments()));
        }
    }

    private ItemStack stack(Entry entry) {
        String icon = entry instanceof GroupEntry group ? group.value().icon() : ((WarpEntry) entry).value().icon();
        String name = entry instanceof GroupEntry group ? group.value().name() : ((WarpEntry) entry).value().name();
        if (entry instanceof GroupEntry) {
            return IconStackFactory.create(manager, icon, name, manager.translations().text("gui.group.lore"));
        }
        Warp warp = ((WarpEntry) entry).value();
        List<Component> lore = WarpLoreFormatter.format(manager.translations(), warp);
        return IconStackFactory.createStyled(manager, icon, name, lore);
    }

    private sealed interface Entry permits GroupEntry, WarpEntry {}
    private record GroupEntry(WarpGroup value, boolean backToGroups) implements Entry {}
    private record WarpEntry(Warp value) implements Entry {}
}
