package ksucapproj.blockstowerdefense1.placeholderAPI;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.ConfigOptions;
import ksucapproj.blockstowerdefense1.logic.game_logic.Economy;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final JavaPlugin plugin;
    private static final PartiesAPI api = BlocksTowerDefense1.getApi();
    private static final ConfigOptions config = BlocksTowerDefense1.getInstance().getBTDConfig();

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

        // Define your custom placeholder
        if (identifier.equalsIgnoreCase("coins")) {

            if (Economy.getPlayerEconomies().get(player) == null){
                return "N/A";
            }
            return String.valueOf(Economy.getPlayerMoney(player)); // Retrieve coins
        }

        if (identifier.equalsIgnoreCase("db_Url")) {
            // Fetch the URL from the config
            return plugin.getConfig().getString("database.url");
        }

        if (identifier.equalsIgnoreCase("speedMaxLevel")) {
            // Fetch the URL from the config
            return String.valueOf(config.getSpeedMaxLevel());
        }

        if (identifier.equalsIgnoreCase("slownessMaxLevel")) {
            // Fetch the URL from the config
            return String.valueOf(config.getSlownessMaxLevel());
        }

        if (identifier.equalsIgnoreCase("slownessDuration")) {
            // Fetch the URL from the config
            return String.valueOf(config.getSlownessDuration());
        }

        if (identifier.equalsIgnoreCase("slownessDurationIncreaseOnUpgrade")) {
            // Fetch the URL from the config
            return String.valueOf(config.getSlownessDurationIncreaseOnUpgrade());
        }

        if (identifier.equalsIgnoreCase("strengthMaxLevel")) {
            // Fetch the URL from the config
            return String.valueOf(config.getStrengthMaxLevel());
        }

        if (identifier.equalsIgnoreCase("sweepingEdgeMaxLevel")) {
            // Fetch the URL from the config
            return String.valueOf(config.getSweepingEdgeMaxLevel());
        }

        if (identifier.equalsIgnoreCase("swordMaterialMaxLevel")) {
            // Fetch the URL from the config
            return String.valueOf(config.getSwordMaterialMaxLevel());
        }


        return null; // Placeholder not found
    }
}
