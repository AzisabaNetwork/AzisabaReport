package net.azisaba.azisabareport.velocity;

import com.google.common.reflect.TypeToken;
import net.azisaba.azisabareport.common.sql.DatabaseConfig;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
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
        ConfigurationNode config = YAMLConfigurationLoader.builder().setPath(configPath).build().load();
        if (config.getNode("reportURL").isMap()) {
            config.getNode("reportURL").getChildrenMap().forEach((key, value) -> {
                List<URL> urls = new ArrayList<>();
                try {
                    for (String s : value.getList(TypeToken.of(String.class))) {
                        urls.add(new URL(s));
                    }
                } catch (IOException | ObjectMappingException e) {
                    throw new RuntimeException(e);
                }
                reportURL.put(key.toString(), urls);
            });
        } else {
            List<URL> urls = new ArrayList<>();
            try {
                for (String s : config.getNode("reportURL").getList(TypeToken.of(String.class))) {
                    urls.add(new URL(s));
                }
            } catch (IOException | ObjectMappingException e) {
                throw new RuntimeException(e);
            }
            reportURL.put("__default__", urls);
        }
        reportBugURL = new URL(Objects.requireNonNull(config.getNode("reportBugURL").getString(), "reportBugURL is not set"));
        reportMention = config.getNode("reportMention").getString("");
        reportBugMention = config.getNode("reportBugMention").getString("");
        uploaderUrl = config.getNode("uploader-url").getString("");
        this.redisHost = config.getNode("redis", "host").getString("localhost");
        this.redisPort = config.getNode("redis", "port").getInt(6379);
        this.redisUsername = config.getNode("redis", "username").getString();
        this.redisPassword = config.getNode("redis", "password").getString();
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

    public @NotNull List<URL> getReportURLs(@NotNull String serverName) {
        return Objects.requireNonNullElse(reportURL.getOrDefault(serverName, reportURL.get("__default__")), Collections.emptyList());
    }
}
