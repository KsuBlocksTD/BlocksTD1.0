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
import ksucapproj.blockstowerdefense1.logic.game_logic.PlayerUpgrades;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.concurrent.CompletableFuture;


/*
    --This is an admin command--

        Usage: /apply-upgrade <upgrade-type> <tier>

 */

public class ApplyUpgradeCommand {

    private static final List<String> upgradeTypes = List.of("SWIFTNESS", "STRENGTH", "MATERIAL", "SLOWNESS", "SWEEPING-EDGE");
    private static PlayerUpgrades playerUpgrades = null;

    @NullMarked
    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("apply-upgrade")
                .requires(ctx -> ctx.getExecutor() instanceof Player)

                .then(Commands.argument("upgrade-type", StringArgumentType.word())
                        .suggests(ApplyUpgradeCommand::getUpgradeTypeSuggestions)
                        .then(Commands.argument("tier", IntegerArgumentType.integer(0,5))
                                .executes(ApplyUpgradeCommand::executeUpgradeCommandLogic)
                        )
                )
                .build();
    }


    private static CompletableFuture<Suggestions> getUpgradeTypeSuggestions(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder) {

        for (String word : upgradeTypes){
            builder.suggest(word);
        }
        return builder.buildFuture();
    }


    private static int executeUpgradeCommandLogic(final CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getExecutor() instanceof Player player)){
            return Command.SINGLE_SUCCESS;
        }

        final String upgradeType = StringArgumentType.getString(ctx, "upgrade-type");
        final int tier = IntegerArgumentType.getInteger(ctx, "tier");


        if (PlayerUpgrades.getPlayerUpgradesMap().get(player) == null){
            PlayerUpgrades.getPlayerUpgradesMap().put(player, new PlayerUpgrades(player));
        }

        playerUpgrades = PlayerUpgrades.getPlayerUpgradesMap().get(player);


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
