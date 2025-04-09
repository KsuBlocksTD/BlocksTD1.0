package ksucapproj.blockstowerdefense1.logic.game_logic.towers;

import org.bukkit.Location;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import static ksucapproj.blockstowerdefense1.placeholderAPI.PlaceholderAPIExpansion.config;

public class BasicTower extends Tower {
    public BasicTower(Location location, Player owner, String mapId, JavaPlugin plugin) {
        super(location, owner, mapId, config.getBasicTowerRadius(), config.getBasicTowerAttacksp(), plugin);
    }

    @Override
    protected String getTowerName() {
        return "Wizard Tower";
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
                towerEntity.getWorld().strikeLightningEffect(zombie.getLocation());
                if(zombie.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                    zombie.damage(0.0);
                } else {zombie.damage(config.getBasicTowerDamage());}
                target.setMetadata("attacker", new FixedMetadataValue(plugin, getTowerOwner(towerEntity.getUniqueId())));

            }
        }
    }
    @Override
    public Boolean getUpgradeTierOne(boolean left){
        if (Boolean.TRUE.equals(left)){
            // increase range upon upgrade by 50%
            scanRadius = scanRadius * 1.5;
            owner.sendRichMessage("<gold>Tower range increased by 50%!");
            return true;
        }
        else if (Boolean.FALSE.equals(left)){
            // increase attack speed by 25%
            attackInterval = attackInterval * 0.75;
            owner.sendRichMessage("<gold>Tower range increased by 50%!");
            return false;
        }
        return null;
    }
}