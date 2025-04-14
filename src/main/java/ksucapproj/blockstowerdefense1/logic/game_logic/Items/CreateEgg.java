package ksucapproj.blockstowerdefense1.logic.game_logic.Items;

import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.ConfigOptions;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static ksucapproj.blockstowerdefense1.placeholderAPI.PlaceholderAPIExpansion.config;


public enum CreateEgg {
    BASIC("Wizard Tower", "A lightning tower with moderate damage and range", config.getBasicTowerCost()),
    FAST("Fast Tower",  "Rapid-fire tower with low damage but high attack speed", config.getFastTowerCost()),
    SNIPER("Sniper Tower", "Long-range tower with high damage but slow attack speed", config.getSniperTowerCost()),
    SPLASH("Splash Tower", "Area-of-effect tower that damages multiple enemies", config.getSplashTowerCost()),
    SLOW("Slow Tower", "Tower that slows down enemies in its range", config.getSlowTowerCost());

    private final String displayName;
    private final String description;
    private final int price;
    private static final JavaPlugin plugin = BlocksTowerDefense1.getInstance();


    CreateEgg(String displayName, String description, int price) {
        this.displayName = displayName;
        this.description = description;
        this.price = price;
    }

    public String getDisplayName() {
        return displayName;
    }


    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }



    public ItemStack createTowerEgg(CreateEgg type) {
        if (type == null) {
            return null;
        }

        ItemStack towerEgg = new ItemStack(Material.ZOMBIE_SPAWN_EGG, 1);
        ItemMeta meta = towerEgg.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(type.getDisplayName()));

            @Nullable List<Component> lore = new ArrayList<>();
            lore.add(Component.text(type.getDescription()));
            lore.add(Component.text("Cost: "  + type.getPrice() + " coins"));
            // Correct key with plugin
            NamespacedKey key = new NamespacedKey(plugin, "tower_type");

            // Store enum name (e.g., BASIC, SNIPER, etc.)
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, this.name());
            meta.lore(lore);

            towerEgg.setItemMeta(meta);
        }
        return towerEgg;
    }
}

