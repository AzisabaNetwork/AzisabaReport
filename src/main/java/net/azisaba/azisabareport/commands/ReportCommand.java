package net.azisaba.azisabareport.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.azisaba.azisabareport.AzisabaReport;
import net.azisaba.azisabareport.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// /report
public class ReportCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();
        if (ConfigManager.getReportURL() == null) {
            sender.sendMessage(Component.text("エラーが発生しました。運営にこのエラー文のスクショと共に報告してください。[No valid ReportURL]").color(NamedTextColor.RED));
            return;
        }
        if (!(sender instanceof Player)) {
            // senderがプレイヤーじゃなかったら
            sender.sendMessage(Component.text("プレイヤー以外は実行できません。").color(NamedTextColor.RED));
            return;
        }
        if (args.length <= 1) {
            sender.sendMessage(Component.text("/report mcid <理由> と記入してください。").color(NamedTextColor.RED));
            return;
        }
        Optional<Player> player = AzisabaReport.getInstance().getServer().getPlayer(args[0]);
        if (!player.isPresent()) {
            sender.sendMessage(Component.text("入力されたプレイヤーが存在しません。").color(NamedTextColor.RED));
            return;
        }
        sender.sendMessage(Component.text("送信されました。").color(NamedTextColor.RED));
        JsonObject o = new JsonObject();
        o.add("username", new JsonPrimitive(((Player) sender).getUsername()));
        o.add("avatar_url", new JsonPrimitive("https://crafatar.com/avatars/" + ((Player) sender).getUniqueId()));
        o.add("content", new JsonPrimitive(ConfigManager.getReportMention()));
        JsonArray embeds = new JsonArray();
        JsonObject embed = new JsonObject();
        embed.add("title", new JsonPrimitive("鯖名"));
        embed.add("color", new JsonPrimitive(16711680));
        embed.add("description", new JsonPrimitive(((Player) sender).getCurrentServer().orElseThrow(IllegalStateException::new).getServerInfo().getName()));
        JsonObject author = new JsonObject();
        author.add("name", new JsonPrimitive(((Player) sender).getUsername()));
        embed.add("author", author);
        JsonArray fields = new JsonArray();
        JsonObject field1 = new JsonObject();
        field1.add("name", new JsonPrimitive("対象者"));
        field1.add("value", new JsonPrimitive(args[0]));
        JsonObject field2 = new JsonObject();
        field2.add("name", new JsonPrimitive("内容"));
        field2.add("value", new JsonPrimitive(String.join(" ", args)));
        JsonObject field3 = new JsonObject();
        field3.add("name", new JsonPrimitive("UUID"));
        field3.add("value", new JsonPrimitive(player.get().getUniqueId().toString()));
        fields.add(field1);
        fields.add(field2);
        fields.add(field3);
        embed.add("fields", fields);
        embeds.add(embed);
        o.add("embeds", embeds);
        requestWebHook(o.toString(), ConfigManager.getReportURL());
    }

    public static void requestWebHook(final String json, final URL url) {
        AzisabaReport.getInstance().getServer().getScheduler().buildTask(AzisabaReport.getInstance(), () -> {
            try {
                final HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                con.addRequestProperty("Content-Type", "application/json; charset=utf-8");
                con.addRequestProperty("User-Agent", "AzisabaReport/1.1");
                con.setDoOutput(true);
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Length", String.valueOf(json.length()));
                final OutputStream stream = con.getOutputStream();
                stream.write(json.getBytes(StandardCharsets.UTF_8));
                stream.flush();
                stream.close();
                con.disconnect();
                con.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).schedule();
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();
        if (!(sender instanceof Player)) return null;
        String lastArg = "";
        if (args.length != 0) {
            lastArg = args[args.length - 1].toLowerCase();
        }
        if (args.length <= 1) {
            final List<String> players = new ArrayList<>();
            for (final Player player : ((Player) sender).getCurrentServer().orElseThrow(IllegalStateException::new).getServer().getPlayersConnected()) {
                if (player.getUsername().toLowerCase().startsWith(lastArg)) {
                    players.add(player.getUsername());
                }
            }
            return players;
        }
        return Collections.singletonList("理由");
    }
}