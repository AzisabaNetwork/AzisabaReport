package net.azisaba.azisabareport.spigot.commands;

import net.azisaba.azisabareport.common.message.ChatMessage;
import net.azisaba.azisabareport.spigot.sql.DataProvider;
import net.azisaba.azisabareport.spigot.sql.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class LookupMessagesCommand implements TabExecutor {
    private final Plugin plugin;
    private final DatabaseManager db;

    public LookupMessagesCommand(@NotNull Plugin plugin, @NotNull DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int page = Math.max(1, Arrays.stream(args).filter(s -> s.startsWith("page=")).findAny().map(s -> Integer.parseInt(s.substring(5))).orElse(1));
            int pageIndex = page - 1;
            List<OfflinePlayer> players = Arrays.stream(args).filter(s -> !s.startsWith("page=")).map(Bukkit::getOfflinePlayer).filter(Objects::nonNull).collect(Collectors.toList());
            List<UUID> uuids = players.stream().map(OfflinePlayer::getUniqueId).collect(Collectors.toList());
            List<ChatMessage> messages = DataProvider.getMessages(db, uuids).stream().sorted(Comparator.comparingLong(ChatMessage::getTimestamp)).collect(Collectors.toList());
            Collections.reverse(messages);
            int fromIndex = pageIndex * 25;
            int toIndex = Math.min(page * 25, messages.size());
            List<ChatMessage> sublist = messages.subList(fromIndex, toIndex);
            Collections.reverse(sublist);
            for (ChatMessage message : sublist) {
                sender.sendMessage(message.toRichString());
            }
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
