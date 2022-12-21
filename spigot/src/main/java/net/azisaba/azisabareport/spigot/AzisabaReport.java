package net.azisaba.azisabareport.spigot;

import net.azisaba.azisabareport.common.util.ClassUtil;
import net.azisaba.azisabareport.spigot.event.SetChatListenerEvent;
import net.azisaba.azisabareport.spigot.listener.BukkitChatListener;
import net.azisaba.azisabareport.spigot.listener.LunaChatListener;
import net.azisaba.azisabareport.spigot.listener.RyuZUPluginChatListener;
import net.azisaba.azisabareport.spigot.message.ChatMessageHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class AzisabaReport extends JavaPlugin {
    public static final String LEGACY_CHANNEL_CHAT = "AZIREPORT";
    public static final String CHANNEL_CHAT = "azisabareport:chat";
    public static boolean isLegacy = false;
    private final ChatMessageHandler chatMessageHandler = new ChatMessageHandler(this);

    @Override
    public void onEnable() {
        try {
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, CHANNEL_CHAT);
        } catch (RuntimeException e) {
            isLegacy = true;
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, LEGACY_CHANNEL_CHAT);
        }
        setupChatListener();
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
}
