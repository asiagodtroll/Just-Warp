package io.github.asiagodtroll.justwarp.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import io.github.asiagodtroll.justwarp.service.JustWarpService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

final class CommandSuggestions {
    private final JustWarpService manager;
    private final List<String> vanillaItemNames;

    CommandSuggestions(JustWarpService manager) {
        this.manager = manager;
        vanillaItemNames = BuiltInRegistries.ITEM.keySet().stream().map(Object::toString).toList();
    }

    CompletableFuture<Suggestions> warps(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return suggestQuoted(manager.warps().stream().map(warp -> warp.name()).toList(), builder, "suggest.warp");
    }

    CompletableFuture<Suggestions> groups(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return suggestQuoted(manager.groups().stream().map(group -> group.name()).toList(), builder, "suggest.group");
    }

    CompletableFuture<Suggestions> iconReferences(CommandContext<CommandSourceStack> context,
                                                  SuggestionsBuilder builder) {
        List<Candidate> names = new ArrayList<>();
        customIconNames().forEach(name -> names.add(new Candidate(name, "suggest.custom_icon")));
        vanillaItemNames.forEach(name -> names.add(new Candidate(name, "suggest.item_icon")));
        return suggestCandidates(names, builder);
    }

    CompletableFuture<Suggestions> customIcons(CommandContext<CommandSourceStack> context,
                                                SuggestionsBuilder builder) {
        return suggestQuoted(customIconNames(), builder, "suggest.custom_icon");
    }

    CompletableFuture<Suggestions> groupsOrNone(CommandContext<CommandSourceStack> context,
                                                 SuggestionsBuilder builder) {
        List<Candidate> names = new ArrayList<>();
        names.add(new Candidate("none", "suggest.no_group"));
        manager.groups().forEach(group -> names.add(new Candidate(group.name(), "suggest.group")));
        return suggestCandidates(names, builder);
    }

    private List<String> customIconNames() {
        return manager.icons().stream().map(icon -> icon.name()).toList();
    }

    private CompletableFuture<Suggestions> suggestQuoted(List<String> names, SuggestionsBuilder builder,
                                                          String tooltipKey) {
        return suggestCandidates(names.stream().map(name -> new Candidate(name, tooltipKey)).toList(), builder);
    }

    private CompletableFuture<Suggestions> suggestCandidates(List<Candidate> candidates, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(candidates, builder,
                candidate -> StringArgumentType.escapeIfRequired(candidate.value()),
                candidate -> Component.literal(manager.translations().text(candidate.tooltipKey())));
    }

    private record Candidate(String value, String tooltipKey) {}

}
