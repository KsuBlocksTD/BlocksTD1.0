package ksucapproj.blockstowerdefense1.logic.game_logic;


import ksucapproj.blockstowerdefense1.logic.game_logic.towers.Tower;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class PlayerEventHandler implements Listener {
    private final JavaPlugin plugin;
    private final StartGame gameManager;

    public PlayerEventHandler(JavaPlugin plugin, StartGame gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }



    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Check if the player is in a game
        if (gameManager.isPlayerInGame(playerUUID)) {
            // Get the player's game data before cleanup
            String mapId = gameManager.getPlayerMapId(playerUUID);
            World world = player.getWorld();

            // Cancel all tasks related to this player's game
            cancelPlayerTasks(playerUUID);

            // Remove all zombies associated with this player's game
            removeGameEntities(world, playerUUID);

            // Remove all towers
            removeTowers(world, mapId);

            // Clean up game resources and data structures
            gameManager.handlePlayerQuit(player);

            // Log the cleanup
            plugin.getLogger().info("Cleaned up game for player " + player.getName());
        }
    }

    private void cancelPlayerTasks(UUID playerUUID) {
        // Cancel zombie spawning tasks
        gameManager.cancelTasks(playerUUID);

        // Cancel end-point detection tasks
        MobHandler.cancelTasksForPlayer(playerUUID);

        // Cancel tower attack tasks if you have them
        Tower.cancelTasksForPlayer(playerUUID);
    }

    private void removeGameEntities(World world, UUID playerUUID) {
        // Remove all zombies that belong to this player's game
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Zombie && entity.hasMetadata("gameSession")) {
                String sessionId = entity.getMetadata("gameSession").get(0).asString();
                if (sessionId.equals(playerUUID.toString())) {
                    entity.remove();
                }
            }
        }
    }

    private void removeTowers(World world, String mapId) {
        // Remove all towers in the world that belong to this map
        // This implementation depends on how you've implemented towers
        for (Entity entity : world.getEntities()) {
            if ((entity.hasMetadata("tower") && entity.hasMetadata("mapId") &&
                    entity.getMetadata("mapId").get(0).asString().equals(mapId)) ||
                    (entity instanceof ArmorStand && entity.getCustomName() != null &&
                            entity.getCustomName().contains("Tower"))) {
                entity.remove();
            }
        }
    }


}