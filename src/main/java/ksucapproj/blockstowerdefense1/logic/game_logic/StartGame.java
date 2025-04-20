package ksucapproj.blockstowerdefense1.logic.game_logic;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import ksucapproj.blockstowerdefense1.logic.DatabaseManager;
import ksucapproj.blockstowerdefense1.logic.GUI.StartGameGUI;
import ksucapproj.blockstowerdefense1.logic.game_logic.towers.Tower;
import ksucapproj.blockstowerdefense1.maps.MapData;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static ksucapproj.blockstowerdefense1.commands.mtd.HubCommand.getHubFromConfig;
import static ksucapproj.blockstowerdefense1.logic.GUI.UpgradeGUI.giveCompass;
import static ksucapproj.blockstowerdefense1.logic.game_logic.Economy.getPlayerEconomies;
import static ksucapproj.blockstowerdefense1.logic.game_logic.Economy.playerJoin;

public class StartGame {
    private static PartiesAPI api;
    private final JavaPlugin plugin;
    private final Map<Set<UUID>, GameSession> playerSessions = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> healingDisabledMaps = new HashMap<>();
    private static final Location hubSpawn = getHubFromConfig();

    private Set<UUID> getSetFromPlayer(UUID playerUUID) {
        Set<UUID> Partyuuid = new HashSet<>();
        if(api.getPartyPlayer(playerUUID) == null){
            return Partyuuid;
        }
        PartyPlayer partyPlayer = api.getPartyPlayer(playerUUID);
        if(api.getParty(partyPlayer.getPartyId()) == null) {
            return Partyuuid;
        }
        Party party = api.getParty(partyPlayer.getPartyId());

        for (PartyPlayer partyMember : party.getOnlineMembers()){
            Partyuuid.add(partyMember.getPlayerUUID());
        }
        return Partyuuid;
    }

    public boolean isInplayerSessions(UUID playerUUID) {
        Set<UUID> Partyuuid = getSetFromPlayer(playerUUID);
        return playerSessions.containsKey(Partyuuid);
    }

    public void setPlayerHealingDisabled(Player player, boolean disabled) {
        player.setMetadata("healing_disabled", new FixedMetadataValue(plugin, disabled));
    }

    public static boolean isHealingDisabled(String mapId) {
        return healingDisabledMaps.getOrDefault(mapId, false);
    }

    public static boolean isPlayerHealingDisabled(Player player) {
        // First check player-specific setting
        if (player.hasMetadata("healing_disabled")) {
            return player.getMetadata("healing_disabled").getFirst().asBoolean();
        }

        // Default to the global setting
        return healingDisabledMaps.getOrDefault("default", false);
    }

    public StartGame(JavaPlugin plugin, PartiesAPI api) {
        this.plugin = plugin;
        StartGame.api = api;
    }

    // salvaged on player join event into regular method
    public void onPlayerStartGame(Player player){


        player.setHealth(20);
        player.setSaturation(999999999);
        player.setGameMode(GameMode.ADVENTURE);
        player.setInvulnerable(true);


//        // checks if msg on join is enabled
//        // if so, send player the specified message
//        if (config.getMOTDOnPlayerJoin() != null){
//            player.sendMessage(config.getMOTDOnPlayerJoin());
//        }

        // this will eventually be the default greeting on player join
//        event.getPlayer().sendMessage("Welcome to the server, " + event.getPlayer().getName() + ".");

        // this checks if a player is in the db already, if not, adds them to it
        DatabaseManager.checkPlayerInDB(player, 2);

    }

    public void startGames(Player player, String mapId) {
        CompletableFuture.supplyAsync(() -> MapData.mapExists(mapId))
                .thenAccept(exists -> {
                    if (!exists) {
                        CompletableFuture.supplyAsync(MapData::getAvailableMaps)
                                .thenAccept(maps -> Bukkit.getScheduler().runTask(plugin, () -> {
                                    player.sendMessage(ChatColor.RED + "Map '" + mapId + "' does not exist!");
                                    player.sendMessage(ChatColor.YELLOW + "Available maps: " + String.join(", ", maps));
                                }));
                        return;
                    }

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        handleStartGameCommand(player, mapId);
                    });
                });
    }

    private void handleStartGameCommand(Player player, String mapId) {
        new Economy();

        PartyPlayer partyPlayer = api.getPartyPlayer(player.getUniqueId());

        if (api.getPartyOfPlayer(player.getUniqueId()) == null) {
            player.sendRichMessage("<red>User is not in a party, making one for game creation.");
            api.createParty(player.getName(), partyPlayer);
        }

        Party party = api.getParty(partyPlayer.getPartyId());

        if (playerSessions.containsKey(getSetFromPlayer(player.getUniqueId()))) {
            cleanupPlayer(player.getUniqueId()); // make sure this works for both players
        }

        GameSession session = new GameSession(mapId);

        Set<UUID> partyUUIDs = getSetFromPlayer(player.getUniqueId());
        playerSessions.put(partyUUIDs, session);
        session.players.addAll(partyUUIDs);

        World world = player.getWorld();
        Location startLocation = MapData.getPathStartLocation(world, mapId, String.valueOf(1));

        for (UUID uuid : partyUUIDs) {
            Player currentPlayer = Bukkit.getPlayer(uuid);
            if (currentPlayer == null) continue;


            setPlayerHealingDisabled(currentPlayer, true);
            onPlayerStartGame(currentPlayer);
            playerJoin(currentPlayer);
            currentPlayer.getInventory().clear();
            PlayerUpgrades.getPlayerUpgradesMap().put(currentPlayer, new PlayerUpgrades(currentPlayer));
            currentPlayer.teleport(startLocation);
            giveCompass(currentPlayer);

            currentPlayer.sendMessage(ChatColor.GREEN + "Game started on map: " + ChatColor.YELLOW + mapId);
            currentPlayer.sendMessage(ChatColor.GREEN + "Type /readyup to start the first round!");
        }
    }

    public void handleReadyUpCommand(Player player) {
        UUID playerUUID = player.getUniqueId();
        Set<UUID> key = getSetFromPlayer(playerUUID);

        if (!playerSessions.containsKey(key)) {
            player.sendMessage(ChatColor.RED + "You must start a game with /startgame <map> first!");
            return;
        }

        GameSession session = playerSessions.get(key);

        if (session.roundInProgress) {
            player.sendMessage(ChatColor.RED + "A round is already in progress! Please wait until it finishes.");
            return;
        }

        session.readyPlayers.add(playerUUID);

        if (session.readyPlayers.containsAll(session.players)) {
            for (UUID uuid : session.players) {
                Player partyMember = Bukkit.getPlayer(uuid);
                if (partyMember != null) {
                    startRound(partyMember);
                }
            }
        } else {
            player.sendMessage(ChatColor.YELLOW + "Waiting for other players to ready up...");
        }
    }

    private void startRound(Player initiatingPlayer) {
        UUID initiatingUUID = initiatingPlayer.getUniqueId();
        Set<UUID> partyUUIDs = getSetFromPlayer(initiatingUUID);

        GameSession session = playerSessions.get(partyUUIDs);
        if (session.roundInProgress) return;

        session.roundInProgress = true;
        session.totalZombiesThisRound = session.zombiesPerRound = session.currentRound * 5;
        session.zombiesKilled.set(0);

        for (UUID uuid : partyUUIDs) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(ChatColor.GREEN + "Starting Round " + session.currentRound +
                        " (" + session.zombiesPerRound + " zombies)");
            }
        }

        World world = initiatingPlayer.getWorld();

        if (session.spawnTask != null && !session.spawnTask.isCancelled()) {
            session.spawnTask.cancel();
        }

        session.spawnTask = new BukkitRunnable() {
            int spawned = 0;

            @Override
            public void run() {
                if (spawned >= session.totalZombiesThisRound) {
                    this.cancel();
                    return;
                }

                List<UUID> partyUUID = session.players;
                Player player = Bukkit.getPlayer(initiatingUUID);
                    if (player != null) {
                        Mob zombie = MobHandler.spawnMob(world, session.currentMapId, session.currentRound);
                        if (zombie != null) {
                            for(UUID uuid : partyUUID) {
                                // Tie mob to game session and attacker
                                zombie.setMetadata("gameSession", new FixedMetadataValue(plugin, uuid));
                                zombie.setMetadata("attacker", new FixedMetadataValue(plugin, uuid));
                            }
                            session.activeZombies.add(zombie.getUniqueId());
                        }
                    }

                spawned++;
            }
        }.runTaskTimer(plugin, 0, 10);
    }


    private static class GameSession {
        int currentRound = 1;
        int zombiesPassed = 0;
        float multiplier = 1.25F;
        int zombiesPerRound = 5;
        boolean isReady = false;
        AtomicInteger zombiesKilled = new AtomicInteger(0);
        int totalZombiesThisRound = 0;
        String currentMapId;
        BukkitTask spawnTask = null;
        boolean roundInProgress = false;
        Set<UUID> activeZombies = new HashSet<>();
        List<UUID> players = new ArrayList<>();
        Set<UUID> readyPlayers = new HashSet<>();

        GameSession(String mapId) {
            this.currentMapId = mapId;
            healingDisabledMaps.put("default", true);
        }
    }


    public void checkIfEndRoundMessage(int round, Player player) {
        String message = null;
        Set<UUID> partyUUIDs = getSetFromPlayer(player.getUniqueId());

        switch (round) {
            case 2 -> message = "Watch out for those Iron Golems... they seem to be be shielding the zombies!";
            case 11 -> message = "Witches inbound! I heard that they heal the undead!";
            case 21 -> message = "Watch the floors! Silverfish are slippery buggers.";
            case 31 -> message = "Piglins are certainly a morale boost for the enemy.";
            case 41 -> message = "Be weary of Blazes! Towers seem to hate them.";
        }

        if (message != null) {
            for (UUID uuid : partyUUIDs) {
                Player currentPlayer = Bukkit.getPlayer(uuid);
                if (currentPlayer == null) continue;
                currentPlayer.sendRichMessage("=========================");
                currentPlayer.sendRichMessage("<yellow>" + message);
            }
        }
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

    public void removePlayerSession(UUID playerUUID) {
        // Remove session tracking
        playerSessions.remove(getSetFromPlayer(playerUUID));
    }

    // cleans up player data
    public void cleanupPlayer(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null || !playerSessions.containsKey(getSetFromPlayer(playerUUID))) {
            return; // No cleanup needed
        }

        GameSession session = playerSessions.get(getSetFromPlayer(playerUUID));
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

        setPlayerHealingDisabled(player, false);

        player.teleport(hubSpawn);
        if(!StartGameGUI.hasCompass(player)) {
            StartGameGUI.giveMapSelectorCompass(player);
        }

        //plugin.getLogger().info("Game session cleaned up for player " + player.getName());
    }

    public boolean isPlayerInGame(UUID playerUUID) {
        return playerSessions.containsKey(getSetFromPlayer(playerUUID));
    }


//      Gets the map ID for a player's current game session.
//      Used by the cleanup process to identify which zombies to remove.

    public String getPlayerMapId(UUID playerUUID) {
        GameSession session = playerSessions.get(getSetFromPlayer(playerUUID));
        return session != null ? session.currentMapId : null;
    }


    public void roundEndMoney(UUID playerUUID){
        // finds the player's game session
        GameSession session = playerSessions.get(getSetFromPlayer(playerUUID));
        // finds the player's created economy
        Economy econ = getPlayerEconomies().get(Bukkit.getPlayer(playerUUID));
        // gives a round bonus based upon what round they are on
        econ.addMoneyOnRoundEnd(session.currentRound);
    }

    public void gameEndStatus(UUID playerUUID, boolean victory){
        Player player = Bukkit.getPlayer(playerUUID);
        if(player == null) {
            return;
        }


        String msg;
        if (victory) {
            player.sendRichMessage("<light_purple>Congratulations! You won!");
            spawnFirework(player);

        }
        else {
            player.sendRichMessage("<light_purple>You lost! Game over..");
        }
        if (PlayerUpgrades.getPlayerUpgradesMap().get(player) == null) {
            return;
        }
        DatabaseManager.updatePlayerData(PlayerUpgrades.getPlayerUpgradesMap().get(player), victory, 3);

        cleanupPlayer(playerUUID);
    }

    public void spawnFirework(Player player) {
        Location loc = player.getLocation(); // player’s feet
        World world = player.getWorld();

        Firework firework = world.spawn(loc, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();

        meta.addEffect(FireworkEffect.builder()
                .withColor(Color.AQUA)
                .withFade(Color.BLUE)
                .with(FireworkEffect.Type.BALL_LARGE)
                .flicker(true)
                .trail(true)
                .build());

        meta.setPower(0); // power 0 = instant-ish
        firework.setFireworkMeta(meta);
    }

// various getters and setters

    public List<UUID> getListOfPlayersInGame(UUID playerUUID) {
        GameSession session = playerSessions.get(getSetFromPlayer(playerUUID));
        return session.players;
    }

    public boolean getRoundInProgress(UUID playerUUID) {
        GameSession session = playerSessions.get(getSetFromPlayer(playerUUID));
        return session.roundInProgress;
    }


    public int getZombiesPassed(UUID playerUUID) {
        GameSession session = playerSessions.get(getSetFromPlayer(playerUUID));
        return session.zombiesPassed;
    }

    public void setOneZombiesPassed(UUID playerUUID) {
        GameSession session = playerSessions.get(getSetFromPlayer(playerUUID));
        session.zombiesPassed++;
    }

    public void setCurrentRound(UUID playerUUID, int newRound) {
        GameSession session = playerSessions.get(getSetFromPlayer(playerUUID));
        session.currentRound = newRound;
        setZombiesPerRound(playerUUID);
    }


    public void removeZombie(UUID zombieId, UUID playerUUID) {
        GameSession session = playerSessions.get(getSetFromPlayer(playerUUID));
        session.activeZombies.remove(zombieId);
    }

    public void setZombiesPerRound(UUID playerUUID) {
        GameSession session = playerSessions.get(getSetFromPlayer(playerUUID));
        session.zombiesPerRound = session.currentRound * 5; // increases by 5 every round
    }

    public void setRoundInProgress(UUID playerUUID, boolean bool) {
        GameSession session = playerSessions.get(getSetFromPlayer(playerUUID));
        session.roundInProgress = bool;
    }

    public int incrAndGetZombiesKilled(UUID playerUUID) {
        GameSession session = playerSessions.get(getSetFromPlayer(playerUUID));
        return session.zombiesKilled.incrementAndGet();
    }

    public  int getZombiesThisRound(UUID playerUUID) {
        GameSession session = playerSessions.get(getSetFromPlayer(playerUUID));
        return session.totalZombiesThisRound;
    }

    public void setIsReady(UUID playerUUID, boolean bool) {
        GameSession session = playerSessions.get(getSetFromPlayer(playerUUID));
        session.isReady = bool;
    }

    public int getCurrentRound(UUID playerUUID) {
        GameSession session = playerSessions.get(getSetFromPlayer(playerUUID));
        return session.currentRound;
    }

    public void setCurrentRound(int currentRound, UUID playerUUID) {
        GameSession session = playerSessions.get(getSetFromPlayer(playerUUID));
        session.currentRound = currentRound;
    }


    }

