package ksucapproj.blockstowerdefense1.logic.game_logic.Items;

import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;



public class GlowingTotem {

    private static final JavaPlugin plugin = BlocksTowerDefense1.getInstance();
    private static final HashMap<UUID, ItemStack> totemTracker = new HashMap<>();
    private static final NamespacedKey NUMBER_KEY = new NamespacedKey(plugin, "rounds_left");
    private static final NamespacedKey notDroppableKey = new NamespacedKey(BlocksTowerDefense1.getInstance(), "not_droppable");


    // This class will create a totem of glowing, and set the meta for it including how many rounds the totem has left
    public static ItemStack createGTotem(UUID playerUUID) {
        int initRounds = 5;
        ItemStack gTotem = new ItemStack(Material.LIGHTNING_ROD, 1);
        ItemMeta meta = gTotem.getItemMeta();
        meta.displayName(Component.text("Totem of Glowing").color(TextColor.color(200, 176, 25)));

        meta.addEnchant(Enchantment.AQUA_AFFINITY, 1, true);
        // Hide the enchantment from the tooltip
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(NUMBER_KEY, PersistentDataType.INTEGER, initRounds);
        container.set(notDroppableKey, PersistentDataType.BOOLEAN, false); // creates a key to disable the player from dropping the item


        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("This totem has " + initRounds +" rounds left").color(TextColor.color(180, 180, 180)));

        meta.lore(lore);

        totemTracker.put(playerUUID, gTotem);

        gTotem.setItemMeta(meta);
        return gTotem;
    }

    public static void reduceRoundsLeft(Player player) {
        UUID playerUUID = player.getUniqueId();
        Inventory inventory = player.getInventory();

        if(!inventory.contains(totemTracker.get(playerUUID))) {
            //debug
            //player.sendRichMessage("no totem contained in inventory");
            return;
        }
            ItemStack oldItem = totemTracker.get(playerUUID);
            ItemMeta meta = oldItem.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            int value = container.get(NUMBER_KEY, PersistentDataType.INTEGER);
            if(value == 1) {
                inventory.remove(oldItem);
            }
            else {
                inventory.remove(oldItem);
                int newValue = value - 1;
                container.set(NUMBER_KEY, PersistentDataType.INTEGER, newValue);
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("This totem has " + newValue +" rounds left"));

                meta.lore(lore);
                oldItem.setItemMeta(meta);
                inventory.addItem(oldItem);
            }
    }

    public static boolean hasGlowingTotem(Player player) {
        UUID playerUUID = player.getUniqueId();
        Inventory inventory = player.getInventory();
        if(inventory.contains(totemTracker.get(playerUUID))) {
            return true;
        }
        else return false;
    }
}
