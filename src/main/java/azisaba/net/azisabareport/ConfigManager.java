package azisaba.net.azisabareport;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

public class ConfigManager{
    private static AzisabaReport plugin;
    private static Configuration config;
    private static URL ReportURL = null;
    private static URL ReportBugURL = null;
    private static String ReportMention = "";
    private static String ReportBugMention = "";


    public static void loadConfig(){
        plugin = AzisabaReport.getInstance();
        if(!plugin.getDataFolder().exists()){
            plugin.getDataFolder().mkdir();
        }

        File file = new File(plugin.getDataFolder(),"config.yml");
        if(!file.exists()){
            try{
                InputStream in = plugin.getResourceAsStream("config.yml");
                Files.copy(in, file.toPath());
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        try{
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "config.yml"));
        }catch(IOException e){
            e.printStackTrace();
        }

        try {
            ReportURL = new URL(config.getString("ReportURL"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            ReportBugURL = new URL(config.getString("ReportBugURL"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        ReportMention = config.getString("ReportMention");
        ReportBugMention = config.getString("ReportBugMention");
    }

    public static URL getReportURL() {
        return ReportURL;
    }

    public static URL getReportBugURL() {
        return ReportBugURL;
    }

    public static String getReportMention(){
        return ReportMention;
    }

    public static String getReportBugMention() {
        return ReportBugMention;
    }

    public static void setReportMention(String st){
        ReportMention = st;
    }
    public static void setReportBugMention(String st){
        ReportBugMention = st;
    }
}
