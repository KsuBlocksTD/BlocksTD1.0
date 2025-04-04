package ksucapproj.blockstowerdefense1.logic.GUI;

import ksucapproj.blockstowerdefense1.logic.game_logic.PlayerSword;
import ksucapproj.blockstowerdefense1.logic.game_logic.PlayerUpgrades;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class UpgradeGUI implements Listener {


    public Map<Player, Inventory> openInventories = new HashMap<>();
    public Material swordMaterial;
    int swordLevel;


    public void openChestGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, Component.text("Upgrade Menu").color(TextColor.color(0, 255, 255)));

        this.swordLevel = PlayerUpgrades.getPlayerUpgradesMap().get(player).getSword().swordLevel;
        //debug
        //player.sendMessage(String.valueOf(swordLevel));


        switch (swordLevel) {
            case 0, 5 -> swordMaterial = Material.GOLDEN_SWORD;
            case 1 -> swordMaterial = Material.STONE_SWORD;
            case 2 -> swordMaterial = Material.IRON_SWORD;
            case 3 -> swordMaterial = Material.DIAMOND_SWORD;
            case 4 -> swordMaterial = Material.NETHERITE_SWORD;
        }

        // Adding placeholder items
        gui.setItem(10, createGuiItem(swordMaterial, Component.text("Upgrade Sword Level").color(TextColor.color(0, 255, 0))));
        gui.setItem(12, createGuiItem(Material.BLAZE_POWDER, Component.text("Upgrade Strength Level").color(TextColor.color(100, 255, 255))));
        gui.setItem(14, createGuiItem(Material.FEATHER, Component.text("Upgrade Speed Level").color(TextColor.color(155, 255, 155))));
        gui.setItem(16, createGuiItem(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, Component.text("Upgrade Sweeping edge Level").color(TextColor.color(255, 50, 50))));
        gui.setItem(19, createGuiItem(Material.SPIDER_EYE, Component.text("Upgrade Slowness Level").color(TextColor.color(5, 50, 250))));


        openInventories.put(player, gui);
        player.openInventory(gui);
    }


    private ItemStack createGuiItem(Material material, Component text) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(text);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void giveCompass(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Upgrade Menu").color(TextColor.color(0, 255, 255)));
            compass.setItemMeta(meta);
        }
        player.getInventory().addItem(compass);
        //player.sendMessage("Compass has been given!");
    }


}

