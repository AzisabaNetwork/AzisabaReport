package net.azisaba.azisabareport.velocity.sql;

import com.zaxxer.hikari.HikariDataSource;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public final class DatabaseManager {
    private static final AtomicInteger ID = new AtomicInteger(0);
    private final ExecutorService queryQueue = Executors.newFixedThreadPool(5, r -> new Thread(r, "AzisabaReport-Query-Thread-" + ID.getAndIncrement()));
    private final @NotNull Logger logger;
    private final @NotNull HikariDataSource dataSource;

    public DatabaseManager(@NotNull Logger logger, @NotNull HikariDataSource dataSource) throws SQLException {
        this.logger = logger;
        this.dataSource = dataSource;
        createTables();
    }

    public @NotNull Future<?> queue(@NotNull @Language("SQL") String query, @NotNull SQLThrowableConsumer<PreparedStatement> action) {
        return queryQueue.submit(() -> {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    action.accept(statement);
                }
            } catch (SQLException e) {
                logger.error("Error executing query", e);
            }
        });
    }

    @SuppressWarnings("SqlNoDataSourceInspection")
    private void createTables() throws SQLException {
        useStatement(statement -> {
            // uuid
            // username
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `players` (
                        `id` VARCHAR(36) NOT NULL,
                        `name` VARCHAR(32) NOT NULL,
                        `name_last_updated` TIMESTAMP NOT NULL DEFAULT NOW() ON UPDATE NOW(),
                        PRIMARY KEY (`id`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `reports` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT,
                        `reporter_id` VARCHAR(36) NOT NULL,
                        `reported_id` VARCHAR(36) NOT NULL,
                        `reason` VARCHAR(255) NOT NULL,
                        `flags` INT NOT NULL DEFAULT 1,
                        `public_comment` TEXT DEFAULT NULL,
                        `comment` TEXT DEFAULT NULL,
                        `created_at` TIMESTAMP NOT NULL DEFAULT NOW(),
                        `updated_at` TIMESTAMP NOT NULL DEFAULT NOW() ON UPDATE NOW(),
                        PRIMARY KEY (`id`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `messages` (
                        `type` TINYINT(1) NOT NULL,
                        `uuid` VARCHAR(36) NOT NULL,
                        `username` VARCHAR(32) NOT NULL,
                        `display_name` TEXT,
                        `channel_name` TEXT,
                        `message` TEXT NOT NULL,
                        `timestamp` BIGINT NOT NULL,
                        `server` TEXT NOT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
                    """);
        });
    }

    @NotNull
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Contract(pure = true)
    public <R> R use(@NotNull SQLThrowableFunction<Connection, R> action) throws SQLException {
        try (Connection connection = getConnection()) {
            return action.apply(connection);
        }
    }

    @Contract(pure = true)
    public void use(@NotNull SQLThrowableConsumer<Connection> action) throws SQLException {
        try (Connection connection = getConnection()) {
            action.accept(connection);
        }
    }

    public void queryVoid(@Language("SQL") @NotNull String sql, @NotNull SQLThrowableConsumer<PreparedStatement> action) throws SQLException {
        use(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                action.accept(statement);
            }
        });
    }

    @Contract
    public <R> R query(@Language("SQL") @NotNull String sql, @NotNull SQLThrowableFunction<PreparedStatement, R> action) throws SQLException {
        return use(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                return action.apply(statement);
            }
        });
    }

    @Contract(pure = true)
    public void useStatement(@NotNull SQLThrowableConsumer<Statement> action) throws SQLException {
        use(connection -> {
            try (Statement statement = connection.createStatement()) {
                action.accept(statement);
            }
        });
    }

    /**
     * Closes the data source.
     */
    public void close() {
        dataSource.close();
    }
}
