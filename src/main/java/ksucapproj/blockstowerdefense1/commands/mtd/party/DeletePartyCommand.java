package ksucapproj.blockstowerdefense1.commands.mtd.party;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

// SEE PARTY COMMAND
// THIS COMMAND IS CURRENTLY UNREGISTERED IN onEnable() >> IS NOT IN USE ATM
// "/mtd party delete"
public class DeletePartyCommand {

    private static final PartiesAPI api = BlocksTowerDefense1.getApi();


    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("delete")

                .requires(ctx -> ctx.getExecutor() instanceof Player)
                .requires(ctx -> ctx.getExecutor().hasPermission("parties.admin.delete"))

                .then(Commands.argument("party-name", StringArgumentType.word())
                        .executes(DeletePartyCommand::deleteCommandLogic)
                )
                .build();
    }

    // this is the execution logic for the registering of the party deletion command
    private static int deleteCommandLogic (final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player sender)) {
            return Command.SINGLE_SUCCESS;
        }

        final String partyName = StringArgumentType.getString(ctx, "party-name");
        if (api == null) { // this is done to ensure there is no null access of the PartiesAPI api
            sender.sendMessage("Error: Parties API is not initialized.");
            return Command.SINGLE_SUCCESS; // if so, return the command with no result to prevent errors
        }

        // this calls a command that is added by the PartiesAPI: "/party delete"
        // this automatically deletes your party if you are the leader
        //  reroutes the command from /mtd party delete -> /party delete
        Bukkit.getScheduler().runTask(BlocksTowerDefense1.getInstance(), () -> sender.performCommand("party delete " + partyName));


        return Command.SINGLE_SUCCESS;
    }
}
