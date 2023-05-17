package net.azisaba.azisabareport.spigot.commands;

import net.azisaba.azisabareport.spigot.AzisabaReport;
import net.azisaba.azisabareport.spigot.data.ReportData;
import net.azisaba.azisabareport.spigot.gui.ReportsScreen;
import net.azisaba.azisabareport.spigot.sql.DataProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ReportsCommand implements TabExecutor {
    private final AzisabaReport plugin;

    public ReportsCommand(@NotNull AzisabaReport plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        UUID target;
        if (args.length > 0 && sender.hasPermission("azisabareport.reports.others")) {
            Player targetPlayer = Bukkit.getPlayerExact(args[0]);
            if (targetPlayer != null) {
                target = targetPlayer.getUniqueId();
            } else {
                target = UUID.fromString(args[0]);
            }
        } else {
            target = player.getUniqueId();
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<ReportData> reports = DataProvider.getReportsBy(plugin.getDatabaseManager(), target);
            ReportsScreen screen = new ReportsScreen(plugin, reports);
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(screen.getInventory()));
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return Collections.emptyList();
    }
}
