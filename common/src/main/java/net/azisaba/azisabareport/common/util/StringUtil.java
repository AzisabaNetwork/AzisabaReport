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
            int j = Math.min(i + maxLength, s.length());
            if (j == s.length()) {
                result.add(s.substring(i, j));
                break;
            }
            int k = s.lastIndexOf(" ", j - 1);
            if (k >= i) {
                j = k + 1;
            }
            result.add(s.substring(i, j));
            i = j;
        }
        return result;
    }
}
