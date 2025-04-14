package ksucapproj.blockstowerdefense1.commands;

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
import ksucapproj.blockstowerdefense1.logic.game_logic.PlayerUpgrades;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.concurrent.CompletableFuture;


/*
    --This is an admin command--

        Usage: /apply-upgrade <upgrade-type> <tier>

 */

// this command is not under the /mtd hierarchy, and is therefore its own standalone command **

public class ApplyUpgradeCommand {

    // this is used to create all upgrade suggestions for the '/apply-upgrade' command
    private static final List<String> upgradeTypes = List.of("SWIFTNESS", "STRENGTH", "MATERIAL", "SLOWNESS", "SWEEPING-EDGE");

    @NullMarked
    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("apply-upgrade")
                .requires(ctx -> ctx.getExecutor() instanceof Player)

                .then(Commands.argument("upgrade-type", StringArgumentType.word())
                        .suggests(ApplyUpgradeCommand::getUpgradeTypeSuggestions)
                        .then(Commands.argument("tier", IntegerArgumentType.integer(0, BlocksTowerDefense1.getInstance().getBTDConfig().getPlayerUniversalMaxLevel()))
                                .executes(ApplyUpgradeCommand::executeUpgradeCommandLogic)
                        )
                )
                .build();
    }


    // this creates the suggestions for the upgrade types from the list declared at the top of the class
    private static CompletableFuture<Suggestions> getUpgradeTypeSuggestions(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder) {

        for (String word : upgradeTypes){
            builder.suggest(word);
        }
        return builder.buildFuture();
    }

    // this function executes the logic once the command is completed
    private static int executeUpgradeCommandLogic(final CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getExecutor() instanceof Player player)){
            return Command.SINGLE_SUCCESS;
        }

        // if the player is NOT in a game, this command will NOT work
        if (PlayerUpgrades.getPlayerUpgradesMap().get(player) == null){
            player.sendRichMessage("<red>You must be in a game in order to use this command!");

            // enable this for tracking/testing purposes of the playerUpgradesMap size
//            player.sendRichMessage("Size of PlayerUpgradesMap(): <gold><size></gold>",
//                    Placeholder.component("size", Component.text(PlayerUpgrades.getPlayerUpgradesMap().size()))
//            );

            return Command.SINGLE_SUCCESS;
        }

        // this takes in the first argument in the command as a variable for the switch
        final String upgradeType = StringArgumentType.getString(ctx, "upgrade-type");
        // this takes in the second argument in the command as the tier for the specified upgrade type
        final int tier = IntegerArgumentType.getInteger(ctx, "tier");


        // gets the player's upgrade object from PlayerUpgrades
        PlayerUpgrades playerUpgrades = PlayerUpgrades.getPlayerUpgradesMap().get(player);

        // switch that takes in the upgrade type the user specifies, and applies it to the value specified
        switch (upgradeType) {
            case "SWIFTNESS" -> playerUpgrades.setSwiftnessLevel(tier);
            case "STRENGTH" -> playerUpgrades.setStrengthLevel(tier);
            case "MATERIAL" -> playerUpgrades.getSword().setSwordLevel(tier);
            case "SLOWNESS" -> playerUpgrades.getSword().setSlownessLevel(tier);
            case "SWEEPING-EDGE" -> playerUpgrades.getSword().setSweepingEdgeLevel(tier);
            default -> player.sendMessage("Upgrade type not found.");
        }



        return Command.SINGLE_SUCCESS;
    }
}
