package ksucapproj.blockstowerdefense1.commands;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.logic.game_logic.StartGame;
import ksucapproj.blockstowerdefense1.maps.MapData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

// this class contains several commands, check each command's build to see its usage

public class GameCommand {

    public static final PartiesAPI api = BlocksTowerDefense1.getApi();
    private final StartGame gameManager;
    private final JavaPlugin plugin;

    public GameCommand(StartGame gameManager, JavaPlugin plugin) {
        this.gameManager = gameManager;
        this.plugin = plugin;
    }


    /*
    --This is an admin command--

        Usage: /setround <round_number>

    */

    // this command is not under the /mtd hierarchy, and is therefore its own standalone command **
    @NullMarked
    public LiteralCommandNode<CommandSourceStack> setRoundCommand() {
        return Commands.literal("setround")
                .requires(ctx -> ctx.getExecutor() instanceof Player)
                .then(Commands.argument("newround", IntegerArgumentType.integer())
                        .executes(ctx -> this.executeSetRoundLogic(ctx)) // Use instance method call
                )
                .build();
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


    // this command is not under the /mtd hierarchy, and is therefore its own standalone command **
    // "/quitgame"
    public LiteralCommandNode<CommandSourceStack> quitGameCommand() {
        return Commands.literal("quitgame")
                .requires(ctx -> ctx.getSender() instanceof Player)
                .executes(this::executeQuitGame)
                .build();
    }

    private int executeQuitGame(final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player sender)) {
            return Command.SINGLE_SUCCESS;
        }

        // Clean game session
        this.gameManager.gameEndStatus(sender.getUniqueId(), false);

        sender.sendRichMessage("<red>You have quit the game.");
        return Command.SINGLE_SUCCESS;
    }



    // this command is not under the /mtd hierarchy, and is therefore its own standalone command **
    // "/startgame <map_name>"
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


    // this command is not under the /mtd hierarchy, and is therefore its own standalone command **
    // "/readyup"
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

}
