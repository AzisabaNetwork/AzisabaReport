package net.azisaba.azisabareport.velocity;

import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.azisaba.azisabareport.velocity.commands.ReportCommand;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.azisaba.azisabareport.velocity.commands.AzisabaReportCoreCommand;
import net.azisaba.azisabareport.velocity.commands.ReportBugCommand;
import net.azisaba.azisabareport.velocity.listener.PlayerListener;
import net.azisaba.azisabareport.velocity.listener.PluginMessageListener;
import net.azisaba.azisabareport.velocity.message.Messages;
import net.azisaba.azisabareport.velocity.sql.DatabaseManager;
import net.azisaba.azisabareport.velocity.tasks.CheckClosedReportTask;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

@Plugin(id = "azisabareport", name = "AzisabaReport", version = "dev-SNAPSHOT",
        url = "https://azisaba.net")
public class AzisabaReport {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final PluginConfig config;
    private final DatabaseManager databaseManager;

    @Inject
    public AzisabaReport(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) throws IOException, SQLException {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.config = new PluginConfig(this);
        this.databaseManager = new DatabaseManager(this.logger, config.databaseConfig.createDataSource());
        plugin = this;
        Messages.load();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getCommandManager().register(new ReportCommand(this).createCommand());
        server.getCommandManager().register("reportbug", new ReportBugCommand(this));
        server.getCommandManager().register(new AzisabaReportCoreCommand().createCommand());
        server.getEventManager().register(this, new PlayerListener(this));
        server.getEventManager().register(this, new PluginMessageListener(this));
        server.getChannelRegistrar().register(
                new LegacyChannelIdentifier("AZIREPORT"),
                MinecraftChannelIdentifier.create("azisabareport", "chat"));
        server.getScheduler()
                .buildTask(this, new CheckClosedReportTask(this))
                .delay(30, TimeUnit.SECONDS)
                .repeat(1, TimeUnit.MINUTES)
                .schedule();
    }

    public @NotNull ProxyServer getServer() {
        return server;
    }

    public @NotNull Logger getLogger() {
        return logger;
    }

    public @NotNull Path getDataDirectory() {
        return dataDirectory;
    }

    public @NotNull PluginConfig getConfig() {
        return config;
    }

    public @NotNull DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    private static AzisabaReport plugin;
    public static AzisabaReport getInstance() {
        return plugin;
    }
}