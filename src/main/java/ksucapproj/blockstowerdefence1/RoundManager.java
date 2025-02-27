package ksucapproj.blockstowerdefence1;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class RoundManager {
    private static int round = 1;

    public static void startRound() {
        Bukkit.broadcastMessage("Starting Round " + round);
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= round * 5) { // Increase difficulty
                    cancel();
                    return;
                }
                MobHandler.spawnMob(new Location(Bukkit.getWorld("world"), 50, 65, 50));
                count++;
            }
        }.runTaskTimer(BlocksTowerDefence1.getInstance(), 0, 20);
        round++;
    }
}
