package ksucapproj.blockstowerdefence1;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class SummonTower {
    private final JavaPlugin plugin;
    private Villager tower;
    private final int SCAN_RADIUS = 3;

    public SummonTower(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnTower(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        // Spawn the villager
        tower = (Villager) world.spawnEntity(location, EntityType.VILLAGER);
        tower.setAI(false); // Disable AI
        tower.setInvulnerable(true); // Make it invincible
        tower.setSilent(true); // No sounds
        tower.setCustomName("Summon Tower");
        tower.setCustomNameVisible(true);

        startScanning();
    }

    private void startScanning() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (tower == null || tower.isDead()) {
                    cancel();
                    return;
                }

                // Create a priority queue (closest zombies first)
                PriorityQueue<Entity> targetQueue = new PriorityQueue<>(Comparator.comparingDouble(e -> e.getLocation().distance(tower.getLocation())));

                // Get nearby entities
                List<Entity> nearbyEntities = tower.getNearbyEntities(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS);
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Zombie) {
                        targetQueue.add(entity); // Add zombie to priority queue
                    }
                }

                // Process the closest target
                if (!targetQueue.isEmpty()) {
                    Entity target = targetQueue.poll(); // Get the closest zombie
                    attackTarget(target);
                }
            }
        }.runTaskTimer(plugin, 0L, 5L); // Scans every 5 ticks (0.25 sec)
    }

    private void attackTarget(Entity target) {
        if (tower == null || tower.isDead() || target == null || target.isDead()) return;

        // Simulate attack (e.g., instant damage)
        ((Zombie) target).damage(2.0); // Deals 1 heart of damage
        target.getWorld().strikeLightningEffect(target.getLocation()); // Visual effect
    }
}
