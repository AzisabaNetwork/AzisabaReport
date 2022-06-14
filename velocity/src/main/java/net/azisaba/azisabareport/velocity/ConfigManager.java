package net.azisaba.azisabareport.velocity;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class ConfigManager {
    private static ConfigurationNode config;
    private static URL ReportURL = null;
    private static URL ReportBugURL = null;
    private static String ReportMention = "";
    private static String ReportBugMention = "";


    public static void loadConfig() {
        AzisabaReport plugin = AzisabaReport.getInstance();
        Path configPath = plugin.getDataDirectory().resolve("config.yml");
        YAMLConfigurationLoader.builder().setPath(configPath);
        if (!Files.isDirectory(plugin.getDataDirectory())) {
            try {
                Files.createDirectory(plugin.getDataDirectory());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!Files.exists(configPath)) {
            try {
                InputStream in = plugin.getClass().getResourceAsStream("/config.yml");
                if (in != null) {
                    Files.copy(in, configPath);
                } else {
                    plugin.getLogger().warn("Could not find config.yml in jar");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            config = YAMLConfigurationLoader.builder().setPath(configPath).build().load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            if (config.getNode("ReportURL").getString() != null) {
                ReportURL = new URL(Objects.requireNonNull(config.getNode("ReportURL").getString(), "ReportURL is null"));
            }
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
        }
        try {
            if (config.getNode("ReportBugURL").getString() != null) {
                ReportBugURL = new URL(Objects.requireNonNull(config.getNode("ReportBugURL").getString(), "ReportBugURL is null"));
            }
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
        }

        ReportMention = config.getNode("ReportMention").getString();
        ReportBugMention = config.getNode("ReportBugMention").getString();
    }

    public static URL getReportURL() {
        return ReportURL;
    }

    public static URL getReportBugURL() {
        return ReportBugURL;
    }

    public static String getReportMention() {
        return ReportMention;
    }

    public static String getReportBugMention() {
        return ReportBugMention;
    }

    public static void setReportMention(String st) {
        ReportMention = st;
    }

    public static void setReportBugMention(String st) {
        ReportBugMention = st;
    }
}
