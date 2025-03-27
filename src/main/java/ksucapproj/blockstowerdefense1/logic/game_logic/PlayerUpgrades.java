package ksucapproj.blockstowerdefense1.logic.game_logic;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.ConfigOptions;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerUpgrades{

    private static final PartiesAPI api = BlocksTowerDefense1.getApi();
    private static final ConfigOptions config = BlocksTowerDefense1.getInstance().getBTDConfig();

    private int swiftnessLevel, strengthLevel;
    private final Player player;
    private final PlayerSword sword;
    private final int playerUpgradesBought;
    private final int totalUpgradesBought;


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

    public PlayerUpgrades(Player player){
        this.player = player;
        this.swiftnessLevel = 0;
        this.strengthLevel = 0;
        this.playerUpgradesBought = 0;

        this.sword = new PlayerSword(player);

        this.totalUpgradesBought = playerUpgradesBought + getSword().getSwordUpgradesBought();
    }

    // if upgradeLevel is less than the maximum specified in the config
    // it will apply one last upgrade, and then on no longer apply

    public static void applySwiftnessUpgrade(PlayerUpgrades upgrades) {

        if (upgrades.swiftnessLevel < config.getSpeedMaxLevel()){

            // changes the current speed effect applied and applies the new level on top
            upgrades.setSwiftnessLevel(upgrades.swiftnessLevel++);
        }
    }


    public static void applyStrengthUpgrade(PlayerUpgrades upgrades) {

        if (upgrades.strengthLevel < config.getStrengthMaxLevel()){

            Player player = upgrades.player;

            // changes the current strength effect applied and applies the new level on top
            upgrades.setStrengthLevel(upgrades.strengthLevel++);
        }
    }




    public void setSwiftnessLevel(int swiftnessLevel) {
        this.swiftnessLevel = swiftnessLevel;
//        player.sendMessage("swiftness level before removal of effect: " + swiftnessLevel);
        player.removePotionEffect(PotionEffectType.SPEED);

        if (swiftnessLevel > 0){
//            player.sendMessage("swiftness level before addition of new effect: " + swiftnessLevel);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, -1, (swiftnessLevel-1), false, false));
//            player.sendMessage("this is the swiftness level passed into addPotionEffect() " + (swiftnessLevel-1));
        }

        player.sendMessage("Swiftness upgrade tier set to " + swiftnessLevel);
    }

    public void setStrengthLevel(int strengthLevel) {
        this.strengthLevel = strengthLevel;

        player.removePotionEffect(PotionEffectType.STRENGTH);

        if (strengthLevel > 0){
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, -1, (strengthLevel-1), false, false));
        }

        player.sendMessage("Swiftness upgrade tier set to " + strengthLevel);
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
}
