package ksucapproj.blockstowerdefense1.logic.game_logic.Items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;


public enum CreateEgg {
    BASIC("Basic Tower", ChatColor.AQUA, "A simple tower with moderate damage and range", 500),
    FAST("Fast Tower", ChatColor.GREEN, "Rapid-fire tower with low damage but high attack speed", 300),
    SNIPER("Sniper Tower", ChatColor.RED, "Long-range tower with high damage but slow attack speed", 750),
    SPLASH("Splash Tower", ChatColor.YELLOW, "Area-of-effect tower that damages multiple enemies", 900),
    SLOW("Slow Tower", ChatColor.BLUE, "Tower that slows down enemies in its range", 450);

    private final String displayName;
    private final ChatColor color;
    private final String description;
    private final int price;

    CreateEgg(String displayName, ChatColor color, String description, int price) {
        this.displayName = displayName;
        this.color = color;
        this.description = description;
        this.price = price;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatColor getColor() {
        return color;
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

        ItemStack towerEgg = new ItemStack(Material.ZOMBIE_SPAWN_EGG, 5);
        ItemMeta meta = towerEgg.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(type.getColor() + type.getDisplayName());

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + type.getDescription());
            lore.add(ChatColor.YELLOW + "Cost: " + ChatColor.GOLD + type.getPrice() + " coins");
            meta.setLore(lore);

            towerEgg.setItemMeta(meta);
        }
        return towerEgg;
    }
}///

