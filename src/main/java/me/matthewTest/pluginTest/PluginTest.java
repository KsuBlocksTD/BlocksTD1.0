package me.matthewTest.pluginTest;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.util.Tick;
import me.matthewTest.pluginTest.commands.mtdCommand;
import me.matthewTest.pluginTest.commands.testCommand;
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
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            //register main commands here
            commands.registrar().register(testCommand.flightCommand());
            commands.registrar().register(testCommand.constructGiveItemCommand());
            commands.registrar().register(mtdCommand.register());

            scheduler.runTaskTimerAsynchronously(this, new asyncTest(this), 20, Tick.tick().fromDuration(Duration.ofMinutes(15)));
        });
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().warning("Plugin uninjected");

    }
}
