package net.azisaba.azisabareport.common.util;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

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
}
