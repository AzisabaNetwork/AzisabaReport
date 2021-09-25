package azisaba.net.azisabareport;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReportCommand extends Command implements TabExecutor {
    public ReportCommand() {
        super("report");
    }

    public void execute(final CommandSender sender, final String[] args) {
        if (ConfigManager.getReportURL() == null) {
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "エラーが発生しました。運営にこのエラー文のスクショと共に報告してください。[No valid ReportURL]"));
            return;
        }
        if (!(sender instanceof ProxiedPlayer)) {
            // senderがプレイヤーじゃなかったら
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "プレイヤー以外は実行できません。"));
            return;
        }
        if (args.length <= 1) {
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/report mcid <理由> と記入してください。"));
            return;
        }
        if (ProxyServer.getInstance().getPlayer(args[0]) == null) {
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "入力されたプレイヤーが存在しません。"));
            return;
        }
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "送信されました。"));
        JsonObject o = new JsonObject();
        o.add("username", new JsonPrimitive(sender.getName()));
        o.add("avatar_url", new JsonPrimitive("https://crafatar.com/avatars/" + ((ProxiedPlayer) sender).getUniqueId()));
        o.add("content", new JsonPrimitive(ConfigManager.getReportMention()));
        JsonArray embeds = new JsonArray();
        JsonObject embed = new JsonObject();
        embed.add("title", new JsonPrimitive("鯖名"));
        embed.add("color", new JsonPrimitive(16711680));
        embed.add("description", new JsonPrimitive(((ProxiedPlayer) sender).getServer().getInfo().getName()));
        JsonObject author = new JsonObject();
        author.add("name", new JsonPrimitive(sender.getName()));
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
        field3.add("value", new JsonPrimitive(ProxyServer.getInstance().getPlayer(args[0]).getUniqueId().toString()));
        fields.add(field1);
        fields.add(field2);
        fields.add(field3);
        embed.add("fields", fields);
        embeds.add(embed);
        o.add("embeds", embeds);
        requestWebHook(o.toString(), ConfigManager.getReportURL());
    }

    public static void requestWebHook(final String json, final URL url) {
        ProxyServer.getInstance().getScheduler().runAsync(AzisabaReport.getInstance(), () -> {
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
        });
    }

    public Iterable<String> onTabComplete(final CommandSender sender, final String[] args) {
        if (!(sender instanceof ProxiedPlayer)) return null;
        String lastArg = "";
        if (args.length != 0) {
            lastArg = args[args.length - 1].toLowerCase();
        }
        if (args.length <= 1) {
            final List<String> players = new ArrayList<>();
            for (final ProxiedPlayer player : ((ProxiedPlayer) sender).getServer().getInfo().getPlayers()) {
                if (player.getName().toLowerCase().startsWith(lastArg)) {
                    players.add(player.getName());
                }
            }
            return players;
        }
        return Collections.singletonList("理由");
    }
}