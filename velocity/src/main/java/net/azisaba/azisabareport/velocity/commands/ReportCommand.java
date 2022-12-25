package net.azisaba.azisabareport.velocity.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.azisaba.azisabareport.common.data.PlayerPosData;
import net.azisaba.azisabareport.common.message.ChatMessage;
import net.azisaba.azisabareport.velocity.AzisabaReport;
import net.azisaba.azisabareport.velocity.data.FileData;
import net.azisaba.azisabareport.common.data.PlayerData;
import net.azisaba.azisabareport.velocity.data.ReportData;
import net.azisaba.azisabareport.velocity.message.Messages;
import net.azisaba.azisabareport.velocity.redis.RedisKeys;
import net.azisaba.azisabareport.velocity.sql.DataProvider;
import net.azisaba.azisabareport.velocity.util.RomajiTextReader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import redis.clients.jedis.Jedis;
import xyz.acrylicstyle.util.serialization.decoder.ByteBufValueDecoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// /report
public class ReportCommand extends AbstractCommand {
    private static final List<String> CHAT_REASON_KEYS = Arrays.asList("spam", "inappropriate-chat", "inappropriate-player-name");
    private static final List<String> IN_GAME_REASON_KEYS = Arrays.asList("cheating", "teaming", "griefing");
    private static final List<String> REASON_KEYS = concatList(CHAT_REASON_KEYS, IN_GAME_REASON_KEYS);

    private final AzisabaReport plugin;

    public ReportCommand(@NotNull AzisabaReport plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull LiteralArgumentBuilder<CommandSource> createBuilder() {
        return literal("report")
                .requires(sender -> sender.hasPermission("azisabareport.command.report"))
                .then(argument("player", StringArgumentType.word())
                        .executes(ctx -> executeNoReason(ctx.getSource(), StringArgumentType.getString(ctx, "player")))
                        .then(argument("reason", StringArgumentType.greedyString())
                                .suggests((context, builder) -> suggest(getReasons(context.getSource()), builder))
                                .executes(ctx ->
                                        execute(ctx.getSource(), StringArgumentType.getString(ctx, "player"), StringArgumentType.getString(ctx, "reason"))
                                )
                        )
                );
    }

    private static @NotNull Set<@NotNull String> getReasonsInAllLocales(@NotNull Stream<String> keyStream) {
        return keyStream
                .map(key -> "command.report.reason." + key)
                .flatMap(key -> Messages.getLocales().values().stream().map(mi -> mi.get(key)))
                .collect(Collectors.toSet());
    }

    private static @NotNull Set<@NotNull String> getFromAllLocales(@NotNull String reasonKey) {
        return Messages.getLocales().values().stream().map(mi -> mi.get(reasonKey)).collect(Collectors.toSet());
    }

    private static @Nullable String findReasonKey(@NotNull String reason) {
        return REASON_KEYS.stream()
                .filter(key -> Objects.equals(key, reason) || getFromAllLocales("command.report.reason." + key).stream().anyMatch(r -> r.equals(reason)))
                .findFirst()
                .orElse(null);
    }

    private static Stream<String> getReasons(@NotNull CommandSource source) {
        return REASON_KEYS.stream().map(key -> "command.report.reason." + key).map(key -> Messages.getRawMessage(source, key));
    }

    private int executeNoReason(@NotNull CommandSource source, @NotNull String player) {
        new Thread(() -> {
            PlayerData data = getPlayerDataNoSelf(source, player);
            if (data == null) return;
            List<ReportData> reports = DataProvider.getActiveReportsFor(plugin.getDatabaseManager(), data.uuid());
            Messages.sendFormatted(source, "command.report.no_reason.header", data.name());
            for (String key : REASON_KEYS) {
                String reason = Messages.getRawMessage(source, "command.report.reason." + key);
                boolean alreadyReported = checkDuplicateReports(reports, source, reason, false);
                Component component = Messages.getFormattedComponent(source, "command.report.no_reason.entry.circle", reason);
                if (alreadyReported) {
                    // active report exists
                    component = component.color(NamedTextColor.DARK_GRAY);
                    component = component.hoverEvent(HoverEvent.showText(Messages.getFormattedComponent(source, "command.report.already_reported")));
                } else {
                    // no active report
                    component = component.hoverEvent(HoverEvent.showText(Messages.getFormattedComponent(source, "command.report.click_to_report", reason)));
                    component = component.clickEvent(ClickEvent.runCommand("/report " + data.name() + " confirm:" + reason));
                }
                source.sendMessage(component);
            }
        }).start();
        return 0;
    }

    private int executeConfirmReport(@NotNull CommandSource source, @NotNull String player, @NotNull String reportReason) {
        new Thread(() -> {
            PlayerData data = getPlayerDataNoSelf(source, player);
            if (data == null) return;
            List<ReportData> reports = DataProvider.getActiveReportsFor(plugin.getDatabaseManager(), data.uuid());
            if (checkDuplicateReports(reports, source, reportReason, true)) return;
            source.sendMessage(Component.empty());
            source.sendMessage(Messages.getFormattedComponent(source, "command.report.no_reason.entry.filled_circle", reportReason).color(NamedTextColor.GREEN));
            Messages.sendFormatted(source, "command.report.confirm_report", data.name());
            Component component = Component.text("[");
            component = component.append(Messages.getFormattedComponent(source, "generic.send").color(NamedTextColor.GREEN));
            component = component.append(Component.text("]"));
            component = component.hoverEvent(HoverEvent.showText(Messages.getFormattedComponent(source, "command.report.click_to_report", reportReason)));
            component = component.clickEvent(ClickEvent.runCommand("/report " + data.name() + " " + reportReason));
            source.sendMessage(component);
            source.sendMessage(Component.empty());
        }).start();
        return 0;
    }

    private void handleChatReport(@NotNull PlayerData target, @NotNull JsonObject payload) {
        List<ChatMessage> messages =
                DataProvider.getRecentMessagesAll(plugin.getDatabaseManager())
                        .stream()
                        .sorted((a, b) -> (int) (b.getTimestamp() - a.getTimestamp()))
                        .toList();
        List<FileData> files = new ArrayList<>();

        // Function<messages, suffix>
        BiConsumer<List<ChatMessage>, String> fn = (list, suffix) -> {
            if (!list.isEmpty()) {
                String text = list.stream().map(ChatMessage::toString).collect(Collectors.joining("\n"));
                JsonArray array = new JsonArray(list.size());
                for (ChatMessage message : list) {
                    array.add(toJson(message));
                }
                files.add(new FileData("chat" + suffix + ".txt", ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8), text.getBytes(StandardCharsets.UTF_8)));
                files.add(new FileData("chat" + suffix + ".json", ContentType.APPLICATION_JSON, array.toString().getBytes(StandardCharsets.UTF_8)));
            }
        };

        // get player pos (server)
        PlayerPosData pos;
        try (Jedis jedis = plugin.getJedisBox().getJedisPool().getResource()) {
            pos = getPlayerPos(jedis, target.uuid());
        }

        // add files
        fn.accept(messages, "_all");
        fn.accept(messages.stream().filter(ChatMessage::isGlobal).toList(), "_global");
        fn.accept(messages.stream().filter(m -> m.relatesTo(target)).toList(), "_related");
        fn.accept(messages.stream().filter(m -> m.getUniqueId().equals(target.uuid())).toList(), "_self");
        if (pos != null) {
            fn.accept(messages.stream().filter(m -> Objects.equals(pos.server(), m.getServer())).toList(), "_server");
        }

        // execute webhook
        executeWebhook(plugin.getConfig().reportURL.toString(), payload, files.toArray(new FileData[0]));
    }

    private int execute(@NotNull CommandSource source, @NotNull String player, @NotNull String reason) {
        if (reason.startsWith("confirm:")) {
            return executeConfirmReport(source, player, reason.substring("confirm:".length()));
        }
        new Thread(() -> {
            PlayerData data = getPlayerDataNoSelf(source, player);
            if (data == null) return;

            // check for duplicate reports
            List<ReportData> reports = DataProvider.getActiveReportsFor(plugin.getDatabaseManager(), data.uuid());
            if (checkDuplicateReports(reports, source, reason, true)) return;

            // collect information
            String senderName;
            UUID senderUUID;
            String senderServerName;
            if (source instanceof Player) {
                senderName = ((Player) source).getUsername();
                senderUUID = ((Player) source).getUniqueId();
                senderServerName = ((Player) source).getCurrentServer().orElseThrow(IllegalStateException::new).getServerInfo().getName();
            } else {
                senderName = "CONSOLE";
                senderUUID = null;
                senderServerName = "null";
            }

            long reportId;
            try {
                reportId = plugin.getDatabaseManager().query("INSERT INTO `reports` (`reporter_id`, `reported_id`, `reason`) VALUES (?, ?, ?)", ps -> {
                    ps.setString(1, (senderUUID == null ? new UUID(0, 0) : senderUUID).toString());
                    ps.setString(2, data.uuid().toString());
                    ps.setString(3, reason);
                    ps.executeUpdate();
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            return generatedKeys.getLong(1);
                        } else {
                            throw new RuntimeException("Failed to insert report");
                        }
                    }
                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // prepare report json
            JsonObject o = new JsonObject();
            o.add("username", new JsonPrimitive(senderName));
            o.add("avatar_url", new JsonPrimitive("https://crafatar.com/avatars/" + senderUUID));
            o.add("content", new JsonPrimitive(plugin.getConfig().reportMention));
            JsonArray embeds = new JsonArray();
            JsonObject embed = new JsonObject();
            embed.add("title", new JsonPrimitive("レポート#ID"));
            embed.add("color", new JsonPrimitive(16711680));
            embed.add("description", new JsonPrimitive("#" + reportId));
            JsonObject author = new JsonObject();
            author.add("name", new JsonPrimitive(senderName));
            embed.add("author", author);
            JsonArray fields = new JsonArray();
            JsonObject field1 = new JsonObject();
            field1.add("name", new JsonPrimitive("鯖名"));
            field1.add("value", new JsonPrimitive("`" + senderServerName + "`"));
            JsonObject field2 = new JsonObject();
            field2.add("name", new JsonPrimitive("対象者"));
            field2.add("value", new JsonPrimitive("`" + data.name() + "` (`" + data.uuid() + "`)"));
            JsonObject field3 = new JsonObject();
            field3.add("name", new JsonPrimitive("理由/証拠"));
            field3.add("value", new JsonPrimitive(RomajiTextReader.convert(reason)));
            JsonObject field4 = new JsonObject();
            field4.add("name", new JsonPrimitive("処理前の内容"));
            field4.add("value", new JsonPrimitive(reason));
            JsonObject field5 = new JsonObject();
            field5.add("name", new JsonPrimitive("通報の種類"));
            field5.add("value", new JsonPrimitive(findReasonKey(reason) == null ? "?" : Objects.requireNonNull(findReasonKey(reason))));
            fields.add(field1);
            fields.add(field2);
            fields.add(field3);
            fields.add(field4);
            fields.add(field5);
            try (Jedis jedis = plugin.getJedisBox().getJedisPool().getResource()) {
                if (senderUUID != null) {
                    PlayerPosData reporterPos = getPlayerPos(jedis, senderUUID);
                    if (reporterPos != null) {
                        JsonObject reporterPosField = new JsonObject();
                        reporterPosField.add("name", new JsonPrimitive("通報者の座標"));
                        reporterPosField.add("value", new JsonPrimitive(reporterPos.toPosString()));
                        fields.add(reporterPosField);
                    }
                }
                PlayerPosData reportedPos = getPlayerPos(jedis, data.uuid());
                if (reportedPos != null) {
                    JsonObject reportedPosField = new JsonObject();
                    reportedPosField.add("name", new JsonPrimitive("対象者の座標"));
                    reportedPosField.add("value", new JsonPrimitive(reportedPos.toPosString()));
                    fields.add(reportedPosField);
                }
            }
            embed.add("fields", fields);
            embeds.add(embed);
            o.add("embeds", embeds);

            // handle report
            if (getReasonsInAllLocales(CHAT_REASON_KEYS.stream()).contains(reason) || CHAT_REASON_KEYS.contains(reason.toLowerCase(Locale.ROOT))) {
                // chat reports

                // handle in separate method
                handleChatReport(data, o);
            } else {
                // other reports

                // execute webhook
                executeWebhook(plugin.getConfig().reportURL.toString(), o);
            }

            // send feedback
            Messages.sendFormatted(source, "command.report.reported", data.name(), reason);
        }).start();
        return 0;
    }

    private boolean checkDuplicateReports(@NotNull List<ReportData> reports, @NotNull CommandSource source, @NotNull String reason, boolean sendMessage) {
        if (reports.stream().anyMatch(r -> r.createdAt() + 1000 * 60 * 5 > System.currentTimeMillis())) {
            // this player has been reported in the last 5 minutes (for any reason)
            if (sendMessage) {
                source.sendMessage(Messages.getFormattedComponent(source, "command.report.reported_recently").color(NamedTextColor.RED));
            }
            return true;
        }
        if (reports.stream().anyMatch(r -> reason.equals(r.reason()))) {
            if (sendMessage) {
                source.sendMessage(Messages.getFormattedComponent(source, "command.report.already_reported").color(NamedTextColor.RED));
            }
            return true;
        }
        String reasonKey = findReasonKey(reason);
        if (reasonKey != null) {
            boolean alreadyReportedChat = CHAT_REASON_KEYS.stream().anyMatch(key -> reports.stream().anyMatch(r -> key.equals(r.reason()) || key.equals(findReasonKey(r.reason()))));
            if ((alreadyReportedChat && CHAT_REASON_KEYS.contains(reasonKey)) ||
                    reports.stream().anyMatch(r -> reasonKey.equals(r.reason()) || reasonKey.equals(findReasonKey(r.reason())))
            ) {
                // same report exists
                if (sendMessage) {
                    source.sendMessage(Messages.getFormattedComponent(source, "command.report.already_reported").color(NamedTextColor.RED));
                }
                return true;
            }
        }
        return false;
    }

    private @Nullable PlayerData getPlayerDataNoSelf(@NotNull CommandSource source, @NotNull String playerName) {
        Optional<PlayerData> opt = DataProvider.getPlayerDataByName(plugin.getDatabaseManager(), playerName);
        if (opt.isEmpty()) {
            sendMessageMissingPlayer(source, playerName);
            return null;
        }
        PlayerData data = opt.get();
        if (source instanceof Player p && data.name().equalsIgnoreCase(p.getUsername())) {
            Messages.sendFormatted(source, "command.report.self");
            return null;
        }
        return data;
    }

    public static void executeWebhook(@NotNull String uri, @NotNull JsonObject json, @NotNull FileData @NotNull ... files) {
        AzisabaReport.getInstance().getServer().getScheduler().buildTask(AzisabaReport.getInstance(), () -> {
            HttpPost execute = new HttpPost(uri);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            for (int i = 0; i < files.length; i++) {
                builder.addPart("files[" + i + "]", new ByteArrayBody(files[i].data(), files[i].contentType(), files[i].name()));
            }
            builder.addTextBody("payload_json", json.toString(), ContentType.APPLICATION_JSON);
            HttpEntity multipart = builder.build();
            execute.setEntity(multipart);
            execute.addHeader("User-Agent", "AzisabaReport/2.x");
            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(execute)) {
                if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
                    AzisabaReport.getInstance().getLogger().error("Unexpected response code: " + response.getStatusLine().getStatusCode());
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                        String read;
                        while ((read = reader.readLine()) != null) {
                            sb.append(read);
                        }
                    }
                    AzisabaReport.getInstance().getLogger().error("Response body: " + sb);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).schedule();
    }

    @SafeVarargs
    private static <T> @UnmodifiableView @NotNull List<T> concatList(@NotNull List<T> @NotNull ... lists) {
        List<T> list = new ArrayList<>();
        for (List<T> l : lists) {
            list.addAll(l);
        }
        return Collections.unmodifiableList(list);
    }

    private static @NotNull JsonObject toJson(@NotNull ChatMessage message) {
        JsonObject o = new JsonObject();
        o.add("type", new JsonPrimitive(message.getType().name()));
        o.add("uuid", new JsonPrimitive(message.getUniqueId().toString()));
        o.add("username", new JsonPrimitive(message.getUsername()));
        if (message.getDisplayName() == null) {
            o.add("display_name", JsonNull.INSTANCE);
        } else {
            o.add("display_name", new JsonPrimitive(message.getDisplayName()));
        }
        if (message.getChannelName() == null) {
            o.add("channel_name", JsonNull.INSTANCE);
        } else {
            o.add("channel_name", new JsonPrimitive(message.getChannelName()));
        }
        o.add("message", new JsonPrimitive(message.getMessage()));
        o.add("timestamp", new JsonPrimitive(message.getTimestamp()));
        o.add("server", new JsonPrimitive(Objects.requireNonNull(message.getServer())));
        return o;
    }

    private static @Nullable PlayerPosData getPlayerPos(@NotNull Jedis jedis, @NotNull UUID uuid) {
        byte[] byteData = jedis.get(RedisKeys.playerPos(uuid));
        if (byteData == null || byteData.length == 0) {
            return null;
        }
        ByteBuf buf = Unpooled.wrappedBuffer(byteData);
        try {
            return PlayerPosData.CODEC.decode(new ByteBufValueDecoder(buf));
        } finally {
            buf.release();
        }
    }
}
