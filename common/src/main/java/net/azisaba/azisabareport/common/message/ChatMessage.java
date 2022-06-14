package net.azisaba.azisabareport.common.message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class ChatMessage {
    private final Type type;
    private final UUID uuid;
    private final String username;
    private final String displayName;
    private final String rawMessage;
    private final String ngMaskedMessage;

    public ChatMessage(@NotNull Type type, @NotNull UUID uuid, @NotNull String username, @Nullable String displayName, @NotNull String rawMessage, @NotNull String ngMaskedMessage) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(rawMessage, "rawMessage");
        Objects.requireNonNull(ngMaskedMessage, "ngMaskedMessage");
        this.type = type;
        this.uuid = uuid;
        this.username = username;
        this.displayName = displayName;
        this.rawMessage = rawMessage;
        this.ngMaskedMessage = ngMaskedMessage;
    }

    @NotNull
    public Type getType() {
        return type;
    }

    @NotNull
    public UUID getUniqueId() {
        return uuid;
    }

    @NotNull
    public String getUsername() {
        return username;
    }

    @Nullable
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    public String getRawMessage() {
        return rawMessage;
    }

    @NotNull
    public String getNgMaskedMessage() {
        return ngMaskedMessage;
    }

    public enum Type {
        GLOBAL,
        CHANNEL,
        PRIVATE,
    }
}
