package ksucapproj.blockstowerdefense1.logic.game_logic;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.ConfigOptions;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerSword {

    private int swordLevel, slownessLevel, sweepingEdgeLevel;
    private final Player player;
    private ItemStack playerSword;
    private ItemMeta swordMeta;


    private static final PartiesAPI api = BlocksTowerDefense1.getApi();
    private static final ConfigOptions config = BlocksTowerDefense1.getInstance().getBTDConfig();


    public PlayerSword(Player player){

        this.player = player;
        this.slownessLevel = 0;
        this.sweepingEdgeLevel = 0;
        this.swordLevel = 1;
        this.playerSword = new ItemStack(Material.WOODEN_SWORD);
        this.swordMeta = playerSword.getItemMeta();

        swordMeta.displayName(Component.text(ChatColor.GOLD + "Tower Defense Sword"));
        swordMeta.setUnbreakable(true);
        swordMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        playerSword.setItemMeta(swordMeta);
    }







    public static void applySwordMaterialUpgrade(PlayerSword upgrades){

        if (upgrades.swordLevel < config.getSwordMaterialMaxLevel()){

            upgrades.setSwordLevel(upgrades.swordLevel++);
        }
    }



    public static void applySlownessUpgrade(PlayerSword upgrades) {

        // Apply slowness to the mob (target)
        if (upgrades.slownessLevel < config.getSlownessMaxLevel()){

            upgrades.slownessLevel++; // increase the level

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

    public void applySweepingEdgeUpgrade(PlayerSword upgrades){

        if (upgrades.sweepingEdgeLevel < config.getSweepingEdgeMaxLevel()){

            upgrades.setSweepingEdgeLevel(upgrades.sweepingEdgeLevel++); // increase the level


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

    public void setSwordLevel(int swordLevel) {

        Material newSwordMaterial = switch (swordLevel) {
            case 1 -> Material.WOODEN_SWORD;
            case 2 -> Material.STONE_SWORD;
            case 3 -> Material.IRON_SWORD;
            case 4 -> Material.DIAMOND_SWORD;
            case 5 -> Material.NETHERITE_SWORD;
            default -> Material.GOLDEN_SWORD;
        };

        swordMeta = playerSword.getItemMeta();

        player.getInventory().removeItem(player.getItemInHand());
        playerSword = new ItemStack(newSwordMaterial);
        playerSword.setItemMeta(swordMeta);
        player.getInventory().addItem(playerSword);
    }


    public void setSlownessLevel(int slownessLevel) {
        this.slownessLevel = slownessLevel;
    }


    // THIS IS WORKING PROPERLY
    // MODEL ALL SWORD UPGRADES OFF THIS
    public void setSweepingEdgeLevel(int sweepingEdgeLevel) {
        this.sweepingEdgeLevel = sweepingEdgeLevel;

        swordMeta = playerSword.getItemMeta();

        if (swordMeta.hasConflictingEnchant(Enchantment.SWEEPING_EDGE)){
            swordMeta.removeEnchant(Enchantment.SWEEPING_EDGE);
        }

        swordMeta.addEnchant(Enchantment.SWEEPING_EDGE, sweepingEdgeLevel, true);
        playerSword.setItemMeta(swordMeta);

        player.getInventory().removeItem(player.getItemInHand());
        player.getInventory().addItem(playerSword);
    }

}
