package azisaba.net.azisabareport;

import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public final class AzisabaReport extends Plugin {

    private static AzisabaReport plugin;

    public void onEnable() {
        plugin = this;
        ConfigManager.loadConfig();
        final ReportCommand reportCommand = new ReportCommand("report");
        final ReportBugCommand reportBugCommand = new ReportBugCommand("reportbug");
        final AzisabaReportCoreCommand azisabaReportCoreCommand = new AzisabaReportCoreCommand("AzisabaReport");
        this.getProxy().getPluginManager().registerCommand((Plugin) this, (Command) reportCommand);
        this.getProxy().getPluginManager().registerCommand((Plugin) this, (Command) reportBugCommand);
        this.getProxy().getPluginManager().registerCommand((Plugin) this, (Command) azisabaReportCoreCommand);
    }

    public void onDisable() {
    }

    public static AzisabaReport getInstance() {
        return plugin;
    }
}