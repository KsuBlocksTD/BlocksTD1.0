package ksucapproj.blockstowerdefense1.logic.game_logic.towers;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import static ksucapproj.blockstowerdefense1.placeholderAPI.PlaceholderAPIExpansion.config;

public class FastTower extends Tower {
    public FastTower(Location location, Player owner, String mapId, JavaPlugin plugin) {
        // Small scan radius, very fast attack interval, low damage
        super(location, owner, mapId, config.getFastTowerRadius(), config.getFastTowerAttacksp(), plugin);
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
            if (entity instanceof Mob & entity.getType() != EntityType.VILLAGER) {
                if (entity.hasMetadata("gameSession") && towerEntity.hasMetadata("owner")) {
                    String zombieOwner = entity.getMetadata("gameSession").getFirst().asString();
                    String towerOwner = towerEntity.getMetadata("owner").getFirst().asString();
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
            if (target instanceof Mob zombie) {
                // Particle effect for fast attacks
                zombie.getWorld().spawnParticle(Particle.SMOKE, zombie.getLocation(), 10);
                // Low damage but rapid attacks
                if(zombie.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                    zombie.damage(0.0);
                } else {zombie.damage(config.getFastTowerDamage());}
                //                 this is the code for setting ownership for a tower:
                target.setMetadata("attacker", new FixedMetadataValue(plugin, getTowerOwner(towerEntity.getUniqueId())));
            }
        }
    }
}
