package me.matthewTest.pluginTest.logic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class exampleListener implements Listener{

    // TO REBUILD THE ARTIFACT: F5

    /*
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        if (event.getTo().getBlockX() == event.getFrom().getBlockX() && event.getTo().getBlockY() == event.getFrom().getBlockY() && event.getTo().getBlockZ() == event.getFrom().getBlockZ()) return;
        event.getPlayer().sendMessage("You have just moved.");
    }
     */

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        int playerCount = Bukkit.getOnlinePlayers().size();

        player.sendMessage("Welcome to the server, " + player.getName() + ".");
        Bukkit.broadcastMessage("The player count is now " + playerCount);

    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        int playerCount = (Bukkit.getOnlinePlayers().size() - 1);
        Bukkit.broadcastMessage("The player count is now " + playerCount);
    }
}
