package net.azisaba.azisabareport.common.data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public final class PlayerData {
    private final UUID uuid;
    private final String name;

    public PlayerData(@NotNull UUID uuid, @NotNull String name) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.name = Objects.requireNonNull(name, "name");
    }

    @Contract(pure = true)
    public @NotNull UUID uuid() {
        return uuid;
    }

    @Contract(pure = true)
    public @NotNull String name() {
        return name;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerData)) return false;
        PlayerData that = (PlayerData) o;
        return uuid.equals(that.uuid) && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name);
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "PlayerData{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                '}';
    }
}
