package net.azisaba.azisabareport.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class StringReader {
    private final String text;
    private int index = 0;

    public StringReader(@NotNull String text) {
        this.text = text;
    }

    public char peek() {
        return text.charAt(index % text.length());
    }

    @NotNull
    public String peekString() {
        return Character.toString(peek());
    }

    @NotNull
    public String peekRemaining() {
        return text.substring(index);
    }

    public String read() {
        return read(1);
    }

    @NotNull
    public String read(int amount) {
        String string = text.substring(index, index + amount);
        index += amount;
        return string;
    }

    public boolean startsWith(@NotNull String prefix) {
        return peekRemaining().startsWith(prefix);
    }

    @Contract("_ -> this")
    @NotNull
    public StringReader skip(int amount) {
        index += amount;
        return this;
    }

    public boolean isEOF() {
        return index >= text.length();
    }
}
