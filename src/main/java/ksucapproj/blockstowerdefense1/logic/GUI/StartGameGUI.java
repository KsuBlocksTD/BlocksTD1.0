package ksucapproj.blockstowerdefense1.logic.GUI;

import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.maps.MapData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class StartGameGUI {
    JavaPlugin plugin = BlocksTowerDefense1.getInstance();
    private static final NamespacedKey COMPASS_KEY = new NamespacedKey(BlocksTowerDefense1.getInstance(), "map_selector");
    private static final NamespacedKey mapKey = new NamespacedKey(BlocksTowerDefense1.getInstance(), "map_name");


        public static boolean hasCompass(Player player) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item == null || item.getType() != org.bukkit.Material.COMPASS) continue;

                ItemMeta meta = item.getItemMeta();
                if (meta == null) continue;

                PersistentDataContainer container = meta.getPersistentDataContainer();
                if (container.has(COMPASS_KEY, PersistentDataType.BYTE)) {
                    return true;
                }
            }
            return false;
        }


    public static void giveMapSelectorCompass(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Map Selector").color(TextColor.color(0, 255, 255)));


            meta.getPersistentDataContainer().set(COMPASS_KEY, PersistentDataType.BYTE, (byte) 1);

            meta.lore(List.of(Component.text("Right-click to choose your map.")));
            compass.setItemMeta(meta);
        }

        player.getInventory().addItem(compass);
    }

    public static void startGameGUI(Player player) {
        List<String> mapNames = MapData.getAvailableMaps(); // <-- replace with your actual class

        int size = ((mapNames.size() - 1) / 9 + 1) * 9; // closest multiple of 9
        Inventory gui = Bukkit.createInventory(null, size, Component.text("Select a Map").color(TextColor.color(100, 255, 100)));

        for (int i = 0; i < mapNames.size(); i++) {
            String mapName = mapNames.get(i);

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.displayName(Component.text(mapName).color(TextColor.color(255, 255, 50)));
                meta.getPersistentDataContainer().set(mapKey, PersistentDataType.STRING, mapName);
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Click to select this map"));
                meta.lore(lore);
                item.setItemMeta(meta);
            }

            gui.setItem(i, item);
        }

        player.openInventory(gui);
    }
}
