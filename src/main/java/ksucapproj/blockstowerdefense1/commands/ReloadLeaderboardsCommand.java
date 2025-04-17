package ksucapproj.blockstowerdefense1.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.LeaderboardManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
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
        if (!(ctx.getSource().getExecutor() instanceof Player player)){
            return Command.SINGLE_SUCCESS;
        }

        LeaderboardManager lb = BlocksTowerDefense1.getInstance().getLeaderboardManager();

        lb.updateAllLeaderboards();
        Bukkit.getScheduler().runTask(BlocksTowerDefense1.getInstance(), () -> player.performCommand("dh reload"));
//        player.sendMessage(lb.getLeaderboard(""));

        return Command.SINGLE_SUCCESS;
    }
}
