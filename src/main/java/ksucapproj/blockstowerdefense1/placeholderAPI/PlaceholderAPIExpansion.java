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

import java.util.List;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final JavaPlugin plugin;
    private static final PartiesAPI api = BlocksTowerDefense1.getApi();
    public static final ConfigOptions config = BlocksTowerDefense1.getInstance().getBTDConfig();

    public PlaceholderAPIExpansion(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mtd"; // Placeholder identifier
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
        return true; // Keeps the placeholder expansion loaded
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        // Placeholders all in the format: mtd_<placeholder>
        // Exs: mtd_coins, mtd_speedMaxLevel
        if (identifier.equalsIgnoreCase("coins")) {

            if (Economy.getPlayerEconomies().get(player) == null){
                return "N/A";
            }
            return String.valueOf(Economy.getPlayerMoney(player)); // Retrieve coins
        }

        if (identifier.equalsIgnoreCase("speedMaxLevel")) {
            return String.valueOf(config.getSpeedMaxLevel());
        }

        if (identifier.equalsIgnoreCase("slownessMaxLevel")) {
            return String.valueOf(config.getSlownessMaxLevel());
        }

        if (identifier.equalsIgnoreCase("slownessDuration")) {
            return String.valueOf(config.getSlownessDuration());
        }

        if (identifier.equalsIgnoreCase("slownessDurationIncreaseOnUpgrade")) {
            return String.valueOf(config.getSlownessDurationIncreaseOnUpgrade());
        }

        if (identifier.equalsIgnoreCase("strengthMaxLevel")) {
            return String.valueOf(config.getStrengthMaxLevel());
        }

        if (identifier.equalsIgnoreCase("sweepingEdgeMaxLevel")) {
            return String.valueOf(config.getSweepingEdgeMaxLevel());
        }

        if (identifier.equalsIgnoreCase("swordMaterialMaxLevel")) {
            return String.valueOf(config.getSwordMaterialMaxLevel());
        }

        if (identifier.equalsIgnoreCase("")){
            return "";
        }

        LeaderboardManager leaderboard = BlocksTowerDefense1.getInstance().getLeaderboardManager();

        // Player's personal rank
        if (identifier.equalsIgnoreCase("top_spent_you")) {
            return leaderboard.getCoinsSpentBy(player.getUniqueId())
                    .map(String::valueOf)
                    .orElse("0");
        }

        // Match: top_spent_name_1, top_spent_value_1, etc.
        for (int i = 1; i <= 5; i++) {
            if (identifier.equalsIgnoreCase("top_spent_name_" + i)) {
                List<LeaderboardManager.TopSpender> list = leaderboard.getTopSpenders();
                if (list.size() >= i) {
                    return Bukkit.getOfflinePlayer(list.get(i - 1).uuid()).getName();
                } else return "---";
            }

            if (identifier.equalsIgnoreCase("top_spent_value_" + i)) {
                List<LeaderboardManager.TopSpender> list = leaderboard.getTopSpenders();
                if (list.size() >= i) {
                    return String.valueOf(list.get(i - 1).totalCoinsSpent());
                } else return "---";
            }
        }


        return null; // Placeholder not found
    }
}
