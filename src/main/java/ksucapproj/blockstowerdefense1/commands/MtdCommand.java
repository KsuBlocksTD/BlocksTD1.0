package ksucapproj.blockstowerdefense1.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import ksucapproj.blockstowerdefense1.commands.party.PartyCommand;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MtdCommand {

    private static final List<String> subcommands = List.of("hub", "party");

    @NullMarked
    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("mtd")
                .requires(ctx -> ctx.getExecutor() instanceof Player)

                //register subcommands here
                .then(PartyCommand.register())
                .then(HubCommand.register())

                .then(Commands.argument("subcommand", StringArgumentType.word())
                        .suggests(MtdCommand::getMtdSuggestions)
                )


                .build();
    }

    private static CompletableFuture<Suggestions> getMtdSuggestions(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder){

        for (String word : subcommands){
            builder.suggest(word);
        }
        return builder.buildFuture();
    }
}
