package net.azisaba.azisabareport.spigot.message;

import net.azisaba.azisabareport.common.message.ChatMessage;
import net.azisaba.azisabareport.spigot.AzisabaReport;
import org.jetbrains.annotations.NotNull;

public class ChatMessageHandler {
    private final AzisabaReport plugin;

    public ChatMessageHandler(@NotNull AzisabaReport plugin) {
        this.plugin = plugin;
    }

    public void handle(@NotNull ChatMessage message) {
    }
}
