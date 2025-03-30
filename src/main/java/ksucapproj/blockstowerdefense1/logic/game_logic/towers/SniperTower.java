package ksucapproj.blockstowerdefense1.logic.game_logic.towers;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class SniperTower extends Tower {
    public SniperTower(Location location, Player owner, String mapId, JavaPlugin plugin) {
        // Large scan radius, slow attack interval, high damage
        super(location, owner, mapId, 15, 60L, plugin);
    }

    @Override
    protected String getTowerName() {
        return "Sniper Tower";
    }

    @Override
    protected void attack() {
        PriorityQueue<Entity> targetQueue = new PriorityQueue<>(
                Comparator.comparingDouble(e -> e.getLocation().distance(towerEntity.getLocation()))
        );

        List<Entity> nearbyEntities = towerEntity.getNearbyEntities(scanRadius, scanRadius, scanRadius);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Mob & entity.getType() != EntityType.VILLAGER) {
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
            if (target instanceof Mob) {
                Mob zombie = (Mob) target;
                // Visual effect of a precise shot
                zombie.getWorld().spawnParticle(Particle.EXPLOSION, zombie.getLocation(), 1);
                // High damage but slow attack
                zombie.damage(25.0);
                target.setMetadata("attacker", new FixedMetadataValue(plugin, getTowerOwner(towerEntity.getUniqueId())));
                // Slight knockback effect
                zombie.setVelocity(new Vector(0, 0.5, 0));
            }
        }
    }
}
