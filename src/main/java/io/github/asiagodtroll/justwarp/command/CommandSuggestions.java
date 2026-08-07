package io.github.asiagodtroll.justwarp.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import io.github.asiagodtroll.justwarp.service.JustWarpService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

final class CommandSuggestions {
    private final JustWarpService manager;

    CommandSuggestions(JustWarpService manager) {
        this.manager = manager;
    }

    CompletableFuture<Suggestions> warps(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(manager.warps().stream().map(warp -> warp.name()).toList(), builder);
    }

    CompletableFuture<Suggestions> groups(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(manager.groups().stream().map(group -> group.name()).toList(), builder);
    }

    CompletableFuture<Suggestions> iconReferences(CommandContext<CommandSourceStack> context,
                                                  SuggestionsBuilder builder) {
        List<String> names = new ArrayList<>(customIconNames());
        BuiltInRegistries.ITEM.keySet().forEach(identifier -> names.add(identifier.toString()));
        return SharedSuggestionProvider.suggest(names, builder);
    }

    CompletableFuture<Suggestions> customIcons(CommandContext<CommandSourceStack> context,
                                                SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(customIconNames(), builder);
    }

    CompletableFuture<Suggestions> groupsOrNone(CommandContext<CommandSourceStack> context,
                                                 SuggestionsBuilder builder) {
        List<String> names = new ArrayList<>();
        names.add("none");
        manager.groups().forEach(group -> names.add(group.name()));
        return SharedSuggestionProvider.suggest(names, builder);
    }

    private List<String> customIconNames() {
        return manager.icons().stream().map(icon -> icon.name()).toList();
    }
}
