package net.azisaba.azisabareport.spigot.listener;

import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitChannelChatEvent;
import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitGlobalChatEvent;
import net.azisaba.azisabareport.spigot.AzisabaReport;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ChatListener implements Listener {
    private final AzisabaReport plugin;

    public ChatListener(@NotNull AzisabaReport plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handleChannelPrivate(LunaChatBukkitChannelChatEvent e) {
        e.getMember().getUniqueId();
    }

    @EventHandler
    public void handleGlobal(LunaChatBukkitGlobalChatEvent e) {
    }
}
