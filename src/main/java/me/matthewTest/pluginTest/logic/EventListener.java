package me.matthewTest.pluginTest.logic;

import com.alessiodp.parties.api.events.bukkit.party.BukkitPartiesPartyPostCreateEvent;
import com.alessiodp.parties.api.events.bukkit.party.BukkitPartiesPartyPreCreateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {
    // TO REBUILD THE ARTIFACT: F5

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        // activates the player join event for economy
        Economy.playerJoin(event.getPlayer());

        int playerCount = Bukkit.getOnlinePlayers().size();

        event.getPlayer().sendMessage("Welcome to the server, " + event.getPlayer().getName() + ".");
        Bukkit.broadcastMessage("The player count is now " + playerCount);

    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        // activates the player leave event for economy
        Economy.playerLeave(event.getPlayer());

        int playerCount = (Bukkit.getOnlinePlayers().size() - 1);
        Bukkit.broadcastMessage("The player count is now " + playerCount);
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event){
        // activates the entity death event for economy
        EntityType mobType = event.getEntityType();
        Player killer = event.getEntity().getKiller();


        Economy.earnMoney(killer, mobType);
    }


    @EventHandler
    public void onPartyCreatePre(BukkitPartiesPartyPreCreateEvent event) {
        System.out.println("[PartiesExample] This event is called when a party is being created");

        if (false)
            event.setCancelled(true); // You can cancel it
    }

    @EventHandler
    public void onPartyCreatePost(BukkitPartiesPartyPostCreateEvent event) {
        System.out.println("[PartiesExample] This event is called when a party has been created");

        // You cannot cancel it
    }
}
