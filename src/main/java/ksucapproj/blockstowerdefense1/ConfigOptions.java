package ksucapproj.blockstowerdefense1;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ConfigOptions {
    private FileConfiguration config;

    public ConfigOptions(JavaPlugin plugin) {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }



    /*

    //////////////////////// For Upgrades Related to Potion Effects /////////////////////////////
    * If the maximum desired tier of an effect is n, use n-1 as the default
        - ex: if speed 5 is the desired maximum, choose 4 as the max default

     */


    public int getPlayerUniversalMaxLevel() {
        return config.getInt("btd.attributes.upgrades.player.universal.max-level"); // Default value is 5 if not set
    }
    public int getPlayerUniversalCostMult() { // not fully implemented
        return config.getInt("btd.attributes.upgrades.player.universal.cost-multiplier"); // Default value is 1 if not set
    }

    // Getter for Speed level max upgrade
    public int getSpeedMaxLevel() {
        return config.getInt("btd.attributes.upgrades.player.speed.max-level");  // Default value is 5 if not set
    }
    public int getSpeedBaseCost() {
        return config.getInt("btd.attributes.upgrades.player.speed.cost"); // Default value is 500 if not set
    }


    // Getter for Slowness level max upgrade
    public int getSlownessMaxLevel() {
        return config.getInt("btd.attributes.upgrades.player.sword.slowness.max-level");  // Default value is 5 if not set
    }
    public int getSlownessBaseCost(){
        return config.getInt("btd.attributes.upgrades.player.sword.slowness.cost"); // Default value is 400 if not set
    }
    public int getSlownessDuration() {
        return config.getInt("btd.attributes.upgrades.player.sword.slowness.duration.ticks"); // Default value is 10 (ticks) if not set
    }

    public boolean getSlownessDurationIncreaseOnUpgrade() {
        return config.getBoolean("btd.attributes.upgrades.player.sword.slowness.duration.increase-duration-on-upgrade"); // Default value is false if not set
    }


    // Getter for Strength level max upgrade
    public int getStrengthMaxLevel() {
        return config.getInt("btd.attributes.upgrades.player.strength.max-level");  // Default value is 5 if not set
    }
    public int getStrengthBaseCost() {
        return config.getInt("btd.attributes.upgrades.player.strength.cost");
    }


    // Getter for Sweeping-Edge level max upgrade
    public int getSweepingEdgeMaxLevel() {
        return config.getInt("btd.attributes.upgrades.player.sword.sweeping-edge.max-level");  // Default value is 5 if not set
    }
    public int getSweepingEdgeBaseCost() {
        return config.getInt("btd.attributes.upgrades.player.sword.sweeping-edge.cost");  // Default value is 600 if not set
    }


    public int getSwordMaterialMaxLevel() {
        // 1: wood, 2: stone, 3: iron, 4: diamond, 5: netherite
        return config.getInt("btd.attributes.upgrades.player.sword.material.max-level");  // Default value is 5 if not set
    }
    public int getSwordMaterialBaseCost() {
        // 1: wood, 2: stone, 3: iron, 4: diamond, 5: netherite
        return config.getInt("btd.attributes.upgrades.player.sword.material.cost");  // Default value is 400 if not set
    }



    // Getter for the message of the day for the server
    public String getMOTDOnPlayerJoin(){ // not fully implemented
        if (config.getBoolean("btd.settings.motd.enable")){
            return config.getString("btd.settings.motd.message");
        }
        return null;
    }

    // Getter for the greeting of a player on join-in
    public String getGreetOnPlayerJoin(){ // not fully implemented
        if (config.getBoolean("btd.settings.greet-player.enable")){
            return config.getString("btd.settings.greet-player.message");
        }
        return null;
    }

    public int getBasicTowerCost(){
        return config.getInt("btd.attributes.upgrades.towers.basic.cost");
    }

    public int getFastTowerCost(){
        return config.getInt("btd.attributes.upgrades.towers.fast.cost");
    }

    public int getSniperTowerCost(){
        return config.getInt("btd.attributes.upgrades.towers.sniper.cost");
    }

    public int getSplashTowerCost(){
        return config.getInt("btd.attributes.upgrades.towers.splash.cost");
    }

    public int getSlowTowerCost(){
        return config.getInt("btd.attributes.upgrades.towers.slow.cost");
    }

    public double getFastTowerDamage() {
        return config.getInt("btd.attributes.upgrades.towers.fast.damage");
    }
    public double getSplashTowerDamage() {
        return config.getInt("btd.attributes.upgrades.towers.splash.damage");
    }
    public double getSplashTowerAOEDamage() {
        return config.getInt("btd.attributes.upgrades.towers.splash.damageAOE");
    }
    public double getBasicTowerDamage() {
        return config.getInt("btd.attributes.upgrades.towers.basic.damage");
    }
    public double getSniperTowerDamage() {
        return config.getInt("btd.attributes.upgrades.towers.sniper.damage");
    }
    public double getSlowTowerDamage() {
        return config.getInt("btd.attributes.upgrades.towers.slow.damage");
    }

    public double getFastTowerRadius() {
        return config.getInt("btd.attributes.upgrades.towers.fast.radius");
    }
    public double getBasicTowerRadius() {
        return config.getInt("btd.attributes.upgrades.towers.basic.radius");
    }
    public double getSlowTowerRadius() {
        return config.getInt("btd.attributes.upgrades.towers.slow.radius");
    }
    public double getSniperTowerRadius() {
        return config.getInt("btd.attributes.upgrades.towers.sniper.radius");
    }
    public double getSplashTowerRadius() {
        return config.getInt("btd.attributes.upgrades.towers.splash.radius");
    }

    public long getFastTowerAttacksp() {
        return config.getInt("btd.attributes.upgrades.towers.fast.attacksp");
    }
    public long getBasicTowerAttacksp() {
        return config.getInt("btd.attributes.upgrades.towers.basic.attacksp");
    }
    public long getSlowTowerAttacksp() {
        return config.getInt("btd.attributes.upgrades.towers.slow.attacksp");
    }
    public long getSniperTowerAttacksp() {
        return config.getInt("btd.attributes.upgrades.towers.sniper.attacksp");
    }
    public long getSplashTowerAttacksp() {
        return config.getInt("btd.attributes.upgrades.towers.splash.attacksp");
    }

    public Integer getZombieReward() {
        return config.getInt("btd.attributes.mobs.zombie.reward");
    }

    public Integer getGolemReward() {
        return config.getInt("btd.attributes.mobs.iron-golem.reward");
    }

    public Integer getWitchReward() {
        return config.getInt("btd.attributes.mobs.witch.reward");
    }

    public Integer getEmanReward() {
        return config.getInt("btd.attributes.mobs.enderman.reward");
    }

    public Integer getPiglinReward() {
        return config.getInt("btd.attributes.mobs.piglin.reward");
    }

    public Integer getBlazeReward() {
        return config.getInt("btd.attributes.mobs.blaze.reward");
    }

    public double getZombieEffectRadius() {
        return config.getInt("btd.attributes.mobs.universal.effect-radius");
    }
}
