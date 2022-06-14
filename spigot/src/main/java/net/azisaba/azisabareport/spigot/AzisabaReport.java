package net.azisaba.azisabareport.spigot;

import net.azisaba.azisabareport.spigot.listener.ChatListener;
import net.azisaba.azisabareport.spigot.message.ChatMessageHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class AzisabaReport extends JavaPlugin {
    private final ChatMessageHandler chatMessageHandler = new ChatMessageHandler(this);

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
    }
}
