package ksucapproj.blockstowerdefense1;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.util.Tick;
import ksucapproj.blockstowerdefense1.commands.*;
import ksucapproj.blockstowerdefense1.logic.AsyncTest;
import ksucapproj.blockstowerdefense1.logic.DatabaseManager;
import ksucapproj.blockstowerdefense1.logic.game_logic.*;
import ksucapproj.blockstowerdefense1.logic.game_logic.towers.Tower;
import ksucapproj.blockstowerdefense1.maps.MapData;
import ksucapproj.blockstowerdefense1.placeholderAPI.PlaceholderAPIExpansion;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;


public class BlocksTowerDefense1 extends JavaPlugin {

    private static PartiesAPI api;
    private static BlocksTowerDefense1 instance;
    private StartGame gameManager;
    private ConfigOptions config;
    private Connection dbConnection;


    public BlocksTowerDefense1() {
        instance = this; // Ensure instance is assigned immediately
    }


    @Override
    public void onEnable() {
        getLogger().info("[BlocksTowerDefense1.0] Initializing BlocksTowerDefense1");
        api = Parties.getApi(); // For static api getter


        instance.saveDefaultConfig();  // Ensures the config is saved if it doesn't exist
        instance.reloadConfig();       // Ensures the latest config is loaded

        saveResource("config.yml", /* replace */ false);
        config = new ConfigOptions(this); // initializes config object

        // confirmation msgs if config is initialized as null ICE
        if (config == null) {
            getLogger().severe("[BlocksTowerDefense1] ERROR: ConfigOptions failed to initialize!");
        } else {
            getLogger().info("[BlocksTowerDefense1] ConfigOptions initialized successfully.");
        }


        gameManager = new StartGame(this, api);


        // Use the same gameManager instance for PlayerEventHandler
        new MobHandler(this);
        new PlayerEventHandler(this, gameManager);
        TestCommand testCommand = new TestCommand(gameManager, this);

        MapData.loadMaps(this);


        getServer().getPluginManager().registerEvents(new MobHandler(this), this);


        BukkitScheduler scheduler = this.getServer().getScheduler(); // For async tasking

        // initializes PLaceholderAPI to be used in its class
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPIExpansion(this).register();
        }


        // Creates connection to the DB asynchronously
        CompletableFuture.supplyAsync(DatabaseManager::connect)
                .thenAccept(conn -> {
                    // sets DBconnection as the initialized conn value
                    BlocksTowerDefense1.getInstance().setDBConnection(conn);
                    Bukkit.getLogger().info("[BlocksTowerDefense1.0] DB connection established!");
                });


        // **** This might be better off moved to the game session creation ****
        // needed for instantiating proper mob killing & economy function
        new Economy(); // Creating economy object


        // this is solely for recompiling the server and keeping a working economy while players are still online
//        Economy.playerCountFix();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            // register main commands here
            commands.registrar().register(TestCommand.flightCommand()); // is a test cmd
            commands.registrar().register(TestCommand.constructGiveItemCommand()); // is a test cmd
            commands.registrar().register(TestCommand.addCoinsCommand()); // has btd functionality
            commands.registrar().register(TestCommand.giveCoinsCommand()); // has btd functionality
            commands.registrar().register(MtdCommand.register());
            commands.registrar().register(SpawnCommand.register());
            commands.registrar().register(ApplyUpgradeCommand.register());
            commands.registrar().register(MapCommand.mapCommand());
            // register gamemanager commands
            commands.registrar().register(testCommand.setRoundCommand());
            commands.registrar().register(testCommand.startGameCommand());
            commands.registrar().register(testCommand.readyUpCommand());
            commands.registrar().register(testCommand.quitGameCommand());

        });

        scheduler.runTaskTimerAsynchronously(this, new AsyncTest(this), 20, Tick.tick().fromDuration(Duration.ofMinutes(15)));

        // This needs to go into a config/function at some point
        World world = Bukkit.getWorlds().get(0);
        if (world != null) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.DO_MOB_LOOT, false);
            world.setTime(1000);
            getLogger().info("[BlocksTowerDefense1.0] Weather and daylight cycle auto-disabled.");
        }

        getLogger().warning("[BlocksTowerDefense1.0] Plugin injected");
    }


    @Override
    public void onDisable() {

//        instance.reloadConfig();
        instance.saveConfig();
        MapData.saveMaps();

        try {
            dbConnection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        MobHandler.cleanupAll();
        Tower.removeAllTowers();

        for (Party party : api.getOnlineParties()){
            party.delete();
        }

        for (Player player : PlayerUpgrades.getPlayerUpgradesMap().keySet()){
            PlayerUpgrades.getPlayerUpgradesMap().remove(player);
        }

        getLogger().info("[BlocksTowerDefense1.0] Disabled BlocksTowerDefense1");
    }


    public static PartiesAPI getApi() {
        return api;
    }

    public StartGame getGameManager() {
        return gameManager;
    }

    public static BlocksTowerDefense1 getInstance() {
        if (instance == null) {
            Bukkit.getLogger().severe("[BlocksTowerDefense1] ERROR: getInstance() was called before initialization!");
            throw new IllegalStateException("BlocksTowerDefense1 instance is not yet initialized!");
        }
        return instance;
    }

    public ConfigOptions getBTDConfig() {
        return config;
    }

    private void setDBConnection(Connection conn) {
        dbConnection = conn;
    }
    public Connection getDBConnection() { return dbConnection;}
}