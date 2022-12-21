package net.azisaba.azisabareport.velocity.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import org.jetbrains.annotations.NotNull;

// /azisabareport
public class AzisabaReportCoreCommand extends AbstractCommand {
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSource> createBuilder() {
        return literal("azisabareport")
                .requires(source -> source.hasPermission("azisabareport.command.azisabareport"))
                .then(literal("reload")
                        .requires(source -> source.hasPermission("azisabareport.command.azisabareport.reload"))
                        .executes(ctx -> reloadConfig(ctx.getSource()))
                );
    }

    private static int reloadConfig(@NotNull CommandSource source) {
        throw new UnsupportedOperationException();
//        Messages.sendFormatted(source, "command.azisaba_report.reload.success");
//        return 1;
    }
}
