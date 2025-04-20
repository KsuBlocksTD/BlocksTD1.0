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
    protected final JavaPlugin plugin;
    protected static final Map<UUID, BukkitTask> towerTasks = new ConcurrentHashMap<>();
    // towerEntity uuid and the BukkitTast its running
    protected static final Map<UUID, UUID> towerOwners = new ConcurrentHashMap<>();
    // towerEntity uuid, owner uuid
    private static final Map<UUID, Tower> towerEntityToTowerMap = new HashMap<>();
    // towerEntity uuid and tower entity

    protected final Location location; // location of tower
    protected final Player owner; // player who spawned the tower
    protected final String mapId; // mapid of tower - I think this is unused
    protected final Villager towerEntity; // Entity of the tower

    protected double scanRadius; // radius the tower can attack in - pulled from config
    protected double attackInterval; // how often the tower attacks - pulled from config
    protected double attackLevel = 0; // current level of the tower's attack interval
    protected double rangeLevel = 0;// current level of the tower's scanRadius
    protected double coinsSpent = 0;

    public UUID getTowerOwner(UUID towerUUID) {
        for (Map.Entry<UUID, UUID> entry : towerOwners.entrySet()) {
            if (entry.getValue().equals(towerUUID)) {
                return entry.getKey();
            }
        }
        return null;
    }


    // our Tower object
    public Tower(Location location, Player owner, String mapId, double scanRadius, long attackInterval, JavaPlugin plugin) {
        this.plugin = plugin;
        this.location = location;
        this.owner = owner;
        this.mapId = mapId;
        this.scanRadius = scanRadius;
        this.attackInterval = attackInterval;
        this.coinsSpent = 0;




        this.towerEntity = spawnTowerEntity(); // spawn the tower entity when its created

        towerEntityToTowerMap.put(towerEntity.getUniqueId(), this); // track the uuid and entity
        BukkitTask task = startTowerBehavior(); // start the tower behavior when created
        towerTasks.put(towerEntity.getUniqueId(), task); // track the task for this tower
        towerOwners.put(towerEntity.getUniqueId(), owner.getUniqueId()); // track who placed this tower
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

                BukkitTask newTask = tower.startTowerBehavior(); // start the new task with updated values
                towerTasks.remove(towerUUID);
                towerTasks.put(towerUUID, newTask); // update the hashmap

            }
        }
    }

    // Upgrading tower's attackRadius
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
        this.coinsSpent = coinsSpent + RANGE_UPGRADE_COST;


        // Confirm upgrade
        player.sendMessage("§aTower range upgraded successfully!");
    }

    // Upgrading tower's attackInterval
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
        this.coinsSpent = coinsSpent + ATTACK_UPGRADE_COST;

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


    // Used with custom datapack for tower skins
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


    // called when making a tower object to spawn it in the map
    protected Villager spawnTowerEntity() {
        Location towerLocation = location.clone().toCenterLocation().add(0, -.5, 0); // Ensure location is centered on the block
        Villager tower = (Villager) towerLocation.getWorld().spawnEntity(towerLocation, EntityType.VILLAGER);

        // Set custom meta for the villager behavior and tags

        // Behavior
        tower.setProfession(getProfessionForTower(getTowerName()));
        tower.setAI(false);
        tower.setInvulnerable(true);
        tower.setSilent(true);
        tower.customName(Component.text(getTowerName()));
        tower.setCustomNameVisible(true);

        // Tags
        tower.setMetadata("tower", new FixedMetadataValue(plugin, "true"));
        tower.setMetadata("owner", new FixedMetadataValue(plugin, owner.getUniqueId().toString()));
        tower.setMetadata("mapId", new FixedMetadataValue(plugin, mapId));

        return tower;
    }

    // Run for each tower
    protected BukkitTask startTowerBehavior() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (towerEntity.isDead()) { // Remove tower from everything if its dead
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

    // So the tower can look at the current target
    protected void faceTarget(Entity target) {
        if (target == null) return;
        Vector direction = target.getLocation().toVector().subtract(towerEntity.getLocation().toVector());
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
        yaw = (yaw + 360) % 360;
        towerEntity.setRotation(yaw, 0); // set look direction

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

                String owner = entity.getMetadata("owner").getFirst().asString();
                String map = entity.getMetadata("mapId").getFirst().asString();

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


    // Various getters and setters
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

    // Eventually calculate sell value from the total value put into the tower
    public int getSellValue() {
        //return baseSellValue + (rangeLevel - 1) * 75 + (speedLevel - 1) * 100;
        return (int) (this.coinsSpent + 450); //temp
    }
}
