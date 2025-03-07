package me.matthewTest.pluginTest.logic;

import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
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
}
