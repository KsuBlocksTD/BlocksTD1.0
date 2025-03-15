package ksucapproj.blockstowerdefence1.commands;

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

public class InvitePartyCommand {

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

    //still requires implementation of command logic
    private static int inviteCommandLogic (final CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getExecutor() instanceof Player sender)){
            return Command.SINGLE_SUCCESS;
        }

        final String targetUsername = StringArgumentType.getString(ctx, "username");
        Player onlinePlayer = Bukkit.getPlayer(targetUsername);


        //still requires implementation of command logic

        //player does not exist
        if (onlinePlayer == null) {
            sender.sendRichMessage("<player> is not currently online.",
                    Placeholder.component("player", Component.text(targetUsername))
            );
        }

        //if the sender and player are the same person
        else if (sender.getName().equalsIgnoreCase(targetUsername)){
            sender.sendMessage("You cannot invite yourself to a party.");
        }

        //if the two checks pass, the invite will go through
        else {
            sender.sendMessage("You invited " + onlinePlayer.getName() + " to your party.");
            onlinePlayer.sendMessage(sender.getName() + " just sent you a party invite!");
        }

        return Command.SINGLE_SUCCESS;
    }
}
