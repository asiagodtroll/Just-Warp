package io.github.asiagodtroll.justwarp.gui;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import io.github.asiagodtroll.justwarp.domain.CustomIcon;
import io.github.asiagodtroll.justwarp.service.JustWarpService;

import java.util.List;

final class IconStackFactory {
    private static final ItemStack FALLBACK_ICON = new ItemStack(Items.STONE);

    private IconStackFactory() {}

    static ItemStack create(JustWarpService manager, String reference, String displayName, String lore) {
        List<Component> components = lore == null ? null : lore.lines().<Component>map(line -> Component.literal(line)
                .withStyle(style -> style.withItalic(false))).toList();
        return createStyled(manager, reference, displayName, components);
    }

    static ItemStack createStyled(JustWarpService manager, String reference, String displayName, List<Component> lore) {
        ItemStack stack = manager.icon(reference)
                .map(IconStackFactory::head)
                .orElseGet(() -> item(reference));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(displayName)
                .withStyle(ChatFormatting.YELLOW)
                .withStyle(style -> style.withBold(true).withItalic(false)));
        if (lore != null) {
            stack.set(DataComponents.LORE, new ItemLore(lore));
        }
        return stack;
    }

    private static ItemStack item(String reference) {
        Identifier identifier = Identifier.tryParse(reference);
        if (identifier == null) {
            return FALLBACK_ICON.copy();
        }
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(identifier));
        return stack.isEmpty() ? FALLBACK_ICON.copy() : stack;
    }

    private static ItemStack head(CustomIcon icon) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        stack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(PlayerHeadProfileFactory.create(icon)));
        return stack;
    }
}
