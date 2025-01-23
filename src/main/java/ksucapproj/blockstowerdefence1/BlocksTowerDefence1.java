package ksucapproj.blockstowerdefence1;

import org.bukkit.plugin.java.JavaPlugin;

public final class BlocksTowerDefence1 extends JavaPlugin {


    //this will run when the server runs
    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().warning("Hello World");
    }


    //this will run when the server ends
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
