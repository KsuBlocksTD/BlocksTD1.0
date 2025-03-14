package ksucapproj.blockstowerdefence1;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;

public class SummonTower {

    private static JavaPlugin plugin = null;
    private static Villager tower;
    private static final int SCAN_RADIUS = 3;

    public SummonTower(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static void spawnTower(Location location) {
        if (location == null || location.getWorld() == null) return;

        Location towerLocation = location.clone().add(0.5, 0, 0.5);
        tower = (Villager) towerLocation.getWorld().spawnEntity(towerLocation, EntityType.VILLAGER);

        tower.setAI(false);
        tower.setInvulnerable(true);
        tower.setSilent(true);
        tower.setCustomName("Basic Tower");
        tower.setCustomNameVisible(true);

        startTowerBehavior();
    }

    private static void startTowerBehavior() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (tower == null || tower.isDead()) {
                    cancel();
                    return;
                }

                PriorityQueue<Entity> targetQueue = new PriorityQueue<>(
                        Comparator.comparingDouble(e -> e.getLocation().distance(tower.getLocation()))
                );

                List<Entity> nearbyEntities = tower.getNearbyEntities(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS);
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Zombie) {
                        targetQueue.add(entity);
                    }
                }

                if (!targetQueue.isEmpty()) {
                    Entity target = targetQueue.poll();
                    faceTarget(target); // Face target before attacking
                    attackZombie(target);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private static void faceTarget(Entity target) {
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

    private static void attackZombie(Entity target) {
        if (tower == null || tower.isDead() || target == null || target.isDead()) return;

        if (target instanceof Zombie) {
            Zombie zombie = (Zombie) target;
            if (tower.getLocation().distance(zombie.getLocation()) <= SCAN_RADIUS) {
                zombie.damage(5.0);
                zombie.setVelocity(new Vector(0, 0.2, 0));
            }
        }
    }
}