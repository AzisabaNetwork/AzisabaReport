package net.azisaba.azisabareport.spigot.listener;

import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitChannelChatEvent;
import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitGlobalChatEvent;
import net.azisaba.azisabareport.common.message.ChatMessage;
import net.azisaba.azisabareport.spigot.AzisabaReport;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class LunaChatListener implements Listener {
    private final AzisabaReport plugin;

    public LunaChatListener(@NotNull AzisabaReport plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handleChannelPrivate(LunaChatBukkitChannelChatEvent e) {
        ChatMessage.Type type;
        if (e.getChannel().isPersonalChat()) {
            type = ChatMessage.Type.PRIVATE;
        } else {
            type = ChatMessage.Type.CHANNEL;
        }
        ChatMessage cm = new ChatMessage(
                type,
                e.getMember().getUniqueId(),
                e.getMember().getName(),
                e.getMember().getDisplayName(),
                e.getChannelName(),
                e.getPreReplaceMessage(),
                System.currentTimeMillis()
        );
        plugin.getChatMessageHandler().handle(cm);
    }

    @EventHandler
    public void handleGlobal(LunaChatBukkitGlobalChatEvent e) {
        if (!(e.getSender() instanceof Player)) {
            return;
        }
        Player player = (Player) e.getSender();
        ChatMessage cm = new ChatMessage(
                ChatMessage.Type.GLOBAL,
                player.getUniqueId(),
                player.getName(),
                player.getDisplayName(),
                null,
                e.getOriginalMessage(),
                System.currentTimeMillis()
        );
        plugin.getChatMessageHandler().handle(cm);
    }
}
