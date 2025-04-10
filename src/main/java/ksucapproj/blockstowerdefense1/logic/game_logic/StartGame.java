package ksucapproj.blockstowerdefense1.logic.game_logic;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import ksucapproj.blockstowerdefense1.logic.game_logic.towers.Tower;
import ksucapproj.blockstowerdefense1.maps.MapData;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static ksucapproj.blockstowerdefense1.logic.GUI.UpgradeGUI.giveCompass;
import static ksucapproj.blockstowerdefense1.logic.game_logic.Economy.*;


public class StartGame {


    private static PartiesAPI api;


    private final JavaPlugin plugin;
    private final Map<UUID, GameSession> playerSessions = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> healingDisabledMaps = new HashMap<>();


    public boolean isInplayerSessions(UUID uuid) {
        return playerSessions.containsKey(uuid);
    }


    // future class for quick-restarts upon loss
    public void resetPlayerGame(Player player, String mapId) {
    }

    public void setHealingDisabled(String mapId, boolean disabled) {
        healingDisabledMaps.put(mapId, disabled);
    }

    public void setPlayerHealingDisabled(Player player, boolean disabled) {
        player.setMetadata("healing_disabled", new FixedMetadataValue(plugin, disabled));
    }

    public static boolean isHealingDisabled(String mapId) {
        return healingDisabledMaps.getOrDefault(mapId, true);
    }

    public static boolean isPlayerHealingDisabled(Player player) {
        // First check player-specific setting
        if (player.hasMetadata("healing_disabled")) {
            return player.getMetadata("healing_disabled").get(0).asBoolean();
        }

        // Fall back to map setting if player has a map metadata
        if (player.hasMetadata("mapId")) {
            String mapId = player.getMetadata("mapId").get(0).asString();
            return isHealingDisabled(mapId);
        }

        // Default to the global setting
        return healingDisabledMaps.getOrDefault("default", true);
    }

    // Game session class to track per-player game state
    private static class GameSession {
        int currentRound = 1;
        int zombiesPassed = 0;
        float multiplier = 1.25F;
        int zombiesPerRound = 5;
        boolean isReady = false;
        AtomicInteger zombiesKilled = new AtomicInteger(0);
        int totalZombiesThisRound = 0;
        String currentMapId = null;
        BukkitTask spawnTask = null;
        boolean roundInProgress = false;  // Flag to track if a round is currently active
        Set<UUID> activeZombies = new HashSet<>();// Track zombie UUIDs that belong to this session

        GameSession(String mapId) {
            this.currentMapId = mapId; healingDisabledMaps.put("default", true);
        }
    }

    // various getters and setters


    public int getZombiesPassed(UUID playerUUID) {
        GameSession session = playerSessions.get(playerUUID);
        return session.zombiesPassed;
    }

    public void setOneZombiesPassed(UUID playerUUID) {
        GameSession session = playerSessions.get(playerUUID);
        session.zombiesPassed++;
    }

    public void setCurrentRound(UUID playerUUID, int newRound) {
        GameSession session = playerSessions.get(playerUUID);
        session.currentRound = newRound;
        setZombiesPerRound(playerUUID);
    }


    public void removeZombie(UUID zombieId, UUID playerUUID) {
        GameSession session = playerSessions.get(playerUUID);
        session.activeZombies.remove(zombieId);
    }

    public void setZombiesPerRound(UUID playerUUID) {
        GameSession session = playerSessions.get(playerUUID);
        session.zombiesPerRound = session.currentRound * 5; // increases by 5 every round
    }

    public void setRoundInProgress(UUID playerUUID, boolean bool) {
        GameSession session = playerSessions.get(playerUUID);
        session.roundInProgress = bool;
    }

    public int incrAndGetZombiesKilled(UUID playerUUID) {
        GameSession session = playerSessions.get(playerUUID);
        return session.zombiesKilled.incrementAndGet();
    }

    public  int getZombiesThisRound(UUID playerUUID) {
        GameSession session = playerSessions.get(playerUUID);
        return session.totalZombiesThisRound;
    }

    public void setIsReady(UUID playerUUID, boolean bool) {
        GameSession session = playerSessions.get(playerUUID);
        session.isReady = bool;
    }

    public int getCurrentRound(UUID playerUUID) {
        GameSession session = playerSessions.get(playerUUID);
        return session.currentRound;
    }

    public void setCurrentRound(int currentRound, UUID playerUUID) {
        GameSession session = playerSessions.get(playerUUID);
        session.currentRound = currentRound;
    }

    public StartGame(JavaPlugin plugin, PartiesAPI api) {
        this.plugin = plugin;
        StartGame.api = api;
    }

    // public access for starting game and ensuring map and player exist
    public void startGames(Player player, String mapId) {
            // Verify the map exists asynchronously
            CompletableFuture.supplyAsync(() -> MapData.mapExists(mapId))
                    .thenAccept(exists -> {
                        if (!exists) {
                            // Get available maps asynchronously for error message
                            CompletableFuture.supplyAsync(MapData::getAvailableMaps)
                                    .thenAccept(maps -> Bukkit.getScheduler().runTask(plugin, () -> {
                                        player.sendMessage(ChatColor.RED + "Map '" + mapId + "' does not exist!");
                                        player.sendMessage(ChatColor.YELLOW + "Available maps: " + String.join(", ", maps));
                                    }));
                            return;
                        }

                        // If map exists, continue setup on the main thread
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            handleStartGameCommand(player, mapId);
                        });
                    });
    }

    // main method call for starting a game
    private void handleStartGameCommand(Player player, String mapId) {
        new Economy();
        player.setHealth(20);

        PartyPlayer partyPlayer = api.getPartyPlayer(player.getUniqueId());

        if (api.getPartyOfPlayer(player.getUniqueId()) == null){
            player.sendRichMessage("<red>User is not in a party, making one for game creation.");

            api.createParty(player.getName(), partyPlayer);
        }

        Party party = api.getParty(partyPlayer.getPartyId());


        // Clean up any existing game session first
        if (playerSessions.containsKey(player.getUniqueId())) {
            cleanupPlayer(player.getUniqueId());
        }

        // Initialize game session
        GameSession session = new GameSession(mapId);


        // Get the world
        World world = player.getWorld();

        // Get the start location for the specified map
        Location startLocation = MapData.getPathStartLocation(world, mapId, String.valueOf(1));

        //player.sendMessage(party.getOnlineMembers().toString());
        int count = party.getOnlineMembers().size();

        for (PartyPlayer partyMember : party.getOnlineMembers()){
            Player currentPlayer = Bukkit.getPlayer(partyMember.getPlayerUUID());


            playerSessions.put(currentPlayer.getUniqueId(), session);


            // Add player to economy system
            playerJoin(currentPlayer);

            // Clear player's inventory first
            currentPlayer.getInventory().clear();

            // Add items for gameplay
            if(count == 0) {
                break;
            }
            if(count == 1)  {
                PlayerUpgrades.getPlayerUpgradesMap().put(currentPlayer, new PlayerUpgrades(currentPlayer));
//                count--;
            }
            if(count == 2) {
                PlayerUpgrades.getPlayerUpgradesMap().put(currentPlayer, new PlayerUpgrades(currentPlayer));
                count--;
            }


            currentPlayer.teleport(startLocation);


            giveCompass(currentPlayer);

            currentPlayer.sendMessage(ChatColor.GREEN + "Game started on map: " + ChatColor.YELLOW + mapId);
            currentPlayer.sendMessage(ChatColor.GREEN + "Type /readyup to start the first round!");

        }

    }


    public void handleReadyUpCommand(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!playerSessions.containsKey(playerUUID)) {
            player.sendMessage(ChatColor.RED + "You must start a game with /startgame <map> first!");
            return;
        }

        GameSession session = playerSessions.get(playerUUID);

        // Check if a round is already in progress
        if (session.roundInProgress) {
            player.sendMessage(ChatColor.RED + "A round is already in progress! Please wait until it finishes.");
            return;
        }

        session.isReady = true;

        // Start round immediately since we're already on the main thread
        startRound(player);
    }

    private void startRound(Player player) {
        UUID playerUUID = player.getUniqueId();
        GameSession session = playerSessions.get(playerUUID);

        if (!session.isReady) {
            player.sendMessage(ChatColor.RED + "Use /readyup first!");
            return;
        }

        // Set the round in progress flag
        session.roundInProgress = true;

        player.sendMessage(ChatColor.GREEN + "Starting Round " + session.currentRound +
                " (" + session.zombiesPerRound + " zombies)");
        World world = player.getWorld();

        session.totalZombiesThisRound = session.zombiesPerRound;
        session.zombiesKilled.set(0);

        // Cancel any existing spawn task
        if (session.spawnTask != null && !session.spawnTask.isCancelled()) {
            session.spawnTask.cancel();
        }

        // Create a new spawn task
        session.spawnTask = new BukkitRunnable() {
            int spawned = 0;

            @Override
            public void run() {
                if (spawned >= session.totalZombiesThisRound) {
                    this.cancel();
                    return;
                }

                // Spawn mob on main thread and track it
                Mob zombie = MobHandler.spawnMob(world, session.currentMapId, session.currentRound);
                if (zombie != null) {
                    // Tag the zombie with metadata to associate it with this game session
                    zombie.setMetadata("gameSession", new FixedMetadataValue(plugin, playerUUID.toString()));
                    // Also track the zombie in our session
                    session.activeZombies.add(zombie.getUniqueId());
                }
                spawned++;
            }
        }.runTaskTimer(plugin, 0, 10); // 500ms interval (10 ticks)
    }


      //Removes all zombies that are part of a specific game session.

    private void removeGameZombies(World world, GameSession session, UUID playerUUID) {
        // Remove zombies tracked in the session
        for (UUID zombieUUID : session.activeZombies) {
            Entity entity = Bukkit.getEntity(zombieUUID);
            if (entity != null) {
                entity.remove();
            }
        }

        // Ensure we catch any zombies that might not be in the tracking set
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Mob && entity.hasMetadata("gameSession")) {
                String sessionId = entity.getMetadata("gameSession").getFirst().asString();
                if (sessionId.equals(playerUUID.toString())) {
                    entity.remove();
                }
            }
        }
        // Clear our tracking set
        session.activeZombies.clear();
    }

    // cleans up player data
    public void cleanupPlayer(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null || !playerSessions.containsKey(playerUUID)) {
            return; // No cleanup needed
        }

        GameSession session = playerSessions.get(playerUUID);
        String mapId = getPlayerMapId(playerUUID);

        // Cancel all scheduled tasks
        if (session.spawnTask != null) {
            session.spawnTask.cancel();
            session.spawnTask = null;
        }
        MobHandler.cancelTasksForPlayer(playerUUID);
        Tower.cancelTasksForPlayer(playerUUID);

        // Remove all entities
        removeGameZombies(player.getWorld(), session, playerUUID);
        Tower.removeTowersForPlayer(player, mapId);

        // Clean up player data
        PlayerUpgrades.playerDelete(player); // This also removes from PlayerUpgrades map

        // Clear inventory
        player.getInventory().clear();

        // Remove session tracking
        playerSessions.remove(playerUUID);

        plugin.getLogger().info("Game session cleaned up for player " + player.getName());
    }

    public boolean isPlayerInGame(UUID playerUUID) {
        return playerSessions.containsKey(playerUUID);
    }


//      Gets the map ID for a player's current game session.
//      Used by the cleanup process to identify which zombies to remove.

    public String getPlayerMapId(UUID playerUUID) {
        GameSession session = playerSessions.get(playerUUID);
        return session != null ? session.currentMapId : null;
    }


    public void roundEndMoney(UUID playerUUID){
        // finds the player's game session
        GameSession session = playerSessions.get(playerUUID);
        // finds the player's created economy
        Economy econ = getPlayerEconomies().get(Bukkit.getPlayer(playerUUID));
        // gives a round bonus based upon what round they are on
        econ.addMoneyOnRoundEnd(session.currentRound);
    }


}