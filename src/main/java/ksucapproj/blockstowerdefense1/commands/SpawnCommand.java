package ksucapproj.blockstowerdefense1.commands;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

public class SpawnCommand {

    private static final PartiesAPI api = BlocksTowerDefense1.getApi();
    private static final BlocksTowerDefense1 instance = BlocksTowerDefense1.getInstance();

    @NullMarked
    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("setspawn")
                .requires(ctx -> ctx.getExecutor() instanceof Player)
                .executes(SpawnCommand::executeSpawnLogic)

                .build();

    }


    // executes the logic for setting the spawn for HubCommand
    private static int executeSpawnLogic(final CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getExecutor() instanceof Player player)){
            return Command.SINGLE_SUCCESS;
        }

        FileConfiguration config = instance.getConfig();

        // gets the player's location, yaw, and pitch at time of setting command
        Location location = player.getLocation();

        config.set("btd.spawn.world", location.getWorld().getName());
        config.set("btd.spawn.x", location.getBlockX());
        config.set("btd.spawn.y", location.getBlockY());
        config.set("btd.spawn.z", location.getBlockZ());
        config.set("btd.spawn.yaw", location.getYaw());
        config.set("btd.spawn.pitch", location.getPitch());
//        player.sendMessage("World name: " + location.getWorld().getName());
//        player.sendMessage("Yaw set: " + location.getYaw());


        // saves the config with these values and reloads the plugin with the update just in case
        instance.saveConfig();
        instance.reloadConfig();

        player.sendRichMessage("<gold>New spawn point set for the hub!");
//        player.sendMessage("World name: " + config.getString("btd.spawn.world"));
//        player.sendMessage("Yaw set: " + config.getDouble("btd.spawn.yaw"));
//        player.sendMessage("cuts to the end of the command"); // for testing
        return Command.SINGLE_SUCCESS;
    }
}