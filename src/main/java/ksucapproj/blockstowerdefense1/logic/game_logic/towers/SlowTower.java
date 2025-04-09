package ksucapproj.blockstowerdefense1.logic.game_logic.towers;


import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import static ksucapproj.blockstowerdefense1.placeholderAPI.PlaceholderAPIExpansion.config;

public class SlowTower extends Tower {
    public SlowTower(Location location, Player owner, String mapId, JavaPlugin plugin) {
        // Medium scan radius, medium attack interval, apply slowness
        super(location, owner, mapId, config.getSlowTowerRadius(), config.getSlowTowerAttacksp(), plugin);
    }

    @Override
    protected String getTowerName() {
        return "Slow Tower";
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
                // Minimal direct damage
                if(zombie.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                    zombie.damage(0.0);
                } else {zombie.damage(config.getSlowTowerDamage());}
                target.setMetadata("attacker", new FixedMetadataValue(plugin, getTowerOwner(towerEntity.getUniqueId())));

                // Apply slowness effect
                zombie.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOWNESS,
                        100,  // Duration (5 seconds)
                        2     //
                ));

                // Visual effect of slowing
                zombie.getWorld().spawnParticle(
                        Particle.DRIPPING_HONEY,
                        zombie.getLocation(),
                        40,
                        0, 0, 0,
                        0.1
                );
            }
        }
    }
}
