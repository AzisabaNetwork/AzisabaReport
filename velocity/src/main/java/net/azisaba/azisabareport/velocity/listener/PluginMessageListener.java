package net.azisaba.azisabareport.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.azisaba.azisabareport.common.message.ChatMessage;
import net.azisaba.azisabareport.velocity.AzisabaReport;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.util.serialization.decoder.ByteBufValueDecoder;

public class PluginMessageListener {
    private final AzisabaReport plugin;

    public PluginMessageListener(@NotNull AzisabaReport plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent e) {
        if (!(e.getSource() instanceof ServerConnection server)) {
            return; // hacking attempt
        }
        if (!e.getIdentifier().getId().equals("azisabareport:chat") && !e.getIdentifier().getId().equals("AZIREPORT")) {
            return;
        }
        e.setResult(PluginMessageEvent.ForwardResult.handled());
        ByteBuf buf = Unpooled.wrappedBuffer(e.getData());
        ChatMessage decodedChatMessage;
        try {
            decodedChatMessage = ChatMessage.NETWORK_CODEC.decode(new ByteBufValueDecoder(buf));
        } finally {
            buf.release();
        }
        plugin.getDatabaseManager().queue("INSERT INTO `messages` VALUES (?, ?, ?, ?, ?, ?, ?, ?)", ps -> {
            ps.setByte(1, (byte) decodedChatMessage.getType().ordinal());
            ps.setString(2, decodedChatMessage.getUniqueId().toString());
            ps.setString(3, decodedChatMessage.getUsername());
            ps.setString(4, decodedChatMessage.getDisplayName());
            ps.setString(5, decodedChatMessage.getChannelName());
            ps.setString(6, decodedChatMessage.getMessage());
            ps.setLong(7, decodedChatMessage.getTimestamp());
            ps.setString(8, server.getServerInfo().getName());
            ps.executeUpdate();
        });
    }
}
