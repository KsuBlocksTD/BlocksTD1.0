package me.matthewTest.pluginTest;

import io.papermc.paper.util.Tick;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.time.Duration;

public final class PluginTest extends JavaPlugin {

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

    /*
    private void registerCommands(String... commands) {
        MultiCommandHandler handler = new MultiCommandHandler();
        for (String command : commands) {
            if (getCommand(command) != null) {
                getCommand(command).setExecutor(handler);
                getLogger().info("Registered command: /" + command);
            } else {
                getLogger().warning("Command not found in plugin.yml: /" + command);
            }
        }
    }
     */
}
