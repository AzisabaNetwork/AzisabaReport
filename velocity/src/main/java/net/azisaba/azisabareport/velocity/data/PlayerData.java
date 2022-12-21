package net.azisaba.azisabareport.velocity.data;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public record PlayerData(@NotNull UUID uuid, @NotNull String name) {
    public PlayerData {
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(name, "name");
    }
}
