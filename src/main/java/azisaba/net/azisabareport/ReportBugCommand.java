package azisaba.net.azisabareport;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Collections;

public class ReportBugCommand extends Command implements TabExecutor {
    public ReportBugCommand() {
        super("reportbug");
    }

    public void execute(final CommandSender sender, final String[] args) {
        if (ConfigManager.getReportBugURL() == null) {
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "エラーが発生しました。運営にこのエラー文のスクショと共に報告してください。[No valid ReportBugURL]"));
            return;
        }
        if (!(sender instanceof ProxiedPlayer)) {
            // senderがプレイヤーじゃなかったら
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "プレイヤー以外は実行できません。"));
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/reportbug <内容> と記入してください。"));
            return;
        }
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "送信されました。"));
        JsonObject o = new JsonObject();
        o.add("username", new JsonPrimitive(sender.getName()));
        o.add("avatar_url", new JsonPrimitive("https://crafatar.com/avatars/" + ((ProxiedPlayer) sender).getUniqueId()));
        o.add("content", new JsonPrimitive(ConfigManager.getReportBugMention()));
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
        field1.add("name", new JsonPrimitive("内容"));
        field1.add("value", new JsonPrimitive(String.join(" ", args)));
        JsonObject field2 = new JsonObject();
        field2.add("name", new JsonPrimitive("UUID"));
        field2.add("value", new JsonPrimitive(((ProxiedPlayer) sender).getUniqueId().toString()));
        fields.add(field1);
        fields.add(field2);
        embed.add("fields", fields);
        embeds.add(embed);
        o.add("embeds", embeds);
        ReportCommand.requestWebHook(o.toString(), ConfigManager.getReportBugURL());
    }

    public Iterable<String> onTabComplete(final CommandSender sender, final String[] args) {
        if (!(sender instanceof ProxiedPlayer)) return null;
        return Collections.singletonList("内容");
    }
}