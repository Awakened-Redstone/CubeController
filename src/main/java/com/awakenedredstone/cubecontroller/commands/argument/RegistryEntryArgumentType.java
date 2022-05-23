/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package com.awakenedredstone.cubecontroller.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class RegistryEntryArgumentType<T>
implements ArgumentType<T> {
    private static final Collection<String> EXAMPLES = Arrays.asList("minecraft:potato", "cubecontroller:entity_jump");
    public static final DynamicCommandExceptionType INVALID_REGISTRY_EXCEPTION = new DynamicCommandExceptionType(id -> new TranslatableText("commands.cubecontroller.error.registryNotFound", id));

    protected final Registry<T> registry;

    public RegistryEntryArgumentType(Registry<T> registry) {
        this.registry = registry;
    }

    public static <T> RegistryEntryArgumentType<T> registry(Registry<T> registry) {
        return new RegistryEntryArgumentType<>(registry);
    }

    public static <T> T getRegistryValue(CommandContext<ServerCommandSource> context, String name, Class<T> type) {
        return context.getArgument(name, type);
    }

    @Override
    public T parse(StringReader stringReader) throws CommandSyntaxException {
        Identifier identifier = Identifier.fromCommandInput(stringReader);
        return registry.getOrEmpty(identifier).orElseThrow(() -> INVALID_REGISTRY_EXCEPTION.create(identifier));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return suggestIdentifiers(registry.getIds(), builder);
    }

    public CompletableFuture<Suggestions> suggestIdentifiers(Iterable<Identifier> candidates, SuggestionsBuilder builder) {
        String string = builder.getRemaining().toLowerCase(Locale.ROOT);
        forEachMatching(candidates, string, id -> id, id -> builder.suggest(id.toString()));
        return builder.buildFuture();
    }

    public <S> void forEachMatching(Iterable<S> candidates, String remaining, Function<S, Identifier> identifier, Consumer<S> action) {
        boolean bl = remaining.indexOf(58) > -1;
        for (S object : candidates) {
            Identifier identifier2 = identifier.apply(object);
            if (bl) {
                String string = identifier2.toString();
                if (!CommandSource.shouldSuggest(remaining, string)) continue;
                action.accept(object);
                continue;
            }
            if (!CommandSource.shouldSuggest(remaining, identifier2.getNamespace()) && (!CommandSource.shouldSuggest(remaining, identifier2.getPath()))) continue;
            action.accept(object);
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

