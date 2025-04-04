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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
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
import java.util.Objects;
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

    // This event is for actions when the player joins the server
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        // activates the player join event for economy
        Player player = event.getPlayer();


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

    // This event handles players leaving mid-game
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Handle party system
        PartyPlayer partyPlayer = api.getPartyPlayer(playerUUID);
        if (partyPlayer != null && partyPlayer.isInParty()) {
            Party party = api.getParty(partyPlayer.getPartyId());
            if (party != null) {
                if (party.getLeader() == playerUUID) {
                    party.delete();
                } else {
                    party.removeMember(partyPlayer);
                }
            }
        }

        // Clean up game data if player was in a game
        if (gameManager.isPlayerInGame(playerUUID)) {
            gameManager.cleanupPlayer(playerUUID);
        }
    }

    // Events for the GUI
    UpgradeGUI openChestGUI = new UpgradeGUI();



    // This event checks what the player clicked in the GUI
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory clickedInventory = event.getInventory();
        if (openChestGUI.openInventories.get(player) != clickedInventory) return;

        event.setCancelled(true); // Prevent item movement
        if (event.getCurrentItem() == null) return;

        ItemStack clickedItem = event.getCurrentItem();


        // If statement for checking which item was clicked, repeated for every item
        if (Objects.equals(clickedItem.getItemMeta().displayName(),
                Component.text("Upgrade Sword Level").color(TextColor.color(0, 255, 0)))) {

            // getting playersword and/or player upgrades object to update the object
            PlayerSword playerSword = PlayerUpgrades.getPlayerUpgradesMap().get(player).getSword();

            // applying upgrade and spending coins
            playerSword.applySwordMaterialUpgrade();

            // returning new level of upgrade selected
                player.sendRichMessage("Your Sword Level Is Now <s_material>",
                        Placeholder.component("s_material", Component.text(String.valueOf(playerSword.swordLevel))));
        }
        if (Objects.equals(clickedItem.getItemMeta().displayName(),
                Component.text("Upgrade Strength Level").color(TextColor.color(100, 255, 255)))) {
            PlayerUpgrades playerUpgrades = PlayerUpgrades.getPlayerUpgradesMap().get(player);
            playerUpgrades.applyStrengthUpgrade();
            player.sendRichMessage("Your Strength Level Is Now <s_lvl>",
                    Placeholder.component("s_lvl", Component.text(String.valueOf(PlayerUpgrades.getPlayerUpgradesMap().get(player).getStrengthLevel()))));
        }
        if (Objects.equals(clickedItem.getItemMeta().displayName(),
                Component.text("Upgrade Speed Level").color(TextColor.color(155, 255, 155)))) {
            PlayerUpgrades playerUpgrades = PlayerUpgrades.getPlayerUpgradesMap().get(player);
            playerUpgrades.applySwiftnessUpgrade();
            player.sendRichMessage("Your Speed Level Is Now <s_lvl>",
                    Placeholder.component("s_lvl", Component.text(String.valueOf(PlayerUpgrades.getPlayerUpgradesMap().get(player).getSwiftnessLevel()))));
        }
        if (Objects.equals(clickedItem.getItemMeta().displayName(),
                Component.text("Upgrade Sweeping edge Level").color(TextColor.color(255, 50, 50)))) {
            PlayerSword playerSword = PlayerUpgrades.getPlayerUpgradesMap().get(player).getSword();
            playerSword.applySweepingEdgeUpgrade();
            player.sendRichMessage("Your Sweeping edge Level Is Now <s_lvl>",
                    Placeholder.component("s_lvl", Component.text(String.valueOf(PlayerUpgrades.getPlayerUpgradesMap().get(player).getSword().getSweepingEdgeLevel()))));
        }
        if (Objects.equals(clickedItem.getItemMeta().displayName(),
                Component.text("Upgrade Slowness Level").color(TextColor.color(5, 50, 250)))) {
            PlayerSword playerSword = PlayerUpgrades.getPlayerUpgradesMap().get(player).getSword();
            playerSword.applySlownessUpgrade();
            player.sendRichMessage("Your Slowness Level Is Now <s_lvl>",
                    Placeholder.component("s_lvl", Component.text(String.valueOf(PlayerUpgrades.getPlayerUpgradesMap().get(player).getSword().getSlownessLevel()))));
        }
    }

    /// can maybe be removed?
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        openChestGUI.openInventories.remove(event.getPlayer());
    }


    // This event is for opening GUI when compass is used
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        // debug
        //player.sendMessage(item.getItemMeta().displayName());
        if (item != null && item.getType() == Material.COMPASS &&
                Objects.equals(item.getItemMeta().displayName(), Component.text("Upgrade Menu").color(TextColor.color(0, 255, 255)))) {
            openChestGUI.openChestGUI(player);
        }
    }


    // This event is for placing the towers with the spawn eggs
    @EventHandler
    public void onPlayerUseEgg(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        ItemStack item = event.getItem();


        // Check if this is a valid tower placement attempt
        if (item != null && item.getType() == Material.ZOMBIE_SPAWN_EGG && item.getItemMeta() != null) {
            String itemName = String.valueOf(item.getItemMeta().displayName());


            // Check if it's one of our tower spawn eggs
            if (itemName.contains("Tower")) {  // Check for color codes
                // Cancel the event to prevent default zombie spawning
                event.setCancelled(true);
                //debug
                //player.sendMessage(itemName);

                // Check if player is in a game
                UUID playerUUID = player.getUniqueId();
                if (!gameManager.isInplayerSessions(playerUUID)) {
                    player.sendRichMessage("<red>You must start a game first!");
                    return;
                }

                String mapId = gameManager.getPlayerMapId(playerUUID);



                // Check which egg was used
                if (itemName.contains("Fast Tower")) {
                    TowerFactory.placeTower(
                            TowerFactory.TowerType.FAST,
                            player,
                            event.getInteractionPoint(),
                            mapId,
                            plugin,
                            item
                    );
                }
                if (itemName.contains("Basic Tower")) {
                    TowerFactory.placeTower(
                            TowerFactory.TowerType.BASIC,
                            player,
                            event.getInteractionPoint(),
                            mapId,
                            plugin,
                            item
                    );
                }
                if (itemName.contains("Sniper Tower")) {
                    TowerFactory.placeTower(
                            TowerFactory.TowerType.SNIPER,
                            player,
                            event.getInteractionPoint(),
                            mapId,
                            plugin,
                            item
                    );
                }
                if (itemName.contains("Splash Tower")) {
                    TowerFactory.placeTower(
                            TowerFactory.TowerType.SPLASH,
                            player,
                            event.getInteractionPoint(),
                            mapId,
                            plugin,
                            item
                    );
                }
                if (itemName.contains("Slow Tower")) {
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


    // This event is for game logic when a zombie or special mob is killed
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Mob zombie)) {
            return;
        }


        if (!zombie.hasMetadata("gameSession")) {
            return;
        }

        String gameSessionId = zombie.getMetadata("gameSession").getFirst().asString();
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
                gameManager.roundEndMoney(playerUUID);


                Bukkit.broadcastMessage(ChatColor.GOLD + "Round " + (currentRound - 1) + " completed!");
                Bukkit.broadcastMessage(ChatColor.GREEN + "Type /readyup for Round " + currentRound);
            }
        }
    }

    // This event is for economy logic when a mob is killed
    /// can maybe be merged with the event above
    @EventHandler
    public void onMobKill(EntityDeathEvent event){
        // activates the entity death event for economy

        EntityType mobType = event.getEntityType();
        Player killer = event.getEntity().getKiller();

        // Handle null killer by assigning death to nearby player
        if(killer == null) {
            if (event.getEntity() instanceof Mob zombie){
                @NotNull Collection<Player> kill = zombie.getLocation().getNearbyPlayers(50);
                killer = kill.iterator().next();

            }
        }
        String playerID;

        if (event.getEntity() instanceof Mob zomb){

            if (zomb.hasMetadata("attacker")){
                playerID = zomb.getMetadata("attacker").getFirst().asString();
                killer = Bukkit.getPlayer(playerID);
                Economy.earnMoney(killer, mobType);
                return;
            }

            Economy.earnMoney(killer, mobType);

        }
    }


    // This event is for disallowing item drops
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

    // This event is for applying slowness when you have the slowness upgrade
    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event){
        if (event.getDamager() instanceof Player){

            PlayerUpgrades player = PlayerUpgrades.getPlayerUpgradesMap().get(event.getDamager());

            if (player.getSword().getSlownessLevel() > 0){
                player.getSword().applySlownessEffect((LivingEntity) event.getEntity());
            }

        }
    }




    // The following events are for simple party actions
    @EventHandler
    public void onPartyCreatePre(BukkitPartiesPartyPreCreateEvent event) {
        Bukkit.getLogger().info("[PartiesExample] This event is called when a party is being created");

        //if (false)
          //  event.setCancelled(true); // You can cancel it
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