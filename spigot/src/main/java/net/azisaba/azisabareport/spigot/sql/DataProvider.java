package net.azisaba.azisabareport.spigot.sql;

import net.azisaba.azisabareport.common.data.PlayerData;
import net.azisaba.azisabareport.common.util.BitField;
import net.azisaba.azisabareport.spigot.data.ReportData;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class DataProvider {
    public static @NotNull List<ReportData> getReportsFor(@NotNull DatabaseManager db, @NotNull UUID uuid) {
        try {
            return db.query("SELECT * FROM `reports` WHERE `reported_id` = ?", ps -> {
                ps.setString(1, uuid.toString());
                return collectReportData(ps);
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull List<ReportData> getReportsBy(@NotNull DatabaseManager db, @NotNull UUID uuid) {
        try {
            return db.query("SELECT * FROM `reports` WHERE `reporter_id` = ?", ps -> {
                ps.setString(1, uuid.toString());
                return collectReportData(ps);
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static List<ReportData> collectReportData(@NotNull PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            List<ReportData> list = new ArrayList<>();
            while (rs.next()) {
                long id = rs.getLong("id");
                UUID reporterId = UUID.fromString(rs.getString("reporter_id"));
                UUID reportedId = UUID.fromString(rs.getString("reported_id"));
                String reason = rs.getString("reason");
                int flags = rs.getInt("flags");
                String publicComment = rs.getString("public_comment");
                String comment = rs.getString("comment");
                long createdAt = rs.getTimestamp("created_at").getTime();
                long updatedAt = rs.getTimestamp("updated_at").getTime();
                list.add(new ReportData(id, reporterId, reportedId, reason, BitField.of(flags), publicComment, comment, createdAt, updatedAt));
            }
            return list;
        }
    }

    public static @NotNull Optional<PlayerData> getPlayerDataById(@NotNull DatabaseManager db, @NotNull UUID uuid) {
        try {
            return db.query("SELECT `name` FROM `players` WHERE `id` = ?", ps -> {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new PlayerData(uuid, rs.getString("name")));
                    } else {
                        return Optional.empty();
                    }
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull List<PlayerData> getPlayerDataByIds(@NotNull DatabaseManager db, @NotNull List<UUID> list) {
        try {
            return db.query("SELECT `name`, `id` FROM `players` WHERE `id` IN (" + list.stream().map(s -> "?").collect(Collectors.joining(", ")) + ")", ps -> {
                for (int i = 0; i < list.size(); i++) {
                    ps.setString(i + 1, list.get(i).toString());
                }
                try (ResultSet rs = ps.executeQuery()) {
                    List<PlayerData> players = new ArrayList<>();
                    while (rs.next()) {
                        players.add(new PlayerData(UUID.fromString(rs.getString("id")), rs.getString("name")));
                    }
                    return players;
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
