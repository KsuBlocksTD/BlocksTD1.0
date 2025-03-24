package ksucapproj.blockstowerdefense1.logic;

import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.placeholderAPI.PlaceholderAPIExpansion;
import me.clip.placeholderapi.PlaceholderAPI;
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

        catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC driver not found.");
            e.printStackTrace();
        }

        catch (SQLException e) {
            System.out.println("Database connection error.");
            e.printStackTrace();
        }
        return conn;
    }


    public static void insertPlayer(Connection conn, String uuidAsString, String name) throws SQLException {
        String sql = "INSERT INTO players (uuid, name) VALUES (?, ?)";

        PreparedStatement pstmt = conn.prepareStatement(sql);

        pstmt.setString(1, uuidAsString);
        pstmt.setString(2, name);
        pstmt.executeUpdate();

        Bukkit.getLogger().info("Inserted: " + uuidAsString + " (username = " + name + ")"); // confirmation msg
    }


    public static boolean userExists(Connection conn, String uuidAsString) throws SQLException {
        String sql = "SELECT 1 FROM players WHERE uuid = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuidAsString);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();  // if a result exists, the user is already in the database
        }
    }

    public static void checkPlayerInDB(Player player){ // has helper methods to create more concise code

        String uuidString = player.getUniqueId().toString();

        try (Connection conn = DatabaseManager.connect()) { // creates connection to the db
            if (conn != null) { // if database connection works
                System.out.println("Connected to SQLite database.");

                if ((DatabaseManager.userExists(conn, uuidString))){
                    Bukkit.getLogger().info("Player exists in db, no add necessary"); // confirmation msg
                }
                else{ // if player does not exist, add them to db
                    DatabaseManager.insertPlayer(conn, uuidString, player.getName() ); // adds their uuid and username
                }
            }
        }

        catch (SQLException e) {
            e.printStackTrace();
        }


    }


}
