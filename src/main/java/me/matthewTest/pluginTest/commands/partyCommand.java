package me.matthewTest.pluginTest.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class partyCommand {
    private static final List<String> subcommands = List.of("invite", "create", "join", "leave", "kick");

    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("party")
                .requires(ctx -> ctx.getExecutor() instanceof Player)

                //register subcommands here
                .then(invitePartyCommand.register())



                .then(Commands.argument("subcommand", StringArgumentType.word())
                        .suggests(partyCommand::getPartySuggestions)
                )

                .build();
    }

    private static CompletableFuture<Suggestions> getPartySuggestions(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder){

        for (String word : subcommands){
            builder.suggest(word);
        }
        return builder.buildFuture();
    }
}
