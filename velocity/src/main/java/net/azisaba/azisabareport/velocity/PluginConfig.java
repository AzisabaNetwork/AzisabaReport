package net.azisaba.azisabareport.velocity;

import net.azisaba.azisabareport.velocity.sql.DatabaseConfig;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public class PluginConfig {
    public final URL reportURL;
    public final URL reportBugURL;
    public final String reportMention;
    public final String reportBugMention;
    public final DatabaseConfig databaseConfig;

    public PluginConfig(@NotNull AzisabaReport plugin) throws IOException {
        Path configPath = plugin.getDataDirectory().resolve("config.yml");
        if (Files.notExists(configPath)) {
            try (InputStream configStream = AzisabaReport.class.getClassLoader().getResourceAsStream("/config.yml")) {
                if (configStream != null) {
                    Files.copy(configStream, configPath);
                }
            }
        }
        ConfigurationNode config = YAMLConfigurationLoader.builder().setPath(configPath).build().load();
        reportURL = new URL(Objects.requireNonNull(config.getNode("reportURL").getString(), "reportURL is not set"));
        reportBugURL = new URL(Objects.requireNonNull(config.getNode("reportBugURL").getString(), "reportBugURL is not set"));
        reportMention = config.getNode("reportMention").getString("");
        reportBugMention = config.getNode("reportBugMention").getString("");
        databaseConfig = loadDatabaseConfig(config.getNode("database"));
    }

    @Contract("_ -> new")
    private @NotNull DatabaseConfig loadDatabaseConfig(@NotNull ConfigurationNode node) {
        String driver = node.getNode("driver").getString();
        String scheme = node.getNode("scheme").getString("jdbc:mariadb");
        String hostname = node.getNode("hostname").getString("localhost");
        int port = node.getNode("port").getInt(3306);
        String name = node.getNode("name").getString("azisabareport");
        String username = node.getNode("username").getString();
        String password = node.getNode("password").getString();
        Properties properties = new Properties();
        node.getNode("properties").getChildrenMap()
                .forEach((key, value) -> properties.setProperty(String.valueOf(key), value.getString()));
        return new DatabaseConfig(driver, scheme, hostname, port, name, username, password, properties);
    }
}
