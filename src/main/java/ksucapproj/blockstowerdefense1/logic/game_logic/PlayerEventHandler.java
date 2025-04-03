package ksucapproj.blockstowerdefense1.logic.game_logic;


import com.alessiodp.parties.api.events.bukkit.party.BukkitPartiesPartyPostCreateEvent;
import com.alessiodp.parties.api.events.bukkit.party.BukkitPartiesPartyPostDeleteEvent;
import com.alessiodp.parties.api.events.bukkit.party.BukkitPartiesPartyPreCreateEvent;
import com.alessiodp.parties.api.events.bukkit.player.BukkitPartiesPlayerPostInviteEvent;
import com.alessiodp.parties.api.events.bukkit.player.BukkitPartiesPlayerPreInviteEvent;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.ConfigOptions;
import ksucapproj.blockstowerdefense1.logic.DatabaseManager;
import ksucapproj.blockstowerdefense1.logic.GUI.UpgradeGUI;
import ksucapproj.blockstowerdefense1.logic.game_logic.towers.Tower;
import ksucapproj.blockstowerdefense1.logic.game_logic.towers.TowerFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public class PlayerEventHandler implements Listener {
    private final JavaPlugin plugin;
    private final StartGame gameManager;

    public static final PartiesAPI api = BlocksTowerDefense1.getApi();
    ConfigOptions config = BlocksTowerDefense1.getInstance().getBTDConfig();

    public PlayerEventHandler(JavaPlugin plugin, StartGame gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        // activates the player join event for economy
        Player player = event.getPlayer();

        int playerCount = Bukkit.getOnlinePlayers().size();

        // checks if msg on join is enabled
        // if so, send player the specified message
        if (config.getMOTDOnPlayerJoin() != null){
            player.sendMessage(config.getMOTDOnPlayerJoin());
        }

        // this will eventually be the default greeting on player join
        event.getPlayer().sendMessage("Welcome to the server, " + event.getPlayer().getName() + ".");

        // this checks if a player is in the db already, if not, adds them to it
        DatabaseManager.checkPlayerInDB(player);

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        PartyPlayer partyPlayer = api.getPartyPlayer(player.getUniqueId());

        if (partyPlayer.isInParty()){
            Party party = api.getParty(partyPlayer.getPartyId());
            if (party.getLeader() == partyPlayer.getPlayerUUID()){
                party.delete();
            }
            else {
                party.removeMember(partyPlayer);
            }
        }

        // Check if the player is in a game
        if (gameManager.isPlayerInGame(playerUUID)) {
            // Get the player's game data before cleanup
            String mapId = gameManager.getPlayerMapId(playerUUID);


            // Cancel zombie spawning tasks
            gameManager.cancelTasks(playerUUID);

            // Cancel end-point detection tasks
            MobHandler.cancelTasksForPlayer(playerUUID);

            // Cancel tower attack tasks if you have them
            Tower.cancelTasksForPlayer(playerUUID);

            // Remove all zombies associated with this player's game
            MobHandler.removeZombiesForPlayer(player);

            // Remove all towers
            Tower.removeTowersForPlayer(player, mapId);

            // Clean up game resources and data structures
            gameManager.handlePlayerQuit(player);

            Economy.playerLeave(player);
            PlayerUpgrades.getPlayerUpgradesMap().remove(player);

            // Log the cleanup
            plugin.getLogger().info("Cleaned up game for player " + player.getName());
        }
    }

    // Events for the GUI
    UpgradeGUI openChestGUI = new UpgradeGUI();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        Inventory clickedInventory = event.getInventory();
        if (openChestGUI.openInventories.get(player) != clickedInventory) return;

        event.setCancelled(true); // Prevent item movement
        if (event.getCurrentItem() == null) return;

        Material clickedMaterial = event.getCurrentItem().getType();
        switch (clickedMaterial) {
            case DIAMOND:
                player.sendMessage(ChatColor.GREEN + "You have clicked on a Diamond!");
                break;
            case GOLD_INGOT:
                player.sendMessage(ChatColor.YELLOW + "You have clicked on a Gold Ingot!");
                break;
            case EMERALD:
                player.sendMessage(ChatColor.DARK_GREEN + "You have clicked on an Emerald!");
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        openChestGUI.openInventories.remove(event.getPlayer());
    }



    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.COMPASS && item.getItemMeta() != null &&
                ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Upgrade Menu")) {
            openChestGUI.openChestGUI(player);
        }
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
                if (!gameManager.isInplayerSessions(playerUUID)) {
                    player.sendMessage(ChatColor.RED + "You must start a game first!");
                    return;
                }

                String mapId = gameManager.getPlayerMapId(playerUUID);



                // Check which egg was used
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
        if (!gameManager.isInplayerSessions(playerUUID)) {
            return;
        }


        // Remove this zombie from tracking
        gameManager.removeZombie(zombie.getUniqueId(), playerUUID);

        int killed = gameManager.incrAndGetZombiesKilled(playerUUID);
        int zombiesThisRound = gameManager.getZombiesThisRound(playerUUID);

        if (killed >= zombiesThisRound) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null && player.isOnline()) {
                int currentRound = gameManager.getCurrentRound(playerUUID);
                // Process round completion
                currentRound++;
                gameManager.setCurrentRound(currentRound, playerUUID);
                gameManager.setZombiesPerRound(playerUUID);
                gameManager.setIsReady(playerUUID, false);

                // Set round as no longer in progress
                gameManager.setRoundInProgress(playerUUID, false);

                Bukkit.broadcastMessage(ChatColor.GOLD + "Round " + (currentRound - 1) + " completed!");
                Bukkit.broadcastMessage(ChatColor.GREEN + "Type /readyup for Round " + currentRound);
            }
        }
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event){
        // activates the entity death event for economy

        EntityType mobType = event.getEntityType();
        Player killer = event.getEntity().getKiller();

        // Handle null killer by assigning death to nearby player
        if(killer == null) {
            if (event.getEntity() instanceof Zombie){
                Zombie zombie = (Zombie) event.getEntity();
                @NotNull Collection<Player> kill = zombie.getLocation().getNearbyPlayers(50);
                killer = kill.iterator().next();

            }
        }
        String playerID;

        if (event.getEntity() instanceof Zombie){
            Zombie zomb = (Zombie) event.getEntity();

            if (zomb.hasMetadata("attacker")){
                playerID = zomb.getMetadata("attacker").get(0).asString();
                killer = Bukkit.getPlayer(playerID);
                Economy.earnMoney(killer, mobType);
                return;
            }

            Economy.earnMoney(killer, mobType);

        }
    }


    @EventHandler
    public void onInvClick(PlayerDropItemEvent event) {

        // check if a player is in a game first, or only work if a game has been created
        // this is just so players cannot drop their swords

        NamespacedKey notDroppableKey = new NamespacedKey(BlocksTowerDefense1.getInstance(), "not_droppable");

        // checks to see if the item has key that disables the ability to drop it
        if (event.getItemDrop().getItemStack().getItemMeta()
                .getPersistentDataContainer().has(notDroppableKey, PersistentDataType.BOOLEAN)){
            event.setCancelled(true); // if true, disable drop ability
        }
    }


    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event){
        if (event.getDamager() instanceof Player){

            PlayerUpgrades player = PlayerUpgrades.getPlayerUpgradesMap().get(event.getDamager());

            if (player.getSword().getSlownessLevel() > 0){
                player.getSword().applySlownessEffect((LivingEntity) event.getEntity());
            }

        }
    }





    @EventHandler
    public void onPartyCreatePre(BukkitPartiesPartyPreCreateEvent event) {
        Bukkit.getLogger().info("[PartiesExample] This event is called when a party is being created");

        if (false)
            event.setCancelled(true); // You can cancel it
    }

    @EventHandler
    public void onPartyCreatePost(BukkitPartiesPartyPostCreateEvent event) {
        Bukkit.getLogger().info("[PartiesExample] This event is called when a party has been created");

        // You cannot cancel it
    }

    @EventHandler
    public void onPartyDeletePost(BukkitPartiesPartyPostDeleteEvent event) {
        Party party = event.getParty();
        party.broadcastMessage("The party leader has chosen to delete the party.", null);
    }


    @EventHandler
    public void onPlayerInvitePre(BukkitPartiesPlayerPreInviteEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Bukkit.getLogger().info("[PartiesExample] This event is called when a player is getting invited");
    }

    @EventHandler
    public void onPlayerInvitePost(BukkitPartiesPlayerPostInviteEvent event) {


        Bukkit.getLogger().info("[PartiesExample] This event is called when a player has been invited");
    }
}