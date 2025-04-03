package ksucapproj.blockstowerdefense1.logic;

import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.logic.game_logic.PlayerUpgrades;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;

public class DatabaseManager {

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

    // MIGHT BE USELESS, TEST IF NEEDED OR NOT
//    private static ConfigOptions getConfigOptions() {
//        BlocksTowerDefense1 instance = BlocksTowerDefense1.getInstance();
//        if (instance == null) {
//            throw new IllegalStateException("[DatabaseManager] ERROR: BlocksTowerDefense1 instance is null!");
//        }
//        ConfigOptions config = instance.getBTDConfig();
//        if (config == null) {
//            throw new IllegalStateException("[DatabaseManager] ERROR: ConfigOptions is null!");
//        }
//        return config;
//    }



    // eventually needs to be done once the server starts rather than being called when each function needs it
    public static Connection connect(){
        Connection conn = null;

        // this is the url for the db that is in plugins/btd1/test_db.db
        // getDataFolder() = btd's plugins folder, getPath() = gets the path to wherever the btd folder is
        String URL = "jdbc:sqlite:" + BlocksTowerDefense1.getInstance().getDataFolder().getPath()+"/test_db.db";
        try {
            Class.forName("org.sqlite.JDBC");
            // initializes the connection to the DB to the URL of the DB ^
            conn = DriverManager.getConnection(URL);
        }

        // catches for the different type of errors
        catch (NullPointerException e){
            Bukkit.getLogger().warning("Database URL not located.");
            e.printStackTrace();
        }

        catch (ClassNotFoundException e) {
            Bukkit.getLogger().warning("SQLite JDBC driver not found.");
            e.printStackTrace();
        }

        catch (SQLException e) {
            Bukkit.getLogger().warning("Database connection error.");
            e.printStackTrace();
        }

        return conn;
    }


    public static void createDatabase(Connection conn) throws SQLException{

    }


    // this function is what inserts a new player into the server's DB, taking in their UUID, username, and time of joining
    // if a player is to be modified but doesn't exist, this function is called to create them at that moment
    private static void insertPlayer(Connection conn, String uuidAsString, String name) throws SQLException{
        String sql = "INSERT INTO players (uuid, name, total_games_played, total_wins, total_coins_gained," +
                " total_coins_spent, total_towers_bought, total_upgrades_bought) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // uses prepared statements to avoid SQL injection
        PreparedStatement pstmt = conn.prepareStatement(sql);

        // these are the parameters that are denoted as ? im the sql String above ^
        pstmt.setString(1, uuidAsString);
        pstmt.setString(2, name);
        pstmt.setInt(3, 0);
        pstmt.setInt(4, 0);
        pstmt.setInt(5, 0);
        pstmt.setInt(6, 0);
        pstmt.setInt(7, 0);
        pstmt.setInt(8, 0);
        // this actually runs the query
        pstmt.executeUpdate();

        // confirmation message
        Bukkit.getLogger().info("Inserted: " + uuidAsString + " (username = " + name + ")"); // confirmation msg
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
    public static void checkPlayerInDB(Player player){ // has helper methods to create more concise code

        // gets the argument player's username
        String uuidString = player.getUniqueId().toString();

        try (Connection conn = DatabaseManager.connect()){ // creates connection to the db
            if (conn != null) { // if database connection works, continue
                Bukkit.getLogger().info("Connected to SQLite database."); // confirmation msg

                if (DatabaseManager.userExists(conn, uuidString)){ // if player exists, finish
                    Bukkit.getLogger().info("Player exists in database, returning.");// confirmation msg
                }

                else { // if player does not exist, add them to db
                    DatabaseManager.insertPlayer(conn, uuidString, player.getName() ); // adds their uuid and username
                }

            }
        }

        catch (SQLException e) {
            e.printStackTrace(); // getErrorCode, get error code for doesn't exist
        }
    }


    // this function is designed to update a player's information in the db at the end of a game
    public static void updatePlayerData(PlayerUpgrades upgrades){

        try (Connection conn = DatabaseManager.connect()){ // creates connection to the db
            if (conn != null){ // if database connection works, continue
                Bukkit.getLogger().info("Connected to SQLite database."); // confirmation msg

                // if player exists, continue, if not, insert them into db before updating their attributes
                // this check is only done in case the player somehow does not exist
                checkPlayerInDB(upgrades.getPlayer());

                // calls the function to total player values on game end
                insertPlayerTotalsOnGameEnd(conn, upgrades);
            }
        }

        catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // NEEDS
    // this takes the player's totals from the game they played and updates their current db values
    private static void insertPlayerTotalsOnGameEnd(Connection conn, PlayerUpgrades upgrades) throws SQLException{
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

        PreparedStatement pstmt = conn.prepareStatement(sql);

        /*
        pstmt.setInt(1, 1);
        pstmt.setInt(2, );
        pstmt.setInt(3, );
        pstmt.setInt(4, );
        pstmt.setInt(5, );
        pstmt.setInt(6, upgrades.getTotalUpgradesBought());
        pstmt.setString(7, uuidString);
        pstmt.executeUpdate();

         */




    }


}
