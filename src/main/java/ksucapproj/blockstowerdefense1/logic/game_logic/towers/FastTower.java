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

public class FastTower extends Tower {
    public FastTower(Location location, Player owner, String mapId, JavaPlugin plugin) {
        // Small scan radius, very fast attack interval, low damage
        super(location, owner, mapId, config.getFastTowerRadius(), config.getFastTowerAttacksp(), plugin);
    }

    @Override
    public String getTowerName() {
        return "Fast Tower";
    }

    @Override
    protected void attack() {
        // add nearby entities into a queue based on distance
        PriorityQueue<Entity> targetQueue = new PriorityQueue<>(
                Comparator.comparingDouble(e -> e.getLocation().distance(towerEntity.getLocation()))
        );

        List<Entity> nearbyEntities = towerEntity.getNearbyEntities(scanRadius, scanRadius, scanRadius);
        for (Entity entity : nearbyEntities) {
            // we need to ensure the entitiy it grabs isnt another tower, and to ensure that the tower that kills the zombie gets credit
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
            faceTarget(target); // to make sure it doesnt just stand there
            if (target instanceof Mob zombie) {
                //spawn attack effect
                zombie.getWorld().spawnParticle(Particle.SMOKE, zombie.getLocation(), 10);

                if(zombie.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) { //Ensure that it can't kill zombies with fire resistance (from blazes)
                    zombie.damage(0.0);
                } else {zombie.damage(config.getFastTowerDamage());}
                //this is the code for setting ownership for a tower:
                target.setMetadata("attacker", new FixedMetadataValue(plugin, getTowerOwner(towerEntity.getUniqueId()))); // This is for kill tracking credit
            }
        }
    }
}
