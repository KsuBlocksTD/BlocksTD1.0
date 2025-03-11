package ksucapproj.blockstowerdefence1;

import org.bukkit.Bukkit;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Location;
import org.bukkit.scoreboard.*;

import java.util.List;

public class MobHandler implements Listener {

    private static JavaPlugin plugin = null;

    public MobHandler(JavaPlugin plugin) { // Constructor to pass plugin instance
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // Create a list of waypoints (for now, 2 waypoints relative to the player's location)
    public static void spawnMob(Location spawnPoint) {
        Zombie zombie = spawnPoint.getWorld().spawn(spawnPoint, Zombie.class);

        // Remove AI for the zombie to prevent automatic behavior
        zombie.setAI(false);
        zombie.setCustomNameVisible(true);

        // Give the zombie a health bar
        displayHealthBar(zombie);

        // Start the zombie movement
        followPath(zombie, spawnPoint);
    }

    // Zombie follows the path (teleport to each waypoint every tick)
    private static void followPath(Zombie zombie, Location startLocation) {
        new BukkitRunnable() {
            int waypointIndex = 0;
            List<Location> waypoints = getWaypoints(startLocation);

            @Override
            public void run() {
                if (waypointIndex >= waypoints.size() || zombie.isDead()) {
                    cancel();
                    return;
                }

                Location targetLocation = waypoints.get(waypointIndex);
                double stepDistance = 0.2; // Reduced step distance to make movement slower

                // Calculate direction to the next waypoint
                Location direction;
                direction = targetLocation.clone().subtract(zombie.getLocation()).multiply(stepDistance);

                // Teleport zombie towards the next waypoint with reduced step distance
                zombie.teleport(zombie.getLocation().add(direction));

                // Move to the next waypoint if close enough
                if (zombie.getLocation().distance(targetLocation) < stepDistance) {
                    waypointIndex++;
                }
            }
        }.runTaskTimer(plugin, 0, 2); // Move every 2 ticks (1/10 second)
    }

    // Define the waypoints relative to the player's location
    private static List<Location> getWaypoints(Location playerLocation) {
        return List.of(
                playerLocation.clone().add(5, 0, 0),  // Move 5 blocks in the positive X direction
                playerLocation.clone().add(0, 0, 5)   // Move 5 blocks in the positive Z direction
        );
    }

    // Display a health bar above the zombie using the scoreboard API
    private static void displayHealthBar(Zombie zombie) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    cancel();
                    return;
                }

                double maxHealth = zombie.getMaxHealth();
                double currentHealth = zombie.getHealth();

                // Calculate health percentage
                int healthPercentage = (int) ((currentHealth / maxHealth) * 100);
                StringBuilder healthBar = new StringBuilder("[");

                // Determine the number of '|' based on health percentage
                int bars = (int) (healthPercentage / 5); // Each bar represents 5% health
                for (int i = 0; i < 20; i++) {
                    if (i < bars) {
                        healthBar.append("█"); // Filled bar
                    } else {
                        healthBar.append(" "); // Empty space
                    }
                }
                healthBar.append("]");

                // Set the title to show the health bar
                zombie.setCustomName(healthBar.toString()); // Set the health bar as custom name
                zombie.setCustomNameVisible(true); // Ensure it is visible
            }
        }.runTaskTimer(plugin, 0, 1); // Update every tick
    }




    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        // Modify entity damage if needed (not used for tower damage, just an example)
        event.setDamage(event.getDamage() * 0.9);
    }
}
