package ksucapproj.blockstowerdefense1.logic.game_logic.Towers;

import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Tower {
    protected static JavaPlugin plugin;
    protected static final Map<UUID, BukkitTask> towerTasks = new ConcurrentHashMap<>();
    protected static final Map<UUID, UUID> towerOwners = new ConcurrentHashMap<>();

    protected final Location location;
    protected final Player owner;
    protected final String mapId;
    protected final Villager towerEntity;

    protected int scanRadius;
    protected long attackInterval;

    public Tower(Location location, Player owner, String mapId, int scanRadius, long attackInterval) {
        if (plugin == null) throw new IllegalStateException("Plugin not initialized");

        this.location = location;
        this.owner = owner;
        this.mapId = mapId;
        this.scanRadius = scanRadius;
        this.attackInterval = attackInterval;

        this.towerEntity = spawnTowerEntity();
        BukkitTask task = startTowerBehavior();
        towerTasks.put(towerEntity.getUniqueId(), task);
        towerOwners.put(towerEntity.getUniqueId(), owner.getUniqueId());
    }

    protected Villager spawnTowerEntity() {
        Location towerLocation = location.clone().add(0.5, 0, 0.5);
        Villager tower = (Villager) towerLocation.getWorld().spawnEntity(towerLocation, EntityType.VILLAGER);
        tower.setAI(false);
        tower.setInvulnerable(true);
        tower.setSilent(true);
        tower.setCustomName(getTowerName());
        tower.setCustomNameVisible(true);

        tower.setMetadata("tower", new FixedMetadataValue(plugin, "true"));
        tower.setMetadata("owner", new FixedMetadataValue(plugin, owner.getUniqueId().toString()));
        tower.setMetadata("mapId", new FixedMetadataValue(plugin, mapId));

        return tower;
    }

    protected BukkitTask startTowerBehavior() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (towerEntity.isDead()) {
                    cancel();
                    towerTasks.remove(towerEntity.getUniqueId());
                    towerOwners.remove(towerEntity.getUniqueId());
                    return;
                }
                attack();
            }
        }.runTaskTimer(plugin, 0L, attackInterval);
    }

    protected abstract String getTowerName();
    protected abstract void attack();

    protected void faceTarget(Entity target) {
        if (target == null) return;
        Vector direction = target.getLocation().toVector().subtract(towerEntity.getLocation().toVector());
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
        yaw = (yaw + 360) % 360;
        towerEntity.setRotation(yaw, 0);
    }

    public static void setPlugin(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
    }

    public static void cancelTasksForPlayer(UUID playerUUID) {
        towerOwners.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(playerUUID)) {
                UUID towerUUID = entry.getKey();
                BukkitTask task = towerTasks.remove(towerUUID);
                if (task != null) task.cancel();
                return true;
            }
            return false;
        });
    }

    public static void removeAllTowers() {
        for (BukkitTask task : towerTasks.values()) {
            if (task != null) task.cancel();
        }
        towerTasks.clear();
        towerOwners.clear();
    }
}
