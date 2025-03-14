package me.matthewTest.pluginTest.logic;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Economy {
    private static HashMap<Player, Integer> playerMoney = new HashMap<>();
    private static final HashMap<EntityType, Integer> mobKillRewards = new HashMap<>();

    public Economy(){
        // add new mobs here, along with their coin reward amt
        mobKillRewards.put(EntityType.ZOMBIE, 10);
        mobKillRewards.put(EntityType.SKELETON, 15);
    }

    public static void earnMoney(Player killer, EntityType mobKilled) {

        // if the mob killed is not in the economy constructor
        if (!(mobKillRewards.containsKey(mobKilled))){
            killer.sendMessage("Killing of this mob will not reward coins.");
            return;
        }

        // divides the reward among the players in the lobby like in normal bloons tower defense
        final int killReward = mobKillRewards.get(mobKilled) / playerMoney.size();


        // notifies all players in the server of who killed what mob (if it is coin eligible)
        Bukkit.broadcastMessage(killer.getName() + " just killed " + mobKilled + " for " + killReward + " coins!");

        // is incorrectly printing both coin amounts for each player to one player (happens to be the killer in one case)
        // this is likely fixed

        for (Player onlinePlayer : playerMoney.keySet()){

            // does the action of giving each online, eligible player their reward amount
            playerMoney.put(onlinePlayer, playerMoney.get(onlinePlayer)+killReward);
            // reminds each player of their total
            onlinePlayer.sendMessage("You now have " + playerMoney.get(onlinePlayer) + " coins!");
        }

    }



    // simply adds the joining player to the current players eligible to kill and gain money
    public static void playerJoin(Player player){
        playerMoney.put(player, 0);
    }



    // removes the player from the hashmap, takes their money,
    // and splits evenly it among all remaining players in the game
    public static void playerLeave(Player leaver){
        Bukkit.broadcastMessage(leaver.getName() + " has left the game! Reallocating coins..");

        final int leaverMoney = playerMoney.get(leaver);

        playerMoney.put(leaver, 0);
        playerMoney.remove(leaver);

        final int numPlayersOnline = playerMoney.size();

        // if no other players are detected, leaver's coins are simply deleted
        if (playerMoney.isEmpty()){
            Bukkit.broadcastMessage("No online players found, deleting all coins.");
            return;
        }

        // if a player in the server is not the leaver, it gives them a portion of the coins
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()){
            if (onlinePlayer != leaver){
                int curPlayerMoney = playerMoney.get(onlinePlayer);
                playerMoney.put(onlinePlayer, curPlayerMoney + leaverMoney/numPlayersOnline);
            }
        }
    }

    public static String getPlayerMoney(Player player) {
        return playerMoney.get(player).toString();
    }

    public static void addPlayerMoney(Player player, int amt){ // this is for manual admin command
        int currMoney = Integer.parseInt(getPlayerMoney(player));
        playerMoney.put(player, currMoney + amt);
    }

    // this is for a compilation fix bug that occurs when the # of players in the lobby does not match # of player in playerMoney
    // this executes on startup by default. if the server starts empty, nothing happens
    public static void playerCountFix(){
        if (playerMoney.size() != Bukkit.getOnlinePlayers().size()){

            for (Player player : Bukkit.getOnlinePlayers()){


                if (!(playerMoney.containsKey(player))){
                    playerMoney.put(player, 0);
                }

            }


        }
    }
}
