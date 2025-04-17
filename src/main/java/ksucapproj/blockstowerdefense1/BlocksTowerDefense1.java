package ksucapproj.blockstowerdefense1;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.util.Tick;
import ksucapproj.blockstowerdefense1.commands.*;
import ksucapproj.blockstowerdefense1.commands.mtd.MtdCommand;
import ksucapproj.blockstowerdefense1.logic.AsyncTest;
import ksucapproj.blockstowerdefense1.logic.DatabaseManager;
import ksucapproj.blockstowerdefense1.logic.game_logic.MobHandler;
import ksucapproj.blockstowerdefense1.logic.game_logic.PlayerEventHandler;
import ksucapproj.blockstowerdefense1.logic.game_logic.PlayerUpgrades;
import ksucapproj.blockstowerdefense1.logic.game_logic.StartGame;
import ksucapproj.blockstowerdefense1.logic.game_logic.towers.Tower;
import ksucapproj.blockstowerdefense1.maps.MapData;
import ksucapproj.blockstowerdefense1.placeholderAPI.PlaceholderAPIExpansion;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

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
    private LeaderboardManager leaderboardManager;


    public BlocksTowerDefense1() {
        instance = this; // Ensure instance is assigned immediately
    }


    @Override
    public void onEnable() {
        getLogger().info("[BlocksTowerDefense] Initializing BlocksTowerDefense1");
        api = Parties.getApi(); // For static api getter

        saveResource("config.yml", /* replace */ false);
        instance.reloadConfig();       // Ensures the latest config is loaded

        config = new ConfigOptions(this); // initializes config object

        // confirmation msgs if config is initialized as null ICE
        if (config == null) {
            getLogger().severe("[BlocksTowerDefense] ERROR: ConfigOptions failed to initialize!");
        } else {
            getLogger().info("[BlocksTowerDefense] ConfigOptions initialized successfully.");
        }

        gameManager = new StartGame(this, api);

        // Use the same gameManager instance for PlayerEventHandler
        new MobHandler(gameManager, this);
        new PlayerEventHandler(this, gameManager);
        GameCommand gameCommand = new GameCommand(gameManager, this);

        MapData.loadMaps(this);

        BukkitScheduler scheduler = this.getServer().getScheduler(); // For async tasking

        // Initialize LeaderboardManager here
        leaderboardManager = new LeaderboardManager();


        // Ensure the leaderboard update only happens once DB connection is ready
        CompletableFuture.supplyAsync(DatabaseManager::connect)
                .thenAccept(conn -> {
                    // Set DB connection
                    BlocksTowerDefense1.getInstance().setDBConnection(conn);
                    Bukkit.getLogger().info("[BlocksTowerDefense] DB connection established!");

                    // Now that DB connection is ready, we can safely start updating the leaderboard
                    startLeaderboardUpdateTask();
                })
                .exceptionally(ex -> {
                    getLogger().severe("[BlocksTowerDefense] Failed to connect to DB: " + ex.getMessage());
                    return null;
                });

        // Register PlaceholderAPI expansion immediately
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPIExpansion(this).register();
        }

        // **** This might be better off moved to the game session creation ****
        // needed for instantiating proper mob killing & economy function
        // this is solely for recompiling the server and keeping a working economy while players are still online
        //Economy.playerCountFix();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            // register main commands here
            commands.registrar().register(CoinsCommand.addCoinsCommand()); // has btd functionality
            commands.registrar().register(CoinsCommand.giveCoinsCommand()); // has btd functionality
            commands.registrar().register(MtdCommand.register());
            commands.registrar().register(SpawnCommand.register());
            commands.registrar().register(ApplyUpgradeCommand.register());
            commands.registrar().register(MapCommand.mapCommand());
            // register gamemanager commands
            commands.registrar().register(gameCommand.setRoundCommand());
            commands.registrar().register(gameCommand.startGameCommand());
            commands.registrar().register(gameCommand.readyUpCommand());
            commands.registrar().register(gameCommand.quitGameCommand());

            commands.registrar().register(ReloadLeaderboardsCommand.register());

        });

        scheduler.runTaskTimerAsynchronously(this, new AsyncTest(this), 20, Tick.tick().fromDuration(Duration.ofMinutes(15)));

        // This needs to go into a config/function at some point
        World world = Bukkit.getWorlds().getFirst();
        if (world != null) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.DO_MOB_LOOT, false);
            world.setTime(1000);
            getLogger().info("[BlocksTowerDefense] Weather and daylight cycle auto-disabled.");
        }

        getLogger().warning("[BlocksTowerDefense] Plugin injected");
    }


    @Override
    public void onDisable() {
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

        getLogger().info("[BlocksTowerDefense] Disabled BlocksTowerDefense1");
    }


    public static PartiesAPI getApi() {
        return api;
    }

    public StartGame getGameManager() {
        return gameManager;
    }

    public static BlocksTowerDefense1 getInstance() {
        if (instance == null) {
            Bukkit.getLogger().severe("[BlocksTowerDefense] ERROR: getInstance() was called before initialization!");
            throw new IllegalStateException("BlocksTowerDefense instance is not yet initialized!");
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

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }


    // New method to start the leaderboard update task
    private void startLeaderboardUpdateTask() {
        // Check if the DB connection is still available
        if (getDBConnection() != null) {
            // Schedule the leaderboard update task to run asynchronously
            Bukkit.getScheduler().runTaskTimerAsynchronously(
                    this,
                    () -> {
                        getLeaderboardManager().updateAllLeaderboards();
                    },
                    0L,
                    20L * 300 // Update every 5 minutes
            );
        } else {
            getLogger().severe("[BlocksTowerDefense] DB connection is null. Leaderboard update will not run.");
        }
    }
}