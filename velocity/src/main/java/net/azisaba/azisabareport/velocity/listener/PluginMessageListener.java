package net.azisaba.azisabareport.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.azisaba.azisabareport.common.data.PlayerPosData;
import net.azisaba.azisabareport.common.message.ChatMessage;
import net.azisaba.azisabareport.common.util.ByteBufUtil;
import net.azisaba.azisabareport.velocity.AzisabaReport;
import net.azisaba.azisabareport.velocity.redis.RedisKeys;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;
import xyz.acrylicstyle.util.serialization.decoder.ByteBufValueDecoder;
import xyz.acrylicstyle.util.serialization.encoder.ByteBufValueEncoder;

import java.util.List;

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
        if (e.getIdentifier().getId().equals("azisabareport:chat") || e.getIdentifier().getId().equals("AZIREPORT")) {
            e.setResult(PluginMessageEvent.ForwardResult.handled());
            handleChat(server, e.getData());
            return;
        }
        if (e.getIdentifier().getId().equals("azisabareport:pp") || e.getIdentifier().getId().equals("AZIREPORT_PP")) {
            e.setResult(PluginMessageEvent.ForwardResult.handled());
            handlePlayerPos(server, e.getData());
        }
    }

    private void handleChat(@NotNull ServerConnection server, byte[] data) {
        ByteBuf buf = Unpooled.wrappedBuffer(data);
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

    private void handlePlayerPos(@NotNull ServerConnection server, byte[] data) {
        ByteBuf buf = Unpooled.wrappedBuffer(data);
        List<PlayerPosData> posData;
        try {
            posData = PlayerPosData.NETWORK_CODEC.list().decode(new ByteBufValueDecoder(buf));
        } finally {
            buf.release();
        }
        try (Jedis jedis = plugin.getJedisBox().getJedisPool().getResource()) {
            posData.forEach(pos -> {
                byte[] bytes = ByteBufUtil.toByteArray(b -> PlayerPosData.CODEC.encode(pos.server(server.getServerInfo().getName()), new ByteBufValueEncoder(b)));
                jedis.set(RedisKeys.playerPos(pos.uuid()), bytes, SetParams.setParams().ex(30));
            });
        }
    }
}
