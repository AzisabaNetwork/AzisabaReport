package net.azisaba.azisabareport.common.util;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

public final class ClassUtil {
    private ClassUtil() {}

    public static boolean isClassPresent(
            @NotNull
            @Language(value = "JAVA", prefix = "class X { void x() { java.lang.Class.forName(\"", suffix = "\"); } }")
            String className
    ) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
