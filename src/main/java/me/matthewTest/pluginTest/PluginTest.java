package me.matthewTest.pluginTest;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.util.Tick;
import me.matthewTest.pluginTest.commands.MtdCommand;
import me.matthewTest.pluginTest.commands.TestCommand;
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

    private static PartiesAPI api;
    private static PluginTest instance;

    @Override
    public void onEnable() {
        // Plugin startup logic

        api = Parties.getApi(); // For static api getter
        instance = this;
        Economy econ = new Economy(); // Creating economy object
        BukkitScheduler scheduler = this.getServer().getScheduler(); // For async tasking

        getServer().getPluginManager().registerEvents(new EventListener(), this);
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPIExpansion(this).register();
        }



        // needed for instantiating proper mob killing & economy function
        // this is solely for recompiling the server and keeping a working economy while players are still online
        Economy.playerCountFix();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            // register main commands here
            commands.registrar().register(TestCommand.flightCommand());
            commands.registrar().register(TestCommand.constructGiveItemCommand());
            commands.registrar().register(TestCommand.addCoinsCommand());
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
        getLogger().warning("Plugin injected");
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic

        for (Party party : api.getOnlineParties()){
            party.delete(); // deletes all remaining parties on server close
        }

        Bukkit.getScheduler().cancelTasks(this);
        getLogger().warning("Plugin uninjected");

    }


    public static PartiesAPI getApi() {
        return api;
    }

    public static PluginTest getInstance() {
        return instance;
    }
}
