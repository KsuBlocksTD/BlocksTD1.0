package ksucapproj.blockstowerdefense1.logic.game_logic;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;
import ksucapproj.blockstowerdefense1.ConfigOptions;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerSword {

    private int swordLevel;
    private final Player player;
    private ItemStack playerSword;
    private ItemMeta swordMeta;


    private static final PartiesAPI api = BlocksTowerDefense1.getApi();
    private static final ConfigOptions config = new ConfigOptions(BlocksTowerDefense1.getInstance());


    public PlayerSword(Player player){

        this.player = player;
        this.swordLevel = 1;
        this.playerSword = new ItemStack(Material.WOODEN_SWORD);
        this.swordMeta = playerSword.getItemMeta();

        swordMeta.displayName(Component.text(ChatColor.GOLD + "Tower Defense Sword"));
        swordMeta.setUnbreakable(true);
        swordMeta.getEnchants();
        swordMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
    }







    public static void applySwordMaterialUpgrade(PlayerSword upgrades){

        if (upgrades.swordLevel < config.getSwordMaxLevel()){

            upgrades.setSwordLevel(upgrades.swordLevel++);
        }
    }


    public ItemStack getPlayerSword() {
        return playerSword;
    }



    public void setSwordLevel(int swordLevel) {
        ItemMeta storedSwordMeta = swordMeta;

        Material newSwordMaterial = switch (swordLevel) {
            case 1 -> Material.WOODEN_SWORD;
            case 2 -> Material.STONE_SWORD;
            case 3 -> Material.IRON_SWORD;
            case 4 -> Material.DIAMOND_SWORD;
            case 5 -> Material.NETHERITE_SWORD;
            default -> Material.GOLDEN_SWORD;
        };

        player.getInventory().removeItem(playerSword);
        playerSword = new ItemStack(newSwordMaterial);
        playerSword.setItemMeta(storedSwordMeta);
    }
}
