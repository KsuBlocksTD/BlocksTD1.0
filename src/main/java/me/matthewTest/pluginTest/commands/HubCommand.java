package me.matthewTest.pluginTest.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.matthewTest.pluginTest.logic.TeleportationLogic;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HubCommand {
    private static TeleportationLogic tpManager;
    private static JavaPlugin plugin;

    public static LiteralCommandNode<CommandSourceStack> register() {
        if (tpManager == null) {
            tpManager = new TeleportationLogic(plugin);
        }
        return Commands.literal("hub")
                .requires(ctx -> ctx.getExecutor() instanceof Player)
                .executes(HubCommand::executeCommandLogic)
                .build();
    }


    private static int executeCommandLogic(final CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getExecutor() instanceof Player player)){
            return Command.SINGLE_SUCCESS;
        }
        //sends player confirmation msg
        player.sendMessage("Teleporting to the hub...");

        //creates target location for the player's request
        Location targetLocation = new Location(player.getWorld(), player.getX(), player.getY()+2, player.getZ());

        //passes the target location to the tpManager that employs the function with the teleport logic
        tpManager.teleportWithRetry(player, targetLocation, 3 );

        return Command.SINGLE_SUCCESS;
    }
}