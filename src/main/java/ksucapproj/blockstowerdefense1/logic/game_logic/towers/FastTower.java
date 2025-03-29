package ksucapproj.blockstowerdefense1.logic.game_logic.towers;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class FastTower extends Tower {
    public FastTower(Location location, Player owner, String mapId, JavaPlugin plugin) {
        // Small scan radius, very fast attack interval, low damage
        super(location, owner, mapId, 4, 5L, plugin);
    }

    @Override
    protected String getTowerName() {
        return "Fast Tower";
    }

    @Override
    protected void attack() {
        PriorityQueue<Entity> targetQueue = new PriorityQueue<>(
                Comparator.comparingDouble(e -> e.getLocation().distance(towerEntity.getLocation()))
        );

        List<Entity> nearbyEntities = towerEntity.getNearbyEntities(scanRadius, scanRadius, scanRadius);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Zombie) {
                if (entity.hasMetadata("gameSession") && towerEntity.hasMetadata("owner")) {
                    String zombieOwner = entity.getMetadata("gameSession").get(0).asString();
                    String towerOwner = towerEntity.getMetadata("owner").get(0).asString();
                    if (zombieOwner.equals(towerOwner)) {
                        targetQueue.add(entity);
                    }
                } else {
                    targetQueue.add(entity);
                }
            }
        }

        if (!targetQueue.isEmpty()) {
            Entity target = targetQueue.poll();
            faceTarget(target);
            if (target instanceof Zombie) {
                Zombie zombie = (Zombie) target;
                // Particle effect for fast attacks
                zombie.getWorld().spawnParticle(org.bukkit.Particle.CRIT, zombie.getLocation(), 10);
                // Low damage but rapid attacks
                zombie.damage(3.0);
            }
        }
    }
}
