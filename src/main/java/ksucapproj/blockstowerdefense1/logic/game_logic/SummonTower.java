package ksucapproj.blockstowerdefense1.logic.game_logic;

import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SummonTower {
    private static JavaPlugin plugin = null;
    private static final int SCAN_RADIUS = 5;

    // Track all towers and their tasks for cleanup
    private static final Map<UUID, BukkitTask> towerTasks = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> towerOwners = new ConcurrentHashMap<>();

    public SummonTower(JavaPlugin plugin) {
        SummonTower.plugin = plugin;
    }


    public static void spawnTower(Location location, Player owner, String mapId) {
        if (location == null || location.getWorld() == null || owner == null) return;

        // Center the tower on the block
        Location towerLocation = location.clone().add(0.5, 0, 0.5);
        Villager tower = (Villager) towerLocation.getWorld().spawnEntity(towerLocation, EntityType.VILLAGER);

        // Configure tower properties
        tower.setAI(false);
        tower.setInvulnerable(true);
        tower.setSilent(true);
        tower.setCustomName("Basic Tower");
        tower.setCustomNameVisible(true);

        // Add metadata for tracking
        tower.setMetadata("tower", new FixedMetadataValue(plugin, "true"));
        tower.setMetadata("owner", new FixedMetadataValue(plugin, owner.getUniqueId().toString()));
        tower.setMetadata("mapId", new FixedMetadataValue(plugin, mapId));

        // Start tower behavior and store the task
        BukkitTask task = startTowerBehavior(tower);
        towerTasks.put(tower.getUniqueId(), task);
        towerOwners.put(tower.getUniqueId(), owner.getUniqueId());
    }


    private static BukkitTask startTowerBehavior(Villager tower) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (tower == null || tower.isDead()) {
                    cancel();
                    towerTasks.remove(tower.getUniqueId());
                    towerOwners.remove(tower.getUniqueId());
                    return;
                }

                PriorityQueue<Entity> targetQueue = new PriorityQueue<>(
                        Comparator.comparingDouble(e -> e.getLocation().distance(tower.getLocation()))
                );

                List<Entity> nearbyEntities = tower.getNearbyEntities(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS);
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Zombie) {
                        // Check if the zombie belongs to the tower owner's game
                        if (entity.hasMetadata("gameSession") && tower.hasMetadata("owner")) {
                            String zombieOwner = entity.getMetadata("gameSession").get(0).asString();
                            String towerOwner = tower.getMetadata("owner").get(0).asString();

                            // Only target zombies from this player's game
                            if (zombieOwner.equals(towerOwner)) {
                                targetQueue.add(entity);
                            }
                        } else {
                            // Fallback if metadata is missing
                            targetQueue.add(entity);
                        }
                    }
                }

                if (!targetQueue.isEmpty()) {
                    Entity target = targetQueue.poll();
                    faceTarget(tower, target);
                    attackZombie(tower, target);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Attack once per second
    }

    /**
     * Make the tower face the target
     */
    private static void faceTarget(Villager tower, Entity target) {
        if (tower == null || target == null) return;

        Location towerLoc = tower.getLocation();
        Location targetLoc = target.getLocation();

        // Calculate direction vector
        Vector direction = targetLoc.toVector().subtract(towerLoc.toVector());

        // Convert vector to yaw rotation
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
        yaw = (yaw + 360) % 360; // Normalize angle

        // Update tower rotation
        tower.setRotation(yaw, 0);
    }

    /**
     * Attack a zombie target
     */
    private static void attackZombie(Villager tower, Entity target) {
        if (tower == null || tower.isDead() || target == null || target.isDead()) return;

        if (target instanceof Zombie) {
            Zombie zombie = (Zombie) target;
            if (tower.getLocation().distance(zombie.getLocation()) <= SCAN_RADIUS) {
                // Create visual effect for tower attack
                tower.getWorld().strikeLightningEffect(zombie.getLocation());

                // Damage the zombie
                zombie.damage(10.0);
                zombie.setVelocity(new Vector(0, 0.2, 0));
            }
        }
    }

    /**
     * Cancel all tower tasks for a specific player
     */
    public static void cancelTasksForPlayer(UUID playerUUID) {
        // Find all towers owned by this player
        Set<UUID> towersToRemove = new HashSet<>();

        for (Map.Entry<UUID, UUID> entry : towerOwners.entrySet()) {
            if (entry.getValue().equals(playerUUID)) {
                UUID towerUUID = entry.getKey();
                towersToRemove.add(towerUUID);

                // Cancel the task
                BukkitTask task = towerTasks.get(towerUUID);
                if (task != null && !task.isCancelled()) {
                    task.cancel();
                }
            }
        }

        // Clean up maps
        for (UUID towerUUID : towersToRemove) {
            towerTasks.remove(towerUUID);
            towerOwners.remove(towerUUID);
        }
    }

    /**
     * Remove all towers for a specific player and map
     */
    public static void removeTowersForPlayer(Player player, String mapId) {
        // Get player UUID for comparison
        UUID playerUUID = player.getUniqueId();

        // Remove all towers in the world that belong to this player and map
        for (Entity entity : player.getWorld().getEntities()) {
            if (entity instanceof Villager &&
                    entity.hasMetadata("tower") &&
                    entity.hasMetadata("owner") &&
                    entity.hasMetadata("mapId")) {

                String owner = entity.getMetadata("owner").get(0).asString();
                String map = entity.getMetadata("mapId").get(0).asString();

                if (owner.equals(playerUUID.toString()) && map.equals(mapId)) {
                    // Cancel the task first
                    UUID entityUUID = entity.getUniqueId();
                    BukkitTask task = towerTasks.get(entityUUID);
                    if (task != null && !task.isCancelled()) {
                        task.cancel();
                    }

                    // Remove from tracking
                    towerTasks.remove(entityUUID);
                    towerOwners.remove(entityUUID);

                    // Remove the entity
                    entity.remove();
                }
            }
        }
    }

    /**
     * Remove all towers from the game
     */
    public static void removeAllTowers() {
        // Cancel all tasks
        for (BukkitTask task : towerTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }

        towerTasks.clear();
        towerOwners.clear();
    }
}