package ksucapproj.blockstowerdefense1.logic.game_logic;

import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.logic.game_logic.towers.Tower;
import ksucapproj.blockstowerdefense1.maps.MapData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MobHandler implements Listener {

    private static JavaPlugin plugin;
    // Track zombie movement tasks for cleanup
    private static final Map<UUID, BukkitTask> zombieMovementTasks = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitTask> healthBarTasks = new ConcurrentHashMap<>();
    //private static final Map<UUID, UUID> zombieOwners = new ConcurrentHashMap<>();

    public MobHandler(JavaPlugin plugin) {
        MobHandler.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static Zombie spawnMob(World world, String mapId) {
        // Get the start location for the specified map
        Location spawnPoint = MapData.getStartLocation(world, mapId);

        if (spawnPoint == null) {
            plugin.getLogger().warning("Failed to spawn zombie: Invalid spawn point for map " + mapId);
            return null;
        }

        Zombie zombie = (Zombie) world.spawnEntity(spawnPoint, EntityType.ZOMBIE);
        zombie.setShouldBurnInDay(false);
        zombie.setBaby(false);
        zombie.setAI(false);
        zombie.setCustomNameVisible(true);

        // Start health bar display
        BukkitTask healthTask = displayHealthBar(zombie);
        healthBarTasks.put(zombie.getUniqueId(), healthTask);

        // Start path following
        BukkitTask movementTask = followPath(zombie, world, mapId);
        zombieMovementTasks.put(zombie.getUniqueId(), movementTask);

        return zombie;
    }

    private static BukkitTask followPath(Zombie zombie, World world, String mapId) {
        // Get waypoints for the specified map
        List<Location> waypoints = MapData.getWaypoints(world, mapId);
        // Get end location for the specified map
        Location endLocation = MapData.getEndLocation(world, mapId);

        if (waypoints == null || waypoints.isEmpty()) {
            plugin.getLogger().warning("No waypoints found for map " + mapId);
            return null;
        }

        return new BukkitRunnable() {
            int waypointIndex = 0;
            final double baseStepDistance = 0.2;
            final double slownessMultiplier = 0.5; // Reduces speed by half when slowed

            @Override
            public void run() {
                // If zombie is dead or invalid, clean up
                if (zombie == null || zombie.isDead() || !zombie.isValid()) {
                    cancel();
                    zombieMovementTasks.remove(zombie.getUniqueId());
                    return;
                }

                // Calculate current step distance based on slowness effect
                double stepDistance = baseStepDistance *
                        (zombie.hasPotionEffect(PotionEffectType.SLOWNESS) ? slownessMultiplier : 1.0);

                // Game end check - zombie reached endpoint
                if (endLocation != null && zombie.getLocation().distance(endLocation) < 1.5) {
                    // Get the player UUID from zombie metadata
                    if (zombie.hasMetadata("gameSession")) {
                        String playerUuidString = zombie.getMetadata("gameSession").get(0).asString();
                        UUID playerUUID = UUID.fromString(playerUuidString);
                        Player player = Bukkit.getPlayer(playerUUID);

                        if (player != null && player.isOnline()) {
                            // Handle game end for this player
                            handleGameEnd(zombie, player, mapId);
                        }
                    }

                    cancel();
                    zombieMovementTasks.remove(zombie.getUniqueId());
                    zombie.remove();
                    return;
                }

                // Path completion check
                if (waypointIndex >= waypoints.size()) {
                    cancel();
                    zombieMovementTasks.remove(zombie.getUniqueId());
                    return;
                }

                Location target = waypoints.get(waypointIndex);
                Vector direction = target.toVector().subtract(zombie.getLocation().toVector());

                if (direction.lengthSquared() > 0) {
                    // Normalize and scale direction
                    direction.normalize().multiply(stepDistance);

                    // Calculate movement yaw
                    float yaw = (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
                    yaw = (yaw + 360) % 360; // Ensure positive angle

                    // Create new location with rotation
                    Location newLocation = zombie.getLocation().add(direction);
                    newLocation.setYaw(yaw);

                    // Teleport with rotation
                    zombie.teleport(newLocation);
                }

                // Waypoint progression
                if (zombie.getLocation().distance(target) < stepDistance) {
                    waypointIndex++;
                    // If we've reached the last waypoint and it's the end location
                    if (waypointIndex == waypoints.size()) {
                        // Check if we have the player information to handle game end
                        if (zombie.hasMetadata("gameSession")) {
                            String playerUuidString = zombie.getMetadata("gameSession").get(0).asString();
                            UUID playerUUID = UUID.fromString(playerUuidString);
                            Player player = Bukkit.getPlayer(playerUUID);

                            if (player != null && player.isOnline()) {
                                handleGameEnd(zombie, player, mapId);
                            }
                        }
                        cancel();
                        zombieMovementTasks.remove(zombie.getUniqueId());
                        zombie.remove();
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    private static BukkitTask displayHealthBar(Zombie zombie) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie == null || zombie.isDead() || !zombie.isValid()) {
                    cancel();
                    healthBarTasks.remove(zombie.getUniqueId());
                    return;
                }

                double maxHealth = zombie.getMaxHealth();
                double currentHealth = zombie.getHealth();
                int healthPercentage = (int) ((currentHealth / maxHealth) * 100);

                StringBuilder healthBar = new StringBuilder();

                // Add color based on health percentage
                if (healthPercentage > 66) {
                    healthBar.append(ChatColor.GREEN);
                } else if (healthPercentage > 33) {
                    healthBar.append(ChatColor.YELLOW);
                } else {
                    healthBar.append(ChatColor.RED);
                }

                healthBar.append("[");
                int bars = healthPercentage / 5;
                healthBar.append("█".repeat(bars))
                        .append(" ".repeat(20 - bars))
                        .append("]");

                zombie.setCustomName(healthBar.toString());
            }
        }.runTaskTimer(plugin, 0, 5); // Update every 1/4 second (5 ticks)
    }

    private static void handleGameEnd(Zombie zombie, Player player, String mapId) {
        // Get the StartGame instance
        StartGame gameManager = BlocksTowerDefense1.getInstance().getGameManager();

        // Notify the player
        player.sendMessage(ChatColor.RED + "GAME OVER! A zombie reached the endpoint!");
        player.sendMessage(ChatColor.RED + "All your progress has been reset!");

        // Reset player's economy to 0
        Economy.setPlayerMoney(player, 0);

        // Remove all towers for this player's session
        Tower.removeTowersForPlayer(player, mapId);

        // Cancel any active tasks for this player's game
        cancelTasksForPlayer(player.getUniqueId());

        // Remove all zombies from this player's game
        removeZombiesForPlayer(player);

        // Reset the player's game state
        gameManager.resetPlayerGame(player, mapId);

        // Make sure the zombie is removed
        zombie.remove();
    }

    /**
     * Remove all zombies associated with a player's game
     */
    public static void removeZombiesForPlayer(Player player) {
        UUID playerUUID = player.getUniqueId();

        for (Entity entity : player.getWorld().getEntities()) {
            if (entity instanceof Zombie && entity.hasMetadata("gameSession")) {
                String sessionId = entity.getMetadata("gameSession").get(0).asString();
                if (sessionId.equals(playerUUID.toString())) {
                    // Cancel any tasks for this zombie
                    BukkitTask movementTask = zombieMovementTasks.get(entity.getUniqueId());
                    if (movementTask != null && !movementTask.isCancelled()) {
                        movementTask.cancel();
                    }

                    BukkitTask healthTask = healthBarTasks.get(entity.getUniqueId());
                    if (healthTask != null && !healthTask.isCancelled()) {
                        healthTask.cancel();
                    }

                    // Remove tracking
                    zombieMovementTasks.remove(entity.getUniqueId());
                    healthBarTasks.remove(entity.getUniqueId());

                    // Remove the zombie
                    entity.remove();
                }
            }
        }
    }

    /**
     * Cancel all tasks for a player's game
     */
    public static void cancelTasksForPlayer(UUID playerUUID) {
        // Convert UUID to string for comparison
        String playerUuidString = playerUUID.toString();

        // Find and cancel tasks for zombies that belong to this player
        for (Entity entity : Bukkit.getWorlds().get(0).getEntities()) {
            if (entity instanceof Zombie && entity.hasMetadata("gameSession")) {
                String sessionId = entity.getMetadata("gameSession").get(0).asString();
                if (sessionId.equals(playerUuidString)) {
                    UUID zombieUUID = entity.getUniqueId();

                    // Cancel movement task
                    BukkitTask movementTask = zombieMovementTasks.get(zombieUUID);
                    if (movementTask != null && !movementTask.isCancelled()) {
                        movementTask.cancel();
                    }

                    // Cancel health bar task
                    BukkitTask healthTask = healthBarTasks.get(zombieUUID);
                    if (healthTask != null && !healthTask.isCancelled()) {
                        healthTask.cancel();
                    }

                    // Remove from tracking
                    zombieMovementTasks.remove(zombieUUID);
                    healthBarTasks.remove(zombieUUID);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Zombie) {
            // Slightly reduce damage to make the game more challenging
            event.setDamage(event.getDamage() * 0.9);
        }
    }

    /**
     * Clean up all resources when the plugin is disabled
     */
    public static void cleanupAll() {
        // Cancel all zombie tasks
        for (BukkitTask task : zombieMovementTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }

        for (BukkitTask task : healthBarTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }

        zombieMovementTasks.clear();
        healthBarTasks.clear();
        //zombieOwners.clear();
    }
}