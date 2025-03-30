package ksucapproj.blockstowerdefense1.logic.game_logic.towers;

import org.bukkit.Location;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class BasicTower extends Tower {
    public BasicTower(Location location, Player owner, String mapId, JavaPlugin plugin) {
        super(location, owner, mapId, 5, 20L, plugin);
    }

    @Override
    protected String getTowerName() {
        return "Basic Tower";
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
                towerEntity.getWorld().strikeLightningEffect(zombie.getLocation());
                zombie.damage(10.0);
                target.setMetadata("attacker", new FixedMetadataValue(plugin, getTowerOwner(towerEntity.getUniqueId())));

            }
        }
    }
}