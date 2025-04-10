package ksucapproj.blockstowerdefense1.logic.game_logic.towers;

import ksucapproj.blockstowerdefense1.logic.game_logic.Economy;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static ksucapproj.blockstowerdefense1.logic.GUI.TowerGUI.*;

public abstract class Tower {
    protected final JavaPlugin plugin;  // Now an instance variable, not static
    protected static final Map<UUID, BukkitTask> towerTasks = new ConcurrentHashMap<>();
    protected static final Map<UUID, UUID> towerOwners = new ConcurrentHashMap<>();
    private static final Map<UUID, Tower> towerEntityToTowerMap = new HashMap<>();


    protected final Location location;
    protected final Player owner;
    protected final String mapId;
    protected final Villager towerEntity;

    protected double scanRadius;
    protected double attackInterval;
    protected double attackLevel = 0;
    protected double rangeLevel = 0;

    public UUID getTowerOwner(UUID towerUUID) {
        for (Map.Entry<UUID, UUID> entry : towerOwners.entrySet()) {
            if (entry.getValue().equals(towerUUID)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Tower(Location location, Player owner, String mapId, double scanRadius, long attackInterval, JavaPlugin plugin) {
        this.plugin = plugin;
        this.location = location;
        this.owner = owner;
        this.mapId = mapId;
        this.scanRadius = scanRadius;
        this.attackInterval = attackInterval;



        this.towerEntity = spawnTowerEntity();
        towerEntityToTowerMap.put(towerEntity.getUniqueId(), this);
        BukkitTask task = startTowerBehavior();
        towerTasks.put(towerEntity.getUniqueId(), task);
        towerOwners.put(towerEntity.getUniqueId(), owner.getUniqueId());
    }


    // Method to upgrade the tower's scan radius and attack interval
    public static void upgradeTower(Villager towerEntity, double newRange, long newAttackSpeed) {
        // Check if the towerEntity is not null
        if (towerEntity != null && towerEntity.hasMetadata("tower")) {
            // Retrieve the tower instance linked to this Villager
            Tower tower = getTowerFromEntity(towerEntity);

            // Upgrade the tower's attributes
            if (tower != null) {
                tower.setScanRadius(newRange);  // Set the new scan radius
                tower.setAttackInterval(newAttackSpeed);  // Set the new attack interval

                // Cancel the old task and start a new one with the updated interval
                UUID towerUUID = towerEntity.getUniqueId();
                BukkitTask oldTask = towerTasks.get(towerUUID);
                if (oldTask != null) {
                    oldTask.cancel();
                }

                BukkitTask newTask = tower.startTowerBehavior();
                towerTasks.put(towerUUID, newTask);

                // Don't try to send a message to the player here - do that from the GUI handler
            }
        }
    }

    // New getters and setters for upgrade levels
    public double getRangeLevel() {
        return rangeLevel;
    }

    public void setRangeLevel(double level) {
        this.rangeLevel = level;
    }

    public double getAttackLevel() {
        return attackLevel;
    }

    public void setAttackLevel(double level) {
        this.attackLevel = level;
    }

    // Getters for tower attributes
    public double getScanRadius() {
        return scanRadius;
    }

    public void setScanRadius(double scanRadius) {
        this.scanRadius = scanRadius;
    }

    public double getAttackInterval() {
        return attackInterval;
    }

    public void setAttackInterval(double attackInterval) {
        this.attackInterval = attackInterval;
    }

    // Calculate sell value based on upgrade levels
    public int getSellValue() {
        // Base value + additional value for each upgrade
        //return baseSellValue + (rangeLevel - 1) * 75 + (speedLevel - 1) * 100;
        return 500;
    }

    public void handleRangeUpgrade(Player player, Tower tower, Villager towerEntity) {
        double currentLevel = tower.getRangeLevel();

        // Check if max level reached
        if (currentLevel >= MAX_UPGRADE_LEVEL) {
            player.sendMessage("§cThis tower's range is already at maximum level!");
            return;
        }

        // Check if player has enough money
        if (!(Economy.getPlayerMoney(player) > RANGE_UPGRADE_COST)) {
            player.sendMessage("§cYou don't have enough money for this upgrade!");
            return;
        }

        // Calculate new range
        double currentRange = tower.getScanRadius();
        double newRange = currentRange + 1.0;

        // Apply upgrade
        Tower.upgradeTower(towerEntity, newRange, (long)tower.getAttackInterval());
        tower.setRangeLevel(currentLevel + 1);

        // Deduct cost
        Economy.spendMoney(player, RANGE_UPGRADE_COST);

        // Confirm upgrade
        player.sendMessage("§aTower range upgraded successfully!");
    }

    public void handleSpeedUpgrade(Player player, Tower tower, Villager towerEntity) {
        double currentLevel = tower.getAttackLevel();

        // Check if max level reached
        if (currentLevel >= MAX_UPGRADE_LEVEL) {
            player.sendMessage("§cThis tower's attack speed is already at maximum level!");
            return;
        }

        // Check if player has enough money
        if (!(Economy.getPlayerMoney(player) > ATTACK_UPGRADE_COST)) {
            player.sendMessage("§cYou don't have enough money for this upgrade!");
            return;
        }

        // Calculate new attack interval (20% faster)
        long currentInterval = (long)tower.getAttackInterval();
        long newInterval = Math.max(1, (long)(currentInterval * 0.8));

        // Apply upgrade
        Tower.upgradeTower(towerEntity, tower.getScanRadius(), newInterval);
        tower.setAttackLevel(currentLevel + 1);

        // Deduct cost
        Economy.spendMoney(player, ATTACK_UPGRADE_COST);

        // Confirm upgrade
        player.sendMessage("§aTower attack speed upgraded successfully!");
    }

    public void handleSellTower(Player player, Tower tower, Villager towerEntity) {
        // Get the sell value
        int sellValue = tower.getSellValue();

        // Add money to player
        Economy.addPlayerMoney(player, sellValue);

        // Kill the tower entity
        towerEntity.remove();

        // Confirm sale
        player.sendMessage("§aTower sold for " + sellValue + " coins!");
    }

    // Retrieve the tower using the Villager entity's UUID
    public static Tower getTowerFromEntity(Villager towerEntity) {
        return towerEntityToTowerMap.get(towerEntity.getUniqueId());
    }

    private static Villager.Profession getProfessionForTower(String type) {
        return switch (type) {
            case "Wizard Tower" -> Villager.Profession.LIBRARIAN;
            case "Slow Tower" -> Villager.Profession.WEAPONSMITH;
            case "Splash Tower" -> Villager.Profession.CLERIC;
            case "Fast Tower" -> Villager.Profession.ARMORER;
            case "Sniper Tower" -> Villager.Profession.LEATHERWORKER;
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }



    protected Villager spawnTowerEntity() {
        Location towerLocation = location.clone().toCenterLocation().add(0, -.5, 0);
        Villager tower = (Villager) towerLocation.getWorld().spawnEntity(towerLocation, EntityType.VILLAGER);
        tower.setProfession(getProfessionForTower(getTowerName()));
        tower.setAI(false);
        tower.setInvulnerable(true);
        tower.setSilent(true);
        tower.customName(Component.text(getTowerName()));
        tower.setCustomNameVisible(true);



        //this is the code for setting ownership for a tower:
        //towerEntity.setMetadata("owner", new FixedMetadataValue(plugin, owner.getUniqueId().toString()));

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
        }.runTaskTimer(plugin, 0L,(long) attackInterval);
    }

    public abstract String getTowerName();
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
        for(Player player : Bukkit.getOnlinePlayers()){
        for (Entity entity : player.getWorld().getEntities()) {
            if (entity instanceof Villager &&
                    entity.hasMetadata("tower") &&
                    entity.hasMetadata("owner") &&
                    entity.hasMetadata("mapId")) {

                    UUID entityUUID = entity.getUniqueId();

                    towerTasks.remove(entityUUID);
                    towerOwners.remove(entityUUID);

                    entity.remove();

            }
        }
    }


        towerTasks.clear();
        towerOwners.clear();
    }

}
