package ksucapproj.blockstowerdefense1.commands;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.logic.game_logic.Economy;
import ksucapproj.blockstowerdefense1.logic.game_logic.StartGame;
import ksucapproj.blockstowerdefense1.maps.MapData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// THIS IS MERELY A TEST CLASS, ALL COMMANDS IN THIS CLASS ARE UNUSED AND JUST FOR REFERENCE FROM PAPERMC DOCS
public class TestCommand {


    public static final PartiesAPI api = BlocksTowerDefense1.getApi();

    @NullMarked
    public static LiteralCommandNode<CommandSourceStack> flightCommand() {
        return Commands.literal("allowflight")
                .requires(ctx -> ctx.getExecutor() instanceof Player)

                .then(Commands.argument("allow", BoolArgumentType.bool())
                        .suggests(TestCommand::getFlightSuggestions)
                        .executes(TestCommand::executeCommandLogic)
                )
                .build();
    }

    private static CompletableFuture<Suggestions> getFlightSuggestions(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder){
        builder.suggest("true", MessageComponentSerializer.message().serialize(
                MiniMessage.miniMessage().deserialize("<green>Enable flight for the player")
        ));
        builder.suggest("false", MessageComponentSerializer.message().serialize(
                MiniMessage.miniMessage().deserialize("<green>Disable flight for the player")
        ));
        return builder.buildFuture();
    }

    private static int executeCommandLogic(final CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getExecutor() instanceof Player player)){
            return Command.SINGLE_SUCCESS;
        }
        final boolean enableflight = BoolArgumentType.getBool(ctx, "allow");
        if (enableflight){
            player.setAllowFlight(true);
            player.sendRichMessage("<player>'s flight is now enabled!",
                    Placeholder.component("player", Component.text(player.getName()))
            );
        }
        else {
            player.setAllowFlight(false);
            player.sendRichMessage("<player>'s flight is now disabled!",
                    Placeholder.component("player", Component.text(player.getName()))
            );
        }
        return Command.SINGLE_SUCCESS;
    }


    public static LiteralCommandNode<CommandSourceStack> constructGiveItemCommand() {
        // Create new command: /giveitem
        return Commands.literal("giveitem")

                // Require a player to execute the command
                .requires(ctx -> ctx.getExecutor() instanceof Player)

                // Declare a new ItemStack argument
                .then(Commands.argument("item", ArgumentTypes.itemStack())

                        // Declare a new integer argument with the bounds of 1 to 99
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 99))

                                // Here, we use method references, since otherwise, our command definition would grow too big
                                .suggests(TestCommand::getAmountSuggestions)
                                .executes(TestCommand::executeCommandLogic1)

                        )
                )
                .build();
    }

    private static CompletableFuture<Suggestions> getAmountSuggestions(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder) {
        // Suggest 1, 16, 32, and 64 to the user when they reach the 'amount' argument
        builder.suggest(1);
        builder.suggest(16);
        builder.suggest(32);
        builder.suggest(64);
        return builder.buildFuture();
    }

    private static int executeCommandLogic1(final CommandContext<CommandSourceStack> ctx) {
        // We know that the executor will be a player, so we can just silently return
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return Command.SINGLE_SUCCESS;
        }

        // If the player has no empty slot, we tell the player that they have no free inventory space
        final int firstEmptySlot = player.getInventory().firstEmpty();
        if (firstEmptySlot == -1) {
            player.sendRichMessage("<light_purple>You do not have enough space in your inventory!");
            return Command.SINGLE_SUCCESS;
        }

        // Retrieve our argument values
        final ItemStack item = ctx.getArgument("item", ItemStack.class);
        final int amount = IntegerArgumentType.getInteger(ctx, "amount");

        // Set the item's amount and give it to the player
        item.setAmount(amount);
        player.getInventory().setItem(firstEmptySlot, item);

        // Send a confirmation message
        player.sendRichMessage("<light_purple>You have been given <white><amount>x</white> <aqua><item></aqua>!",
                Placeholder.component("amount", Component.text(amount)),
                Placeholder.component("item", Component.translatable(item).hoverEvent(item))
        );
        return Command.SINGLE_SUCCESS;
    }
}