package net.azisaba.azisabareport;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.azisaba.azisabareport.commands.AzisabaReportCoreCommand;
import net.azisaba.azisabareport.commands.ReportBugCommand;
import net.azisaba.azisabareport.commands.ReportCommand;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "azisabareport", name = "AzisabaReport", version = "dev-SNAPSHOT",
        url = "https://azisaba.net")
public class AzisabaReport {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    public AzisabaReport(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        plugin = this;
        ConfigManager.loadConfig();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getCommandManager().register("report", new ReportCommand());
        server.getCommandManager().register("reportbug", new ReportBugCommand());
        server.getCommandManager().register("azisabareport", new AzisabaReportCoreCommand());
    }

    private static AzisabaReport plugin;

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public static AzisabaReport getInstance() {
        return plugin;
    }
}