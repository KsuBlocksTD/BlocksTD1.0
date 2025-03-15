package ksucapproj.blockstowerdefence1;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SummonTowerCommand implements CommandExecutor {

    private final SummonTower summonTower;

    public SummonTowerCommand(JavaPlugin plugin) {
        this.summonTower = new SummonTower(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("summontower.use")) {
            player.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        // Spawn the tower at the player's location
        summonTower.spawnTower(player.getLocation());
        player.sendMessage("§aTower spawned at your location!");

        return true;
    }
}
