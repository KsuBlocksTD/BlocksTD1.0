package me.matthewTest.pluginTest.commands.party;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.matthewTest.pluginTest.PluginTest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CreatePartyCommand {

    private static final PartiesAPI api = PluginTest.getApi();


    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("create")
                .requires(ctx -> ctx.getExecutor() instanceof Player)
                .executes(CreatePartyCommand::createCommandLogic)
                .build();
    }


    private static int createCommandLogic (final CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player sender)) {
            return Command.SINGLE_SUCCESS;
        }

        if (api == null) {
            sender.sendMessage("Error: Parties API is not initialized.");
            return Command.SINGLE_SUCCESS;
        }

        String creatorName = sender.getName();

        Bukkit.getScheduler().runTask(PluginTest.getInstance(), () -> sender.performCommand("party create " + creatorName));


        return Command.SINGLE_SUCCESS;
    }
}
