package azisaba.net.azisabareport;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.api.plugin.Command;

public class ReportCommand extends Command implements TabExecutor {
    public ReportCommand(final String name) {
        super(name);
    }

    public void execute(final CommandSender sender, final String[] args) {
        if (ConfigManager.getReportURL() == null) {
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "エラーが発生しました。運営にこのエラー文のスクショと共に報告してください。[No valid ReportURL]"));
            return;
        }
        if (!(sender instanceof ProxiedPlayer)) {
            //senderがプレイヤーじゃなかったら
            sender.sendMessage(net.md_5.bungee.api.chat.TextComponent.fromLegacyText(ChatColor.RED + "プレイヤー以外は実行できません。"));
            return;
        }
        if (args.length <= 1) {
            sender.sendMessage(net.md_5.bungee.api.chat.TextComponent.fromLegacyText(ChatColor.RED + "/report mcid <内容>" + " と記入してください。"));
            return;
        }
        if (ProxyServer.getInstance().getPlayer(args[0]) == null) {
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "入力されたプレイヤーが存在しません。"));
            return;
        }


        ProxyServer.getInstance().getScheduler().runAsync(AzisabaReport.getInstance(), () -> {
            try {
                String temp_message = "{\n  \"username\": \"%reporter%\",\n  \"avatar_url\": \"https://crafatar.com/avatars/%reporter_uuid%\",\n  \"content\": \"" + ConfigManager.getReportMention() + "\",\n  \"embeds\": [\n    {\n      \"title\": \"\u9bd6\u540d\",\n      \"color\": 16711680,\n      \"description\": \"%servername%\",\n      \"timestamp\": \"\",\n      \"url\": \"\",\n      \"author\": {\n        \"name\": \"%reporter%\"\n      },\n      \"image\": {},\n      \"thumbnail\": {},\n      \"footer\": {},\n      \"fields\": [\n        {\n          \"name\": \"\u5bfe\u8c61\u8005\",\n          \"value\": \"%player%\",\n          \"inline\": false\n        },\n        {\n          \"name\": \"\u5185\u5bb9\",\n          \"value\": \"%content%\"\n        },\n        {\n          \"name\": \"UUID\",\n          \"value\": \"%reporter_uuid%\"\n        }\n      ]\n    }\n  ]\n}";
                String message = temp_message.replace("%servername%", ((ProxiedPlayer) sender).getServer().getInfo().getName()).replace("%player%", args[0]).replace("%player%", args[0]).replace("%uuid%", String.valueOf(ProxyServer.getInstance().getPlayer(args[0]).getUniqueId())).replace("%reporter%", sender.getName()).replace("%reporter_uuid%", String.valueOf(((ProxiedPlayer) sender).getUniqueId())).replace("%content%", String.join(" ", (CharSequence[]) args).replace("\"", "\\\""));
                requestWebHook(message, ConfigManager.getReportURL());
                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "送信されました。"));
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        });
    }

    private static void requestWebHook(final String json, final URL url) throws IOException {
        final HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.addRequestProperty("Content-Type", "application/JSON; charset=utf-8");
        con.addRequestProperty("User-Agent", "DiscordWebHook");
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Length", String.valueOf(json.length()));
        final OutputStream stream = con.getOutputStream();
        stream.write(json.getBytes(StandardCharsets.UTF_8));
        stream.flush();
        stream.close();
        final int status = con.getResponseCode();
        if (status == 200 || status != 204) {
        }
        con.disconnect();
    }

    public Iterable<String> onTabComplete(final CommandSender sender, final String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(net.md_5.bungee.api.chat.TextComponent.fromLegacyText(ChatColor.RED + "プレイヤー以外は実行できません。"));
            return null;
        }
        String lastArg = "";
        if (args.length != 0) {
            lastArg = args[args.length - 1].toLowerCase();
        }
        if (args.length <= 1) {
            final List<String> players = new ArrayList<String>();
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