package ksucapproj.blockstowerdefense1.logic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TeleportationLogic {

    private final JavaPlugin plugin;

    public TeleportationLogic(JavaPlugin plugin){
        this.plugin = plugin;
    }

    // this attempts async teleportation up to 3 times
    public void teleportWithRetry(Player player, Location location, int maxRetries){

        // if the teleport is out of attempts, the player must retry if it did not work
        if (maxRetries <=  0){
            player.sendMessage("Teleport failed with several retries, please try again.");
            return;
        }

        player.teleportAsync(location).thenAccept(success -> {
            if (success){ // if teleport is successful, confirmation msg
                player.sendRichMessage("<green><bold>Teleport Successful!");
            }
            else{
                player.sendRichMessage("<red><italic>Teleport failed, retrying."); // confirmation msg upon failed attempt
                Bukkit.getScheduler().runTaskLater(plugin,
                        () -> teleportWithRetry(player, location, maxRetries - 1), // tp attempt
                        20L // 1-second delay before retrying
                );

            }
        }

        ).exceptionally(ex -> {
            ex.printStackTrace();
            player.sendMessage("<red><bold>An error occurred during teleportation.");
            return null;
        }
        );
    }
}
