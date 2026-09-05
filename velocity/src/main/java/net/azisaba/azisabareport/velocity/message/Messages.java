package net.azisaba.azisabareport.velocity.message;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Messages {
    private static final Yaml YAML = new Yaml();
    private static final Map<String, MessageInstance> LOCALES = new ConcurrentHashMap<>();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static MessageInstance fallback;

    public static void load() throws IOException {
        fallback = Objects.requireNonNullElse(load(Locale.ENGLISH.getLanguage()), MessageInstance.FALLBACK);
        for (String language : Locale.getISOLanguages()) {
            var instance = Messages.load(language);
            if (instance != null) {
                LOCALES.put(language, instance);
            } else {
                LOCALES.put(language, fallback);
            }
        }
    }

    @Nullable
    public static MessageInstance load(@NotNull String language) throws IOException {
        try (InputStream in = Messages.class.getResourceAsStream("/messages_" + language + ".yml")) {
            if (in == null) {
                return null;
            }
            Map<Object, Object> map = YAML.load(in);
            Map<String, String> cache = new ConcurrentHashMap<>();
            return MessageInstance.createSimple(s -> cache.computeIfAbsent(s, key -> String.valueOf(map.get(key))));
        }
    }

    @NotNull
    public static MessageInstance getInstance(@Nullable Locale locale) {
        Objects.requireNonNull(fallback, "messages not loaded yet");
        if (locale == null) {
            return fallback;
        }
        return LOCALES.getOrDefault(locale.getLanguage(), fallback);
    }

    @NotNull
    public static Component format(@NotNull String s, Object... args) {
        TagResolver.Builder placeholders = TagResolver.builder();
        for (int i = 0; i < args.length; i++) {
            String name = "arg" + i;
            if (args[i] instanceof Component component) {
                placeholders.resolver(Placeholder.component(name, component));
            } else {
                placeholders.resolver(Placeholder.unparsed(name, String.valueOf(args[i])));
            }
        }
        return MINI_MESSAGE.deserialize(s, placeholders.build());
    }

    @NotNull
    public static Component getFormattedComponent(@NotNull CommandSource source, @NotNull String key, Object... args) {
        Locale locale = Locale.ENGLISH;
        if (source instanceof Player) {
            locale = ((Player) source).getEffectiveLocale();
        }
        return format(getInstance(locale).get(key), args);
    }

    public static @NotNull String getRawMessage(@NotNull CommandSource source, @NotNull String key) {
        Locale locale = Locale.ENGLISH;
        if (source instanceof Player) {
            locale = ((Player) source).getEffectiveLocale();
        }
        return getInstance(locale).get(key);
    }

    public static void sendFormatted(@NotNull CommandSource source, @NotNull String key, Object @NotNull ... args) {
        Component formatted = getFormattedComponent(source, key, args);
        source.sendMessage(formatted);
    }

    @Contract(pure = true)
    public static @NotNull @UnmodifiableView Map<String, MessageInstance> getLocales() {
        return Collections.unmodifiableMap(LOCALES);
    }
}
