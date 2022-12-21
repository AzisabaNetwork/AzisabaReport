package net.azisaba.azisabareport.velocity.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.azisaba.azisabareport.velocity.AzisabaReport;
import net.azisaba.azisabareport.velocity.message.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public abstract class AbstractCommand {
    @NotNull
    public abstract LiteralArgumentBuilder<CommandSource> createBuilder();

    @Contract(" -> new")
    @NotNull
    public final BrigadierCommand createCommand() {
        return new BrigadierCommand(createBuilder());
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull LiteralArgumentBuilder<CommandSource> literal(@NotNull String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static <T> @NotNull RequiredArgumentBuilder<CommandSource, T> argument(@NotNull String name, @NotNull ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    @NotNull
    public static <S> String getString(@NotNull CommandContext<S> context, @NotNull String name) {
        return StringArgumentType.getString(context, name);
    }

    @Contract(pure = true)
    public static @NotNull SuggestionProvider<CommandSource> suggestPlayers() {
        return (source, builder) -> suggest(AzisabaReport.getInstance().getServer().getAllPlayers().stream().map(Player::getUsername), builder);
    }

    @Contract(pure = true)
    public static @NotNull SuggestionProvider<CommandSource> suggestServers() {
        return (source, builder) ->
                suggest(
                        AzisabaReport.getInstance()
                                .getServer()
                                .getAllServers()
                                .stream()
                                .map(RegisteredServer::getServerInfo)
                                .map(ServerInfo::getName),
                        builder
                );
    }

    @NotNull
    public static CompletableFuture<Suggestions> suggest(@NotNull Stream<String> suggestions, @NotNull SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase(Locale.ROOT);
        suggestions.filter((suggestion) -> matchesSubStr(input, suggestion.toLowerCase(Locale.ROOT))).forEach(builder::suggest);
        return builder.buildFuture();
    }

    public static boolean matchesSubStr(@NotNull String input, @NotNull String suggestion) {
        for(int i = 0; !suggestion.startsWith(input, i); ++i) {
            i = suggestion.indexOf('_', i);
            if (i < 0) {
                return false;
            }
        }
        return true;
    }

    public static int sendMessageMissingPlayer(@NotNull CommandSource source, @Nullable String playerName) {
        Messages.sendFormatted(source, "generic.player-not-found", playerName);
        return 0;
    }

    public static int sendMessageMissingServer(@NotNull CommandSource source, @Nullable String serverName) {
        source.sendMessage(Component.text("Server not found: " + serverName).color(NamedTextColor.RED));
        return 0;
    }
}
