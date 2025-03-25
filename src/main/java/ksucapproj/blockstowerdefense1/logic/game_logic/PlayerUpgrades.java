package ksucapproj.blockstowerdefense1.logic.game_logic;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.ConfigOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerUpgrades{

    private static final PartiesAPI api = BlocksTowerDefense1.getApi();
    private static final ConfigOptions config = new ConfigOptions(BlocksTowerDefense1.getInstance());

    private int swiftnessLevel, strengthLevel, slownessLevel, sweepingEdgeLevel;
    private Player player;

    // all levels are initialized to 0, which is representative of their swiftness tier (0-5)
    // i.e. swiftness level 0 = no swiftness, swiftness level 1 = swiftness 1, etc.

    public PlayerUpgrades(Player player){
        this.player = player;
        this.swiftnessLevel = 0;
        this.strengthLevel = 0;
        this.slownessLevel = 0;
        this.sweepingEdgeLevel = 0;
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
            upgrades.strengthLevel++; // increase the level

            // changes the current strength effect applied and applies the new level on top
            upgrades.setStrengthLevel(upgrades.strengthLevel++);
        }
    }

    public static void applySlownessUpgrade(PlayerUpgrades upgrades, LivingEntity target) {

        // Apply slowness to the mob (target)
        if (upgrades.slownessLevel < config.getSlownessMaxLevel()){

            upgrades.slownessLevel++; // increase the level

            if (config.getSlownessDurationIncreaseOnUpgrade()){ // enables longer duration on
                // applies slowness, 10 ticks + 2 per slownessLevel (0.5s), amplifier of player's slownessLevel
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, (config.getSlownessDuration() + (upgrades.slownessLevel * 2) + 2), upgrades.slownessLevel, false, true));
                return;
            }

            // applies slowness, 10 ticks (0.5s), amplifier of player's slownessLevel
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, config.getSlownessDuration(), upgrades.slownessLevel, false, true));
        }

    }

    public static void applySweepingEdgeUpgrade(PlayerUpgrades upgrades){

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

    public void setSlownessLevel(int slownessLevel) {
        this.slownessLevel = slownessLevel;

//        if (slownessLevel > 0){
//
//        }

        player.sendMessage("Slowness upgrade tier set to " + slownessLevel);
    }

    public void setSweepingEdgeLevel(int sweepingEdgeLevel) {
        this.sweepingEdgeLevel = sweepingEdgeLevel;

//        if (sweepingEdgeLevel > 0){
//
//        }

        player.sendMessage("Swiftness upgrade tier set to " + sweepingEdgeLevel);
    }
}
