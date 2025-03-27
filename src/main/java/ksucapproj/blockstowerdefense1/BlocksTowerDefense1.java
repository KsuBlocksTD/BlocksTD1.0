package ksucapproj.blockstowerdefense1;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.util.Tick;
import ksucapproj.blockstowerdefense1.commands.ApplyUpgradeCommand;
import ksucapproj.blockstowerdefense1.commands.MtdCommand;
import ksucapproj.blockstowerdefense1.commands.SpawnCommand;
import ksucapproj.blockstowerdefense1.commands.TestCommand;
import ksucapproj.blockstowerdefense1.logic.AsyncTest;
import ksucapproj.blockstowerdefense1.logic.Economy;
import ksucapproj.blockstowerdefense1.logic.EventListener;
import ksucapproj.blockstowerdefense1.logic.game_logic.*;
import ksucapproj.blockstowerdefense1.maps.MapData;
import ksucapproj.blockstowerdefense1.placeholderAPI.PlaceholderAPIExpansion;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.time.Duration;


public class BlocksTowerDefense1 extends JavaPlugin {

    private static PartiesAPI api;
    private static BlocksTowerDefense1 instance;
    private StartGame gameManager;
    private ConfigOptions config;


    @Override
    public void onEnable() {
        getLogger().info("BlocksTowerDefence1 has been enabled!");

        config = new ConfigOptions(this);
        config.loadConfig();

        gameManager = new StartGame(this);

        // Register commands with the same instance
        getCommand("startgame").setExecutor(gameManager);
        getCommand("readyup").setExecutor(gameManager);
        getCommand("summontower").setExecutor(new SummonTowerCommand(this));
        getCommand("tdmap").setExecutor(new MapData.MapCommand());
        getCommand("tdmap").setTabCompleter(new MapData.MapCommand());


        // Use the same gameManager instance for PlayerEventHandler
        new MobHandler(this);
        new SummonTower(this);
        new PlayerEventHandler(this, gameManager);

        MapData.saveDefaultConfig(this);
        MapData.loadMaps(this);

        getServer().getPluginManager().registerEvents(new MobHandler(this), this);

        getServer().getPluginManager().registerEvents(new MobHandler(this), this);

        api = Parties.getApi(); // For static api getter
        instance = this;
        new Economy(); // Creating economy object
        BukkitScheduler scheduler = this.getServer().getScheduler(); // For async tasking

        getServer().getPluginManager().registerEvents(new EventListener(), this);
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPIExpansion(this).register();
        }



        // needed for instantiating proper mob killing & economy function
        // this is solely for recompiling the server and keeping a working economy while players are still online

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            // register main commands here
            commands.registrar().register(TestCommand.flightCommand());
            commands.registrar().register(TestCommand.constructGiveItemCommand());
            commands.registrar().register(TestCommand.addCoinsCommand());
            commands.registrar().register(TestCommand.giveCoinsCommand());
            commands.registrar().register(MtdCommand.register());
            commands.registrar().register(SpawnCommand.register());
            commands.registrar().register(ApplyUpgradeCommand.register());

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
        saveConfig();
        MapData.saveMaps();

        MobHandler.cleanupAll();
        SummonTower.removeAllTowers();

        for (Party party : api.getOnlineParties()){
            party.delete();
        }

        getLogger().info("BlocksTowerDefence1 has been disabled!");


        for (Party party : api.getOnlineParties()){
            party.delete();
        }
    }


    public static PartiesAPI getApi() {
        return api;
    }

    public StartGame getGameManager() {
        return gameManager;
    }

    public static BlocksTowerDefense1 getInstance() {
        return instance;
    }

    public ConfigOptions getBTDConfig() {
        return config;
    }
}