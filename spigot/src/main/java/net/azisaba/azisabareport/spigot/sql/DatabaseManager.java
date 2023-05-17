package net.azisaba.azisabareport.spigot.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.azisaba.azisabareport.common.sql.DatabaseConfig;
import net.azisaba.azisabareport.common.sql.SQLThrowableConsumer;
import net.azisaba.azisabareport.common.sql.SQLThrowableFunction;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.mariadb.jdbc.Driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DatabaseManager {
    private static final AtomicInteger ID = new AtomicInteger(0);
    private final ExecutorService queryQueue = Executors.newFixedThreadPool(5, r -> new Thread(r, "AzisabaReport-Query-Thread-" + ID.getAndIncrement()));
    private final @NotNull Logger logger;
    private final @NotNull HikariDataSource dataSource;

    @Contract("_ -> new")
    public static @NotNull HikariDataSource createDataSource(@NotNull DatabaseConfig databaseConfig) {
        new Driver();
        HikariConfig config = new HikariConfig();
        if (databaseConfig.driver() != null) {
            config.setDriverClassName(databaseConfig.driver());
        }
        config.setJdbcUrl(databaseConfig.toUrl());
        config.setUsername(databaseConfig.username());
        config.setPassword(databaseConfig.password());
        config.setDataSourceProperties(databaseConfig.properties());
        return new HikariDataSource(config);
    }

    public DatabaseManager(@NotNull Logger logger, @NotNull HikariDataSource dataSource) throws SQLException {
        this.logger = logger;
        this.dataSource = dataSource;
    }

    public @NotNull Future<?> queue(@NotNull @Language("SQL") String query, @NotNull SQLThrowableConsumer<PreparedStatement> action) {
        return queryQueue.submit(() -> {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    action.accept(statement);
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error executing query", e);
            }
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
