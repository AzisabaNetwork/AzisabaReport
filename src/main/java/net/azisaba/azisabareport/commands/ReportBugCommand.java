package net.azisaba.azisabareport.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.azisaba.azisabareport.ConfigManager;
import net.azisaba.azisabareport.util.CoolTime;
import net.azisaba.azisabareport.util.RomajiTextReader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Collections;
import java.util.List;

// /reportbug
public class ReportBugCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();
        if (ConfigManager.getReportBugURL() == null) {
            sender.sendMessage(Component.text("エラーが発生しました。運営にこのエラー文のスクショと共に報告してください。[No valid ReportBugURL]", NamedTextColor.RED));
            return;
        }
        if (!(sender instanceof Player)) {
            // senderがプレイヤーじゃなかったら
            sender.sendMessage(Component.text("プレイヤー以外は実行できません。", NamedTextColor.RED));
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(Component.text("/reportbug <内容> と記入してください。", NamedTextColor.RED));
            return;
        }
        if (CoolTime.isCoolDown(((Player) sender).getUsername(), 1000*60*3)) {
            sender.sendMessage(Component.text("3分以内に連続で通報することはできません", NamedTextColor.RED));
            return;
        }
        CoolTime.startCoolDown(((Player) sender).getUsername());
        sender.sendMessage(Component.text("送信されました。", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("注意: 複雑なバグの場合はreportbugを使用せず、Discordのサポート受付へ送信してください。", NamedTextColor.GOLD));
        JsonObject o = new JsonObject();
        o.add("username", new JsonPrimitive(((Player) sender).getUsername()));
        o.add("avatar_url", new JsonPrimitive("https://crafatar.com/avatars/" + ((Player) sender).getUniqueId()));
        o.add("content", new JsonPrimitive(ConfigManager.getReportBugMention()));
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
        field1.add("name", new JsonPrimitive("内容"));
        field1.add("value", new JsonPrimitive(RomajiTextReader.convert(String.join(" ", args))));
        JsonObject field2 = new JsonObject();
        field2.add("name", new JsonPrimitive("処理前の内容"));
        field2.add("value", new JsonPrimitive(String.join(" ", args)));
        JsonObject field3 = new JsonObject();
        field3.add("name", new JsonPrimitive("UUID"));
        field3.add("value", new JsonPrimitive(((Player) sender).getUniqueId().toString()));
        fields.add(field1);
        fields.add(field2);
        fields.add(field3);
        embed.add("fields", fields);
        embeds.add(embed);
        o.add("embeds", embeds);
        ReportCommand.requestWebHook(o.toString(), ConfigManager.getReportBugURL());
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.source() instanceof Player) {
            return Collections.singletonList("内容");
        }
        return Collections.emptyList();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("azisabareport.reportbug");
    }
}
