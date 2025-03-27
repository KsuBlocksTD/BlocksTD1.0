package ksucapproj.blockstowerdefense1.logic;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;


public class Economy {
    private int totalCoinsGained;
    private int totalCoinsSpent;

    private static final HashMap<Player, Integer> playerMoney = new HashMap<>();
    private static final HashMap<EntityType, Integer> mobKillRewards = new HashMap<>();


        /*
            These values will only be affected ONLY by:
                * the amt of coins the player earns from mob kills
                * the amount of coins they spend on towers/upgrades

            -- Notable exclusions:
                * sending/receiving money to/from a teammate
                * adding/removing coins manually via admin command
                * manually upgrading player/sword stats via admin command
         */

    public Economy(Player player){
        this.totalCoinsGained = 0;
        this.totalCoinsSpent = 0;
    }

    public Economy(){
        // add new mobs here, along with their coin reward amt
        mobKillRewards.put(EntityType.ZOMBIE, 10);
        mobKillRewards.put(EntityType.SKELETON, 15);
    }


    public static void earnMoney(Player killer, EntityType mobKilled, Economy playerMoneyTotal) {

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
            playerMoney.put(onlinePlayer, (playerMoney.get(onlinePlayer) + killReward));
            playerMoneyTotal.totalCoinsGained += killReward;

            // reminds each player of their total
            //onlinePlayer.sendMessage("You now have " + playerMoney.get(onlinePlayer) + " coins!");
        }

    }

    public static boolean spendMoney(Player spender, int cost, Economy playerTotalMoney){

        if (playerMoney.get(spender) >= cost){
            playerMoney.put(spender, (playerMoney.get(spender) - cost));
            playerTotalMoney.totalCoinsSpent += cost;
            return true;
        }

        return false;
    }




    // this is for manual admin command
    public static void addPlayerMoney(Player player, int amt){
        int currMoney = Integer.parseInt(getPlayerMoney(player));
        playerMoney.put(player, currMoney + amt);
    }


    public static void shareMoneyWithTeammate(Player sender, Player receiver, int amt){
        int currSenderMoney = playerMoney.get(sender); // sets value for sender's current coin total

        // if the amt to send is more than currSenderMoney, it sends all of sender's money
        if (amt > currSenderMoney){
            amt = currSenderMoney;
        }

        setPlayerMoney(sender, currSenderMoney - amt);

        // send confirmation message for send coins transaction
        sender.sendRichMessage("You sent <player> <amount> coins!",
                Placeholder.component("player", Component.text(receiver.getName())),
                Placeholder.component("amount", Component.text(amt))
        );

        setPlayerMoney(receiver, (playerMoney.get(receiver) + amt));

        // send confirmation message for receive coins transaction
        receiver.sendRichMessage("<player> sent you <amount> coins!",
                Placeholder.component("amount", Component.text(amt)),
                Placeholder.component("player", Component.text(sender.getName()))
        );
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

    // this is for a compilation fix bug that occurs when the # of players in the lobby does not match # of player in playerMoney
    // this executes on startup by default. if the server starts empty, nothing happens
    public static void playerCountFix(){
        if (playerMoney.size() != Bukkit.getOnlinePlayers().size()){

            for (Player player : Bukkit.getOnlinePlayers()){


                if (!(playerMoney.containsKey(player))){
                    playerMoney.put(player, 0);
                    new Economy(player);
                }

            }


        }
    }

    public static void setPlayerMoney(Player player, int number) {
        playerMoney.put(player, number);
    }

    public int getTotalCoinsGained() {
        return totalCoinsGained;
    }

    public int getTotalCoinsSpent() {
        return totalCoinsSpent;
    }
}
