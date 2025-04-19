package ksucapproj.blockstowerdefense1.logic.game_logic;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;

import static ksucapproj.blockstowerdefense1.placeholderAPI.PlaceholderAPIExpansion.config;

/*
    ** How Economy is Utilized **

    -- In StartGame --

        - playerJoin(<player_name>);
        ----------------------------
        ~ A player only has an Economy while in a game session, created in StartGame

        ~ Player's economy is created
            * This is a static reference to playerJoin() in Economy


        - PlayerUpgrades.playerDelete(<player_name>);
        ---------------------------------------------
        ~ Player's economy is deleted
            * This is a static reference to playerDelete in PlayerUpgrades

        ~ All the following are deleted in PlayerUpgrades.playerDelete:
            * Tracked PlayerSword obj.
            * Player's active potion effects
            * Player's Economy obj.
            * Player's PlayerUpgrades HashMap<Player, PlayerUpgrades> instance
 */


public class Economy {

    private Player player = null;
    private int currTotal;
    private int totalCoinsGained;
    private int totalCoinsSpent;

    private static final HashMap<Player, Economy> playerEconomies = new HashMap<>();
    private static final HashMap<EntityType, Integer> mobKillRewards = new HashMap<>();


        /*
            These values will only be affected ONLY by:
                * the amt of coins the player earns:
                    -  from mob kills
                    - at the end of each round
                * the amt of coins the player spends:
                    -  on towers/upgrades

            -- Notable exclusions:
                * sending/receiving money to/from a teammate
                * adding/removing coins manually via admin command
                * manually upgrading player/sword stats via admin command
         */


    // Economy object for a player to be placed into upon loading into a game
    public Economy(Player player){
        this.player = player;
        this.currTotal = config.getPlayerStartingCoins(); // starting value variable in config, default: 500
//        Bukkit.getLogger().warning(String.valueOf(currTotal));
//        Bukkit.getLogger().warning(String.valueOf(config.getPlayerStartingCoins()));
        this.totalCoinsGained = 0;
        this.totalCoinsSpent = 0;
    }

    // Economy object that the server has to track all the mob types and their costs
    public Economy(){
        // add new mobs here, along with their coin reward amt
        mobKillRewards.put(EntityType.ZOMBIE, config.getZombieReward());
//        Bukkit.getLogger().warning(String.valueOf(config.getZombieReward()));
        mobKillRewards.put(EntityType.IRON_GOLEM, config.getGolemReward());
        mobKillRewards.put(EntityType.WITCH, config.getWitchReward());
        mobKillRewards.put(EntityType.SILVERFISH, config.getSfishReward());
        mobKillRewards.put(EntityType.PIGLIN, config.getPiglinReward());
        mobKillRewards.put(EntityType.BLAZE, config.getBlazeReward());
    }


    // this function handles the earning of money when they or their tower kills a certain mob
    public static void earnMoney(Player killer, EntityType mobKilled) {

        // if the mob killed is not in the economy constructor
        if (!(mobKillRewards.containsKey(mobKilled))){
            killer.sendMessage("Killing of this mob will not reward coins.");
            return;
        }


        // divides the reward among the players in the lobby like in normal bloons tower defense
        final int killReward = mobKillRewards.get(mobKilled) / playerEconomies.size();


        // notifies all players in the server of who killed what mob (if it is coin eligible)
//        Bukkit.broadcastMessage(killer.getName() + " just killed " + mobKilled + " for " + killReward + " coins!");

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

        // if the player can buy the item, it reduces their money by that amt
        // also increments their total spent by that same value
        if (spender.currTotal >= cost){

            spender.currTotal -= cost;
            spender.totalCoinsSpent += cost;

            return true;
        }

        // if the player doesn't have enough coins, function returns false
        return false;
    }




    // this is for manual admin command
    public static void addPlayerMoney(Player player, int amt){
        Economy playerEconomy = playerEconomies.get(player);
        playerEconomy.currTotal += amt;
    }

    // this is for giving each player their end of round bonus
    public void addMoneyOnRoundEnd(int roundNum){
        int endRoundBonus = (100 + roundNum);
        currTotal += endRoundBonus;
        totalCoinsGained += endRoundBonus;
    }


    // function that handles the money sharing functionality with the sender's teammate
    public static void shareMoneyWithTeammate(Player sender, Player receiver, int amt){

        Economy senderEconomy = playerEconomies.get(sender);
        int currSenderMoney = senderEconomy.currTotal;  // sets value for sender's current coin total

        // if the amt to send is more than currSenderMoney, it sends all of sender's money
        if (amt > currSenderMoney){
            amt = currSenderMoney;
        }

        // takes the specified amount from the player's economy
        senderEconomy.setPlayerMoney(currSenderMoney-amt);

        // send confirmation message for send coins transaction
        sender.sendRichMessage("<aqua>You sent <dark_aqua><player></dark_aqua> <gold><amount></gold> coins!",
                Placeholder.component("player", Component.text(receiver.getName())),
                Placeholder.component("amount", Component.text(amt))
        );

        Economy teammate = playerEconomies.get(receiver);

        // gives the specified amt to the teammate's economy
        teammate.setPlayerMoney(teammate.currTotal + amt);

        // send confirmation message for receive coins transaction
        receiver.sendRichMessage("<aqua><dark_aqua><player></dark_aqua> sent you <gold><amount></gold> coins!",
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



        // if no other players are detected, leaver's coins are simply deleted
        if (playerEconomies.isEmpty()){
            Bukkit.broadcastMessage("No online players found, deleting all coins.");
            return;
        }

        final int leaverMoney = getPlayerEconomies().get(leaver).currTotal;

        // initializes leaver's economy total to zero and removes them from the tracked hashmap
        getPlayerEconomies().get(leaver).currTotal = 0;
        playerEconomies.remove(leaver);

        final int numPlayersOnline = playerEconomies.size();


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
    // THIS HAS BEEN DISABLED and is only kept ICE
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
