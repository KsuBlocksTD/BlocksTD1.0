package ksucapproj.blockstowerdefense1.logic.game_logic;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.ConfigOptions;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import static ksucapproj.blockstowerdefense1.logic.game_logic.Economy.*;

public class PlayerUpgrades{

    private static final PartiesAPI api = BlocksTowerDefense1.getApi();
    private static final ConfigOptions config = BlocksTowerDefense1.getInstance().getBTDConfig();
    private static final HashMap<Player, PlayerUpgrades> playerUpgradesMap = new HashMap<>();

    private int playerUpgradesBought;
    private int swiftnessLevel, strengthLevel;
    private final Player player;
    private final PlayerSword sword;
    private int totalUpgradesBought;
    private int currTotal;
    private int cost;


    // all levels are initialized to 0, which is representative of their swiftness tier (0-5)
    // i.e. swiftness level 0 = no swiftness, swiftness level 1 = swiftness 1, etc.

    /*
        -- Eventually requires the implementation of total upgrades bought --
        * start with a counter tallying the total amount of upgrades bought throughout the game
            - increment this value each time one is purchased
        * also tally the amount of playerSword upgrades bought
        * at the end of the game, call a function in databaseManager that adds the num of total upgrades
        to the currently stored total
     */


    // This is the constructor that creates a player and their sword upon game creation
    public PlayerUpgrades(Player player){
        this.player = player;
        this.swiftnessLevel = 0;
        this.strengthLevel = 0;
        this.playerUpgradesBought = 0;

        this.sword = new PlayerSword(player);

        this.totalUpgradesBought = playerUpgradesBought + getSword().getSwordUpgradesBought();
        currTotal = getPlayerEconomies().get(player).getCurrTotal();
    }

    // if upgradeLevel is less than the maximum specified in the config
    // it will apply one last upgrade, and then on no longer apply

    public void applySwiftnessUpgrade() {
        currTotal = getPlayerEconomies().get(player).getCurrTotal();
        // base cost is 500 atm
        cost = config.getSpeedBaseCost() * (swiftnessLevel +1);
        if (currTotal >= cost){

            if (swiftnessLevel < config.getSpeedMaxLevel()) {

                Economy.spendMoney(player, cost);
                // changes the current speed effect applied and applies the new level on top
                setSwiftnessLevel(++swiftnessLevel);
                playerUpgradesBought += 1;
                player.sendMessage("Your speed level is now level " + swiftnessLevel);
                return;
            }
            sendMaxLevelMsg(); // if player is already max level, notify and return
            return;
        }
        sendCannotAffordMsg(); // if player cannot afford the upgrade, notify and return
    }


    public void applyStrengthUpgrade() {
        currTotal = getPlayerEconomies().get(player).getCurrTotal();
        cost = config.getStrengthBaseCost() * (strengthLevel +1);
        // base cost is 750 atm
        if (currTotal >= cost){

            if (strengthLevel < config.getStrengthMaxLevel()) {

                Economy.spendMoney(player, cost);
                // changes the current strength effect applied and applies the new level on top
                setStrengthLevel(++strengthLevel);
                player.sendMessage("Your strength level is now level " + strengthLevel);
                return;
            }
            sendMaxLevelMsg(); // if player is already max level, notify and return
            return;
        }
        sendCannotAffordMsg(); // if player cannot afford the upgrade, notify and return
    }


    // deletes the playerUpgrades object along with their playerSword and economy
    public static void playerDelete(Player player){
        PlayerUpgrades leaver = playerUpgradesMap.get(player);

        // Deletes player's tracked sword
        leaver.getSword().removeTrackedSword();
        // Deletes player's potion effects
        player.clearActivePotionEffects();
        // Deletes player's economy
        playerLeave(player);
        // Deletes player from player upgrades map
        playerUpgradesMap.remove(player);
    }


    // utilized by application of upgrade and for admin command
    // gives the player a speed potion effect for their corresponding swiftness level
    public void setSwiftnessLevel(int swiftnessLevel) {
        this.swiftnessLevel = swiftnessLevel;
//        player.sendMessage("swiftness level before removal of effect: " + swiftnessLevel);
        player.removePotionEffect(PotionEffectType.SPEED);

        if (swiftnessLevel > 0){
//            player.sendMessage("swiftness level before addition of new effect: " + swiftnessLevel);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, -1, (swiftnessLevel-1), false, false));
//            player.sendMessage("this is the swiftness level passed into addPotionEffect() " + (swiftnessLevel-1));
        }

//        player.sendMessage("Swiftness upgrade tier set to " + swiftnessLevel);
    }
    // utilized by application of upgrade and for admin command
    // gives the player a speed potion effect for their corresponding strength level
    public void setStrengthLevel(int strengthLevel) {
        this.strengthLevel = strengthLevel;

        player.removePotionEffect(PotionEffectType.STRENGTH);

        if (strengthLevel > 0){
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, -1, (strengthLevel-1), false, false));
        }

        //player.sendMessage("Swiftness upgrade tier set to " + strengthLevel);
    }

    public int getStrengthLevel() {
        return this.strengthLevel;
    }

    public int getSwiftnessLevel() {
        return this.swiftnessLevel;
    }

    public PlayerSword getSword() {
        return sword;
    }

    public Player getPlayer() {
        return player;
    }

    public int getTotalUpgradesBought() {
        return totalUpgradesBought;
    }

    public static HashMap<Player, PlayerUpgrades> getPlayerUpgradesMap() {
        return playerUpgradesMap;
    }

    public void sendCannotAffordMsg(){
        player.sendRichMessage("<red>You don't have enough coins for this upgrade!");
    }

    public void sendMaxLevelMsg(){
        player.sendRichMessage("<red>You already have the maximum level for this upgrade!");
    }
}
