package net.azisaba.azisabareport.spigot.listener;

import net.azisaba.azisabareport.common.message.ChatMessage;
import net.azisaba.azisabareport.spigot.AzisabaReport;
import net.azisaba.ryuzupluginchat.event.AsyncChannelMessageEvent;
import net.azisaba.ryuzupluginchat.event.AsyncGlobalMessageEvent;
import net.azisaba.ryuzupluginchat.event.AsyncPrivateMessageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class RyuZUPluginChatListener implements Listener {
    private final AzisabaReport plugin;

    public RyuZUPluginChatListener(@NotNull AzisabaReport plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handlePrivate(AsyncPrivateMessageEvent e) {
        ChatMessage cm = new ChatMessage(
                ChatMessage.Type.PRIVATE,
                e.getMessage().getSentPlayerUuid(),
                e.getMessage().getSentPlayerName(),
                e.getMessage().getSentPlayerDisplayName(),
                e.getMessage().getSentPlayerName() + ">" + e.getMessage().getReceivedPlayerName(),
                e.getMessage().getPreReplaceMessage(),
                System.currentTimeMillis()
        );
        plugin.getChatMessageHandler().handle(cm);
    }

    @EventHandler
    public void handleChannel(AsyncChannelMessageEvent e) {
        ChatMessage cm = new ChatMessage(
                ChatMessage.Type.CHANNEL,
                e.getMessage().getPlayerUuid(),
                e.getMessage().getPlayerName(),
                e.getMessage().getPlayerDisplayName(),
                e.getMessage().getLunaChatChannelName(),
                e.getMessage().getPreReplaceMessage(),
                System.currentTimeMillis()
        );
        plugin.getChatMessageHandler().handle(cm);
    }

    @EventHandler
    public void handleGlobal(AsyncGlobalMessageEvent e) {
        ChatMessage cm = new ChatMessage(
                ChatMessage.Type.GLOBAL,
                e.getMessage().getPlayerUuid(),
                e.getMessage().getPlayerName(),
                e.getMessage().getPlayerDisplayName(),
                null,
                e.getMessage().getPreReplaceMessage(),
                System.currentTimeMillis()
        );
        plugin.getChatMessageHandler().handle(cm);
    }
}
