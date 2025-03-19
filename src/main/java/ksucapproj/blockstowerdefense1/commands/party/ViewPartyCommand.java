package ksucapproj.blockstowerdefense1.commands.party;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ViewPartyCommand {
    private static final PartiesAPI api = BlocksTowerDefense1.getApi();

    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("view-all")
                .requires(ctx -> ctx.getExecutor() instanceof Player)
                .executes(ViewPartyCommand::viewCommandLogic)
                .build();
    }


    //still requires implementation of command logic
    private static int viewCommandLogic (final CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getExecutor() instanceof Player sender)){
            return Command.SINGLE_SUCCESS;
        }

        if (api == null) {
            sender.sendMessage("Error: Parties API is not initialized.");
            return Command.SINGLE_SUCCESS;
        }

        Bukkit.getScheduler().runTask(BlocksTowerDefense1.getInstance(), () -> sender.performCommand("party list"));


        return Command.SINGLE_SUCCESS;
    }
}
