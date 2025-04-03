package ksucapproj.blockstowerdefense1.logic.GUI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.HashMap;
import java.util.Map;

public class UpgradeGUI implements Listener {

    public Map<Player, Inventory> openInventories = new HashMap<>();

    public void openChestGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.BLUE + "Upgrade GUI");

        // Adding placeholder items
        gui.setItem(11, createGuiItem(Material.DIAMOND, ChatColor.GREEN + "Click for Diamonds!"));
        gui.setItem(13, createGuiItem(Material.GOLD_INGOT, ChatColor.YELLOW + "Click for Gold!"));
        gui.setItem(15, createGuiItem(Material.EMERALD, ChatColor.DARK_GREEN + "Click for Emeralds!"));

        openInventories.put(player, gui);
        player.openInventory(gui);
    }

    private ItemStack createGuiItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void giveCompass(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "Upgrade Menu");
            compass.setItemMeta(meta);
        }
        player.getInventory().addItem(compass);
        player.sendMessage("Compass has been given!");
    }


}

