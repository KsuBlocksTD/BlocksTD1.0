package ksucapproj.blockstowerdefense1.logic;

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
import ksucapproj.blockstowerdefense1.logic.game_logic.Economy;
import ksucapproj.blockstowerdefense1.logic.game_logic.PlayerUpgrades;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;

public class EventListener implements Listener {
    // TO REBUILD THE ARTIFACT: F5

    public static final PartiesAPI api = BlocksTowerDefense1.getApi();
    ConfigOptions config = BlocksTowerDefense1.getInstance().getBTDConfig();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        // activates the player join event for economy
        Player player = event.getPlayer();


        // This needs to be eventually called when the player is put into a game state instead of when joining the lobby
        // and at that point will be deleted here
//        Economy.playerJoin(player);
//        PlayerUpgrades.getPlayerUpgradesMap().put(player, new PlayerUpgrades(player));


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
    public void onPlayerQuit(PlayerQuitEvent event){

        Player player = event.getPlayer();
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



        // activates the player leave event for economy
        // this needs to check if the player is in a session first ***************************
//        Economy.playerLeave(player);
//        PlayerUpgrades.getPlayerUpgradesMap().remove(player);

        int playerCount = (Bukkit.getOnlinePlayers().size() - 1);
        Bukkit.broadcastMessage("The player count is now " + playerCount);
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
    public void onMobKill(EntityDeathEvent event){
        // activates the entity death event for economy

        EntityType mobType = event.getEntityType();
        Player killer = event.getEntity().getKiller();

        if (killer != null){
            Economy.earnMoney(killer, mobType);
        }

        else{

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
