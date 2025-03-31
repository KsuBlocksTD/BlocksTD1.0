package ksucapproj.blockstowerdefense1;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigOptions {
    private FileConfiguration config;  // Remove `final` so we can update it after loading

    public ConfigOptions(JavaPlugin plugin) {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }



    /*

    //////////////////////// For Upgrades Related to Potion Effects /////////////////////////////
    * If the maximum desired tier of an effect is n, use n-1 as the default
        - ex: if speed 5 is the desired maximum, choose 4 as the max default

     */


    public String getDatabaseURL() {
        String url = config.getString("server.database.url");

        // Debugging Output
        if (url == null || url.isEmpty()) {
            Bukkit.getLogger().severe("[ConfigOptions] ERROR: Database URL is missing from config.yml!");
        } else {
            Bukkit.getLogger().info("[ConfigOptions] Loaded Database URL: " + url);
        }

        return url;
    }


    // Getter for Speed level max upgrade
    public int getSpeedMaxLevel() {
        return config.getInt("server.attributes.upgrades.player.speed.max-level", 5);  // Default value is 5 if not set
    }
    public int getSpeedBaseCost() {
        return config.getInt("server.attributes.upgrades.player.speed.cost", 500); // Default value is 500 if not set
    }


    // Getter for Slowness level max upgrade
    public int getSlownessMaxLevel() {
        return config.getInt("server.attributes.upgrades.player.sword.slowness.max-level", 5);  // Default value is 5 if not set
    }
    public int getSlownessBaseCost(){
        return config.getInt("server.attributes.upgrades.player.sword.slowness.cost", 400); // Default value is 400 if not set
    }
    public int getSlownessDuration() {
        return config.getInt("server.attributes.upgrades.player.sword.slowness.duration", 10); // Default value is 10 (ticks) if not set
    }

    public boolean getSlownessDurationIncreaseOnUpgrade() {
        return config.getBoolean("server.attributes.upgrades.player.sword.slowness.duration.increase-duration-on-upgrade", false); // Default value is false if not set
    }


    // Getter for Strength level max upgrade
    public int getStrengthMaxLevel() {
        return config.getInt("server.attributes.upgrades.player.strength.max-level", 5);  // Default value is 5 if not set
    }
    public int getStrengthBaseCost() {
        return config.getInt("server.attributes.upgrades.player.strength.cost", 750);
    }


    // Getter for Sweeping-Edge level max upgrade
    public int getSweepingEdgeMaxLevel() {
        return config.getInt("server.attributes.upgrades.player.sword.sweeping-edge.max-level", 5);  // Default value is 5 if not set
    }
    public int getSweepingEdgeBaseCost() {
        return config.getInt("server.attributes.upgrades.player.sword.sweeping-edge.cost", 400);  // Default value is 600 if not set
    }


    public int getSwordMaterialMaxLevel() {
        // 1: wood, 2: stone, 3: iron, 4: diamond, 5: netherite
        return config.getInt("server.attributes.upgrades.player.sword.material.max-level", 5);  // Default value is 5 if not set
    }
    public int getSwordMaterialBaseCost() {
        // 1: wood, 2: stone, 3: iron, 4: diamond, 5: netherite
        return config.getInt("server.attributes.upgrades.player.sword.material.cost", 400);  // Default value is 400 if not set
    }



    // Getter for the message of the day for the server
    public String getMOTDOnPlayerJoin(){
        if (config.getBoolean("server.settings.motd.enable", false)){
            return config.getString("server.settings.motd.message");
        }
        return null;
    }

    // Getter for the greeting of a player on join-in
    public String getGreetOnPlayerJoin(){
        if (config.getBoolean("server.settings.greet-player.enable", true)){
            return config.getString("server.settings.greet-player.message");
        }
        return null;
    }


    public int getBasicTowerCost(){
        return config.getInt("server.attributes.upgrades.towers.basic.cost", 500);
    }

    public int getFastTowerCost(){
        return config.getInt("server.attributes.upgrades.towers.fast.cost", 300);
    }

    public int getSniperTowerCost(){
        return config.getInt("server.attributes.upgrades.towers.sniper.cost", 750);
    }

    public int getSplashTowerCost(){
        return config.getInt("server.attributes.upgrades.towers.splash.cost", 900);
    }

    public int getSlowTowerCost(){
        return config.getInt("server.attributes.upgrades.towers.slow.cost", 450);
    }

}
