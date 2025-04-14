package ksucapproj.blockstowerdefense1.commands.mtd.party;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

// SEE PARTY COMMAND

// "/mtd party leave"
public class LeavePartyCommand {

    private static final PartiesAPI api = BlocksTowerDefense1.getApi();

    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("leave")
                .requires(ctx -> ctx.getExecutor() instanceof Player)
                .executes(LeavePartyCommand::leaveCommandLogic)
                .build();
    }


    // this is the execution logic for the registering of the party creation command
    private static int leaveCommandLogic (final CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getExecutor() instanceof Player sender)){
            return Command.SINGLE_SUCCESS;
        }

        if (api == null) { // this is done to ensure there is no null access of the PartiesAPI api
            sender.sendMessage("Error: Parties API is not initialized.");
            return Command.SINGLE_SUCCESS; // if so, return the command with no result to prevent errors
        }

        // this calls a command that is added by the PartiesAPI: "/party leave"
        // if the player is in a party, it will target their party and leave it without requiring party specification
        // reroutes the command from /mtd party leave -> /party leave
        Bukkit.getScheduler().runTask(BlocksTowerDefense1.getInstance(), () -> sender.performCommand("party leave"));


        return Command.SINGLE_SUCCESS;
    }
}
