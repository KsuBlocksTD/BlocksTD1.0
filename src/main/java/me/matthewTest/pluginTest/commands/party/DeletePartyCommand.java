package me.matthewTest.pluginTest.commands.party;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.matthewTest.pluginTest.PluginTest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class DeletePartyCommand {

    private static final PartiesAPI api = PluginTest.getApi();


    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("delete")
                .requires(ctx -> ctx.getExecutor() instanceof Player)
                .executes(DeletePartyCommand::deleteCommandLogic)
                .build();
    }


    private static int deleteCommandLogic (final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player sender)) {
            return Command.SINGLE_SUCCESS;
        }

        if (api == null) {
            sender.sendMessage("Error: Parties API is not initialized.");
            return Command.SINGLE_SUCCESS;
        }

        PartyPlayer partyPlayer = api.getPartyPlayer(sender.getUniqueId());
        @Nullable Party party = api.getParty(partyPlayer.getPartyId());

//        if (partyPlayer == null) {
//            sender.sendMessage("Error: Could not retrieve party player.");
//            return Command.SINGLE_SUCCESS;
//        }

        if (party == null) {
            sender.sendMessage("Error: Could not retrieve party.");
            return Command.SINGLE_SUCCESS;
        }

        if (partyPlayer.isInParty() && (party.getLeader() == partyPlayer.getPlayerUUID())) {
            party.delete();
            return Command.SINGLE_SUCCESS;
        }


        return Command.SINGLE_SUCCESS;
    }
}
