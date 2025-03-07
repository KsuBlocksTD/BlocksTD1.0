package me.matthewTest.pluginTest.logic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TeleportationLogic {

    private final JavaPlugin plugin;

    public TeleportationLogic(JavaPlugin plugin){
        this.plugin = plugin;
    }

    public void teleportWithRetry(Player player, Location location, int maxRetries){
        if (maxRetries <=  0){
            player.sendMessage("Teleport failed with several retries, please try again.");
            return;
        }

        player.teleportAsync(location).thenAccept(success -> {
            if (success){
                player.sendMessage("Teleport Successful!");
            }
            else{
                player.sendMessage("Teleport failed, retrying.");
                Bukkit.getScheduler().runTaskLater(plugin,
                        () -> teleportWithRetry(player, location, maxRetries - 1),
                        20L //1-second delay before retrying
                );

            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            player.sendMessage("An error occurred during teleportation.");
            return null;
        });
    }
}
