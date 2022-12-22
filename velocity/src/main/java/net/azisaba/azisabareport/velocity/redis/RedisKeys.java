package net.azisaba.azisabareport.velocity.redis;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class RedisKeys {
    private RedisKeys() {
    }

    @Contract(pure = true)
    public static byte @NotNull [] playerPos(@NotNull UUID uuid) {
        return ("azisaba_report:player_pos:" + uuid).getBytes(StandardCharsets.UTF_8);
    }
}
