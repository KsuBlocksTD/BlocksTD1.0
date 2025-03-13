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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class CreatePartyCommand {

    private static PartiesAPI api;


    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("create")
                .requires(ctx -> ctx.getExecutor() instanceof Player)
                .executes(CreatePartyCommand::createCommandLogic)
                .build();
    }


    private static int createCommandLogic (final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player sender)) {
            return Command.SINGLE_SUCCESS;
        }

        if (api == null) {
            sender.sendMessage("Error: Parties API is not initialized.");
            return Command.SINGLE_SUCCESS;
        }

        PartyPlayer partyCreator = api.getPartyPlayer(sender.getUniqueId());

        if (partyCreator == null) {
            sender.sendMessage("Error: Could not retrieve party player.");
            return Command.SINGLE_SUCCESS;
        }

        if (partyCreator.isInParty()) {
            sender.sendMessage("You're already in a party, cannot create.");
            return Command.SINGLE_SUCCESS;
        }

        api.createParty(null, partyCreator);
        partyCreator.setPartyId(partyCreator.getPartyId());

        Party party = api.getParty(partyCreator.getPartyId());


        if (party == null) {
            sender.sendMessage("Error: Party creation failed.");
            return Command.SINGLE_SUCCESS;
        }

        // for debugging purposes
        Bukkit.broadcastMessage("New party created: " + party.getName());
        Bukkit.broadcastMessage("Player UUID: " + partyCreator.getPlayerUUID());
        Bukkit.broadcastMessage("Party UUID: " + party.getId());


        party.delete(); // only here temporarily.

        return Command.SINGLE_SUCCESS;
    }


    public static void setApi(PartiesAPI apiInstance) {
        api = apiInstance;
    }


}
