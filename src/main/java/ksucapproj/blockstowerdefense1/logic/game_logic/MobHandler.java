package ksucapproj.blockstowerdefense1.logic.game_logic;

import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.logic.game_logic.towers.Tower;
import ksucapproj.blockstowerdefense1.maps.MapData;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MobHandler implements Listener {

    private static JavaPlugin plugin;
    // Track zombie movement tasks for cleanup
    private static final Map<UUID, BukkitTask> zombieMovementTasks = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitTask> healthBarTasks = new ConcurrentHashMap<>();
    private static final double K = 0.059; // Growth rate factor
    private static final Random random = new Random();
    //private static final Map<UUID, UUID> zombieOwners = new ConcurrentHashMap<>();

    public MobHandler(JavaPlugin plugin) {
        MobHandler.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }



    public static boolean checkIfBuffed(int round) {
        if (round < 1) return false; // Ensure round is valid

        // Compute probability using the formula P(R) = 1 - e^(-K * (R - 1))
        double probability = 1 - Math.exp(-K * (round - 1));

        // Generate a random number between 0 and 1, and compare with probability
        return random.nextDouble() < probability;
    }

    public static boolean checkifSpawnSpecial(int round) {
        // Calculate probability using the formula
        double probability = (0.14 / (1 + Math.exp(-0.1 * (round - 1)))) + 0.01;

        // Generate a random number between 0 and 1 and compare with the calculated probability
        return random.nextDouble() < probability;
    }


    public static void zombieEquip(int count, int currentRound, Zombie zombie) {
        // Chestplate logic
        Material chestplate = getArmorForRound(currentRound, "chestplate");
        if (chestplate != null) {
            zombie.getEquipment().setChestplate(new ItemStack(chestplate));
        }

        if(count > 1) {
            // Leggings logic
            Material leggings = getArmorForRound(currentRound, "leggings");
            if (leggings != null) {
                zombie.getEquipment().setLeggings(new ItemStack(leggings));
            }
        }

        if(count > 2) {
            // Full armor
            Material boots = getArmorForRound(currentRound, "boots");
            if (boots != null) {
                zombie.getEquipment().setBoots(new ItemStack(boots));
            }
            Material helm = getArmorForRound(currentRound, "helmet");
            if (helm != null) {
                zombie.getEquipment().setHelmet(new ItemStack(helm));
            }
        }


    }

    // Get the armor level available
    private static Material getArmorForRound(int currentRound, String armorType) {
        switch (armorType) {
            case "chestplate" -> {
                if (currentRound < 10) {
                    return Material.GOLDEN_CHESTPLATE;
                } else if (currentRound < 20) {
                    return Material.IRON_CHESTPLATE;
                } else if (currentRound < 30) {
                    return Material.DIAMOND_CHESTPLATE;
                }else if (currentRound < 40) {
                    return Material.NETHERITE_CHESTPLATE;
                }
            }
            case "leggings" -> {
                if (currentRound < 10) {
                    return Material.GOLDEN_LEGGINGS;
                } else if (currentRound < 20) {
                    return Material.IRON_LEGGINGS;
                } else if (currentRound < 30) {
                    return Material.DIAMOND_LEGGINGS;
                }else if (currentRound < 40) {
                    return Material.NETHERITE_LEGGINGS;
                }
            }
            case "boots" -> {
                if (currentRound < 10) {
                    return Material.GOLDEN_BOOTS;
                } else if (currentRound < 20) {
                    return Material.IRON_BOOTS;
                } else if (currentRound < 30) {
                    return Material.DIAMOND_BOOTS;
                }else if (currentRound < 40) {
                    return Material.NETHERITE_BOOTS;
                }
            }
            case "helmet" -> {
                if (currentRound < 10) {
                    return Material.GOLDEN_HELMET;
                } else if (currentRound < 20) {
                    return Material.IRON_HELMET;
                } else if (currentRound < 30) {
                    return Material.DIAMOND_HELMET;
                }else if (currentRound < 40) {
                    return Material.NETHERITE_HELMET;
                }
            }
        }
        return null; // Return null if no armor should be equipped for that round
    }///

    // Get the mob types available
    public static EntityType getMob(int currentRound) {
        List<EntityType> availableMobs = new ArrayList<>();

        // Determine available mobs based on round
        if (currentRound >= 1) availableMobs.add(EntityType.IRON_GOLEM);
        if (currentRound >= 11) availableMobs.add(EntityType.WITCH);
        if (currentRound >= 21) availableMobs.add(EntityType.ENDERMAN);
        if (currentRound >= 31) availableMobs.add(EntityType.PIGLIN);
        if (currentRound >= 41) availableMobs.add(EntityType.BLAZE);

        // Select a random mob from the available ones
        return availableMobs.get(random.nextInt(availableMobs.size()));
    }///


    // apply the buffs the special zombies provide based on radius
    private static void applyEffectBasedOnNearbyMob(Zombie zombie) {
        Location zombieLocation = zombie.getLocation();
        double radius = 5.0;

        for (Entity entity : zombieLocation.getWorld().getNearbyEntities(zombieLocation, radius, radius, radius)) {
            if (entity instanceof IronGolem) {
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 10, 3));
                return;
            } else if (entity instanceof Witch) {
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 1));
                return;
            } else if (entity instanceof Enderman) {
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, 1));
                return;
            } else if (entity instanceof Piglin) {
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 10, 3));
                return;
            } else if (entity instanceof Blaze) {
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 4, 1)); // make them invulnerable until blaze is killed
                return;
            }
        }
    }///


    public static Mob spawnMob(World world, String mapId, int currentRound) {
        // Count for random buffs loop
        int count = 0;

        // Get the start location for the specified map
        Location spawnPoint = MapData.getStartLocation(world, mapId);


        // Spawn special mobs for difficulty increase
        if(checkifSpawnSpecial(currentRound)) {
            EntityType type = getMob(currentRound);

            Entity entity = world.spawnEntity(spawnPoint, type);

            if (entity instanceof Mob mob) {
                // Set spawn restrictions
                mob.setAI(false);
                mob.setCustomNameVisible(true);
                if (mob instanceof Ageable ageable) {
                    ageable.setAdult();
                }

                // Start health bar display
                BukkitTask healthTask = displayHealthBar(mob);
                healthBarTasks.put(mob.getUniqueId(), healthTask);

                // Start path following
                BukkitTask movementTask = followPath(mob, world, mapId);
                zombieMovementTasks.put(mob.getUniqueId(), movementTask);
                return mob;

            }
        }

        // Spawn zombie
        Zombie zombie = (Zombie) world.spawnEntity(spawnPoint, EntityType.ZOMBIE);


        // Loop to add buffs on the current zombie that scales with the current round
        while(checkIfBuffed(currentRound)) {
            if(count > 3) {break;}
            count++;
            zombieEquip(count, currentRound, zombie);
            checkIfBuffed(currentRound);
        }


        // Set spawn restrictions
        zombie.setShouldBurnInDay(false);
        zombie.setAdult();
        zombie.setAI(false);
        zombie.setCustomNameVisible(true);



        // Start health bar display
        BukkitTask healthTask = displayHealthBar(zombie);
        healthBarTasks.put(zombie.getUniqueId(), healthTask);

        // Start path following
        BukkitTask movementTask = followPath(zombie, world, mapId);
        zombieMovementTasks.put(zombie.getUniqueId(), movementTask);

        return zombie;
    }


    private static BukkitTask followPath(Mob zombie, World world, String mapId) {
        // Get waypoints for the specified map
        List<Location> waypoints = MapData.getWaypoints(world, mapId);
        // Get end location for the specified map
        Location endLocation = MapData.getEndLocation(world, mapId);

        if (waypoints == null || waypoints.isEmpty()) {
            plugin.getLogger().warning("No waypoints found for map " + mapId);
            return null;
        }

        return new BukkitRunnable() {
            int waypointIndex = 0;
            final double baseStepDistance = 0.2;
            final double slownessMultiplier = 0.5; // Reduces speed by half when slowed
            final  double speedMultiplier = 2.0;// Doubles speed when applied
            private int tickcounter = 0; // Used to execute some logic at a different rate than the rate of run()

            @Override
            public void run() {
                // If zombie is dead or invalid, clean up
                if (zombie == null || zombie.isDead() || !zombie.isValid()) {
                    cancel();
                    zombieMovementTasks.remove(zombie.getUniqueId());
                    return;
                }

                // Checking if zombie is near special zombie and applying buff every 10 ticks
                if(zombie.getType() == EntityType.ZOMBIE & tickcounter % 20 == 0) {
                    applyEffectBasedOnNearbyMob((Zombie) zombie);
                    tickcounter = 0;
                }


                // Calculate current step distance based on current effect
                double stepDistance = baseStepDistance * (zombie.hasPotionEffect(PotionEffectType.SLOWNESS) ? slownessMultiplier : 1.0);
                stepDistance = stepDistance * (zombie.hasPotionEffect(PotionEffectType.SPEED) ? speedMultiplier : 1.0);

                // Game end check - zombie reached endpoint
                if (endLocation != null && zombie.getLocation().distance(endLocation) < .2) {
                    // Get the player UUID from zombie metadata
                    if (zombie.hasMetadata("gameSession")) {
                        String playerUuidString = zombie.getMetadata("gameSession").getFirst().asString();
                        UUID playerUUID = UUID.fromString(playerUuidString);
                        Player player = Bukkit.getPlayer(playerUUID);

                        if (player != null && player.isOnline()) {
                            // Handle game end for this player
                            handleGameEnd(zombie, player, mapId);
                        }
                    }

                    cancel();
                    zombieMovementTasks.remove(zombie.getUniqueId());
                    zombie.remove();
                    return;
                }

                // Path completion check
                if (waypointIndex >= waypoints.size()) {
                    cancel();
                    zombieMovementTasks.remove(zombie.getUniqueId());
                    return;
                }

                Location target = waypoints.get(waypointIndex);
                Vector direction = target.toVector().subtract(zombie.getLocation().toVector());

                if (direction.lengthSquared() > 0) {
                    // Normalize and scale direction
                    direction.normalize().multiply(stepDistance);

                    // Calculate movement yaw
                    float yaw = (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
                    yaw = (yaw + 360) % 360; // Ensure positive angle

                    // Create new location with rotation
                    Location newLocation = zombie.getLocation().add(direction);
                    newLocation.setYaw(yaw);

                    // Teleport with rotation
                    zombie.teleport(newLocation);
                }

                // Waypoint progression
                if (zombie.getLocation().distance(target) < stepDistance) {
                    waypointIndex++;
                    // If we've reached the last waypoint and it's the end location
                    if (waypointIndex == waypoints.size()) {
                        // Check if we have the player information to handle game end
                        if (zombie.hasMetadata("gameSession")) {
                            String playerUuidString = zombie.getMetadata("gameSession").get(0).asString();
                            UUID playerUUID = UUID.fromString(playerUuidString);
                            Player player = Bukkit.getPlayer(playerUUID);

                            if (player != null && player.isOnline()) {
                                handleGameEnd(zombie, player, mapId);
                            }
                        }
                        cancel();
                        zombieMovementTasks.remove(zombie.getUniqueId());
                        zombie.remove();
                    }
                }tickcounter++;
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    private static BukkitTask displayHealthBar(Mob zombie) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie == null || zombie.isDead() || !zombie.isValid()) {
                    cancel();
                    healthBarTasks.remove(zombie.getUniqueId());
                    return;
                }

                double maxHealth = zombie.getMaxHealth();
                double currentHealth = zombie.getHealth();
                int healthPercentage = (int) ((currentHealth / maxHealth) * 100);

                StringBuilder healthBar = new StringBuilder();

                // Add color based on health percentage
                if (healthPercentage > 66) {
                    healthBar.append(ChatColor.GREEN);
                } else if (healthPercentage > 33) {
                    healthBar.append(ChatColor.YELLOW);
                } else {
                    healthBar.append(ChatColor.RED);
                }

                healthBar.append("[");
                int bars = healthPercentage / 5;
                healthBar.append("█".repeat(bars))
                        .append(" ".repeat(20 - bars))
                        .append("]");

                zombie.setCustomName(healthBar.toString());
            }
        }.runTaskTimer(plugin, 0, 5); // Update every 1/4 second (5 ticks)
    }

    private static void handleGameEnd(Mob zombie, Player player, String mapId) {
        // Get the StartGame instance
        StartGame gameManager = BlocksTowerDefense1.getInstance().getGameManager();

        // Notify the player
        player.sendMessage(ChatColor.RED + "GAME OVER! A zombie reached the endpoint!");
        player.sendMessage(ChatColor.RED + "All your progress has been reset!");

        // Reset player's economy to 0
        Economy.setPlayerMoney(player, 0);

        // Remove all towers for this player's session
        Tower.removeTowersForPlayer(player, mapId);

        // Cancel any active tasks for this player's game
        cancelTasksForPlayer(player.getUniqueId());

        // Remove all zombies from this player's game
        removeZombiesForPlayer(player);

        // Reset the player's game state
        gameManager.resetPlayerGame(player, mapId);

    }

    /**
     * Remove all zombies associated with a player's game
     */
    public static void removeZombiesForPlayer(Player player) {
        UUID playerUUID = player.getUniqueId();

        for (Entity entity : player.getWorld().getEntities()) {
            if (entity instanceof Mob && entity.hasMetadata("gameSession")) {
                String sessionId = entity.getMetadata("gameSession").get(0).asString();
                if (sessionId.equals(playerUUID.toString())) {
                    // Cancel any tasks for this zombie
                    BukkitTask movementTask = zombieMovementTasks.get(entity.getUniqueId());
                    if (movementTask != null && !movementTask.isCancelled()) {
                        movementTask.cancel();
                    }

                    BukkitTask healthTask = healthBarTasks.get(entity.getUniqueId());
                    if (healthTask != null && !healthTask.isCancelled()) {
                        healthTask.cancel();
                    }

                    // Remove tracking
                    zombieMovementTasks.remove(entity.getUniqueId());
                    healthBarTasks.remove(entity.getUniqueId());

                    // Remove the zombie
                    entity.remove();
                }
            }
        }
    }

    /**
     * Cancel all tasks for a player's game
     */
    public static void cancelTasksForPlayer(UUID playerUUID) {
        // Convert UUID to string for comparison
        String playerUuidString = playerUUID.toString();

        // Find and cancel tasks for zombies that belong to this player
        for (Entity entity : Bukkit.getWorlds().get(0).getEntities()) {
            if (entity instanceof Zombie && entity.hasMetadata("gameSession")) {
                String sessionId = entity.getMetadata("gameSession").get(0).asString();
                if (sessionId.equals(playerUuidString)) {
                    UUID zombieUUID = entity.getUniqueId();

                    // Cancel movement task
                    BukkitTask movementTask = zombieMovementTasks.get(zombieUUID);
                    if (movementTask != null && !movementTask.isCancelled()) {
                        movementTask.cancel();
                    }

                    // Cancel health bar task
                    BukkitTask healthTask = healthBarTasks.get(zombieUUID);
                    if (healthTask != null && !healthTask.isCancelled()) {
                        healthTask.cancel();
                    }

                    // Remove from tracking
                    zombieMovementTasks.clear();
                    healthBarTasks.remove(zombieUUID);
                }
            }
        }
    }

    /**
     * Clean up all resources when the plugin is disabled
     */
    public static void cleanupAll() {
        // Cancel all zombie tasks
        for (BukkitTask task : zombieMovementTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }

        for (BukkitTask task : healthBarTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }

        zombieMovementTasks.clear();
        healthBarTasks.clear();
        //zombieOwners.clear();
    }
}