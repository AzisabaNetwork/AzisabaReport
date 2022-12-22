package net.azisaba.azisabareport.common.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class ByteBufUtil {
    private ByteBufUtil() { throw new AssertionError(); }

    /**
     * Fully read the byte array from the buffer (from its readerIndex). Decreases the reference count by 1 and
     * deallocates the passed ByteBuf if the reference count reaches at 0.
     * @param buf buffer
     * @return byte array
     */
    public static byte @NotNull [] toByteArray(@NotNull ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        buf.release();
        return bytes;
    }

    public static byte @NotNull [] toByteArray(@NotNull Consumer<@NotNull ByteBuf> action) {
        ByteBuf buf = Unpooled.buffer();
        try {
            action.accept(buf);
            return toByteArray(buf);
        } finally {
            if (buf.refCnt() > 0) {
                buf.release();
            }
        }
    }
}
