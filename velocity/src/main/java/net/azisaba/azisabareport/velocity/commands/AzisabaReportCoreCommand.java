package net.azisaba.azisabareport.velocity.commands;

import net.azisaba.azisabareport.velocity.ConfigManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

// /azisabareport
public class AzisabaReportCoreCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();
        if (args.length < 1) {
            sender.sendMessage(Component.text("The argument is missing!").color(NamedTextColor.RED));
            return;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "setreportmention":
                if (!sender.hasPermission("azisabareport.setmentions")) {
                    sender.sendMessage(Component.text("You don't have a permission!").color(NamedTextColor.RED));
                    return;
                }
                if (args.length < 2) {
                    sender.sendMessage(Component.text("The argument is missing! At least two arguments are required.").color(NamedTextColor.RED));
                    return;
                }
                ConfigManager.setReportMention(args[1]);
                sender.sendMessage(Component.text("Succeed to set report mention.").color(NamedTextColor.GREEN));
                break;

            case "setreportbugmention":
                if (!sender.hasPermission("azisabareport.setmentions")) {
                    sender.sendMessage(Component.text("You don't have a permission!").color(NamedTextColor.RED));
                    return;
                }
                if (args.length < 2) {
                    sender.sendMessage(Component.text("The argument is missing! At least two arguments are required.").color(NamedTextColor.RED));
                    return;
                }
                ConfigManager.setReportBugMention(args[1]);
                sender.sendMessage(Component.text("Succeed to set report bug mention.").color(NamedTextColor.GREEN));
                break;

            case "reload":
                if (!sender.hasPermission("azisabareport.reload")) {
                    sender.sendMessage(Component.text("You don't have a permission!").color(NamedTextColor.RED));
                    return;
                }
                ConfigManager.loadConfig();
                sender.sendMessage(Component.text("Succeed to reload config file.").color(NamedTextColor.GREEN));
                break;

            default:
                sender.sendMessage(Component.text("Unknown command!").color(NamedTextColor.RED));
                break;
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> commands = Arrays.asList("setreportmention", "setreportbugmention", "reload");
        if (args.length <= 1) return commands;
        if (args.length <= 2) {
            if (args[0].equalsIgnoreCase("setreportmention") || args[0].equalsIgnoreCase("setreportbugmention")) {
                return Collections.singletonList("MentionsList");
            }
        }
        return commands;
    }
}
