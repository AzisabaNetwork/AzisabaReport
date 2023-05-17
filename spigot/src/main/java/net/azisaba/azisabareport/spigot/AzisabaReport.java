package net.azisaba.azisabareport.spigot;

import net.azisaba.azisabareport.common.sql.DatabaseConfig;
import net.azisaba.azisabareport.common.util.ClassUtil;
import net.azisaba.azisabareport.spigot.commands.ReportsCommand;
import net.azisaba.azisabareport.spigot.event.SetChatListenerEvent;
import net.azisaba.azisabareport.spigot.gui.ReportsScreen;
import net.azisaba.azisabareport.spigot.listener.BukkitChatListener;
import net.azisaba.azisabareport.spigot.listener.LunaChatListener;
import net.azisaba.azisabareport.spigot.listener.RyuZUPluginChatListener;
import net.azisaba.azisabareport.spigot.message.ChatMessageHandler;
import net.azisaba.azisabareport.spigot.sql.DatabaseManager;
import net.azisaba.azisabareport.spigot.tasks.SendPlayerPosTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public class AzisabaReport extends JavaPlugin {
    public static final String LEGACY_CHANNEL_CHAT = "AZIREPORT";
    public static final String CHANNEL_CHAT = "azisabareport:chat";
    public static final String LEGACY_CHANNEL_PLAYER_POS = "AZIREPORT_PP";
    public static final String CHANNEL_PLAYER_POS = "azisabareport:pp";
    public static boolean isLegacy = false;
    private final ChatMessageHandler chatMessageHandler = new ChatMessageHandler(this);
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            databaseManager = new DatabaseManager(getLogger(), DatabaseManager.createDataSource(createDatabaseConfig()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Bukkit.getPluginManager().registerEvents(new ReportsScreen.EventListener(), this);
        Objects.requireNonNull(Bukkit.getPluginCommand("reports")).setExecutor(new ReportsCommand(this));
        try {
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, CHANNEL_CHAT);
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, CHANNEL_PLAYER_POS);
        } catch (RuntimeException e) {
            isLegacy = true;
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, LEGACY_CHANNEL_CHAT);
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, LEGACY_CHANNEL_PLAYER_POS);
        }
        Bukkit.getScheduler().runTaskLater(this, this::setupChatListener, 1);
        new SendPlayerPosTask(this).runTaskTimerAsynchronously(this, 20, 20);
    }

    @Contract(" -> new")
    private @NotNull DatabaseConfig createDatabaseConfig() {
        String driver = getConfig().getString("database.driver");
        String scheme = getConfig().getString("database.scheme", "jdbc:mariadb");
        String hostname = getConfig().getString("database.hostname", "localhost");
        int port = getConfig().getInt("database.port", 3306);
        String name = getConfig().getString("database.name", "azisabareport");
        String username = getConfig().getString("database.username", "azisabareport");
        String password = getConfig().getString("database.password", "azisabareport");
        Properties properties = new Properties();
        ConfigurationSection section = getConfig().getConfigurationSection("database.properties");
        if (section != null) {
            section.getValues(true).forEach((k, v) -> properties.setProperty(k, String.valueOf(v)));
        }
        return new DatabaseConfig(driver, scheme, hostname, port, name, username, password, properties);
    }

    private void setupChatListener() {
        Listener listener;
        // Find suitable chat listener
        if (ClassUtil.isClassPresent("net.azisaba.ryuzupluginchat.event.AsyncPrivateMessageEvent") &&
                ClassUtil.isClassPresent("net.azisaba.ryuzupluginchat.event.AsyncChannelMessageEvent") &&
                ClassUtil.isClassPresent("net.azisaba.ryuzupluginchat.event.AsyncGlobalMessageEvent")) {
            // RyuZUPluginChat
            listener = new RyuZUPluginChatListener(this);
        } else if (ClassUtil.isClassPresent("com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitChannelChatEvent") &&
                ClassUtil.isClassPresent("com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitGlobalChatEvent")) {
            // LunaChat
            listener = new LunaChatListener(this);
        } else {
            // Bukkit
            listener = new BukkitChatListener(this);
        }
        // Let other plugins change the listener if they want to
        SetChatListenerEvent event = new SetChatListenerEvent(listener);
        Bukkit.getPluginManager().callEvent(event);
        // Register the listener
        Bukkit.getPluginManager().registerEvents(event.getListener(), this);
    }

    public @NotNull ChatMessageHandler getChatMessageHandler() {
        return chatMessageHandler;
    }

    public @NotNull DatabaseManager getDatabaseManager() {
        return Objects.requireNonNull(databaseManager);
    }
}
