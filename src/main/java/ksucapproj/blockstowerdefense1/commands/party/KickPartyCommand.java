package ksucapproj.blockstowerdefense1.commands.party;

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

public class KickPartyCommand {

    private static final PartiesAPI api = BlocksTowerDefense1.getApi();

    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("kick")
                .requires(ctx -> ctx.getExecutor() instanceof Player)

                .then(Commands.argument("username", StringArgumentType.word())
                        .suggests(KickPartyCommand::getNameSuggestions)
                        .executes(KickPartyCommand::kickCommandLogic)
                )
                .build();
    }


    private static CompletableFuture<Suggestions> getNameSuggestions(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder){

        Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .forEach(builder::suggest);

        return builder.buildFuture();
    }

    private static int kickCommandLogic (final CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getExecutor() instanceof Player sender)){
            return Command.SINGLE_SUCCESS;
        }

        if (api == null) {
            sender.sendMessage("Error: Parties API is not initialized.");
            return Command.SINGLE_SUCCESS;
        }

        final String targetUsername = StringArgumentType.getString(ctx, "username");

        Bukkit.getScheduler().runTask(BlocksTowerDefense1.getInstance(), () -> sender.performCommand("party kick " + targetUsername));



        return Command.SINGLE_SUCCESS;
    }
}
