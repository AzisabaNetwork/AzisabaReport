package net.azisaba.azisabareport.common.data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.acrylicstyle.util.serialization.codec.Codec;

import java.util.Objects;
import java.util.UUID;

public final class PlayerPosData {
    public static final Codec<PlayerPosData> CODEC =
            Codec.<PlayerPosData>builder()
                    .group(
                            Codec.UUID.fieldOf("uuid").getter(PlayerPosData::uuid),
                            Codec.STRING.fieldOf("world").getter(PlayerPosData::world),
                            Codec.DOUBLE.fieldOf("x").getter(PlayerPosData::x),
                            Codec.DOUBLE.fieldOf("y").getter(PlayerPosData::y),
                            Codec.DOUBLE.fieldOf("z").getter(PlayerPosData::z),
                            Codec.STRING.fieldOf("server").getter(PlayerPosData::server)
                    )
                    .build(PlayerPosData::new)
                    .named("PlayerPosData");
    public static final Codec<PlayerPosData> NETWORK_CODEC =
            Codec.<PlayerPosData>builder()
                    .group(
                            Codec.UUID.fieldOf("uuid").getter(PlayerPosData::uuid),
                            Codec.STRING.fieldOf("world").getter(PlayerPosData::world),
                            Codec.DOUBLE.fieldOf("x").getter(PlayerPosData::x),
                            Codec.DOUBLE.fieldOf("y").getter(PlayerPosData::y),
                            Codec.DOUBLE.fieldOf("z").getter(PlayerPosData::z)
                    )
                    .build(PlayerPosData::new)
                    .named("PlayerPosData[Network]");

    private final UUID uuid;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final @Nullable String server;

    public PlayerPosData(@NotNull UUID uuid, @NotNull String world, double x, double y, double z, @Nullable String server) {
        this.uuid = uuid;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.server = server;
    }

    public PlayerPosData(@NotNull UUID uuid, @NotNull String world, double x, double y, double z) {
        this(uuid, world, x, y, z, null);
    }

    public @NotNull UUID uuid() {
        return uuid;
    }

    public @NotNull String world() {
        return world;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public @Nullable String server() {
        return server;
    }

    public @NotNull PlayerPosData server(@Nullable String server) {
        return new PlayerPosData(uuid, world, x, y, z, server);
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerPosData)) return false;
        PlayerPosData that = (PlayerPosData) o;
        return Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0 && Double.compare(that.z, z) == 0 && Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, x, y, z);
    }

    @Override
    public String toString() {
        return "PlayerPosData{" +
                "uuid=" + uuid +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public @NotNull String toPosString() {
        return round(x()) + ", " + round(y()) + ", " + round(z()) + " @ `" + server() + "/" + world() + "`";
    }

    private static double round(double d) {
        return Math.round(d * 100.0) / 100.0;
    }
}
