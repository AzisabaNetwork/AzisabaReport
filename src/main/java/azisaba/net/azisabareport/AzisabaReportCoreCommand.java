package azisaba.net.azisabareport;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AzisabaReportCoreCommand extends Command implements TabExecutor {

    public AzisabaReportCoreCommand(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "The argument is missing!"));
            return;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "setreportmention":
                if (!sender.hasPermission("AzisabaReport.setMentions")) {
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You don't have a permission!"));
                    return;
                }
                if (args.length < 2) {
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "The argument is missing! At least two arguments are required."));
                    return;
                }
                ConfigManager.setReportMention(args[1]);
                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "Succeed to set report mention."));
                break;

            case "setreportbugmention":
                if (!sender.hasPermission("AzisabaReport.setMentions")) {
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You don't have a permission!"));
                    return;
                }
                if (args.length < 2) {
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "The argument is missing! At least two arguments are required."));
                    return;
                }
                ConfigManager.setReportBugMention(args[1]);
                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "Succeed to set report bug mention."));
                break;

            case "reload":
                if (!sender.hasPermission("AzisabaReport.reload")) {
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You don't have a permission!"));
                    return;
                }
                ConfigManager.loadConfig();
                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "Succeed to reload config file."));
                break;

            default:
                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Unknown command!"));
                break;
        }
    }

    @Override
    public Iterable<String> onTabComplete(final CommandSender sender, final String[] args) {
        final List<String> commandList = new ArrayList<String>();
        if (args.length <= 1) {
            commandList.add("setReportMention");
            commandList.add("setReportBugMention");
            commandList.add("reload");
            return commandList;
        }

        if (args.length <= 2) {
            if (args[0].equalsIgnoreCase("setreportmention") || args[0].equalsIgnoreCase("setreportbugmention")) {
                return Collections.singletonList("MentionsList");
            }
        }

        return commandList;
    }
}
