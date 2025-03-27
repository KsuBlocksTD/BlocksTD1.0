package ksucapproj.blockstowerdefense1.logic;

import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.logic.game_logic.PlayerUpgrades;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;

public class DatabaseManager {

    private static final String URL = BlocksTowerDefense1.getInstance().getConfig().getString("database.url"); // Change the path accordingly




    public static Connection connect(){
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(URL);
        }

        catch (NullPointerException e){
            Bukkit.getLogger().warning("Database URL not located.");
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


    private static void insertPlayer(Connection conn, String uuidAsString, String name) throws SQLException{
        String sql = "INSERT INTO players (uuid, name) VALUES (?, ?)";

        PreparedStatement pstmt = conn.prepareStatement(sql);

        pstmt.setString(1, uuidAsString);
        pstmt.setString(2, name);
        pstmt.executeUpdate();

        Bukkit.getLogger().info("Inserted: " + uuidAsString + " (username = " + name + ")"); // confirmation msg
    }


    private static boolean userExists(Connection conn, String uuidAsString) throws SQLException {
        String sql = "SELECT 1 FROM players WHERE uuid = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuidAsString);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();  // if a result exists, the user is already in the database
        }
    }

    public static void checkPlayerInDB(Player player){ // has helper methods to create more concise code

        String uuidString = player.getUniqueId().toString();

        try (Connection conn = DatabaseManager.connect()){ // creates connection to the db
            if (conn != null) { // if database connection works
                Bukkit.getLogger().info("Connected to SQLite database.");

                if (DatabaseManager.userExists(conn, uuidString)){
                    Bukkit.getLogger().info("Player exists in database, returning.");// confirmation msg
                }

                else { // if player does not exist, add them to db
                    DatabaseManager.insertPlayer(conn, uuidString, player.getName() ); // adds their uuid and username
                }

            }
        }

        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updatePlayerData(PlayerUpgrades upgrades){

        try (Connection conn = DatabaseManager.connect()){
            if (conn != null){
                Bukkit.getLogger().info("Connected to SQLite database.");

                checkPlayerInDB(upgrades.getPlayer());

                insertPlayerTotalsOnGameEnd(conn, upgrades);
            }
        }

        catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // NEEDS
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
