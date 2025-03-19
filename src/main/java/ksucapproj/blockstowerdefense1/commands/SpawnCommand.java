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


    private static int executeSpawnLogic(final CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getExecutor() instanceof Player player)){
            return Command.SINGLE_SUCCESS;
        }

        FileConfiguration config = instance.getConfig();


        Location location = player.getLocation();

        config.set("spawn.world", location.getWorld().getName());
        config.set("spawn.x", location.getBlockX());
        config.set("spawn.y", location.getBlockY());
        config.set("spawn.z", location.getBlockZ());
        config.set("spawn.yaw", location.getYaw());
        config.set("spawn.pitch", location.getPitch());

        instance.saveConfig();

        player.sendMessage("New spawn point set for the hub!");
//        player.sendMessage("Yaw set: " + config.getDouble("spawn.pitch"));
//        player.sendMessage("cuts to the end of the command"); // for testing
        return Command.SINGLE_SUCCESS;
    }
}
