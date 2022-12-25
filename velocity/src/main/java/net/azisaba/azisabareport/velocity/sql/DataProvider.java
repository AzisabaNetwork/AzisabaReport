package net.azisaba.azisabareport.velocity.sql;

import net.azisaba.azisabareport.common.message.ChatMessage;
import net.azisaba.azisabareport.common.util.BitField;
import net.azisaba.azisabareport.common.data.PlayerData;
import net.azisaba.azisabareport.velocity.data.ReportData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DataProvider {
    public static @NotNull Optional<PlayerData> getPlayerDataByName(@NotNull DatabaseManager db, @NotNull String name) {
        try {
            return db.query("SELECT `id`, `name` FROM `players` WHERE LOWER(`name`) = LOWER(?) ORDER BY `name_last_updated` DESC LIMIT 1", ps -> {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new PlayerData(UUID.fromString(rs.getString("id")), rs.getString("name")));
                    } else {
                        return Optional.empty();
                    }
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
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

    public static @NotNull List<ReportData> getRecentReports(@NotNull DatabaseManager db) {
        try {
            return db.query("SELECT * FROM `reports` WHERE `updated_at` >= ?", ps -> {
                ps.setLong(1, System.currentTimeMillis() - 1000 * 60 * 60 * 24);
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

    public static @NotNull List<ReportData> getActiveReportsFor(@NotNull DatabaseManager db, @NotNull UUID uuid) {
        return getReportsFor(db, uuid)
                .stream()
                .filter(r -> r.updatedAt() + 1000 * 60 * 60 * 24 > System.currentTimeMillis()) // created in 24 hours
                .filter(r -> !r.flags().contains(ReportData.CLOSED)) // not closed
                .toList();
    }

    public static @NotNull List<ChatMessage> getRecentMessagesBy(@NotNull DatabaseManager db, @NotNull UUID uuid) {
        try {
            return db.query("SELECT * FROM `messages` WHERE `uuid` = ? AND `timestamp` > ? ORDER BY `timestamp` DESC LIMIT 200", ps -> {
                ps.setString(1, uuid.toString());
                // 30 minutes
                ps.setLong(2, System.currentTimeMillis() - 1000 * 60 * 30);
                try (ResultSet rs = ps.executeQuery()) {
                    return toChatMessages(rs);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull List<ChatMessage> getRecentMessagesRelatesTo(@NotNull DatabaseManager db, @NotNull UUID uuid) {
        try {
            PlayerData player = getPlayerDataById(db, uuid).orElseThrow();
            return db.query("SELECT * FROM `messages` WHERE (`uuid` = ? OR LOWER(`channel_name`) LIKE ? OR LOWER(`message`) LIKE ?) AND `timestamp` > ? ORDER BY `timestamp` DESC LIMIT 200", ps -> {
                ps.setString(1, uuid.toString());
                ps.setString(2, "%" + player.name().toLowerCase() + "%");
                ps.setString(3, "%" + player.name().toLowerCase() + "%");
                // 30 minutes
                ps.setLong(2, System.currentTimeMillis() - 1000 * 60 * 30);
                try (ResultSet rs = ps.executeQuery()) {
                    return toChatMessages(rs);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull List<ChatMessage> getRecentMessagesIn(@NotNull DatabaseManager db, @NotNull String server) {
        try {
            return db.query("SELECT * FROM `messages` WHERE `server` = ? AND `timestamp` > ? ORDER BY `timestamp` DESC LIMIT 200", ps -> {
                ps.setString(1, server);
                // 15 minutes
                ps.setLong(2, System.currentTimeMillis() - 1000 * 60 * 30);
                try (ResultSet rs = ps.executeQuery()) {
                    return toChatMessages(rs);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull List<ChatMessage> getRecentMessagesGlobal(@NotNull DatabaseManager db) {
        try {
            // ordinal of ChatMessage.Type#GLOBAL is 0
            return db.query("SELECT * FROM `messages` WHERE `type` = 0 AND `timestamp` > ? ORDER BY `timestamp` DESC LIMIT 200", ps -> {
                // 15 minutes
                ps.setLong(1, System.currentTimeMillis() - 1000 * 60 * 30);
                try (ResultSet rs = ps.executeQuery()) {
                    return toChatMessages(rs);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull List<ChatMessage> getRecentMessagesAll(@NotNull DatabaseManager db) {
        try {
            return db.query("SELECT * FROM `messages` WHERE `timestamp` > ? ORDER BY `timestamp` DESC LIMIT 200", ps -> {
                // 15 minutes
                ps.setLong(1, System.currentTimeMillis() - 1000 * 60 * 30);
                try (ResultSet rs = ps.executeQuery()) {
                    return toChatMessages(rs);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static @NotNull List<ChatMessage> toChatMessages(@NotNull ResultSet rs) throws SQLException {
        List<ChatMessage> list = new ArrayList<>();
        while (rs.next()) {
            list.add(toChatMessage(rs));
        }
        return list;
    }

    @Contract("_ -> new")
    private static @NotNull ChatMessage toChatMessage(@NotNull ResultSet rs) {
        try {
            ChatMessage.Type type = ChatMessage.Type.values()[rs.getByte("type")];
            UUID id = UUID.fromString(rs.getString("uuid"));
            String username = rs.getString("username");
            String displayName = rs.getString("display_name");
            String channelName = rs.getString("channel_name");
            String message = rs.getString("message");
            long timestamp = rs.getLong("timestamp");
            String server = rs.getString("server");
            return new ChatMessage(type, id, username, displayName, channelName, message, timestamp, server);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
