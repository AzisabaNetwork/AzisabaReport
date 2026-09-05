package net.azisaba.azisabareport.velocity;

import net.azisaba.azisabareport.common.sql.DatabaseConfig;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class PluginConfig {
    public final Map<String, List<URL>> reportURL = new HashMap<>();
    public final URL reportBugURL;
    public final String reportMention;
    public final String reportBugMention;
    public final String uploaderUrl;
    public final String redisHost;
    public final int redisPort;
    public final String redisUsername;
    public final String redisPassword;
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
        ConfigurationNode config = YamlConfigurationLoader.builder().path(configPath).build().load();
        if (config.node("reportURL").isMap()) {
            config.node("reportURL").childrenMap().forEach((key, value) -> {
                List<URL> urls = new ArrayList<>();
                try {
                    for (String s : value.getList(String.class, Collections.emptyList())) {
                        urls.add(new URL(s));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                reportURL.put(key.toString(), urls);
            });
        } else {
            List<URL> urls = new ArrayList<>();
            try {
                for (String s : config.node("reportURL").getList(String.class, Collections.emptyList())) {
                    urls.add(new URL(s));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            reportURL.put("__default__", urls);
        }
        reportBugURL = new URL(Objects.requireNonNull(config.node("reportBugURL").getString(), "reportBugURL is not set"));
        reportMention = config.node("reportMention").getString("");
        reportBugMention = config.node("reportBugMention").getString("");
        uploaderUrl = config.node("uploader-url").getString("");
        this.redisHost = config.node("redis", "host").getString("localhost");
        this.redisPort = config.node("redis", "port").getInt(6379);
        this.redisUsername = config.node("redis", "username").getString();
        this.redisPassword = config.node("redis", "password").getString();
        databaseConfig = loadDatabaseConfig(config.node("database"));
    }

    @Contract("_ -> new")
    private @NotNull DatabaseConfig loadDatabaseConfig(@NotNull ConfigurationNode node) {
        String driver = node.node("driver").getString();
        String scheme = node.node("scheme").getString("jdbc:mariadb");
        String hostname = node.node("hostname").getString("localhost");
        int port = node.node("port").getInt(3306);
        String name = node.node("name").getString("azisabareport");
        String username = node.node("username").getString();
        String password = node.node("password").getString();
        Properties properties = new Properties();
        node.node("properties").childrenMap()
                .forEach((key, value) -> properties.setProperty(String.valueOf(key), value.getString()));
        return new DatabaseConfig(driver, scheme, hostname, port, name, username, password, properties);
    }

    public @NotNull List<URL> getReportURLs(@NotNull String serverName) {
        return Objects.requireNonNullElse(reportURL.getOrDefault(serverName, reportURL.get("__default__")), Collections.emptyList());
    }
}
