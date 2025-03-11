package me.matthewTest.pluginTest.placeholderAPI;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.matthewTest.pluginTest.logic.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final JavaPlugin plugin;

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

        return null; // Placeholder not found
    }
}
