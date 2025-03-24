package ksucapproj.blockstowerdefense1.placeholderAPI;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.logic.Economy;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final JavaPlugin plugin;
    private static final PartiesAPI api = BlocksTowerDefense1.getApi();

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
            return Economy.getPlayerMoney(player); // Retrieve coins
        }

        if (identifier.equalsIgnoreCase("db_url")) {
            // Fetch the URL from the config
            return plugin.getConfig().getString("database.url");
        }

        return null; // Placeholder not found
    }
}
