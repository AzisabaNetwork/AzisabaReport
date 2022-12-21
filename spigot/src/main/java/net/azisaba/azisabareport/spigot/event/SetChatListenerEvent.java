package net.azisaba.azisabareport.spigot.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SetChatListenerEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Listener listener;

    public SetChatListenerEvent(@NotNull Listener listener) {
        this.listener = listener;
    }

    public @NotNull Listener getListener() {
        return listener;
    }

    public void setListener(@NotNull Listener listener) {
        this.listener = Objects.requireNonNull(listener, "listener");
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
