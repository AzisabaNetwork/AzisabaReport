package net.azisaba.azisabareport.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.azisaba.azisabareport.AzisabaReport;
import net.azisaba.azisabareport.ConfigManager;
import net.azisaba.azisabareport.util.CoolTime;
import net.azisaba.azisabareport.util.RomajiTextReader;
import net.azisaba.velocityredisbridge.VelocityRedisBridge;
import net.azisaba.velocityredisbridge.util.PlayerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// /report
public class ReportCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();
        if (ConfigManager.getReportURL() == null) {
            sender.sendMessage(Component.text("エラーが発生しました。運営にこのエラー文のスクショと共に報告してください。[No valid ReportURL]", NamedTextColor.RED));
            return;
        }
        if (!(sender instanceof Player)) {
            // senderがプレイヤーじゃなかったら
            sender.sendMessage(Component.text("プレイヤー以外は実行できません。", NamedTextColor.RED));
            return;
        }
        if (args.length <= 1) {
            sender.sendMessage(Component.text("/report mcid <証拠> <理由> と記入してください。", NamedTextColor.RED));
            sender.sendMessage(Component.text("証拠を含めることができない場合は公式Discord #サポート受付 で通報をしてください。"));
            return;
        }
        PlayerInfo player = null;
        for (PlayerInfo playerInfo : VelocityRedisBridge.getApi().getAllPlayerInfo()) {
            if (playerInfo.getUsername().equalsIgnoreCase(args[0])) {
                player = playerInfo;
                break;
            }
        }
        if (player == null) {
            sender.sendMessage(Component.text("入力されたプレイヤーが存在しません。", NamedTextColor.RED));
            return;
        }
        String message = String.join(" ", dropFirst(args));
        if (!message.contains("https://")) {
            sender.sendMessage(Component.text("内容にURLを含めてください。", NamedTextColor.RED));
            return;
        }
        if (CoolTime.isCoolDown(((Player) sender).getUsername(), 1000*60*3)) {
            sender.sendMessage(Component.text("3分以内に連続で通報することはできません", NamedTextColor.RED));
            return;
        }
        CoolTime.startCoolDown(((Player) sender).getUsername());
        sender.sendMessage(Component.text("送信されました。", NamedTextColor.GREEN));
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
        field1.add("value", new JsonPrimitive(player.getUsername()));
        JsonObject field2 = new JsonObject();
        field2.add("name", new JsonPrimitive("理由/証拠"));
        field2.add("value", new JsonPrimitive(RomajiTextReader.convert(message)));
        JsonObject field3 = new JsonObject();
        field3.add("name", new JsonPrimitive("処理前の内容"));
        field3.add("value", new JsonPrimitive(message));
        JsonObject field4 = new JsonObject();
        field4.add("name", new JsonPrimitive("UUID"));
        field4.add("value", new JsonPrimitive(player.getUuid().toString()));
        fields.add(field1);
        fields.add(field2);
        fields.add(field3);
        fields.add(field4);
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

    private static String[] dropFirst(String[] array) {
        if (array.length == 0) throw new IllegalArgumentException("length == 0");
        if (array.length == 1) return new String[0];
        List<String> list = new ArrayList<>(Arrays.asList(array));
        list.remove(0);
        return list.toArray(new String[0]);
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
        return Collections.singletonList("理由/証拠");
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("azisabareport.report");
    }
}
