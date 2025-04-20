package ksucapproj.blockstowerdefense1.logic;

import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.logic.game_logic.Economy;
import ksucapproj.blockstowerdefense1.logic.game_logic.PlayerUpgrades;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;

/*
    ** How DatabaseManager is Utilized **

    -- In BlocksTowerDefense1 --

        - CompletableFuture.supplyAsync(DatabaseManager::connect)
                .thenAccept(conn -> {
                    // sets DBconnection as the initialized conn value
                    BlocksTowerDefense1.getInstance().setDBConnection(conn);
                    Bukkit.getLogger().info("[BlocksTowerDefense1.0] DB connection established!");
                });
        ------------------------------------------------------------------------------------------
        ~ Before onEnable(), in the BTD1 class, an object "private Connection dbConnection" is created null
        to be later fulfilled in onEnable() with this implementation
        ~ CompletableFuture creates an async thread to have the database's connection to be done off the main thread
        ~ the aforementioned 'dbConnection' is instantiated with this connection when it is finished

        ~ This connection is saved with getter/setter functions for retrieval at a later time

        ~ This database connection is closed in onDisable() upon server close as well
 */

public class DatabaseManager {

    private static final JavaPlugin plugin = BlocksTowerDefense1.getInstance();
    private static Connection conn;
    private static boolean tableCreated;

    public DatabaseManager(){
        conn = BlocksTowerDefense1.getInstance().getDBConnection();
        tableCreated = false;
    }

    /*
        -- TO-DO LIST FOR DATABASE
    * Create database connection upon server loading (in onEnable() )
        - Hopefully get to creating an async thread, create database connection on it, and move it back to main thread
        - Save this creation to a getter (like the getInstance() functions below onDisable() ) to be grabbed later
    * Create a createTable function that would create the table in case it doesn't exist
        - all functions try, and then catch the error code of the table not existing, then create the table in the catch
        - once created, retry the function that generated the error (potentially)
        - also might have to create a .db file to store this created table in

    * scheduler.runTaskAsynchronously() might be the way to make the db connection asynchronously, and then come back
    to main thread in the function itself?

     */

    // getDataFolder() = btd's plugins folder, getPath() = gets the path to wherever the btd folder/file is


    // eventually needs to be done once the server starts rather than being called when each function needs it
    public static Connection connect(){

        File dbFile = new File(BlocksTowerDefense1.getInstance().getDataFolder(), "btd_db.db");
        boolean isNew = !dbFile.exists();

        // this is the url for the db that is in plugins/btd1/test_db.db
        String URL = "jdbc:sqlite:" + dbFile.getPath();

        try {
            Class.forName("org.sqlite.JDBC");
            // initializes the connection to the DB to the URL of the DB ^
            conn = DriverManager.getConnection(URL);

            // just a confirmation msg
            if (isNew) { // if the db file does not already exist
                Bukkit.getLogger().severe("[BlocksTowerDefense] Database file does not exist! Creating new file.");
            } else { // if db file previously exists
                Bukkit.getLogger().warning("[BlocksTowerDefense] Using existing database file.");
            }

            // if not already created, attempts to create the database table
            if (!tableCreated){
                createPlayersTable(conn);
            }
        }

        // catches: JDBC driver DNE or SQL Exception

        catch (ClassNotFoundException e) {
            Bukkit.getLogger().severe("[BlocksTowerDefense] SQLite JDBC driver not found.");
            e.printStackTrace();
        }
        catch (SQLException e) {
            Bukkit.getLogger().warning("[BlocksTowerDefense] SQL error: " + e.getMessage());
            Bukkit.getLogger().warning("[BlocksTowerDefense] Error code: " + e.getErrorCode());
            Bukkit.getLogger().warning("[BlocksTowerDefense] SQL state: " + e.getSQLState());
        }

        return conn;
    }


    public static void createPlayersTable(Connection conn) throws SQLException{

        String sql = """
        CREATE TABLE IF NOT EXISTS players (
            uuid VARCHAR(36) NOT NULL UNIQUE,
            name VARCHAR(16) NOT NULL UNIQUE,
            total_games_played INTEGER NOT NULL,
            total_wins INTEGER NOT NULL,
            fastest_win_in_seconds INTEGER,
            total_coins_gained INTEGER NOT NULL,
            total_coins_spent INTEGER NOT NULL,
            total_towers_bought INTEGER NOT NULL,
            total_upgrades_bought INTEGER NOT NULL,
            first_join_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            PRIMARY KEY(uuid)
        );
        """;

        try{
            Statement createTable = conn.createStatement();
            createTable.execute(sql);
        }

        catch (SQLException e){
            Bukkit.getLogger().warning("[BlocksTowerDefense] SQL error: " + e.getMessage());
            Bukkit.getLogger().warning("[BlocksTowerDefense] Error code: " + e.getErrorCode());
            Bukkit.getLogger().warning("[BlocksTowerDefense] SQL state: " + e.getSQLState());
        }

        tableCreated = true;
    }


    // this function is what inserts a new player into the server's DB, taking in their UUID, username, and time of joining
    // if a player is to be modified but doesn't exist, this function is called to create them at that moment
    private static void insertPlayer(Connection conn, String uuidAsString, String name) throws SQLException{
        String sql = "INSERT INTO players (uuid, name, total_games_played, total_wins, total_coins_gained," +
                " total_coins_spent, total_towers_bought, total_upgrades_bought) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // uses prepared statements to avoid SQL injection
        PreparedStatement pstmt = conn.prepareStatement(sql);

        // these are the parameters that are denoted as ? in the sql String above ^
        pstmt.setString(1, uuidAsString);
        pstmt.setString(2, name);
        pstmt.setInt(3, 0);
        pstmt.setInt(4, 0);
        pstmt.setInt(5, 0);
        pstmt.setInt(6, 0);
        pstmt.setInt(7, 0);
        pstmt.setInt(8, 0);
        // this actually runs the insert
        pstmt.executeUpdate();

        // confirmation message
        Bukkit.getLogger().info("[BlocksTowerDefense] Inserted: " + uuidAsString + " (username = " + name + ")"); // confirmation msg
    }


    // this function checks against the database with the player's UUID to detect if they already exist or not
    private static boolean userExists(Connection conn, String uuidAsString) throws SQLException {
        String sql = "SELECT 1 FROM players WHERE uuid = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuidAsString);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();  // if a result exists, the user is already in the database
        }

    }



    // this function will try insert a new player on join
    // if they do exist, the function ends, if they do not, it calls insertPLayer() to insert them
    public static void checkPlayerInDB(Player player, int maxRetries){

        if (maxRetries <= 0){
            Bukkit.getLogger().warning("[BlocksTowerDefense] Check if " + player.getName() + " is in database failed.");
            return;
        }

        // gets the argument player's username
        String uuidString = player.getUniqueId().toString();

        try {
            if (conn != null) { // if database connection works, continue
                Bukkit.getLogger().info("[BlocksTowerDefense] Connected to SQLite database."); // confirmation msg

                // helper method
                if (DatabaseManager.userExists(conn, uuidString)){ // if player exists, finish
                    Bukkit.getLogger().info("[BlocksTowerDefense] Player exists in database, returning.");// confirmation msg
                }

                // helper method
                else { // if player does not exist, add them to db
                    DatabaseManager.insertPlayer(conn, uuidString, player.getName() ); // adds their uuid and username
                }

            }

            else { // if conn is null, attempt 3 tries to establish and retry database call
                conn = connect();
                checkPlayerInDB(player, maxRetries - 1);
            }
        }

        catch (SQLException e){
            Bukkit.getLogger().warning("[BlocksTowerDefense] SQL error: " + e.getMessage());
            Bukkit.getLogger().warning("[BlocksTowerDefense] Error code: " + e.getErrorCode());
            Bukkit.getLogger().warning("[BlocksTowerDefense] SQL state: " + e.getSQLState());
        }
    }



    // this function is designed to update a player's information in the db at the end of a game
    public static void updatePlayerData(PlayerUpgrades upgrades, boolean victoryStatus, int maxRetries){
        if (upgrades.getPlayer() == null) {
            return;
        }

        if (maxRetries <= 0){
            Bukkit.getLogger().warning("[BlocksTowerDefense] Update to " + upgrades.getPlayer().getName() + "'s data failed.");
            return;
        }

        try {
            if (conn != null){ // if database connection works, continue
                Bukkit.getLogger().info("[BlocksTowerDefense] Connected to SQLite database."); // confirmation msg

                // if player exists, continue, if not, insert them into db before updating their attributes
                // this check is only done in case the player somehow does not exist


                checkPlayerInDB(upgrades.getPlayer(), maxRetries);

                // calls the method to total player values on game end
                insertPlayerTotalsOnGameEnd(conn, upgrades, victoryStatus);
            }

            else { // if conn is null, attempt 3 tries to establish and retry database call
                conn = connect();
                updatePlayerData(upgrades, victoryStatus,maxRetries - 1);
            }
        }

        catch (SQLException e) {
            Bukkit.getLogger().warning("[BlocksTowerDefense] SQL error: " + e.getMessage());
            Bukkit.getLogger().warning("[BlocksTowerDefense] Error code: " + e.getErrorCode());
            Bukkit.getLogger().warning("[BlocksTowerDefense] SQL state: " + e.getSQLState());
        }
    }


    // NEEDS
    // this takes the player's totals from the game they played and updates their current db values
    private static void insertPlayerTotalsOnGameEnd(Connection conn, PlayerUpgrades upgrades, boolean victory) throws SQLException{
        String sql = """
            UPDATE players
            SET
            total_games_played = total_games_played + ?,
            total_coins_gained = total_coins_gained + ?,
            total_coins_spent = total_coins_spent + ?,
            total_towers_bought = total_towers_bought + ?,
            total_wins = total_wins + ?,
            total_upgrades_bought = total_upgrades_bought + ?
            WHERE uuid = ?;
            """;

        String uuidString = upgrades.getPlayer().getUniqueId().toString();
        Player player = upgrades.getPlayer();
        Economy playerEcon = Economy.getPlayerEconomies().get(player);

        PreparedStatement pstmt = conn.prepareStatement(sql);



        /*

        /*-- ALREADY ADDED --
        * 1, total games played // leaderboard
        * 2, total coins gained // leaderboard
        * 3, total coins spent // leaderboard
        * 6, total upgrades bought // leaderboard
        * 7, uuid // needed for leaderboard

        -- NEEDS --
        * 4, total towers bought // not a leaderboard yet
        * 5, total wins*/ // leaderboard




        pstmt.setInt(1, 1);
        pstmt.setInt(2, playerEcon.getTotalCoinsGained());
        pstmt.setInt(3, playerEcon.getTotalCoinsSpent());
        pstmt.setInt(4, 0); // zero as temp value

        // zero if the player loses, one if they win
        pstmt.setInt(5, victory ? 1 : 0);

        pstmt.setInt(6, upgrades.getTotalUpgradesBought());
        pstmt.setString(7, uuidString);
        pstmt.executeUpdate();






    }


}
