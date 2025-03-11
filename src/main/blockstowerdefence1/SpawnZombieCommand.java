package ksucapproj.blockstowerdefence1;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnZombieCommand implements CommandExecutor {

    private final MobHandler mobHandler;

    public SpawnZombieCommand(JavaPlugin plugin) {
        this.mobHandler = new MobHandler(plugin);  // Initialize MobHandler
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("spawnzombie.use")) {
            player.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        // Spawn the zombie at the player's location and start its pathfinding logic
        mobHandler.spawnMob(player.getLocation());


        return true;
    }
}
