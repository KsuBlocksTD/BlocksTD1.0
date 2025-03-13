package me.matthewTest.pluginTest;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.util.Tick;
import me.matthewTest.pluginTest.commands.MtdCommand;
import me.matthewTest.pluginTest.commands.TestCommand;
import me.matthewTest.pluginTest.commands.party.CreatePartyCommand;
import me.matthewTest.pluginTest.logic.Economy;
import me.matthewTest.pluginTest.logic.EventListener;
import me.matthewTest.pluginTest.placeholderAPI.PlaceholderAPIExpansion;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.time.Duration;

public final class PluginTest extends JavaPlugin {
    // TO REBUILD THE ARTIFACT: F5

    @Override
    public void onEnable() {
        // Plugin startup logic
        BukkitScheduler scheduler = this.getServer().getScheduler(); // For async tasking
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPIExpansion(this).register();
        }

        PartiesAPI api = Parties.getApi();
        CreatePartyCommand.setApi(api);



        // needed for instantiating proper mob killing & economy function
        Economy econ = new Economy();
        // this is solely for recompiling the server and keeping a working economy while players are still online
        Economy.playerCountFix();

        getLogger().warning("Plugin injected");
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            // register main commands here
            commands.registrar().register(TestCommand.flightCommand());
            commands.registrar().register(TestCommand.constructGiveItemCommand());
            commands.registrar().register(MtdCommand.register());

        });

        scheduler.runTaskTimerAsynchronously(this, new AsyncTest(this), 20, Tick.tick().fromDuration(Duration.ofMinutes(15)));


        World world = Bukkit.getWorlds().get(0);
        if (world != null) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setTime(1000);
            getLogger().info("Weather and daylight cycle auto-disabled.");
        }
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().warning("Plugin uninjected");

    }
}
