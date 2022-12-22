package net.azisaba.azisabareport.common.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ListUtil {
    private ListUtil() {}

    public static <T> @NotNull List<List<T>> split(@NotNull List<T> list, int size) {
        List<List<T>> result = new ArrayList<>();
        if (list.size() <= size) {
            result.add(list);
            return result;
        }
        for (int i = 0; i < list.size(); i += size) {
            result.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return result;
    }
}
