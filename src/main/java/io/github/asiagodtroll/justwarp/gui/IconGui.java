package io.github.asiagodtroll.justwarp.gui;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import io.github.asiagodtroll.justwarp.service.JustWarpService;

import java.util.List;
import java.util.Map;

public final class IconGui {
    private final JustWarpService manager;
    private final WarpGui warpGui;

    public IconGui(JustWarpService manager, WarpGui warpGui) {
        this.manager = manager;
        this.warpGui = warpGui;
    }

    public void open(ServerPlayer player, int page) {
        List<String> icons = manager.icons().stream().map(icon -> icon.name()).toList();
        List<ItemStack> items = icons.stream()
                .map(icon -> IconStackFactory.create(manager, icon, icon, null)).toList();
        PagedChestView.open(manager, player, page, manager.translations().text("gui.icons.title"), items,
                manager.translations(), (clickingPlayer, index) -> {
                    String icon = icons.get(index);
                    warpGui.openIcon(clickingPlayer, icon, 0, backPlayer -> open(backPlayer, page));
                }, null, Map.of());
    }
}
