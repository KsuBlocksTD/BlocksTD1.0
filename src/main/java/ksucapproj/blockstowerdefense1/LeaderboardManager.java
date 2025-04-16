package ksucapproj.blockstowerdefense1;

import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class LeaderboardManager {

    public record TopSpender(UUID uuid, int totalCoinsSpent) { }

    private static Connection conn;
    private final List<TopSpender> topSpenders = new ArrayList<>();
    private final Map<UUID, Integer> playerRankMap = new HashMap<>();

    public LeaderboardManager(){
        conn = BlocksTowerDefense1.getInstance().getDBConnection();
    }


    public void updateLeaderboard() {
        topSpenders.clear();
        playerRankMap.clear();

        try {
            if (conn != null){
                Bukkit.getLogger().info("DB connection is valid: " + (conn != null));
                PreparedStatement ps = conn.prepareStatement("SELECT uuid, total_coins_spent FROM players ORDER BY total_coins_spent DESC LIMIT 100");
                ResultSet rs = ps.executeQuery();

                int rank = 1;
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    int spent = rs.getInt("total_coins_spent");

                    TopSpender spender = new TopSpender(uuid, spent);
                    topSpenders.add(spender);
                    playerRankMap.put(uuid, rank);
                    rank++;
                }

            }

            else {
                Bukkit.getLogger().info("DB connection is valid: " + (conn != null));
            }
        }


        catch (SQLException e){
            Bukkit.getLogger().warning("[BlocksTowerDefense1.0] SQL error: " + e.getMessage());
            Bukkit.getLogger().warning("[BlocksTowerDefense1.0] Error code: " + e.getErrorCode());
            Bukkit.getLogger().warning("[BlocksTowerDefense1.0] SQL state: " + e.getSQLState());
        }

    }

    public Optional<Integer> getCoinsSpentBy(UUID uuid) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT total_coins_spent FROM players WHERE uuid = ?");
            ps.setString(1, uuid.toString());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(rs.getInt("total_coins_spent"));
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[BTD] SQL error fetching coins spent for " + uuid + ": " + e.getMessage());
        }

        return Optional.empty();
    }

    public List<TopSpender> getTopSpenders() {
        return topSpenders;
    }

    public Optional<Integer> getPlayerRank(UUID uuid) {
        return Optional.ofNullable(playerRankMap.get(uuid));
    }

    public Optional<TopSpender> getPlayerEntry(UUID uuid) {
        return topSpenders.stream().filter(ts -> ts.uuid().equals(uuid)).findFirst();
    }
}