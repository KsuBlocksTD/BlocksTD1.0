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


/*
    --This is an admin command--

        Usage: /setspawn

 */

// this command is not under the /mtd hierarchy, and is therefore its own standalone command **

public class SpawnCommand {

    private static final PartiesAPI api = BlocksTowerDefense1.getApi();
    private static final BlocksTowerDefense1 instance = BlocksTowerDefense1.getInstance();

    // this .register() function is registered under the base command "/mtd <subcommand>"
    @NullMarked
    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("setspawn")
                .requires(ctx -> ctx.getExecutor() instanceof Player)
                .requires(ctx -> ctx.getExecutor().hasPermission("blockstd.admin.setspawn"))

                .executes(SpawnCommand::executeSpawnLogic)

                .build();

    }


    // executes the logic for setting the spawn for HubCommand
    private static int executeSpawnLogic(final CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getExecutor() instanceof Player player)){
            return Command.SINGLE_SUCCESS;
        }

        // gets and stores the plugin instance's config.yml file
        FileConfiguration config = instance.getConfig();

        // gets the player's location, yaw, and pitch at time of setting command
        Location location = player.getLocation();

        // sets existing spawn config values to the player's world coordinate values
        config.set("btd.spawn.world", location.getWorld().getName());
        config.set("btd.spawn.x", location.getX());
        config.set("btd.spawn.y", location.getBlockY());
        config.set("btd.spawn.z", location.getZ());
        config.set("btd.spawn.yaw", location.getYaw());
        config.set("btd.spawn.pitch", location.getPitch());
//        player.sendMessage("World name: " + location.getWorld().getName());
//        player.sendMessage("Yaw set: " + location.getYaw());


        // saves the config with these values and reloads the plugin with the update just in case
        instance.saveConfig();
        instance.reloadConfig();

        player.sendRichMessage("<gold>New spawn point set for the hub!"); // confirmation msg
//        player.sendMessage("World name: " + config.getString("btd.spawn.world"));
//        player.sendMessage("Yaw set: " + config.getDouble("btd.spawn.yaw"));
//        player.sendMessage("cuts to the end of the command"); // for testing
        return Command.SINGLE_SUCCESS;
    }
}