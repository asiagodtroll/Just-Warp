package io.github.asiagodtroll.justwarp.gui;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import io.github.asiagodtroll.justwarp.service.Translations;
import io.github.asiagodtroll.justwarp.service.JustWarpService;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

final class PagedChestView {
    private static final int PAGE_SIZE = 45;
    private static final int PREVIOUS_SLOT = 45;
    private static final int INFO_SLOT = 49;
    private static final int NEXT_SLOT = 53;

    private PagedChestView() {}

    static void open(JustWarpService service, ServerPlayer player, int requestedPage, String title,
                     List<ItemStack> entries, Translations translations,
                     BiConsumer<ServerPlayer, Integer> activate, Consumer<ServerPlayer> back,
                     Map<Integer, Button> buttons) {
        int pageCount = Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.max(0, Math.min(requestedPage, pageCount - 1));
        int from = page * PAGE_SIZE;
        int to = Math.min(entries.size(), from + PAGE_SIZE);
        SimpleContainer container = new SimpleContainer(54);
        for (int index = from; index < to; index++) {
            container.setItem(index - from, entries.get(index));
        }
        for (int slot = PAGE_SIZE; slot < 54; slot++) {
            container.setItem(slot, named(Items.STAINED_GLASS_PANE.white(), " ", null));
        }
        if (page > 0) {
            container.setItem(PREVIOUS_SLOT,
                    IconStackFactory.create(service, "arrow_left", translations.text("gui.previous"), null));
        }
        if (back == null) {
            container.setItem(INFO_SLOT, named(Items.PAPER, translations.text("gui.page", page + 1, pageCount), null));
        } else {
            container.setItem(INFO_SLOT, IconStackFactory.create(service, "back", translations.text("gui.back"),
                    translations.text("gui.page", page + 1, pageCount)));
        }
        if (page + 1 < pageCount) {
            container.setItem(NEXT_SLOT,
                    IconStackFactory.create(service, "arrow_right", translations.text("gui.next"), null));
        }
        buttons.forEach((slot, button) -> container.setItem(slot, button.stack()));

        player.openMenu(new SimpleMenuProvider((syncId, inventory, ignored) ->
                new ReadOnlyChestMenu(syncId, inventory, container, (slot, clickingPlayer) -> {
                    if (!(clickingPlayer instanceof ServerPlayer serverPlayer)) {
                        return;
                    }
                    if (slot == PREVIOUS_SLOT && page > 0) {
                        open(service, serverPlayer, page - 1, title, entries, translations, activate, back, buttons);
                    } else if (slot == NEXT_SLOT && page + 1 < pageCount) {
                        open(service, serverPlayer, page + 1, title, entries, translations, activate, back, buttons);
                    } else if (slot == INFO_SLOT && back != null) {
                        back.accept(serverPlayer);
                    } else if (buttons.containsKey(slot)) {
                        buttons.get(slot).action().accept(serverPlayer);
                    } else if (slot >= 0 && slot < to - from && activate != null) {
                        activate.accept(serverPlayer, from + slot);
                    }
                }), Component.literal(title)
                .withStyle(style -> style.withBold(true).withItalic(false))));
    }

    private static ItemStack named(net.minecraft.world.item.Item item, String name, String lore) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name)
                .withStyle(style -> style.withBold(false).withItalic(false)));
        if (lore != null) {
            stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal(lore)
                    .withStyle(style -> style.withItalic(false)))));
        }
        return stack;
    }

    record Button(ItemStack stack, Consumer<ServerPlayer> action) {}
}
