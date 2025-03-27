package ksucapproj.blockstowerdefense1.commands.party;

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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PartyCommand {
    private static final PartiesAPI api = BlocksTowerDefense1.getApi();
    private static final List<String> subcommands = List.of("invite", "create", "view-all", "leave", "kick", "delete");

    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("party")
                .requires(ctx -> ctx.getExecutor() instanceof Player)

                //register subcommands here
                .then(InvitePartyCommand.register())
                .then(CreatePartyCommand.register())
                .then(DeletePartyCommand.register())
                .then(ViewPartyCommand.register())
                .then(LeavePartyCommand.register())
                .then(KickPartyCommand.register())

                // /mtd party help command
                .then(Commands.literal("help")
                        .executes(PartyCommand::helpCommandLogic)
                        .then(Commands.argument("page-number", IntegerArgumentType.integer())
                                .executes(PartyCommand::helpCommandLogic)
                        )
                )

                // /mtd party info <party-name> command
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

        if (api == null) {
            sender.sendMessage("Error: Parties API is not initialized.");
            return Command.SINGLE_SUCCESS;
        }

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

        Bukkit.getScheduler().runTask(BlocksTowerDefense1.getInstance(), () -> sender.performCommand("party info "));

        return Command.SINGLE_SUCCESS;
    }


    // if the player is the leader of the party, return true, else return false
    public static Boolean checkPartyLeaderStatus (Player player){

        Party party = api.getPartyOfPlayer(player.getUniqueId());

        if (party == null || party.getLeader() == null){
            return null;
        }

        return party.getLeader().equals(player.getUniqueId());
    }

    // if the party DNE or has only one player in it, return true, else return false
    public static boolean isGameCoop (Player player){

        Party party = api.getPartyOfPlayer(player.getUniqueId());

        return party != null && (party.getOnlineMembers().size() != 1);

    }
}