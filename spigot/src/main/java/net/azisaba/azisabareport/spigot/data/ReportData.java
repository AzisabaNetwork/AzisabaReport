package net.azisaba.azisabareport.spigot.data;

import net.azisaba.azisabareport.common.util.BitField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public final class ReportData {
    public static final int OPEN = 1;
    public static final int CLOSED = 1 << 1;
    public static final int RESOLVED = 1 << 2; // reviewed and handled by the staff
    public static final int INVALID = 1 << 3; // invalid report
    public static final int NEED_MORE_PROOF = 1 << 4; // needs more proof to be reviewed
    private final long id;
    private final @NotNull UUID reporterId;
    private final @NotNull UUID reportedId;
    private final @NotNull String reason;
    private final @NotNull BitField flags;
    private final @Nullable String publicComment;
    private final @Nullable String comment;
    private final long createdAt;
    private final long updatedAt;

    public ReportData(
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
        this.id = id;
        this.reporterId = reporterId;
        this.reportedId = reportedId;
        this.reason = reason;
        this.flags = flags;
        this.publicComment = publicComment;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long id() {
        return id;
    }

    public @NotNull UUID reporterId() {
        return reporterId;
    }

    public @NotNull UUID reportedId() {
        return reportedId;
    }

    public @NotNull String reason() {
        return reason;
    }

    public @NotNull BitField flags() {
        return flags;
    }

    public @Nullable String publicComment() {
        return publicComment;
    }

    public @Nullable String comment() {
        return comment;
    }

    public long createdAt() {
        return createdAt;
    }

    public long updatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ReportData that = (ReportData) obj;
        return this.id == that.id &&
                Objects.equals(this.reporterId, that.reporterId) &&
                Objects.equals(this.reportedId, that.reportedId) &&
                Objects.equals(this.reason, that.reason) &&
                Objects.equals(this.flags, that.flags) &&
                Objects.equals(this.publicComment, that.publicComment) &&
                Objects.equals(this.comment, that.comment) &&
                this.createdAt == that.createdAt &&
                this.updatedAt == that.updatedAt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reporterId, reportedId, reason, flags, publicComment, comment, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "ReportData[" +
                "id=" + id + ", " +
                "reporterId=" + reporterId + ", " +
                "reportedId=" + reportedId + ", " +
                "reason=" + reason + ", " +
                "flags=" + flags + ", " +
                "publicComment=" + publicComment + ", " +
                "comment=" + comment + ", " +
                "createdAt=" + createdAt + ", " +
                "updatedAt=" + updatedAt + ']';
    }

}
