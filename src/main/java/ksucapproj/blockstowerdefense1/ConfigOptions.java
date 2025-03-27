package ksucapproj.blockstowerdefense1;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigOptions {

    private final JavaPlugin plugin;
    private final FileConfiguration config;

    public ConfigOptions(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    // Save the config file
    private void saveConfig() {
        plugin.saveConfig();  // Save changes made to the config file
    }

    // Reload the config file (useful for runtime updates)
    public void reloadConfig() {
        plugin.reloadConfig();  // Reload config.yml to apply any manual changes
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();  // Ensures the config.yml is created if not already present
    }

    public void getConfig(){
        plugin.getConfig();
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


    // Getter for Sweeping-Edge level max upgrade
    public int getSweepingEdgeMaxLevel() {
        return config.getInt("server.upgrades.player.sword.sweeping-edge.max-level", 5);  // Default value is 5 if not set
    }


    public int getSwordMaterialMaxLevel() {
        // 1: wood, 2: stone, 3: iron, 4: diamond, 5: netherite
        return config.getInt("server.upgrades.player.sword.material.max-level", 5);  // Default value is 5 if not set
    }



}
