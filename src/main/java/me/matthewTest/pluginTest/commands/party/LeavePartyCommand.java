package me.matthewTest.pluginTest.commands.party;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.matthewTest.pluginTest.PluginTest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LeavePartyCommand {

    private static final PartiesAPI api = PluginTest.getApi();

    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("leave")
                .requires(ctx -> ctx.getExecutor() instanceof Player)
                .executes(LeavePartyCommand::leaveCommandLogic)
                .build();
    }


    //still requires implementation of command logic
    private static int leaveCommandLogic (final CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getExecutor() instanceof Player sender)){
            return Command.SINGLE_SUCCESS;
        }

        if (api == null) {
            sender.sendMessage("Error: Parties API is not initialized.");
            return Command.SINGLE_SUCCESS;
        }

        Bukkit.getScheduler().runTask(PluginTest.getInstance(), () -> sender.performCommand("party leave"));


        return Command.SINGLE_SUCCESS;
    }
}
