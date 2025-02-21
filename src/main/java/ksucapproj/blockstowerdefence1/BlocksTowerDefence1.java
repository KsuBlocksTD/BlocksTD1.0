package ksucapproj.blockstowerdefence1;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

public final class BlocksTowerDefence1 extends JavaPlugin {
    private static BlocksTowerDefence1 instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Tower Defense Plugin Enabled!");

        // Register Commands & Events
        Objects.requireNonNull(getCommand("startgame")).setExecutor(new StartGameCommand());
        Objects.requireNonNull(getCommand("upgradetower")).setExecutor(new UpgradeTowerCommand());

        getServer().getPluginManager().registerEvents((Listener) new mobHandler(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Tower Defense Plugin Disabled.");
    }

    public static BlocksTowerDefence1 getInstance() {
        return instance;
    }
}
class mobHandler {
    private static final List<Location> waypoints = List.of(
            new Location(Bukkit.getWorld("world"), 100, 65, 100),
            new Location(Bukkit.getWorld("world"), 120, 65, 120),
            new Location(Bukkit.getWorld("world"), 140, 65, 140)
    );

    public static void spawnMob(Location spawnPoint) {
        Zombie mob = spawnPoint.getWorld().spawn(spawnPoint, Zombie.class);
        followPath(mob);
    }

    private static void followPath(Zombie entity) {
        new BukkitRunnable() {
            int waypointIndex = 0;
            @Override
            public void run() {
                if (waypointIndex >= waypoints.size() || entity.isDead()) {
                    cancel();
                    return;
                }
                entity.teleport(waypoints.get(waypointIndex));
                waypointIndex++;
            }
        }.runTaskTimer(BlocksTowerDefence1.getInstance(), 0, 40); // Move every 2 seconds
    }
}


public class Tower {
    private Location location;
    private double range = 10.0;

    public Tower(Location location) {
        this.location = location;
    }

    public void startShooting() {
        new BukkitRunnable() {
            @Override
            public void run() {
                List<LivingEntity> mobs = (List<LivingEntity>) location.getWorld().getEntitiesByClass(LivingEntity.class);
                for (LivingEntity mob : mobs) {
                    if (mob.getLocation().distance(location) <= range) {
                        shootAt(mob);
                        break;
                    }
                }
            }
        }.runTaskTimer(BlocksTowerDefence1.getInstance(), 0, 40);
    }

    private void shootAt(LivingEntity mob) {
        World world = location.getWorld();
        Arrow arrow = world.spawnArrow(location.add(0, 2, 0), new Vector(0, 1), 2.0f, 12.0f);
        arrow.setVelocity(mob.getLocation().toVector().subtract(arrow.getLocation().toVector()).normalize().multiply(2));
    }
}

public class Economy implements Listener {
    static final HashMap<Player, Integer> playerMoney = new HashMap<>();

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player player) {
            playerMoney.put(player, playerMoney.getOrDefault(player, 0) + 10);
            player.sendMessage("You earned $10! Balance: $" + playerMoney.get(player));
        }
    }

    public static int getBalance(Player player) {
        return playerMoney.getOrDefault(player, 0);
    }
}

