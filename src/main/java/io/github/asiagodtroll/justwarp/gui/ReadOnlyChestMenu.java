package io.github.asiagodtroll.justwarp.gui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiConsumer;

public final class ReadOnlyChestMenu extends ChestMenu {
    private final BiConsumer<Integer, Player> clickHandler;

    public ReadOnlyChestMenu(int syncId, Inventory inventory, Container container,
                             BiConsumer<Integer, Player> clickHandler) {
        super(MenuType.GENERIC_9x6, syncId, inventory, container, 6);
        this.clickHandler = clickHandler;
    }

    @Override
    public void clicked(int slot, int button, ContainerInput input, Player player) {
        if (slot >= 0 && slot < 54) {
            if (input == ContainerInput.PICKUP) {
                clickHandler.accept(slot, player);
            }
            return;
        }
        super.clicked(slot, button, input, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }
}
