package net.azisaba.azisabareport.velocity.data;

import org.apache.http.entity.ContentType;
import org.jetbrains.annotations.NotNull;

public record FileData(@NotNull String name, @NotNull ContentType contentType, byte @NotNull [] data) {}
