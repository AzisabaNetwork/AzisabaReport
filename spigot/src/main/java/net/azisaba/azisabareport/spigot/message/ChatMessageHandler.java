package net.azisaba.azisabareport.spigot.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.azisaba.azisabareport.common.message.ChatMessage;
import net.azisaba.azisabareport.common.util.ByteBufUtil;
import net.azisaba.azisabareport.spigot.AzisabaReport;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.util.serialization.encoder.ByteBufValueEncoder;

public class ChatMessageHandler {
    private final AzisabaReport plugin;

    public ChatMessageHandler(@NotNull AzisabaReport plugin) {
        this.plugin = plugin;
    }

    public void handle(@NotNull ChatMessage message) {
        ByteBuf buf = Unpooled.buffer();
        ChatMessage.NETWORK_CODEC.encode(message, new ByteBufValueEncoder(buf));
        Player player = Bukkit.getOnlinePlayers().stream().findAny().orElse(null);
        if (player == null) {
            return;
        }
        try {
            if (AzisabaReport.isLegacy) {
                player.sendPluginMessage(plugin, AzisabaReport.LEGACY_CHANNEL_CHAT, ByteBufUtil.toByteArray(buf));
            } else {
                player.sendPluginMessage(plugin, AzisabaReport.CHANNEL_CHAT, ByteBufUtil.toByteArray(buf));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send packet");
            e.printStackTrace();
        }
    }
}
