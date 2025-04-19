package ksucapproj.blockstowerdefense1.logic.game_logic;

import ksucapproj.blockstowerdefense1.logic.game_logic.Items.GlowingTotem;
import ksucapproj.blockstowerdefense1.logic.game_logic.towers.Tower;
import ksucapproj.blockstowerdefense1.maps.MapData;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static ksucapproj.blockstowerdefense1.placeholderAPI.PlaceholderAPIExpansion.config;

public class MobHandler {

    private static JavaPlugin plugin;
    // Track zombie movement tasks for cleanup
    private static final Map<UUID, BukkitTask> zombieMovementTasks = new ConcurrentHashMap<>();
    // Track zombie healthbars
    private static final Map<UUID, BukkitTask> healthBarTasks = new ConcurrentHashMap<>();
    private static final double K = 0.059; // Growth rate factor
    private static final Random random = new Random();
    private static StartGame gameManager; // Get the gameManager from startgame


    public MobHandler(StartGame gameManager, JavaPlugin plugin) {
        MobHandler.plugin = plugin;
        this.gameManager = gameManager;
    }


    // Recursive formula to check how many armor pieces each zombie that spawns will have
    public static boolean checkIfBuffed(int round) {
        if (round < 1) return false; // Ensure round is valid

        // Compute probability using the formula P(R) = 1 - e^(-K * (R - 1))
        double probability = 1 - Math.exp(-K * (round - 1));

        // Generate a random number between 0 and 1, and compare with probability
        return random.nextDouble() < probability;
    }

    // Every time a zombie is spawned it checks if there should be a special zombie spawned instead
    public static boolean checkifSpawnSpecial(int round) {
        // Calculate probability using the formula
        double probability = (0.14 / (1 + Math.exp(-0.1 * (round - 1)))) + 0.01;

        // Generate a random number between 0 and 1 and compare with the calculated probability
        return random.nextDouble() < probability;
    }


    // If a zombie rolls armor this will equip them with the correct armor
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
                }else  {
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
                }else  {
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
                }else  {
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
                }else  {
                    return Material.NETHERITE_HELMET;
                }
            }
        }
        return null; // Return null if no armor should be equipped for that round
    }

    // Get the mob types available
    public static EntityType getMob(int currentRound) {
        List<EntityType> availableMobs = new ArrayList<>();

        // Determine available mobs based on round
        if (currentRound >= 2) availableMobs.add(EntityType.IRON_GOLEM);
        if (currentRound >= 11) availableMobs.add(EntityType.WITCH);
        if (currentRound >= 21) availableMobs.add(EntityType.SILVERFISH);
        if (currentRound >= 31) availableMobs.add(EntityType.PIGLIN);
        if (currentRound >= 41) availableMobs.add(EntityType.BLAZE);

        // Select a random mob from the available ones
        return availableMobs.get(random.nextInt(availableMobs.size()));
    }


    // apply the buffs the special zombies provide based on radius
    private static void applyEffectBasedOnNearbyMob(Zombie zombie) {
        Location zombieLocation = zombie.getLocation();
        double radius = config.getZombieEffectRadius();

        for (Entity entity : zombieLocation.getWorld().getNearbyEntities(zombieLocation, radius, radius, radius)) {
            if (entity instanceof IronGolem) {
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 10, 3)); // IronGolems apply  a 60% damage reduction
                return;
            } else if (entity instanceof Witch) {
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 1)); // Will heal nearby zombies every 20 ticks
                return;
            } else if (entity instanceof Silverfish) {
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1)); // Doubles the speed of zombies
                return;
            } else if (entity instanceof Piglin) {
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 1000, 3)); // Gives nearby zombies 6 temporary HP
                return;
            } else if (entity instanceof Blaze) {
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 18, 1)); // Make them invulnerable until blaze is killed
                return;
            }
        }
    }

    // Called in order to spawn the special or regular zombie
    public static Mob spawnMob(World world, String mapId, int currentRound) {
        // Count for random buffs loop
        int count = 0;

        // First select a random path ID that the mob will follow
        String randomPathId = MapData.getRandomPathId(mapId);

        // Get the starting location for this specific path
        Location spawnPoint = MapData.getWaypoints(world, mapId, randomPathId).getFirst();
        // If there are no waypoints, fall back to default start location
        if (spawnPoint == null) {
            spawnPoint = MapData.getPathStartLocation(world, mapId, String.valueOf(1));
        }

        // Spawn special mobs for difficulty increase
        if (checkifSpawnSpecial(currentRound)) {
            EntityType type = getMob(currentRound); // Get random mob from list that can spawn currently

            Entity entity = world.spawnEntity(spawnPoint, type); // spawn the mob

            if (entity instanceof Mob mob) {
                // Set spawn restrictions
                mob.setAI(false);
                mob.setCustomNameVisible(true);
                if (mob instanceof Ageable ageable) { // ensure no baby piglins
                    ageable.setAdult();
                }

                // Start health bar display
                BukkitTask healthTask = displayHealthBar(mob);
                healthBarTasks.put(mob.getUniqueId(), healthTask);

                // Start path following - pass the specific randomPathId
                BukkitTask movementTask = followPath(mob, world, mapId, randomPathId);
                zombieMovementTasks.put(mob.getUniqueId(), movementTask);
                return mob;
            }
        }

        // Spawn zombie if spawnspecial fails
        Zombie zombie = (Zombie) world.spawnEntity(spawnPoint, EntityType.ZOMBIE);

        // Loop to add buffs on the current zombie that scales with the current round
        while (checkIfBuffed(currentRound)) { // will loop up to 3 times to determine how much armor to give them
            if (count > 3) {break;}
            count++;
            zombieEquip(count, currentRound, zombie);
            checkIfBuffed(currentRound);
        }

        // Set spawn restrictions
        zombie.setShouldBurnInDay(false);
        zombie.setAdult();
        zombie.setAI(false);
        zombie.setCustomNameVisible(true);
        zombie.eject();

        // Start health bar display
        BukkitTask healthTask = displayHealthBar(zombie);
        healthBarTasks.put(zombie.getUniqueId(), healthTask);

        // Start path following - pass the specific randomPathId
        BukkitTask movementTask = followPath(zombie, world, mapId, randomPathId);
        zombieMovementTasks.put(zombie.getUniqueId(), movementTask);

        return zombie;
    }

    // Follow the randomly selected path for the current map
    private static BukkitTask followPath(Mob zombie, World world, String mapId, String initialPathId) {
        // Get waypoints for the initial path
        List<Location> waypoints = MapData.getWaypoints(world, mapId, initialPathId);

        if (waypoints == null || waypoints.isEmpty()) {
            plugin.getLogger().warning("No waypoints found for map " + mapId + " path " + initialPathId);
            return null;
        }

        return new BukkitRunnable() {
            int waypointIndex = 0; // The current waypoint the zombie is at
            final List<Location> currentWaypoints = waypoints; // The waypoints for the current path
            final double baseStepDistance = 0.2; // How far the zombie moves every tick
            final double slownessMultiplier = 0.5; // Reduces speed by half when slowed
            final double speedMultiplier = 2.0;// Doubles speed when applied
            private int tickcounter = 0; // Used to execute some logic at a different rate than the tick rate of run()

            @Override
            public void run() {
                // If zombie is dead or invalid, clean up
                if (zombie == null || zombie.isDead() || !zombie.isValid()) {
                    cancel();
                    zombieMovementTasks.remove(zombie.getUniqueId());
                    return;
                }

                // Checking if zombie is near special zombie and applying buff every 20 ticks
                if(zombie.getType() == EntityType.ZOMBIE & tickcounter % 20 == 0) {
                    applyEffectBasedOnNearbyMob((Zombie) zombie);
                    tickcounter = 0;
                }
                // applying glowing effect to special mobs only every 10 ticks
                else if(zombie.getType() != EntityType.ZOMBIE & tickcounter % 10 == 0){
                    String playerUuidString0 = zombie.getMetadata("gameSession").getFirst().asString(); // Get one of the player's UUID's from the zombie meta
                    UUID playerUUID0 = UUID.fromString(playerUuidString0);
                    if(GlowingTotem.hasGlowingTotem(Bukkit.getPlayer(playerUUID0))) {
                        zombie.addPotionEffect(PotionEffectType.GLOWING.createEffect(40, 6));
                    }
                }


                // Calculate current step distance based on current effect
                double stepDistance = baseStepDistance * (zombie.hasPotionEffect(PotionEffectType.SLOWNESS) ? slownessMultiplier : 1.0);
                stepDistance = stepDistance * (zombie.hasPotionEffect(PotionEffectType.SPEED) ? speedMultiplier : 1.0);

                if(zombie.hasPotionEffect(PotionEffectType.SLOWNESS) && tickcounter % 12 == 0) {
                    // Visual effect of ALL slowness effects
                    zombie.getWorld().playSound(zombie.getLocation(), Sound.BLOCK_HONEY_BLOCK_STEP, 1f, 1f);
                    zombie.getWorld().spawnParticle(
                            Particle.FALLING_HONEY,
                            zombie.getLocation().add(0,1,0),
                            5,
                            0.2, .8, 0.2,
                            0
                    );
                }

                // Check if we're done with the current path - end of path is always the end point
                if (waypointIndex >= currentWaypoints.size()) {
                    // Check if we have the player information to handle game end
                    if (zombie.hasMetadata("gameSession")) {
                        // Get required info to update values
                        String playerUuidString = zombie.getMetadata("gameSession").getFirst().asString(); // Get one of the player's UUID's from the zombie meta
                        UUID playerUUID = UUID.fromString(playerUuidString);
                        List<UUID> listOfPlayers = gameManager.getListOfPlayersInGame(playerUUID); // Get all players in game from one player's UUID

                        for (UUID listOfPlayer : listOfPlayers) { // Do all logic to all players in the game
                            Player currentPlayer = Bukkit.getPlayer(listOfPlayer);
                        if (gameManager.getZombiesPassed(playerUUID) > 9) {
                            // If there have been more than 9 zombies that have passed the player
                            handleGameEnd(currentPlayer, mapId);
                            gameManager.cleanupPlayer(playerUUID);
                            zombieMovementTasks.remove(zombie.getUniqueId());
                            zombie.remove();
                            cancel();
                        }
                        else {
                            // if a zombie passed but the player is still alive
                                currentPlayer.setInvulnerable(false);
                                currentPlayer.damage(2);
                                currentPlayer.setInvulnerable(true);
                                gameManager.setOneZombiesPassed(playerUUID);
                                zombie.setKiller(null); // As of 4/17 it counts this as a player kill still - needs better handling
                                zombie.damage(9999);
                                currentPlayer.sendRichMessage("<red>A zombie has gotten past your defenses!");
                            }
                        }
                    }

                    cancel();
                    zombieMovementTasks.remove(zombie.getUniqueId());
                    return;
                }

                // Get the current target waypoint
                Location target = currentWaypoints.get(waypointIndex);
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

                }

                tickcounter++;
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

    private static void handleGameEnd(Player player, String mapId) {
        // Notify the player
        player.sendRichMessage("<red>GAME OVER! A zombie reached the endpoint!");
        player.sendRichMessage("<red>All your progress has been reset!");

        // Reset player's economy to 0
        Economy.setPlayerMoney(player, 0);

        // Remove all towers for this player's session
        Tower.removeTowersForPlayer(player, mapId);

        // Cancel any active tasks for this player's game
        cancelTasksForPlayer(player.getUniqueId());

        // Remove all zombies from this player's game
        removeZombiesForPlayer(player);

    }


    //Remove all zombies associated with a player's game
    public static void removeZombiesForPlayer(Player player) {
        UUID playerUUID = player.getUniqueId();

        for (Entity entity : player.getWorld().getEntities()) {
            if (entity instanceof Mob && entity.hasMetadata("gameSession")) {
                String sessionId = entity.getMetadata("gameSession").getFirst().asString();
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


    // Cancel all tasks for a player's game
    public static void cancelTasksForPlayer(UUID playerUUID) {
        // Convert UUID to string for comparison
        String playerUuidString = playerUUID.toString();

        // Find and cancel tasks for zombies that belong to this player
        for (Entity entity : Bukkit.getWorlds().getFirst().getEntities()) {
            if (entity instanceof Zombie && entity.hasMetadata("gameSession")) {
                String sessionId = entity.getMetadata("gameSession").getFirst().asString();
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


      // Clean up all resources when the plugin is disabled
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