package ksucapproj.blockstowerdefense1;

import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class LeaderboardManager {

    public record LeaderboardEntry(UUID uuid, int value) { }

    private final Map<String, List<LeaderboardEntry>> leaderboards = new HashMap<>();
    private final Map<String, Map<UUID, Integer>> leaderboardRanks = new HashMap<>();

    private static final List<String> TRACKED_STATS = List.of(
            "fastest_win_in_seconds",
            "total_coins_gained",
            "total_coins_spent",
            "total_upgrades_bought",
            "total_games_played",
            "total_wins"
    );

    private Connection getConnection() {

        return BlocksTowerDefense1.getInstance().getDBConnection();
    }

    public void updateAllLeaderboards() {
        for (String stat : TRACKED_STATS) {
            updateLeaderboard(stat);
        }
    }

    public void updateLeaderboard(String statColumn) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        Map<UUID, Integer> ranks = new HashMap<>();

        if (getConnection() == null) {
            Bukkit.getLogger().warning("[BTD] Database connection is null!");
            return;
        }

        try {
            String query = "SELECT uuid, " + statColumn + " FROM players ORDER BY " + statColumn + " DESC LIMIT 100";
            PreparedStatement ps = getConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            int rank = 1;
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                int value = rs.getInt(statColumn);

                if (!isValidStatValue(statColumn, value)) continue; // <- Skip invalid/default values

                entries.add(new LeaderboardEntry(uuid, value));
                ranks.put(uuid, rank++);
            }

            leaderboards.put(statColumn, entries);
            leaderboardRanks.put(statColumn, ranks);

        }

        catch (SQLException e) {
            Bukkit.getLogger().severe("[BTD] SQL error updating leaderboard for '" + statColumn + "': " + e.getMessage());
        }
    }



    public Optional<Integer> getStatForPlayer(UUID uuid, String statColumn) {
        if (getConnection() == null) return Optional.empty();

        try {
            String query = "SELECT " + statColumn + " FROM players WHERE uuid = ?";
            PreparedStatement ps = getConnection().prepareStatement(query);
            ps.setString(1, uuid.toString());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(rs.getInt(statColumn));
            }
        }

        catch (SQLException e) {
            Bukkit.getLogger().severe("[BTD] SQL error fetching stat '" + statColumn + "' for player " + uuid + ": " + e.getMessage());
        }

        return Optional.empty();
    }

    public List<LeaderboardEntry> getLeaderboard(String statColumn) {
        return leaderboards.getOrDefault(statColumn, Collections.emptyList());
    }

    public Optional<Integer> getPlayerRank(UUID uuid, String statColumn) {
        Map<UUID, Integer> ranks = leaderboardRanks.get(statColumn);
        return ranks != null ? Optional.ofNullable(ranks.get(uuid)) : Optional.empty();
    }

    public Optional<LeaderboardEntry> getPlayerEntry(UUID uuid, String statColumn) {
        return getLeaderboard(statColumn).stream()
                .filter(entry -> entry.uuid().equals(uuid))
                .findFirst();
    }

    public List<String> getTrackedStats() {
        return TRACKED_STATS;
    }

    private boolean isValidStatValue(String stat, int value) {
        return switch (stat) {
            case "fastest_win_in_seconds" -> value > 0 && value < 9999; // Avoid uninitialized or absurdly long times
            default -> value > 0; // For everything else, 0 is meaningless
        };
    }


    public void checkAndUpdateRelevantLeaderboards(UUID playerUUID) {
        List<String> leaderboardStats = getTrackedStats();

        for (String stat : leaderboardStats) {
            getStatForPlayer(playerUUID, stat).ifPresent(newValue -> {
                List<LeaderboardEntry> topEntries = getLeaderboard(stat);

                if (topEntries.size() < 5) {
                    updateLeaderboard(stat);
                    return;
                }

                boolean isTimeStat = stat.equals("fastest_win_in_seconds");
                int threshold = topEntries.get(4).value();
                boolean qualifies = isTimeStat ? newValue < threshold : newValue > threshold;

                if (qualifies) {
                    updateLeaderboard(stat);
                }
            });
        }
    }
}
