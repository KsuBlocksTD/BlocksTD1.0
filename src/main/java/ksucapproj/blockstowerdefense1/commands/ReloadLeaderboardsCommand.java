package ksucapproj.blockstowerdefense1.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.LeaderboardManager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

public class ReloadLeaderboardsCommand {

    @NullMarked
    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("lb-reload")
//                .requires(ctx -> ctx.getExecutor() instanceof Player)
                .executes(ReloadLeaderboardsCommand::executeReloadLogic)

                .build();

    }


    // executes the logic for setting the spawn for HubCommand
    private static int executeReloadLogic(final CommandContext<CommandSourceStack> ctx){

        LeaderboardManager lb = BlocksTowerDefense1.getInstance().getLeaderboardManager();

        if (!(ctx.getSource().getExecutor() instanceof Player player)) {

            Server server = Bukkit.getServer();
            ConsoleCommandSender console = server.getConsoleSender();

            lb.updateAllLeaderboards();
            Bukkit.getScheduler().runTask(BlocksTowerDefense1.getInstance(), () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dh reload")
            );

            console.sendRichMessage("[BlocksTowerDefense] <green>Leaderboards reloaded and holograms updated.");
            return Command.SINGLE_SUCCESS;
        }

        lb.updateAllLeaderboards();
        Bukkit.getScheduler().runTask(BlocksTowerDefense1.getInstance(), () -> player.performCommand("dh reload"));
        player.sendRichMessage("<green>Leaderboards reloaded and holograms updated.");

        return Command.SINGLE_SUCCESS;
    }
}
