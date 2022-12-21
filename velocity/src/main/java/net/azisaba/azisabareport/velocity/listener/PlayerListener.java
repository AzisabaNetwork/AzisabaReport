package net.azisaba.azisabareport.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import net.azisaba.azisabareport.velocity.AzisabaReport;
import org.jetbrains.annotations.NotNull;

public class PlayerListener {
    private final AzisabaReport plugin;

    public PlayerListener(@NotNull AzisabaReport plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onLogin(LoginEvent e) {
        plugin.getDatabaseManager().queue("INSERT INTO `players` (`id`, `name`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `name` = VALUES(`name`)", ps -> {
            ps.setString(1, e.getPlayer().getUniqueId().toString());
            ps.setString(2, e.getPlayer().getUsername());
            ps.executeUpdate();
        });
    }
}
