package ksucapproj.blockstowerdefense1.logic.game_logic;

import ksucapproj.blockstowerdefense1.maps.MapData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class MobHandler implements Listener {

    private static JavaPlugin plugin;

    public MobHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void spawnMob(World world) {
        Location spawnPoint = MapData.getStartLocation(world);
        Zombie zombie = spawnPoint.getWorld().spawn(spawnPoint, Zombie.class);

        zombie.setShouldBurnInDay(false);
        zombie.setBaby(false);

        zombie.setAI(false);
        zombie.setCustomNameVisible(true);
        displayHealthBar(zombie);
        followPath(zombie, world);
    }

    private static void followPath(Zombie zombie, World world) {
        List<Location> waypoints = MapData.getWaypoints(world);
        Location endLocation = MapData.getEndLocation(world);

        new BukkitRunnable() {
            int waypointIndex = 0;
            final double stepDistance = 0.2;

            @Override
            public void run() {
                // Game end check (existing code)
                if (zombie.getLocation().getBlock().equals(endLocation.getBlock())) {
                    handleGameEnd(zombie);
                    cancel();
                    return;
                }

                // Path completion check (existing code)
                if (waypointIndex >= waypoints.size() || zombie.isDead()) {
                    cancel();
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

                // Waypoint progression (existing code)
                if (zombie.getLocation().distance(target) < stepDistance) {
                    waypointIndex++;
                    if (waypointIndex == waypoints.size()) {
                        handleGameEnd(zombie);
                        cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 2);
    }

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
                int healthPercentage = (int) ((currentHealth / maxHealth) * 100);

                StringBuilder healthBar = new StringBuilder("[");
                int bars = healthPercentage / 5;
                healthBar.append("█".repeat(bars))
                        .append(" ".repeat(20 - bars))
                        .append("]");

                zombie.setCustomName(healthBar.toString());
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private static void handleGameEnd(Zombie zombie) {
        Bukkit.broadcastMessage(ChatColor.RED + "GAME OVER! A zombie reached the endpoint!");
        zombie.remove();
        // Add additional game reset logic here
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        event.setDamage(event.getDamage() * 0.9);
    }
}