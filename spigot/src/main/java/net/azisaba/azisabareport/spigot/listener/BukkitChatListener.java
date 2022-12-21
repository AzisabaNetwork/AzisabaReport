package net.azisaba.azisabareport.spigot.listener;

import net.azisaba.azisabareport.common.message.ChatMessage;
import net.azisaba.azisabareport.spigot.AzisabaReport;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

public class BukkitChatListener implements Listener {
    private final AzisabaReport plugin;

    public BukkitChatListener(@NotNull AzisabaReport plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handleGlobal(AsyncPlayerChatEvent e) {
        ChatMessage cm = new ChatMessage(
                ChatMessage.Type.GLOBAL,
                e.getPlayer().getUniqueId(),
                e.getPlayer().getName(),
                e.getPlayer().getDisplayName(),
                null,
                e.getMessage(),
                System.currentTimeMillis()
        );
        plugin.getChatMessageHandler().handle(cm);
    }
}
