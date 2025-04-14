package ksucapproj.blockstowerdefense1.commands.mtd;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.commands.mtd.party.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

// this function is under the /mtd hierarchy, and is therefore not its own standalone command **
// "/mtd party"

public class PartyCommand {
    private static final PartiesAPI api = BlocksTowerDefense1.getApi();
    private static final List<String> subcommands = List.of("invite", "create", "view-all", "leave", "kick", "delete");

    // this .register() function is registered under the base command "/mtd <subcommand>" as a subcommand
    // all subcommands of PartyCommand are granddaughter commands of MtdCommand
    // once PartyCommand is registered, all its subcommands are therefore registered as well
    // command hierarchy display exists in MtdCommand
    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("party")
                .requires(ctx -> ctx.getExecutor() instanceof Player)

                //register subcommands here
                // these are all commands that follow the structure: "/mtd party <subcommand>"
                // this action does not execute the commands here, but simply 'build' them under /mtd party so that
                // they're accessible and ready for use when needed
                .then(InvitePartyCommand.register())
                .then(CreatePartyCommand.register())
                .then(DeletePartyCommand.register())
                .then(ViewPartyCommand.register())
                .then(LeavePartyCommand.register())
                .then(KickPartyCommand.register())

                // "/mtd party help" command
                // this command is separate from the other subcommands because its command logic
                // is small enough to include inside the PartyCommand file
                .then(Commands.literal("help")
                        .executes(PartyCommand::helpCommandLogic)
                        .then(Commands.argument("page-number", IntegerArgumentType.integer())
                                .executes(PartyCommand::helpCommandLogic)
                        )
                )

                // /mtd party info <party-name> command
                // this command is separate from the other subcommands because its command logic
                // is small enough to include inside the PartyCommand file
                .then(Commands.literal("info")
                        .executes(PartyCommand::infoCommandLogic)
                        .then(Commands.argument("party-name", StringArgumentType.string())
                                .executes(PartyCommand::infoCommandLogic)
                        )
                )

                .then(Commands.argument("subcommand", StringArgumentType.word())
                        .suggests(PartyCommand::getPartySuggestions)
                )

                .build();
    }

    // gets the suggestions for party's subcommands based upon the strings in the list at the top of the class
    private static CompletableFuture<Suggestions> getPartySuggestions(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder){

        for (String word : subcommands){
            builder.suggest(word);
        }
        return builder.buildFuture();
    }

    // used in case one of the other commands needs use of player names
    private static CompletableFuture<Suggestions> getNameSuggestions(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder){

        Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .forEach(builder::suggest);

        return builder.buildFuture();
    }



    // help command logic
    private static int helpCommandLogic (final CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getExecutor() instanceof Player sender)){
            return Command.SINGLE_SUCCESS;
        }

        if (api == null) { // this is done to ensure there is no null access of the PartiesAPI api
            sender.sendMessage("Error: Parties API is not initialized.");
            return Command.SINGLE_SUCCESS; // if so, return the command with no result to prevent errors
        }

        //  reroutes the command from /mtd party help -> /party help
        Bukkit.getScheduler().runTask(BlocksTowerDefense1.getInstance(), () -> sender.performCommand("party help "));


        return Command.SINGLE_SUCCESS;
    }




    // info command logic
    private static int infoCommandLogic (final CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getExecutor() instanceof Player sender)){
            return Command.SINGLE_SUCCESS;
        }

        if (api == null) {
            sender.sendMessage("Error: Parties API is not initialized.");
            return Command.SINGLE_SUCCESS;
        }

        //  reroutes the command from /mtd party info -> /party info
        Bukkit.getScheduler().runTask(BlocksTowerDefense1.getInstance(), () -> sender.performCommand("party info "));

        return Command.SINGLE_SUCCESS;
    }


    // checkPartyLeaderStatus() serves as a function to quickly pass a Bukkit Player obj and determine if the player is:
    // not only in a party, but also the leader of the party they're in or not
    // if the player is the leader of the party, return true, else return false
    public static Boolean checkPartyLeaderStatus (Player player){

        Party party = api.getPartyOfPlayer(player.getUniqueId());

        if (party == null || party.getLeader() == null){
            return null;
        }

        return party.getLeader().equals(player.getUniqueId());
    }

    // isGameCoop() has no use-cases ATM, and was created before determining the player is to be in a party before
    // being placed into a game session-- this was left as an ICE for potential uses
    // if the party DNE or has only one player in it, return true, else return false
    public static boolean isGameCoop (Player player){

        Party party = api.getPartyOfPlayer(player.getUniqueId());

        return party != null && (party.getOnlineMembers().size() != 1);

    }
}