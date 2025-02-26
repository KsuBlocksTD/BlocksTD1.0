package me.matthewTest.pluginTest.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class invitePartyCommand {

    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("invite")
                .requires(ctx -> ctx.getExecutor() instanceof Player)

                .then(Commands.argument("username", StringArgumentType.word())
                        .suggests(invitePartyCommand::getNameSuggestions)
                        .executes(invitePartyCommand::inviteCommandLogic)
                )

                .build();
    }

    private static CompletableFuture<Suggestions> getNameSuggestions(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder){

        Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .forEach(builder::suggest);

        return builder.buildFuture();
    }

    //still requires implementation of command logic
    private static int inviteCommandLogic (final CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getExecutor() instanceof Player player)){
            return Command.SINGLE_SUCCESS;
        }

        final String targetUsername = StringArgumentType.getString(ctx, "username");

        //logic is not properly working.. not returning offline users if the prompted name is a partial match for one that is online
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equalsIgnoreCase(targetUsername)) {
                break;
            }
            else {
                player.sendRichMessage("<player> is not currently online.",
                        Placeholder.component("player", Component.text(targetUsername))
                );
                return Command.SINGLE_SUCCESS;
            }
        }
        //still requires implementation of command logic

        return Command.SINGLE_SUCCESS;
    }
}
