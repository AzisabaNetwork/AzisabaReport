package net.azisaba.azisabareport.velocity.tasks;

import com.velocitypowered.api.proxy.Player;
import net.azisaba.azisabareport.velocity.AzisabaReport;
import net.azisaba.azisabareport.velocity.data.PlayerData;
import net.azisaba.azisabareport.velocity.data.ReportData;
import net.azisaba.azisabareport.velocity.message.Messages;
import net.azisaba.azisabareport.velocity.sql.DataProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CheckClosedReportTask implements Runnable {
    private final AzisabaReport plugin;
    private final List<ReportData> reports = new ArrayList<>();

    public CheckClosedReportTask(@NotNull AzisabaReport plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        List<ReportData> newReports = DataProvider.getRecentReports(plugin.getDatabaseManager());
        for (ReportData report : reports) {
            if (report.flags().contains(ReportData.CLOSED)) {
                continue;
            }
            ReportData newReport = newReports.stream().filter(r -> r.id() == report.id()).findFirst().orElse(null);
            if (newReport == null) {
                continue;
            }
            if (newReport.flags().contains(ReportData.CLOSED)) {
                onReportClosed(newReport);
            }
        }
        reports.clear();
        reports.addAll(newReports);
    }

    private void onReportClosed(@NotNull ReportData report) {
        Optional<Player> player = plugin.getServer().getPlayer(report.reporterId());
        player.ifPresent(p -> {
            boolean containsValidFlags = false;
            if (report.flags().contains(ReportData.RESOLVED)) {
                containsValidFlags = true;
                Messages.sendFormatted(p, "report.resolved");
            } else if (report.flags().contains(ReportData.NEED_MORE_PROOF)) {
                containsValidFlags = true;
                PlayerData data = DataProvider.getPlayerDataById(plugin.getDatabaseManager(), report.reportedId()).orElseThrow(IllegalArgumentException::new);
                Messages.sendFormatted(p, "report.need_more_proof", data.name());
            } else if (report.flags().contains(ReportData.INVALID)) {
                containsValidFlags = true;
                PlayerData data = DataProvider.getPlayerDataById(plugin.getDatabaseManager(), report.reportedId()).orElseThrow(IllegalArgumentException::new);
                Messages.sendFormatted(p, "report.invalid", data.name());
            }
            if (containsValidFlags && report.publicComment() != null) {
                Messages.sendFormatted(p, "report.additional_comment", report.publicComment());
            }
        });
    }
}
