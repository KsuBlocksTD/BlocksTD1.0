package ksucapproj.blockstowerdefense1.logic.game_logic.towers;

import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.logic.game_logic.Economy;
import ksucapproj.blockstowerdefense1.logic.game_logic.Items.CreateEgg;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class TowerEggPurchase {

    private static final JavaPlugin plugin = BlocksTowerDefense1.getInstance();

    // Allows eggs to be purchased instead of starting in player inventory
    public static boolean processPurchase(Player player, ItemStack clickedItem) {

        // Get the item meta
        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return false;  // Invalid item or no meta data
        }

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) {
            return false;
        }

        // Check persistent data container for tower type
        NamespacedKey key = new NamespacedKey(plugin, "tower_type");
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (!container.has(key, PersistentDataType.STRING)) {
            return false;  // No tower type data present
        }

        // Get the tower type as a string
        String typeName = container.get(key, PersistentDataType.STRING);
        CreateEgg towerType = CreateEgg.valueOf(typeName);

        // Check player coins and deduct if they can afford the egg
        int playerCoins = Economy.getPlayerMoney(player);
        int towerCost = towerType.getPrice();

        if (playerCoins < towerCost) {
            player.sendMessage("§cYou don't have enough coins to buy this tower!");
            return false;  // Not enough coins
        }

        // Deduct coins from the player
        Economy.spendMoney(player, towerCost);

        // Give the player the egg
        ItemStack towerEgg = towerType.createTowerEgg(CreateEgg.valueOf(typeName));
        player.getInventory().addItem(towerEgg);
        player.sendMessage("§aYou bought a " + towerType.getDisplayName() + " for " + towerCost + " coins!");

        return true;
    }


}
