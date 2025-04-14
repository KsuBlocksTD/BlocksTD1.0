package ksucapproj.blockstowerdefense1.logic.game_logic.towers;


import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import static ksucapproj.blockstowerdefense1.placeholderAPI.PlaceholderAPIExpansion.config;

public class SplashTower extends Tower {
    public SplashTower(Location location, Player owner, String mapId, JavaPlugin plugin) {
        // Medium scan radius, medium attack interval, area of effect damage
        super(location, owner, mapId, config.getSplashTowerRadius(), config.getSplashTowerAttacksp(), plugin);
    }

    @Override
    public String getTowerName() {
        return "Splash Tower";
    }

    @Override
    protected void attack() {
        // make it throw a potion eventually  this crap is obnoxious
//        Sound sound = Sound.sound(Key.key("entity.splash_potion.throw"), Sound.Source.NEUTRAL, 1, 1);
//        playSound(sound, towerEntity.getLocation(),  1f, 1f);
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
            if (target instanceof Mob primaryZombie) {
                // Damage primary target
                if(primaryZombie.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                    primaryZombie.damage(0.0);
                } else {primaryZombie.damage(config.getSplashTowerDamage());}

                primaryZombie.getWorld().spawnParticle(Particle.EXPLOSION, primaryZombie.getLocation(), 8);
                primaryZombie.setMetadata("attacker", new FixedMetadataValue(plugin, getTowerOwner(towerEntity.getUniqueId())));

                // Damage nearby zombies within 2 blocks
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Mob nearbyZombie &&
                            entity.getLocation().distance(primaryZombie.getLocation()) <= 2.0) {

                        if(nearbyZombie.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                            nearbyZombie.damage(0.0);
                        } else {nearbyZombie.damage(config.getSplashTowerAOEDamage());}
                        nearbyZombie.setMetadata("attacker", new FixedMetadataValue(plugin, getTowerOwner(towerEntity.getUniqueId())));

                    }
                }
            }
        }
    }
}
