package ksucapproj.blockstowerdefense1;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigOptions {
    private FileConfiguration config;  // Remove `final` so we can update it after loading

    public ConfigOptions(JavaPlugin plugin) {
        config = plugin.getConfig();
    }



    /*

    //////////////////////// For Upgrades Related to Potion Effects /////////////////////////////
    * If the maximum desired tier of an effect is n, use n-1 as the default
        - ex: if speed 5 is the desired maximum, choose 4 as the max default

     */




    // Getter for Speed level max upgrade
    public int getSpeedMaxLevel() {
        return config.getInt("server.upgrades.player.speed.max-level", 5);  // Default value is 5 if not set
    }
    public int getSpeedBaseCost() {
        return config.getInt("server.upgrades.player.speed.cost", 500);
    }


    // Getter for Slowness level max upgrade
    public int getSlownessMaxLevel() {
        return config.getInt("server.upgrades.player.sword.slowness.max-level", 5);  // Default value is 5 if not set
    }

    public int getSlownessDuration() {
        return config.getInt("server.upgrades.player.sword.slowness.duration", 10); // Default value is 10 (ticks) if not set
    }

    public boolean getSlownessDurationIncreaseOnUpgrade() {
        return config.getBoolean("server.upgrades.player.sword.slowness.duration.increase-duration-on-upgrade", false); // Default value is false if not set
    }


    // Getter for Strength level max upgrade
    public int getStrengthMaxLevel() {
        return config.getInt("server.upgrades.player.strength.max-level", 5);  // Default value is 5 if not set
    }
    public int getStrengthBaseCost() {
        return config.getInt("server.upgrades.player.strength.cost", 750);
    }


    // Getter for Sweeping-Edge level max upgrade
    public int getSweepingEdgeMaxLevel() {
        return config.getInt("server.upgrades.player.sword.sweeping-edge.max-level", 5);  // Default value is 5 if not set
    }


    public int getSwordMaterialMaxLevel() {
        // 1: wood, 2: stone, 3: iron, 4: diamond, 5: netherite
        return config.getInt("server.upgrades.player.sword.material.max-level", 5);  // Default value is 5 if not set
    }




    public String getMOTDOnPlayerJoin(){
        if (config.getBoolean("server.settings.greet-player.enable")){
            return config.getString("server.settings.greet-player.message");
        }
        return null;
    }

    public int getBasicTowerCost(){
        return config.getInt("server.economy.towers.basic.cost", 500);
    }

    public int getFastTowerCost(){
        return config.getInt("server.economy.towers.fast.cost", 300);
    }

    public int getSniperTowerCost(){
        return config.getInt("server.economy.towers.sniper.cost", 750);
    }

    public int getSplashTowerCost(){
        return config.getInt("server.economy.towers.splash.cost", 900);
    }

    public int getSlowTowerCost(){
        return config.getInt("server.economy.towers.slow.cost", 450);
    }

}
