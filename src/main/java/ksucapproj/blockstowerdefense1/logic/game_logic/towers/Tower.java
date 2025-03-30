package ksucapproj.blockstowerdefense1.logic.game_logic.towers;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Tower {
    protected final JavaPlugin plugin;  // Now an instance variable, not static
    protected static final Map<UUID, BukkitTask> towerTasks = new ConcurrentHashMap<>();
    protected static final Map<UUID, UUID> towerOwners = new ConcurrentHashMap<>();

    protected final Location location;
    protected final Player owner;
    protected final String mapId;
    protected final Villager towerEntity;

    protected int scanRadius;
    protected long attackInterval;

    public UUID getTowerOwner(UUID towerUUID) {
        for (Map.Entry<UUID, UUID> entry : towerOwners.entrySet()) {
            if (entry.getValue().equals(towerUUID)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Tower(Location location, Player owner, String mapId, int scanRadius, long attackInterval, JavaPlugin plugin) {
        this.plugin = plugin;
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
        //this is the code for setting ownership for a tower:
      //  towerEntity.setMetadata("owner", new FixedMetadataValue(plugin, owner.getUniqueId().toString()));

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


    //Cancel all tower tasks for a specific player

    public static void cancelTasksForPlayer(UUID playerUUID) {
        Set<UUID> towersToRemove = new HashSet<>();

        for (Map.Entry<UUID, UUID> entry : towerOwners.entrySet()) {
            if (entry.getValue().equals(playerUUID)) {
                UUID towerUUID = entry.getKey();
                towersToRemove.add(towerUUID);

                BukkitTask task = towerTasks.get(towerUUID);
                if (task != null && !task.isCancelled()) {
                    task.cancel();
                }
            }
        }

        for (UUID towerUUID : towersToRemove) {
            towerTasks.remove(towerUUID);
            towerOwners.remove(towerUUID);
        }
    }


    //Remove all towers for a specific player and map

    public static void removeTowersForPlayer(Player player, String mapId) {
        UUID playerUUID = player.getUniqueId();

        for (Entity entity : player.getWorld().getEntities()) {
            if (entity instanceof Villager &&
                    entity.hasMetadata("tower") &&
                    entity.hasMetadata("owner") &&
                    entity.hasMetadata("mapId")) {

                String owner = entity.getMetadata("owner").get(0).asString();
                String map = entity.getMetadata("mapId").get(0).asString();

                if (owner.equals(playerUUID.toString()) && map.equals(mapId)) {
                    UUID entityUUID = entity.getUniqueId();
                    BukkitTask task = towerTasks.get(entityUUID);
                    if (task != null && !task.isCancelled()) {
                        task.cancel();
                    }

                    towerTasks.remove(entityUUID);
                    towerOwners.remove(entityUUID);

                    entity.remove();
                }
            }
        }
    }


    public static void removeAllTowers() {
        for (BukkitTask task : towerTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }

        towerTasks.clear();
        towerOwners.clear();
    }
}
