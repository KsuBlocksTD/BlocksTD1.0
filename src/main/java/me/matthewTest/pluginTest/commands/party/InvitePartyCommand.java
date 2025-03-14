package me.matthewTest.pluginTest.commands.party;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.PartyInvite;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.matthewTest.pluginTest.PluginTest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class InvitePartyCommand {

    private static final PartiesAPI api = PluginTest.getApi();

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
            PartyPlayer invitingPlayer = api.getPartyPlayer(sender.getUniqueId());
            PartyPlayer playerToInvite = api.getPartyPlayer(onlinePlayer.getUniqueId());

            if (playerToInvite.isInParty()){
                sender.sendMessage("This player is already in a party.");
                return Command.SINGLE_SUCCESS;
            }

            if (!(invitingPlayer.isInParty())){
                sender.sendMessage("You must be in a party before inviting another player.");
                return Command.SINGLE_SUCCESS;
            }


            // at the moment this functionality does not work
            //////////////////////////////////////////////NEEDS HELP /////////////////////////////////////////////////////////

            Bukkit.getScheduler().runTaskAsynchronously(PluginTest.getInstance(), () -> {
                System.out.println("Async task: Sending invite to " + playerToInvite.getName());

                PartyInvite invite = api.getParty(invitingPlayer.getPartyId()).invitePlayer(playerToInvite, invitingPlayer);

                Bukkit.getScheduler().runTask(PluginTest.getInstance(), () -> {
                    System.out.println("Main thread: Adding invite to pending list");
                    playerToInvite.getPendingInvites().add(invite);
                });
            });


//            sender.sendMessage("You invited " + onlinePlayer.getName() + " to your party.");
//            onlinePlayer.sendMessage(sender.getName() + " just sent you a party invite!");
        }

        return Command.SINGLE_SUCCESS;
    }
}
