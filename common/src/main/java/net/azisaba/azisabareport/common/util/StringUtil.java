package net.azisaba.azisabareport.common.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {
    public static @NotNull List<String> split(@NotNull String s, int maxLength) {
        if (maxLength <= 0) {
            throw new IllegalArgumentException("maxLength must be positive");
        }
        List<String> result = new ArrayList<>();
        int i = 0;
        while (i < s.length()) {
            int min = Math.min(i + maxLength, s.length());
            int j = min;
            while (j > i && !Character.isWhitespace(s.charAt(j - 1))) {
                j--;
            }
            if (i == j) { // special case when no split point found
                j = min; // take whole substring
            }
            result.add(s.substring(i, j));
            i = j;
        }
        return result;
    }
}
