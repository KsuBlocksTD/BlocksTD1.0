package ksucapproj.blockstowerdefense1.logic.game_logic;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;


public class Economy {

    private Player player = null;
    private int currTotal;
    private int totalCoinsGained;
    private int totalCoinsSpent;

    private static final HashMap<Player, Economy> playerEconomies = new HashMap<>();
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
        this.player = player;
        this.currTotal = 500;
        this.totalCoinsGained = 0;
        this.totalCoinsSpent = 0;
    }

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
        final int killReward = mobKillRewards.get(mobKilled) / playerEconomies.size();


        // notifies all players in the server of who killed what mob (if it is coin eligible)
        Bukkit.broadcastMessage(killer.getName() + " just killed " + mobKilled + " for " + killReward + " coins!");

        // is incorrectly printing both coin amounts for each player to one player (happens to be the killer in one case)
        // this is likely fixed

        for (Economy onlinePlayer : playerEconomies.values()){

            // does the action of giving each online, eligible player their reward amount
            onlinePlayer.currTotal += killReward;
            onlinePlayer.totalCoinsSpent += killReward;

        }

    }

    // will not need to be static upon StartGame change
    public static boolean spendMoney(Player player, int cost){

        Economy spender = playerEconomies.get(player);

        if (spender.currTotal >= cost){

            spender.currTotal -= cost;
            spender.totalCoinsSpent += cost;

            return true;
        }

        return false;
    }




    // this is for manual admin command
    public static void addPlayerMoney(Player player, int amt){
        Economy playerEconomy = playerEconomies.get(player);
        playerEconomy.currTotal += amt;
    }

    public void addMoneyOnRoundEnd(int roundNum){
        int endRoundBonus = (100 + roundNum);
        currTotal += endRoundBonus;
        totalCoinsGained += endRoundBonus;
    }


    public static void shareMoneyWithTeammate(Player sender, Player receiver, int amt){

        Economy senderEconomy = playerEconomies.get(sender);
        int currSenderMoney = senderEconomy.currTotal;  // sets value for sender's current coin total

        // if the amt to send is more than currSenderMoney, it sends all of sender's money
        if (amt > currSenderMoney){
            amt = currSenderMoney;
        }

        senderEconomy.setPlayerMoney(currSenderMoney -= amt);

        // send confirmation message for send coins transaction
        sender.sendRichMessage("You sent <player> <amount> coins!",
                Placeholder.component("player", Component.text(receiver.getName())),
                Placeholder.component("amount", Component.text(amt))
        );

        Economy teammate = playerEconomies.get(receiver);

        teammate.setPlayerMoney(teammate.currTotal + currSenderMoney);

        // send confirmation message for receive coins transaction
        receiver.sendRichMessage("<player> sent you <amount> coins!",
                Placeholder.component("amount", Component.text(amt)),
                Placeholder.component("player", Component.text(sender.getName()))
        );
    }



    // simply adds the joining player to the current players eligible to kill and gain money
    // will not need to be static upon StartGame change
    // upon change requires deletion of join in EventListener
    public static void playerJoin(Player player){
        playerEconomies.put(player, new Economy(player));
    }





    // removes the player from the hashmap, takes their money,
    // and splits evenly it among all remaining players in the game
    public static void playerLeave(Player leaver){
        Bukkit.broadcastMessage(leaver.getName() + " has left the game! Reallocating coins..");

        final int leaverMoney = getPlayerEconomies().get(leaver).currTotal;

        getPlayerEconomies().get(leaver).currTotal = 0;
        playerEconomies.remove(leaver);

        final int numPlayersOnline = playerEconomies.size();

        // if no other players are detected, leaver's coins are simply deleted
        if (playerEconomies.isEmpty()){
            Bukkit.broadcastMessage("No online players found, deleting all coins.");
            return;
        }

        // if a player in the server is not the leaver, it gives them a portion of the coins
        for (Economy onlinePlayer : playerEconomies.values()){
            if (onlinePlayer.player != leaver){
                onlinePlayer.currTotal += (leaverMoney / numPlayersOnline);
            }
        }
    }

    public static int getPlayerMoney(Player player) {
        return playerEconomies.get(player).currTotal;
    }

    // this is for a compilation fix bug that occurs when the # of players in the lobby does not match # of player in playerMoney
    // this executes on startup by default. if the server starts empty, nothing happens


    public static void playerCountFix(){
        if (playerEconomies.size() != Bukkit.getOnlinePlayers().size()){

            for (Player player : Bukkit.getOnlinePlayers()){


                if (!(playerEconomies.containsKey(player))){
                    playerEconomies.put(player, new Economy(player));
                }

            }

        }
    }

    public static HashMap<Player, Economy> getPlayerEconomies() {
        return playerEconomies;
    }

    public void setPlayerMoney(int currTotal) {
        this.currTotal = currTotal;
    }
    public static void setPlayerMoney(Player player, int currTotal){
        Economy playerEconomy = playerEconomies.get(player);
        playerEconomy.currTotal = currTotal;
    }

    public int getTotalCoinsGained() {
        return totalCoinsGained;
    }

    public int getTotalCoinsSpent() {
        return totalCoinsSpent;
    }

    public Player getPlayer() {
        return player;
    }

    public int getCurrTotal() {
        return currTotal;
    }
}
