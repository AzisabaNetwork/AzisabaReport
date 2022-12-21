package net.azisaba.azisabareport.velocity.data;

import net.azisaba.azisabareport.common.util.BitField;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.UnaryOperator;

public record ReportData(
        long id,
        @NotNull UUID reporterId,
        @NotNull UUID reportedId,
        @NotNull String reason,
        @NotNull BitField flags,
        @Nullable String publicComment,
        @Nullable String comment,
        long createdAt,
        long updatedAt
) {
    public static final int OPEN = 1;
    public static final int CLOSED = 1 << 1;
    public static final int RESOLVED = 1 << 2; // reviewed and handled by the staff
    public static final int INVALID = 1 << 3; // invalid report
    public static final int NEED_MORE_PROOFS = 1 << 4; // needs more proofs to be reviewed

    @Contract("_ -> new")
    public @NotNull ReportData flags(@NotNull UnaryOperator<BitField> flags) {
        return new ReportData(id, reporterId, reportedId, reason, flags.apply(this.flags), publicComment, comment, createdAt, updatedAt);
    }

    @Contract("_ -> new")
    public @NotNull ReportData flags(@NotNull BitField flags) {
        return new ReportData(id, reporterId, reportedId, reason, flags, publicComment, comment, createdAt, updatedAt);
    }

    @Contract("_ -> new")
    public @NotNull ReportData publicComment(@NotNull String publicComment) {
        return new ReportData(id, reporterId, reportedId, reason, flags, publicComment, comment, createdAt, updatedAt);
    }

    @Contract("_ -> new")
    public @NotNull ReportData comment(@NotNull String comment) {
        return new ReportData(id, reporterId, reportedId, reason, flags, publicComment, comment, createdAt, updatedAt);
    }
}
