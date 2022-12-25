package net.azisaba.azisabareport.common.message;

import net.azisaba.azisabareport.common.data.PlayerData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.acrylicstyle.util.serialization.codec.Codec;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ChatMessage {
    public static final Codec<ChatMessage> CODEC =
            Codec.<ChatMessage>builder()
                    .group(
                            Type.CODEC.fieldOf("type").getter(ChatMessage::getType),
                            Codec.UUID.fieldOf("uuid").getter(ChatMessage::getUniqueId),
                            Codec.STRING.fieldOf("username").getter(ChatMessage::getUsername),
                            Codec.STRING.optionalFieldOf("display_name").getter(m -> Optional.ofNullable(m.getDisplayName())),
                            Codec.STRING.optionalFieldOf("channel_name").getter(m -> Optional.ofNullable(m.getChannelName())),
                            Codec.STRING.fieldOf("message").getter(ChatMessage::getMessage),
                            Codec.LONG.fieldOf("timestamp").getter(ChatMessage::getTimestamp),
                            Codec.STRING.fieldOf("server").getter(ChatMessage::getServer)
                    )
                    .build(ChatMessage::new)
                    .named("ChatMessage");
    public static final Codec<ChatMessage> NETWORK_CODEC =
            Codec.<ChatMessage>builder()
                    .group(
                            Type.CODEC.fieldOf("type").getter(ChatMessage::getType),
                            Codec.UUID.fieldOf("uuid").getter(ChatMessage::getUniqueId),
                            Codec.STRING.fieldOf("username").getter(ChatMessage::getUsername),
                            Codec.STRING.optionalFieldOf("display_name").getter(m -> Optional.ofNullable(m.getDisplayName())),
                            Codec.STRING.optionalFieldOf("channel_name").getter(m -> Optional.ofNullable(m.getChannelName())),
                            Codec.STRING.fieldOf("message").getter(ChatMessage::getMessage),
                            Codec.LONG.fieldOf("timestamp").getter(ChatMessage::getTimestamp)
                    )
                    .build(ChatMessage::new)
                    .named("ChatMessage[Network]");

    private final Type type;
    private final UUID uuid;
    private final String username;
    private final @Nullable String displayName;
    private final @Nullable String channelName;
    private final String message;
    private final long timestamp;
    private final @Nullable String server;

    public ChatMessage(
            @NotNull Type type,
            @NotNull UUID uuid,
            @NotNull String username,
            @Nullable String displayName,
            @Nullable String channelName,
            @NotNull String message,
            long timestamp,
            @Nullable String server
    ) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(message, "message");
        this.type = type;
        this.uuid = uuid;
        this.username = username;
        this.displayName = displayName;
        this.channelName = channelName;
        this.message = message;
        this.timestamp = timestamp;
        this.server = server;
    }

    public ChatMessage(
            @NotNull Type type,
            @NotNull UUID uuid,
            @NotNull String username,
            @Nullable String displayName,
            @Nullable String channelName,
            @NotNull String message,
            long timestamp
    ) {
        this(type, uuid, username, displayName, channelName, message, timestamp, null);
    }

    public @NotNull Type getType() {
        return type;
    }

    public @NotNull UUID getUniqueId() {
        return uuid;
    }

    public @NotNull String getUsername() {
        return username;
    }

    public @Nullable String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the channel name, if any. <code>null</code> indicates that the message was sent in the global chat.
     * Private chat messages will also have the channel name.
     * @return the channel name
     */
    public @Nullable String getChannelName() {
        return channelName;
    }

    public @NotNull String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public @Nullable String getServer() {
        return server;
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull ChatMessage setServer(@Nullable String server) {
        return new ChatMessage(type, uuid, username, displayName, channelName, message, timestamp, server);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(new Date(timestamp)).append("] ");
        sb.append("[@").append(server).append("] ");
        sb.append("[").append(type.name().charAt(0)).append(type.name().substring(1).toLowerCase()).append("] ");
        if (channelName != null) sb.append("[").append(channelName).append("] ");
        if (displayName == null) {
            sb.append("<").append(username).append(">").append(message);
        } else {
            sb.append("<<").append(username).append(">> ");
            sb.append("<").append(displayName.replace('&', '§')).append("> ").append(message);
        }
        return sb.toString();
    }

    public boolean isGlobal() {
        return channelName == null;
    }

    public boolean relatesTo(@NotNull PlayerData data) {
        return uuid.equals(data.uuid()) ||
                (channelName != null && channelName.toLowerCase(Locale.ROOT).contains(data.name().toLowerCase(Locale.ROOT))) ||
                message.toLowerCase(Locale.ROOT).contains(data.name().toLowerCase(Locale.ROOT));
    }

    public enum Type {
        GLOBAL,
        CHANNEL,
        PRIVATE,
        ;

        public static final Codec<Type> CODEC = Codec.INT.xmap(i -> Type.values()[i], Type::ordinal).named("ChatMessage$Type");
    }
}
