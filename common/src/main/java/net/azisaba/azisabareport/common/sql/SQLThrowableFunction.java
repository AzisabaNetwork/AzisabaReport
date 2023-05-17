package net.azisaba.azisabareport.common.sql;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public interface SQLThrowableFunction<T, R> {
    @Contract(pure = true)
    R apply(@NotNull T t) throws SQLException;
}
