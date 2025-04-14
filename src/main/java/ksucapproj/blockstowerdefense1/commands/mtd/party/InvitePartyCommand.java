package ksucapproj.blockstowerdefense1.commands.mtd.party;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.mojang.brigadier.Command;
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

import java.util.concurrent.CompletableFuture;

// SEE PARTY COMMAND

// "/mtd party invite <player_name>"
public class InvitePartyCommand {

    private static final PartiesAPI api = BlocksTowerDefense1.getApi();

    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("invite")
                .requires(ctx -> ctx.getExecutor() instanceof Player)

                .then(Commands.argument("username", StringArgumentType.word())
                        .suggests(InvitePartyCommand::getNameSuggestions)
                        .executes(InvitePartyCommand::inviteCommandLogic)
                )

                .build();
    }

    private static CompletableFuture<Suggestions> getNameSuggestions(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder){

        Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .forEach(builder::suggest);

        return builder.buildFuture();
    }

    // this is the execution logic for the registering of the party creation command
    private static int inviteCommandLogic (final CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getExecutor() instanceof Player sender)){
            return Command.SINGLE_SUCCESS;
        }

        if (api == null) { // this is done to ensure there is no null access of the PartiesAPI api
            sender.sendMessage("Error: Parties API is not initialized.");
            return Command.SINGLE_SUCCESS; // if so, return the command with no result to prevent errors
        }

        // takes in the argument as a name for a player to be invited
        final String targetUsername = StringArgumentType.getString(ctx, "username");

        // this calls a command that is added by the PartiesAPI: "/party invite <player_name>"
        // reroutes the command from /mtd party invite <player_name> -> /party invite <player_name>
        Bukkit.getScheduler().runTask(BlocksTowerDefense1.getInstance(), () -> sender.performCommand("party invite " + targetUsername));



        return Command.SINGLE_SUCCESS;
    }
}
