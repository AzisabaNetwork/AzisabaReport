package azisaba.net.azisabareport;

import net.md_5.bungee.api.plugin.Plugin;

public final class AzisabaReport extends Plugin {

    private static AzisabaReport plugin;

    public void onEnable() {
        plugin = this;
        ConfigManager.loadConfig();
        final ReportCommand reportCommand = new ReportCommand();
        final ReportBugCommand reportBugCommand = new ReportBugCommand();
        final AzisabaReportCoreCommand azisabaReportCoreCommand = new AzisabaReportCoreCommand();
        this.getProxy().getPluginManager().registerCommand(this, reportCommand);
        this.getProxy().getPluginManager().registerCommand(this, reportBugCommand);
        this.getProxy().getPluginManager().registerCommand(this, azisabaReportCoreCommand);
    }

    public static AzisabaReport getInstance() {
        return plugin;
    }
}