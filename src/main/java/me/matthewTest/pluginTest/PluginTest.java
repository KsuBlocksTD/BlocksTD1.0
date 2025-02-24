package me.matthewTest.pluginTest;

import io.papermc.paper.util.Tick;
import me.matthewTest.pluginTest.commands.mtdCommand;
import me.matthewTest.pluginTest.logic.exampleListener;
import me.matthewTest.pluginTest.logic.multiCommandHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import java.time.Duration;

public final class PluginTest extends JavaPlugin {
    // TO REBUILD THE ARTIFACT: F5

    @Override
    public void onEnable() {
        // Plugin startup logic
        BukkitScheduler scheduler = this.getServer().getScheduler(); // For async tasking


        getLogger().warning("Plugin injected");
        getServer().getPluginManager().registerEvents(new exampleListener(), this);
        getCommand("test").setExecutor(new multiCommandHandler());
        getCommand("hello").setExecutor(new multiCommandHandler());
        getCommand("mtd").setExecutor(new mtdCommand(this));
        scheduler.runTaskTimerAsynchronously(this, new asyncTest(this), 20, Tick.tick().fromDuration(Duration.ofMinutes(15)));
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().warning("Plugin uninjected");

    }
}
