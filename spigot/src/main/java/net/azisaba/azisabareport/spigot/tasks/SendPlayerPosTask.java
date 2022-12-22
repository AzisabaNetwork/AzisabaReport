package net.azisaba.azisabareport.spigot.tasks;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.azisaba.azisabareport.common.data.PlayerPosData;
import net.azisaba.azisabareport.common.util.ByteBufUtil;
import net.azisaba.azisabareport.common.util.ListUtil;
import net.azisaba.azisabareport.spigot.AzisabaReport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.util.serialization.encoder.ByteBufValueEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SendPlayerPosTask extends BukkitRunnable {
    private final AzisabaReport plugin;

    public SendPlayerPosTask(@NotNull AzisabaReport plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try {
            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            if (players.isEmpty()) return;
            List<List<Player>> split = ListUtil.split(players, 250);
            for (List<Player> list : split) {
                List<PlayerPosData> posData = list.stream().map(p -> {
                    Location loc = p.getLocation();
                    return new PlayerPosData(p.getUniqueId(), loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
                }).collect(Collectors.toList());
                ByteBuf buf = Unpooled.buffer();
                ByteBufValueEncoder encoder = new ByteBufValueEncoder(buf);
                try {
                    PlayerPosData.NETWORK_CODEC.list().encode(posData, encoder);
                    String channel;
                    if (AzisabaReport.isLegacy) {
                        channel = AzisabaReport.LEGACY_CHANNEL_PLAYER_POS;
                    } else {
                        channel = AzisabaReport.CHANNEL_PLAYER_POS;
                    }
                    Bukkit.getOnlinePlayers()
                            .stream()
                            .findAny()
                            .orElseThrow(IllegalArgumentException::new)
                            .sendPluginMessage(plugin, channel, ByteBufUtil.toByteArray(buf));
                } finally {
                    if (buf.refCnt() > 0) {
                        buf.release();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
