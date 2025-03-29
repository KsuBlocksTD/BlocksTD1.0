package ksucapproj.blockstowerdefense1.logic.game_logic;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import ksucapproj.blockstowerdefense1.logic.game_logic.towers.TowerFactory;
import ksucapproj.blockstowerdefense1.maps.MapData;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class StartGame implements CommandExecutor, Listener {


    private static PartiesAPI api;


    private final JavaPlugin plugin;
    private final Map<UUID, GameSession> playerSessions = new ConcurrentHashMap<>();

    public void resetPlayerGame(Player player, String mapId) {
    }

    // Game session class to track per-player game state
    private static class GameSession {
        int currentRound = 1;
        int zombiesPerRound = 5;
        boolean isReady = false;
        AtomicInteger zombiesKilled = new AtomicInteger(0);
        int totalZombiesThisRound = 0;
        String currentMapId = null;
        BukkitTask spawnTask = null;
        boolean roundInProgress = false;  // Flag to track if a round is currently active
        Set<UUID> activeZombies = new HashSet<>();// Track zombie UUIDs that belong to this session

        GameSession(String mapId) {
            this.currentMapId = mapId;
        }
    }

    public StartGame(JavaPlugin plugin, PartiesAPI api) {
        this.plugin = plugin;
        StartGame.api = api;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }


        if (command.getName().equalsIgnoreCase("startgame")) {
            // Handle map lookup asynchronously but game setup on main thread
            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "You must specify a map!");
                player.sendMessage(ChatColor.YELLOW + "Usage: /startgame <map>");

                // Get available maps asynchronously
                CompletableFuture.supplyAsync(MapData::getAvailableMaps)
                        .thenAccept(maps -> Bukkit.getScheduler().runTask(plugin, () ->
                                player.sendMessage(ChatColor.YELLOW + "Available maps: " + String.join(", ", maps))));
                return true;
            }

            // Get the specified map
            String mapId = args[0];

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
        else if (command.getName().equalsIgnoreCase("readyup")) {
            // Handle readyup entirely on the main thread
            handleReadyUpCommand(player);
        }

        return true;
    }

    private void handleStartGameCommand(Player player, String mapId) {

        PartyPlayer partyPlayer = api.getPartyPlayer(player.getUniqueId());

        if (api.getPartyOfPlayer(player.getUniqueId()) == null){
            player.sendRichMessage("<red>User is not in a party, making one for game creation.");

            api.createParty(player.getName(), partyPlayer);
        }

//        PlayerUpgrades Player1;
//        PlayerUpgrades Player2;

        Party party = api.getParty(partyPlayer.getPartyId());


        // Clean up any existing game session first
        if (playerSessions.containsKey(player.getUniqueId())) {
            handlePlayerQuit(player);
        }

        // Basic Tower
        ItemStack basicTowerEgg = createTowerEgg("Basic Tower", ChatColor.AQUA,
                "A simple tower with moderate damage and range");

        // Future tower placeholders
        ItemStack fastTowerEgg = createTowerEgg("Fast Tower", ChatColor.GREEN,
                "Rapid-fire tower with low damage but high attack speed");

        ItemStack sniperTowerEgg = createTowerEgg("Sniper Tower", ChatColor.RED,
                "Long-range tower with high damage but slow attack speed");

        ItemStack splashTowerEgg = createTowerEgg("Splash Tower", ChatColor.YELLOW,
                "Area-of-effect tower that damages multiple enemies");

        ItemStack slowTowerEgg = createTowerEgg("Slow Tower", ChatColor.BLUE,
                "Tower that slows down enemies in its range");


        // Initialize game session
        GameSession session = new GameSession(mapId);


        // Get the world
        World world = player.getWorld();

        // Get the start location for the specified map
        Location startLocation = MapData.getStartLocation(world, mapId);

        //player.sendMessage(party.getOnlineMembers().toString());
        int count = party.getOnlineMembers().size();

        for (PartyPlayer partyMember : party.getOnlineMembers()){
            Player currentPlayer = Bukkit.getPlayer(partyMember.getPlayerUUID());







            playerSessions.put(currentPlayer.getUniqueId(), session);


            // Add player to economy system
            Economy.playerJoin(currentPlayer);

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

            // Give player tower items
            currentPlayer.getInventory().addItem(basicTowerEgg);
            currentPlayer.getInventory().addItem(fastTowerEgg);
            currentPlayer.getInventory().addItem(sniperTowerEgg);
            currentPlayer.getInventory().addItem(splashTowerEgg);
            currentPlayer.getInventory().addItem(slowTowerEgg);



            currentPlayer.sendMessage(ChatColor.GREEN + "Game started on map: " + ChatColor.YELLOW + mapId);
            currentPlayer.sendMessage(ChatColor.GREEN + "Type /readyup to start the first round!");

        }

    }


    //Helper method to create tower spawn eggs with proper metadata

    private ItemStack createTowerEgg(String name, ChatColor color, String description) {
        ItemStack towerEgg = new ItemStack(Material.ZOMBIE_SPAWN_EGG, 5);
        ItemMeta meta = towerEgg.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color + name);

            // Add lore with description and price
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + description);
            lore.add(ChatColor.YELLOW + "Cost: " + ChatColor.GOLD + "500 coins");
            meta.setLore(lore);

            towerEgg.setItemMeta(meta);
        }
        return towerEgg;
    }

    private void handleReadyUpCommand(Player player) {
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
                Zombie zombie = MobHandler.spawnMob(world, session.currentMapId);
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

    @EventHandler
    public void onPlayerUseEgg(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if this is a valid tower placement attempt
        if (item != null && item.getType() == Material.ZOMBIE_SPAWN_EGG && item.getItemMeta() != null) {
            String itemName = item.getItemMeta().getDisplayName();

            // Check if it's one of our tower spawn eggs
            if (itemName.startsWith("§")) {  // Check for color codes
                // Cancel the event to prevent default zombie spawning
                event.setCancelled(true);

                // Check if player is in a game
                UUID playerUUID = player.getUniqueId();
                if (!playerSessions.containsKey(playerUUID)) {
                    player.sendMessage(ChatColor.RED + "You must start a game first!");
                    return;
                }

                GameSession session = playerSessions.get(playerUUID);
                String mapId = session.currentMapId;




                if (itemName.equals("§aFast Tower")) {
                    TowerFactory.placeTower(
                            TowerFactory.TowerType.FAST,
                            player,
                            event.getInteractionPoint(),
                            mapId,
                            plugin,
                            item
                    );
                }
                if (itemName.equals("§bBasic Tower")) {
                    TowerFactory.placeTower(
                            TowerFactory.TowerType.BASIC,
                            player,
                            event.getInteractionPoint(),
                            mapId,
                            plugin,
                            item
                    );
                }
                if (itemName.equals("§cSniper Tower")) {
                    TowerFactory.placeTower(
                            TowerFactory.TowerType.SNIPER,
                            player,
                            event.getInteractionPoint(),
                            mapId,
                            plugin,
                            item
                    );
                }
                if (itemName.equals("§eSplash Tower")) {
                    TowerFactory.placeTower(
                            TowerFactory.TowerType.SPLASH,
                            player,
                            event.getInteractionPoint(),
                            mapId,
                            plugin,
                            item
                    );
                }
                if (itemName.equals("§9Slow Tower")) {
                    TowerFactory.placeTower(
                            TowerFactory.TowerType.SLOW,
                            player,
                            event.getInteractionPoint(),
                            mapId,
                            plugin,
                            item

                    );
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Zombie)) {
            return;
        }

        Zombie zombie = (Zombie) event.getEntity();


        if (!zombie.hasMetadata("gameSession")) {
            return;
        }

        String gameSessionId = zombie.getMetadata("gameSession").get(0).asString();
        UUID playerUUID = UUID.fromString(gameSessionId);

        // Make sure this player still has an active session
        if (!playerSessions.containsKey(playerUUID)) {
            return;
        }

        GameSession session = playerSessions.get(playerUUID);

        // Remove this zombie from tracking
        session.activeZombies.remove(zombie.getUniqueId());

        int killed = session.zombiesKilled.incrementAndGet();

        if (killed >= session.totalZombiesThisRound) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null && player.isOnline()) {
                // Process round completion
                session.currentRound++;
                session.zombiesPerRound += (session.currentRound <= 10) ? 7 : 10;
                session.isReady = false;

                // Set round as no longer in progress
                session.roundInProgress = false;

                Bukkit.broadcastMessage(ChatColor.GOLD + "Round " + (session.currentRound - 1) + " completed!");
                Bukkit.broadcastMessage(ChatColor.GREEN + "Type /readyup for Round " + session.currentRound);
            }
        }
    }

    /**
     * Gets the map ID for a player's current game session.
     * Used by the cleanup process to identify which zombies to remove.
     */
    public String getPlayerMapId(UUID playerUUID) {
        GameSession session = playerSessions.get(playerUUID);
        return session != null ? session.currentMapId : null;
    }

    /**
     * Method to clean up resources when a player quits.
     * This includes canceling tasks and removing all entities associated with the game.
     */
    public void handlePlayerQuit(Player player) {
        UUID playerUUID = player.getUniqueId();
        GameSession session = playerSessions.get(playerUUID);

        if (session == null) {
            return; // No cleanup needed
        }

        // Cancel any ongoing spawn task
        if (session.spawnTask != null && !session.spawnTask.isCancelled()) {
            session.spawnTask.cancel();
        }

        // Remove any zombies that are part of this game session
        removeGameZombies(player.getWorld(), session, playerUUID);

        // Remove the session from our tracking
        playerSessions.remove(playerUUID);
        // Deletes player and their sword
        PlayerUpgrades.playerDelete(player);
        Economy.playerLeave(player);

        for (ItemStack item : player.getInventory().getContents()){
            if (item == null) {
                continue;
            }
            player.getInventory().remove(item);
        }

        plugin.getLogger().info("Game session cleaned up for player " + player.getName());
    }

    /**
     * Removes all zombies that are part of a specific game session.
     */
    private void removeGameZombies(World world, GameSession session, UUID playerUUID) {

        for (UUID zombieUUID : session.activeZombies) {
            Entity entity = Bukkit.getEntity(zombieUUID);
            if (entity != null) {
                entity.remove();
            }
        }


        for (Entity entity : world.getEntities()) {
            if (entity instanceof Zombie && entity.hasMetadata("gameSession")) {
                String sessionId = entity.getMetadata("gameSession").get(0).asString();
                if (sessionId.equals(playerUUID.toString())) {
                    entity.remove();
                }
            }
        }

        // Clear our tracking set
        session.activeZombies.clear();
    }

    public boolean isPlayerInGame(UUID playerUUID) {
        return playerSessions.containsKey(playerUUID);
    }


    public void cancelTasks(UUID playerUUID) {
        GameSession session = playerSessions.get(playerUUID);
        if (session != null && session.spawnTask != null) {
            session.spawnTask.cancel();
            session.spawnTask = null;
        }
    }


}