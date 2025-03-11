package ksucapproj.blockstowerdefence1;

import org.bukkit.plugin.java.JavaPlugin;

public class BlocksTowerDefence1 extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("BlocksTowerDefence1 has been enabled!");

        // Register command and event listeners
        getCommand("summontower").setExecutor(new SummonTowerCommand(this));
        getCommand("spawnzombie").setExecutor(new SpawnZombieCommand(this));
        getServer().getPluginManager().registerEvents(new MobHandler(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("BlocksTowerDefence1 has been disabled!");
    }
}
