package ksucapproj.blockstowerdefense1.logic.GUI;

import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.ConfigOptions;
import ksucapproj.blockstowerdefense1.logic.game_logic.Items.CreateEgg;
import ksucapproj.blockstowerdefense1.logic.game_logic.PlayerSword;
import ksucapproj.blockstowerdefense1.logic.game_logic.PlayerUpgrades;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ObjectInputFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;




public class UpgradeGUI implements Listener {


    public Map<Player, Inventory> openInventories = new HashMap<>();
    public Material swordMaterial;
    int swordLevel;
    private static final ConfigOptions config = BlocksTowerDefense1.getInstance().getBTDConfig();
    @Nullable List<Component> lore = new ArrayList<>();


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
        gui.setItem(1, createGuiItem(swordMaterial, Component.text("Upgrade Sword Level").color(TextColor.color(0, 255, 0)), player));
        gui.setItem(3, createGuiItem(Material.BLAZE_POWDER, Component.text("Upgrade Strength Level").color(TextColor.color(100, 255, 255)), player));
        gui.setItem(5, createGuiItem(Material.FEATHER, Component.text("Upgrade Speed Level").color(TextColor.color(155, 255, 155)), player));
        gui.setItem(7, createGuiItem(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, Component.text("Upgrade Sweeping edge Level").color(TextColor.color(255, 50, 50)), player));
        gui.setItem(10, createGuiItem(Material.SPIDER_EYE, Component.text("Upgrade Slowness Level").color(TextColor.color(5, 50, 250)), player));
        gui.setItem(12, CreateEgg.BASIC.createTowerEgg(CreateEgg.BASIC));
        gui.setItem(14, CreateEgg.SPLASH.createTowerEgg(CreateEgg.SPLASH));
        gui.setItem(16, CreateEgg.SLOW.createTowerEgg(CreateEgg.SLOW));
        gui.setItem(21, CreateEgg.SNIPER.createTowerEgg(CreateEgg.SNIPER));
        gui.setItem(23, CreateEgg.FAST.createTowerEgg(CreateEgg.FAST));

        openInventories.put(player, gui);
        player.openInventory(gui);
    }


    private ItemStack createGuiItem(Material material, Component text, Player player) {
        lore.clear();
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(text);
            if(material == swordMaterial) {
                lore.add(Component.text("Cost: " + PlayerUpgrades.getPlayerUpgradesMap().get(player).getSword().getSwordLevel() * config.getSwordMaterialBaseCost()));
                meta.lore(lore);
            }
            if(material == Material.BLAZE_POWDER) {
                lore.add(Component.text("Cost: " + (PlayerUpgrades.getPlayerUpgradesMap().get(player).getStrengthLevel() +1)* config.getStrengthBaseCost()));
                meta.lore(lore);
            }
            if(material == Material.FEATHER) {
                lore.add(Component.text("Cost: " + (PlayerUpgrades.getPlayerUpgradesMap().get(player).getSwiftnessLevel() +1)* config.getSpeedBaseCost()));
                meta.lore(lore);
            }
            if(material == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE) {
                lore.add(Component.text("Cost: " + (PlayerUpgrades.getPlayerUpgradesMap().get(player).getSword().getSweepingEdgeLevel() +1)* config.getSweepingEdgeBaseCost()));
                meta.lore(lore);
            }
            if(material == Material.SPIDER_EYE) {
                lore.add(Component.text("Cost: " + (PlayerUpgrades.getPlayerUpgradesMap().get(player).getSword().getSlownessLevel() +1)* config.getSlownessBaseCost()));
                meta.lore(lore);
            }
            item.setItemMeta(meta);

        }
        return item;
    }

    public static void giveCompass(Player player) {
        @Nullable List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Use this to upgrade your power or buy towers!"));
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Upgrade Menu").color(TextColor.color(0, 255, 255)));
            meta.lore(lore);
            compass.setItemMeta(meta);
        }
        player.getInventory().addItem(compass);
        //player.sendMessage("Compass has been given!");
    }


}

