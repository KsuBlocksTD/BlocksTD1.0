package ksucapproj.blockstowerdefence1;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;


import org.bukkit.entity.Player;



public final class BlocksTowerDefence1 extends JavaPlugin {
    private static BlocksTowerDefence1 instance;



    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Tower Defense Plugin Enabled!");

        // Ensure commands exist before setting executors
        if (getCommand("summontower") != null) {
            getCommand("summontower").setExecutor(new SummonTowerCommand(this));
        } else {
            getLogger().severe("Command 'summontower' not found in plugin.yml!");
        }

        if (getCommand("startgame") != null) {
            getCommand("startgame").setExecutor(new StartGameCommand());
        } else {
            getLogger().severe("Command 'startgame' not found in plugin.yml!");
        }

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


class Economy implements Listener {
    static final HashMap<Player, Integer> playerMoney = new HashMap<>();

     public static int getBalance(Player player) {
         return 0;
     }

     @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        //if (event.getEntity().getKiller() instanceof Player player) {
            //playerMoney.put(player, playerMoney.getOrDefault(player, 0) + 10);
            //player.sendMessage("You earned $10! Balance: $" + playerMoney.get(player));
        }
    }

    //public static int getBalance(Player player) {
      //  return 0; //playerMoney.getOrDefault(player, 0);
    //}
//}

