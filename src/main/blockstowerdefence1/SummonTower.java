package ksucapproj.blockstowerdefence1;

import org.bukkit.Bukkit;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;

public class SummonTower {

    private final JavaPlugin plugin;
    private Villager tower;
    private static final int SCAN_RADIUS = 3; // The range within which zombies are detected

    public SummonTower(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnTower(Location location) {
        Location towerLocation = location.clone();

        // Spawn the villager tower
        tower = (Villager) towerLocation.getWorld().spawnEntity(towerLocation, org.bukkit.entity.EntityType.VILLAGER);
        tower.setAI(false); // Disable AI for the tower
        tower.setInvulnerable(true); // Make it invincible
        tower.setSilent(true); // No sounds
        tower.setCustomName("Tower");
        tower.setCustomNameVisible(true);

        startTowerBehavior();
    }

    private void startTowerBehavior() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (tower == null || tower.isDead()) {
                    cancel();
                    return;
                }

                // Create a priority queue for zombies within range
                PriorityQueue<Entity> targetQueue = new PriorityQueue<>(Comparator.comparingDouble(e -> e.getLocation().distance(tower.getLocation())));

                // Get all nearby entities (zombies) within the scan radius
                List<Entity> nearbyEntities = tower.getNearbyEntities(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS);
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Zombie) {
                        targetQueue.add(entity); // Add the zombie to the priority queue
                    }
                }

                // If a zombie is found, apply damage
                if (!targetQueue.isEmpty()) {
                    Entity target = targetQueue.poll(); // Get the closest zombie
                    attackZombie(target);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Check every 20 ticks (1 second)
    }

    private void attackZombie(Entity target) {
        if (tower == null || tower.isDead() || target == null || target.isDead()) return;

        // Deal 5 damage to the zombie
        if (target instanceof Zombie) {
            Zombie zombie = (Zombie) target;

            // Only deal damage if the zombie is within range
            if (tower.getLocation().distance(target.getLocation()) <= SCAN_RADIUS) {
                zombie.damage(5.0); // Deal 5 damage to the closest zombie
            }
        }
    }
}
