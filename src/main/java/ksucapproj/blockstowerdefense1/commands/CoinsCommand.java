package ksucapproj.blockstowerdefense1.commands;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
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
import ksucapproj.blockstowerdefense1.logic.game_logic.Economy;
import ksucapproj.blockstowerdefense1.logic.game_logic.PlayerUpgrades;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// this class contains several commands, check each command's build to see its usage

public class CoinsCommand {

    public static final PartiesAPI api = BlocksTowerDefense1.getApi();


    /*

    --This is an admin command--

        Usage: /addcoins <target> <coinamt>

    */

    // this command is not under the /mtd hierarchy, and is therefore its own standalone command **
    @NullMarked
    public static LiteralCommandNode<CommandSourceStack> addCoinsCommand() {
        return Commands.literal("addcoins")
                .requires(ctx -> ctx.getExecutor() instanceof Player)
                .requires(ctx -> ctx.getExecutor().hasPermission("blockstd.admin.game.addcoins"))

                .then(Commands.argument("target", StringArgumentType.word())
                        .suggests(CoinsCommand::getOnlinePlayersSuggestions)
                        .then(Commands.argument("coinamt", IntegerArgumentType.integer(1, 100000))
                                .executes(CoinsCommand::executeAddCoinsLogic)
                        )
                )
                .build();
    }



    // Suggests online players for the target argument
    private static CompletableFuture<Suggestions> getOnlinePlayersSuggestions(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder) {
        Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    // Command logic for /addcoins <target> <cointamt> (admin only command)
    private static int executeAddCoinsLogic(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player sender)) {
            return Command.SINGLE_SUCCESS;
        }

        if (PlayerUpgrades.getPlayerUpgradesMap().get(sender) == null){
            sender.sendRichMessage("<red>You must be in a game to use this command!");
            return Command.SINGLE_SUCCESS;
        }

        // Get the arguments
        final String targetName = StringArgumentType.getString(ctx, "target");
        final int coinAmount = IntegerArgumentType.getInteger(ctx, "coinamt");

        // Get the target player
        Player targetPlayer = Bukkit.getPlayer(targetName);

        // Check if the player exists
        if (targetPlayer == null) {
            sender.sendRichMessage("<red><player> is not currently online!",
                    Placeholder.component("player", Component.text(targetName))
            );
            return Command.SINGLE_SUCCESS;
        }



        Economy.addPlayerMoney(targetPlayer, coinAmount);  // Calls the function for adding player money in Economy

        // Send confirmation messages
        sender.sendRichMessage("<aqua><player> has received <gold><amount></gold> coins!",
                Placeholder.component("player", Component.text(targetName)),
                Placeholder.component("amount", Component.text(coinAmount))
        );

        targetPlayer.sendRichMessage("<aqua>You have received <gold><amount></gold> coins from <dark_aqua><player></dark_aqua>!",
                Placeholder.component("amount", Component.text(coinAmount)),
                Placeholder.component("player", Component.text(sender.getName()))
        );

        return Command.SINGLE_SUCCESS;
    }





    // this command is not under the /mtd hierarchy, and is therefore its own standalone command **
    // "/givecoins <coinamt>"
    @NullMarked
    public static LiteralCommandNode<CommandSourceStack> giveCoinsCommand() {
        return Commands.literal("givecoins")
                .requires(ctx -> ctx.getExecutor() instanceof Player)
                .requires(ctx -> ctx.getExecutor().hasPermission("blockstd.game.givecoins"))


                .then(Commands.argument("coinamt", IntegerArgumentType.integer())
                        .executes(CoinsCommand::executeGiveCoinsLogic)
                )

                .build();
    }


    // Command logic for /givecoins <cointamt> (in-game player command)
    private static int executeGiveCoinsLogic(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player sender)) {
            return Command.SINGLE_SUCCESS;
        }

        // Get the argument
        final int coinAmount = IntegerArgumentType.getInteger(ctx, "coinamt");
        final PartyPlayer senderPP = api.getPartyPlayer(sender.getUniqueId());

        // Get the target player
        if (senderPP.isInParty()) {

            Party party = api.getPartyOfPlayer(senderPP.getPlayerUUID());

            if (party.getOnlineMembers().size() == 1) {
                sender.sendRichMessage("<red>Invalid party size, no teammate acquired!");
                return Command.SINGLE_SUCCESS;
            }

            for (UUID teammate : party.getMembers()) {

                if (teammate != sender.getUniqueId()) {
                    Economy.shareMoneyWithTeammate(sender, Bukkit.getPlayer(teammate), coinAmount);
                }
            }
        }

        return Command.SINGLE_SUCCESS;
    }


}
