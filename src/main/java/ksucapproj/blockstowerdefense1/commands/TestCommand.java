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
public class TestCommand {


    public static final PartiesAPI api = BlocksTowerDefense1.getApi();
    private final StartGame gameManager;
    private final JavaPlugin plugin;

    public TestCommand(StartGame gameManager, JavaPlugin plugin) {
        this.gameManager = gameManager;
        this.plugin = plugin;
    }

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


    // /addcoins <target> <coinamt>
    @NullMarked
    public static LiteralCommandNode<CommandSourceStack> addCoinsCommand() {
        return Commands.literal("addcoins")
                .requires(ctx -> ctx.getExecutor() instanceof Player)

                .then(Commands.argument("target", StringArgumentType.word())
                        .suggests(TestCommand::getOnlinePlayersSuggestions)
                        .then(Commands.argument("coinamt", IntegerArgumentType.integer(1, 100000))
                                .executes(TestCommand::executeAddCoinsLogic)
                        )
                )
                .build();
    }


    // /givecoins <coinamt>
    @NullMarked
    public static LiteralCommandNode<CommandSourceStack> giveCoinsCommand() {
        return Commands.literal("givecoins")
                .requires(ctx -> ctx.getExecutor() instanceof Player)

                .then(Commands.argument("coinamt", IntegerArgumentType.integer())
                        .executes(TestCommand::executeGiveCoinsLogic)
                )

                .build();
    }

    @NullMarked
    public LiteralCommandNode<CommandSourceStack> setRoundCommand() {
        return Commands.literal("setround")
                .requires(ctx -> ctx.getExecutor() instanceof Player)
                .then(Commands.argument("newround", IntegerArgumentType.integer())
                        .executes(ctx -> this.executeSetRoundLogic(ctx)) // Use instance method call
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

        // Get the arguments
        final String targetName = StringArgumentType.getString(ctx, "target");
        final int coinAmount = IntegerArgumentType.getInteger(ctx, "coinamt");

        // Get the target player
        Player targetPlayer = Bukkit.getPlayer(targetName);

        // Check if the player exists
        if (targetPlayer == null) {
            sender.sendRichMessage("<player> is not currently online!",
                    Placeholder.component("player", Component.text(targetName))
            );
            return Command.SINGLE_SUCCESS;
        }



        Economy.addPlayerMoney(targetPlayer, coinAmount);  // Replace with your actual economy system

        // Send confirmation messages
        sender.sendRichMessage("<player> has received <amount> coins!",
                Placeholder.component("player", Component.text(targetName)),
                Placeholder.component("amount", Component.text(coinAmount))
        );

        targetPlayer.sendRichMessage("You have received <amount> coins from <player>!",
                Placeholder.component("amount", Component.text(coinAmount)),
                Placeholder.component("player", Component.text(sender.getName()))
        );

        return Command.SINGLE_SUCCESS;
    }

    public LiteralCommandNode<CommandSourceStack> quitGameCommand() {
        return Commands.literal("quitgame")
                .requires(ctx -> ctx.getExecutor() instanceof Player)
                .executes(ctx -> this.executeQuitGame(ctx))
                .build();
    }

    private int executeQuitGame(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player sender)) {
            return Command.SINGLE_SUCCESS;
        }

        // Call the handlePlayerQuit method
        this.gameManager.handlePlayerQuit(sender);

        sender.sendMessage(ChatColor.RED + "You have quit the game.");
        return Command.SINGLE_SUCCESS;
    }

    public LiteralCommandNode<CommandSourceStack> startGameCommand() {
        return Commands.literal("startgame")
                .then(Commands.argument("map", StringArgumentType.word())
                        .executes(this::executeStartGameCommand))
                .build();
    }

    private int executeStartGameCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!(source.getExecutor() instanceof Player player)) {
            return Command.SINGLE_SUCCESS;
        }

        String mapId = StringArgumentType.getString(context, "map");

        // Verify map exists asynchronously
        CompletableFuture.supplyAsync(() -> MapData.mapExists(mapId))
                .thenAccept(exists -> {
                    if (!exists) {
                        CompletableFuture.supplyAsync(MapData::getAvailableMaps)
                                .thenAccept(maps -> {
                                    player.sendMessage(ChatColor.RED + "Map '" + mapId + "' does not exist!");
                                    player.sendMessage(ChatColor.YELLOW + "Available maps: " + String.join(", ", maps));
                                });
                        return;
                    }

                    // Proceed with game setup
                    Bukkit.getScheduler().runTask(plugin, () -> this.gameManager.startGames(player, mapId));
                });

        return Command.SINGLE_SUCCESS;
    }


    public LiteralCommandNode<CommandSourceStack> readyUpCommand() {
        return Commands.literal("readyup")
                .executes(this::executeReadyUpCommand)
                .build();
    }

    private int executeReadyUpCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!(source.getExecutor() instanceof Player player)) {
            return Command.SINGLE_SUCCESS;
        }

        // Handle the ready-up logic
        this.gameManager.handleReadyUpCommand(player);

        return Command.SINGLE_SUCCESS;
    }






    private int executeSetRoundLogic(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player sender)) {
            return Command.SINGLE_SUCCESS;
        }

        final int newRound = IntegerArgumentType.getInteger(ctx, "newround");

        // Use the instance variable instead of static method
        this.gameManager.setCurrentRound(sender.getUniqueId(), newRound);

        sender.sendMessage("New round is " + newRound);
        return Command.SINGLE_SUCCESS;
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
                sender.sendMessage("Invalid party size, no teammate acquired!");
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