package ksucapproj.blockstowerdefense1.logic.GUI;

import ksucapproj.blockstowerdefense1.logic.game_logic.towers.Tower;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Bukkit.createInventory;


public class TowerGUI {

    // Store tower entity ID with the GUI that's open for it
    public static final java.util.Map<UUID, Villager> openGUIs = new java.util.HashMap<>();

    // Cost configuration for upgrades
    public static final int RANGE_UPGRADE_COST = 1500;
    public static final int ATTACK_UPGRADE_COST = 2000;
    public static final int MAX_UPGRADE_LEVEL = 3;

    @Nullable
    private List<Component> createLore() {
        return new ArrayList<>();
    }

    public void openTowerGUI(Player player, Tower tower, Villager towerEntity) {
        // Create a GUI with upgrade options
        Inventory gui = createInventory(null, 27, Component.text("Tower Control"));

        // Add tower info at the top
        ItemStack towerInfo = createGuiItem(
                Material.BEACON,
                Component.text(tower.getTowerName()).color(TextColor.color(255, 255, 85)),
                Arrays.asList(
                        Component.text("Current Range: " + tower.getScanRadius()).color(TextColor.color(170, 170, 255)),
                        Component.text("Current Attack Speed: " + (20.0 / tower.getAttackInterval()) + "/s").color(TextColor.color(255, 170, 170))
                )
        );
        gui.setItem(4, towerInfo);

        // Range upgrade
        ItemStack rangeUpgrade = createUpgradeItem(
                Material.BOW,
                Component.text("Upgrade Range").color(TextColor.color(0, 255, 0)),
                tower.getRangeLevel(),
                RANGE_UPGRADE_COST,
                "range"
        );
        gui.setItem(11, rangeUpgrade);

        // Speed upgrade
        ItemStack speedUpgrade = createUpgradeItem(
                Material.CLOCK,
                Component.text("Upgrade Attack Speed").color(TextColor.color(0, 255, 0)),
                tower.getAttackLevel(),
                ATTACK_UPGRADE_COST,
                "speed"
        );
        gui.setItem(15, speedUpgrade);

        // Sell tower
        ItemStack sellTower = createGuiItem(
                Material.EMERALD,
                Component.text("Sell Tower").color(TextColor.color(255, 215, 0)),
                Arrays.asList(
                        Component.text("Sell Value: " + tower.getSellValue()).color(TextColor.color(0, 255, 0))
                )
        );
        gui.setItem(22, sellTower);

        // Store the association between this GUI and the tower entity
        openGUIs.put(player.getUniqueId(), towerEntity);

        // Open the GUI for the player
        player.openInventory(gui);
    }

    private ItemStack createUpgradeItem(Material material, Component text, double currentLevel, int cost, String upgradeType) {
        List<Component> lore = new ArrayList<>();

        if (currentLevel >= MAX_UPGRADE_LEVEL) {
            lore.add(Component.text("MAX LEVEL REACHED").color(TextColor.color(255, 215, 0)));
        } else {
            lore.add(Component.text("Current Level: " + currentLevel + "/" + MAX_UPGRADE_LEVEL).color(TextColor.color(170, 170, 255)));
            lore.add(Component.text("Upgrade Cost: " + cost).color(TextColor.color(255, 255, 85)));

            if (upgradeType.equals("range")) {
                lore.add(Component.text("+ 1.0 Range").color(TextColor.color(0, 255, 0)));
            } else if (upgradeType.equals("speed")) {
                lore.add(Component.text("+ 20% Attack Speed").color(TextColor.color(0, 255, 0)));
            }
        }

        return createGuiItem(material, text, lore);
    }

    private ItemStack createGuiItem(Material material, Component text, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(text);
            if (lore != null && !lore.isEmpty()) {
                meta.lore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
