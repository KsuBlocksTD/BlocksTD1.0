package ksucapproj.blockstowerdefense1.logic.game_logic;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.ConfigOptions;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class PlayerSword {

    private int swordLevel, slownessLevel, sweepingEdgeLevel;
    private int swordUpgradesBought;

    private final Player player;
    private ItemStack playerSword;
    private ItemMeta swordMeta;
    private final String swordUUID;


    private static final PartiesAPI api = BlocksTowerDefense1.getApi();
    private static final ConfigOptions config = BlocksTowerDefense1.getInstance().getBTDConfig();

    // Currently, logic for handling the command of setting sword attributes is hard coded into
    // ApplyUpgradeCommand.java : 40

    public PlayerSword(Player player){

        // all sword attributes initialized before being applied to an upgrade mechanic
        this.player = player;
        this.slownessLevel = 0;
        this.sweepingEdgeLevel = 0;
        this.swordLevel = 1;
        this.swordUpgradesBought = 0;

        // sword attributes instantiated before being passed to createPlayerSword()
        this.playerSword = new ItemStack(Material.WOODEN_SWORD);
        this.swordMeta = playerSword.getItemMeta();
        this.swordUUID = createPlayerSword(playerSword, swordMeta);
    }




    public void applySwordMaterialUpgrade(){

        if (swordLevel < config.getSwordMaterialMaxLevel()){

            setSwordLevel(++swordLevel); // takes in the new material level and makes the new sword with it
            swordUpgradesBought += 1; // increments bought upgrades counter
        }
    }


    public void applySlownessUpgrade() {

        // Apply slowness to the mob (target)
        if (slownessLevel < config.getSlownessMaxLevel()){

            ++slownessLevel; // increase the level
            swordUpgradesBought += 1; // increments bought upgrades counter
        }

    }

    public void applySlownessEffect(LivingEntity target){

        if (config.getSlownessDurationIncreaseOnUpgrade()){ // enables longer duration on

            // applies slowness, 10 ticks + 2 per slownessLevel (0.5s), amplifier of player's slownessLevel
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, (config.getSlownessDuration() + getExtraSlownessDuration()), slownessLevel, false, true));
            return;
        }

        // applies slowness, 10 ticks (0.5s), amplifier of player's slownessLevel
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, config.getSlownessDuration(), slownessLevel, false, true));
    }

    public void applySweepingEdgeUpgrade(){

        if (sweepingEdgeLevel < config.getSweepingEdgeMaxLevel()){

            setSweepingEdgeLevel(++sweepingEdgeLevel); // increase the level
            swordUpgradesBought += 1;
        }
    }


    public int getSweepingEdgeLevel() {
        return sweepingEdgeLevel;
    }

    public int getSlownessLevel() {
        return slownessLevel;
    }

    public int getExtraSlownessDuration() {
        return (slownessLevel) * 2;
    }

    public int getSwordUpgradesBought() {
        return swordUpgradesBought;
    }

    public void setSwordLevel(int swordLevel) {

        // based upon the player's sword level, it initializes the newSwordMaterial as it
        Material newSwordMaterial = switch (swordLevel) {
            case 1 -> Material.WOODEN_SWORD;
            case 2 -> Material.STONE_SWORD;
            case 3 -> Material.IRON_SWORD;
            case 4 -> Material.DIAMOND_SWORD;
            case 5 -> Material.NETHERITE_SWORD;
            default -> Material.GOLDEN_SWORD;
        };

        swordMeta = playerSword.getItemMeta(); // stores sword meta-data before sword's deletion

        removeTrackedSword();

        playerSword = new ItemStack(newSwordMaterial);

        NamespacedKey key = new NamespacedKey(BlocksTowerDefense1.getInstance(), "sword_id");
        swordMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, swordUUID);
        playerSword.setItemMeta(swordMeta);

        player.getInventory().addItem(playerSword);
    }


    public void setSlownessLevel(int slownessLevel) {
        this.slownessLevel = slownessLevel;
    }


    // THIS IS WORKING PROPERLY
    // MODEL ALL SWORD UPGRADES OFF THIS
    public void setSweepingEdgeLevel(int sweepingEdgeLevel) {
        this.sweepingEdgeLevel = sweepingEdgeLevel; // sets PlayerSword sweepingEdgeLevel as parameter taken one

        swordMeta = playerSword.getItemMeta(); // stores playerSword's meta-data to be retrieved later

        if (swordMeta.hasConflictingEnchant(Enchantment.SWEEPING_EDGE)){ // if the sword has the enchant first
            swordMeta.removeEnchant(Enchantment.SWEEPING_EDGE);
        }

        swordMeta.addEnchant(Enchantment.SWEEPING_EDGE, sweepingEdgeLevel, true); // adds the enchant with the specified level
        playerSword.setItemMeta(swordMeta); // places the old sword's meta-data into the new sword's

        removeTrackedSword(); // deletes old sword

        player.getInventory().addItem(playerSword);
    }


    // This properly creates the sword with a trackable ID for deletion when being modified
    private String createPlayerSword(ItemStack playerSword, ItemMeta swordMeta){

        // Gives the sword the basic information and needs when first handed to the player
        swordMeta.displayName(Component.text(ChatColor.GOLD + "Tower Defense Sword")); // sword name-tag
        swordMeta.setUnbreakable(true); // makes the sword unbreakable
        swordMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE); // the sword will not show its unbreakable status
        swordMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

        // this is the key to later identify the sword by later
        NamespacedKey key = new NamespacedKey(BlocksTowerDefense1.getInstance(), "sword_id");
        NamespacedKey notDroppableKey = new NamespacedKey(BlocksTowerDefense1.getInstance(), "not_droppable");
        PersistentDataContainer data = swordMeta.getPersistentDataContainer();
        String swordUUID = player.getUniqueId() + "-" + UUID.randomUUID(); // creates a random UUID for the sword when created
        data.set(key, PersistentDataType.STRING, swordUUID); // stores the swordUUID into the sword's persistent meta-data
        data.set(notDroppableKey, PersistentDataType.BOOLEAN, false); // creates a key to disable the player from dropping the item

        playerSword.setItemMeta(swordMeta); // this applies all changes done to swordMeta to the playerSword

        player.getInventory().addItem(playerSword); // gives the player the sword

        return swordUUID; // used for initializing the UUID in the PlayerSword constructor
    }


    public void removeTrackedSword(){
        if (swordUUID == null) return; // if the player doesn't have a sword, it cannot be removed

        NamespacedKey key = new NamespacedKey(BlocksTowerDefense1.getInstance(), "sword_id");

        // variables created outside of scope to prevent unnecessary creation
        PersistentDataContainer data;
        String itemUUID;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !item.hasItemMeta()) continue; // if inv slots are empty or don't have meta-data they are skipped

            data = item.getItemMeta().getPersistentDataContainer();
            if (data.has(key, PersistentDataType.STRING)) {

                itemUUID = data.get(key, PersistentDataType.STRING); // if the item's itemUUID matches sword_id, it removes the item from inv

                if (swordUUID.equals(itemUUID)) {
                    player.getInventory().remove(item);
                    break; // break the loop after deleting the sword
                }
            }
        }
    }

}
