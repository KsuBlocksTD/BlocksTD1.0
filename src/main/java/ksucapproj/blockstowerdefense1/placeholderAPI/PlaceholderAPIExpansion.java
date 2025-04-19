package ksucapproj.blockstowerdefense1.placeholderAPI;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.ConfigOptions;
import ksucapproj.blockstowerdefense1.LeaderboardManager;
import ksucapproj.blockstowerdefense1.logic.game_logic.Economy;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final JavaPlugin plugin;
    private static final PartiesAPI api = BlocksTowerDefense1.getApi();
    public static final ConfigOptions config = BlocksTowerDefense1.getInstance().getBTDConfig();
    private static final LeaderboardManager leaderboard = BlocksTowerDefense1.getInstance().getLeaderboardManager();

    // Friendly name -> DB column
    private static final Map<String, String> statAliasMap = Map.of(
            "spent", "total_coins_spent",
            "gained", "total_coins_gained",
            "upgrades", "total_upgrades_bought",
            "wins", "total_wins",
            "games", "total_games_played",
            "fastest", "fastest_win_in_seconds"
    );

    public PlaceholderAPIExpansion(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mtd";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";

        // Config values
        switch (identifier.toLowerCase()) {
            case "coins":
                return Economy.getPlayerEconomies().get(player) == null ? "N/A" : formatNumber(Economy.getPlayerMoney(player));
            case "speedmaxlevel": return String.valueOf(config.getSpeedMaxLevel());
            case "slownessmaxlevel": return String.valueOf(config.getSlownessMaxLevel());
            case "slownessduration": return String.valueOf(config.getSlownessDuration());
            case "slownessdurationincreaseonupgrade": return String.valueOf(config.getSlownessDurationIncreaseOnUpgrade());
            case "strengthmaxlevel": return String.valueOf(config.getStrengthMaxLevel());
            case "sweepingedgemaxlevel": return String.valueOf(config.getSweepingEdgeMaxLevel());
            case "swordmaterialmaxlevel": return String.valueOf(config.getSwordMaterialMaxLevel());
        }

        // Leaderboard: mtd_top_<stat>_you
        if (identifier.startsWith("top_") && identifier.endsWith("_you")) {
            String friendlyStat = identifier.substring(4, identifier.length() - 4);
            String dbStat = statAliasMap.getOrDefault(friendlyStat, friendlyStat);

            return leaderboard.getStatForPlayer(player.getUniqueId(), dbStat)
                    .map(val -> formatStat(dbStat, val))
                    .orElse("0");
        }

        // Leaderboard rank: mtd_top_<stat>_you_rank
        if (identifier.startsWith("top_") && identifier.endsWith("_you_rank")) {
            String friendlyStat = identifier.substring(4, identifier.length() - "_you_rank".length());
            String dbStat = statAliasMap.getOrDefault(friendlyStat, friendlyStat);

            return leaderboard.getPlayerRank(player.getUniqueId(), dbStat)
                    .map(String::valueOf)
                    .orElse("-");
        }

        // Leaderboard entries: mtd_top_<stat>_name_1 / mtd_top_<stat>_value_1
        // mtd is not counted in the identifier because it is added after (mtd is not in String[])
        if (identifier.startsWith("top_")) {
            String[] parts = identifier.split("_");
            if (parts.length == 4) {
                String friendlyStat = parts[1];
                String dbStat = statAliasMap.getOrDefault(friendlyStat, friendlyStat);

                int index;
                try {
                    index = Integer.parseInt(parts[3]) - 1;
                }

                catch (NumberFormatException e) {
                    return "---";
                }

                List<LeaderboardManager.LeaderboardEntry> entries = leaderboard.getLeaderboard(dbStat);
                if (index >= entries.size()) return "---";

                LeaderboardManager.LeaderboardEntry entry = entries.get(index);

                // Handles top_stat_name, top: [0], stat: [1], name: [2]
                if (parts[2].equalsIgnoreCase("name")) {
                    return Optional.ofNullable(Bukkit.getOfflinePlayer(entry.uuid()).getName()).orElse("---");
                }

                if (parts[2].equalsIgnoreCase("value")) {
                    return formatStat(dbStat, entry.value());
                }
            }
        }

        return null;
    }

    // Leaderboard: Format Seconds to time format of xx:xx (minutes, seconds)
    private String formatStat(String stat, int value) {
        if (stat.equalsIgnoreCase("fastest_win_in_seconds")) {
            int minutes = value / 60;
            int seconds = value % 60;
            return String.format("%02d:%02d", minutes, seconds);
        }
        return formatNumber(value);
    }

    // Leaderboard: Format large numbers with commas every 10^3 (1000 -> 1,000)
    private String formatNumber(int number) {
        return NumberFormat.getNumberInstance(Locale.US).format(number);
    }
}
