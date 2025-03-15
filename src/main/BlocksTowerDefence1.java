package ksucapproj.blockstowerdefence1;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.util.Tick;
import ksucapproj.blockstowerdefence1.commands.MtdCommand;
import ksucapproj.blockstowerdefence1.commands.TestCommand;
import ksucapproj.blockstowerdefence1.logic.EventListener;
import ksucapproj.blockstowerdefence1.logic.Economy;
import ksucapproj.blockstowerdefence1.logic.StartGame;
import ksucapproj.blockstowerdefence1.placeholderAPI.PlaceholderAPIExpansion;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.time.Duration;


public class BlocksTowerDefence1 extends JavaPlugin {

    private static PartiesAPI api;
    private static BlocksTowerDefence1 instance;

    @Override
    public void onEnable() {
        StartGame startGame = new StartGame(this);
        getLogger().info("BlocksTowerDefence1 has been enabled!");

        // Register command and event listeners
        getCommand("summontower").setExecutor(new SummonTowerCommand(this));
        getCommand("startgame").setExecutor(startGame);
        getCommand("readyup").setExecutor(startGame);

        getServer().getPluginManager().registerEvents(new MobHandler(this), this);

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
        getLogger().info("BlocksTowerDefence1 has been disabled!");
    }
}
