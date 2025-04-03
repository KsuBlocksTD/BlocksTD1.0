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

public class SplashTower extends Tower {
    public SplashTower(Location location, Player owner, String mapId, JavaPlugin plugin) {
        // Medium scan radius, medium attack interval, area of effect damage
        super(location, owner, mapId, 6, 30L, plugin);///
    }

    @Override
    protected String getTowerName() {
        return "Splash Tower";
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
            if (target instanceof Mob primaryZombie) {
                // Damage primary target
                if(primaryZombie.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                    primaryZombie.damage(0.0);
                } else {primaryZombie.damage(10.0);}///

                primaryZombie.getWorld().spawnParticle(Particle.EXPLOSION, primaryZombie.getLocation(), 10);
                primaryZombie.setMetadata("attacker", new FixedMetadataValue(plugin, getTowerOwner(towerEntity.getUniqueId())));

                // Damage nearby zombies within 2 blocks
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Mob nearbyZombie &&
                            entity.getLocation().distance(primaryZombie.getLocation()) <= 2.0) {

                        if(nearbyZombie.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                            nearbyZombie.damage(0.0);
                        } else {nearbyZombie.damage(5.0);}///
                        nearbyZombie.setMetadata("attacker", new FixedMetadataValue(plugin, getTowerOwner(towerEntity.getUniqueId())));
                        // Slight slow for each zombie
                        nearbyZombie.addPotionEffect(new PotionEffect(
                                PotionEffectType.SLOWNESS,
                                10,  // Duration
                                1     // Amplifier
                        ));
                    }
                }
            }
        }
    }
}
