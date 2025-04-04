
// this is the code for setting ownership for a tower:
//towerEntity.setMetadata("owner", new FixedMetadataValue(plugin, owner.getUniqueId().toString()));
//zombie.setMetadata("attacker", new FixedMetadataValue(plugin, owner.getUniqueId().toString()));


package ksucapproj.blockstowerdefense1.logic.game_logic.towers;

import ksucapproj.blockstowerdefense1.logic.game_logic.Economy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class TowerFactory {
    public enum TowerType {
        // All the different towers, their cost and class
        BASIC(500, BasicTower.class),
        FAST(300, FastTower.class),
        SNIPER(750, SniperTower.class),
        SPLASH(900, SplashTower.class),
        SLOW(450, SlowTower.class);

        private final int cost;
        private final Class<? extends Tower> towerClass;

        TowerType(int cost, Class<? extends Tower> towerClass) {
            this.cost = cost;
            this.towerClass = towerClass;
        }

        public int getCost() {
            return cost;
        }

        public Class<? extends Tower> getTowerClass() {
            return towerClass;
        }
    }///

    public static void placeTower(TowerType towerType, Player player, Location placementLocation, String mapId, JavaPlugin plugin, ItemStack item) {
        // Check economy synchronously
        int coins = Economy.getPlayerMoney(player);

        if (coins >= towerType.getCost()) {


            // Use default location if placement location is null
            if (placementLocation == null) {
                player.sendRichMessage("<red>Invalid Tower location");
                return;
            }

            try {
                // Use reflection to create the tower instance
                towerType.getTowerClass().getConstructor(Location.class, Player.class, String.class, JavaPlugin.class).newInstance(placementLocation, player, mapId, plugin);

                // Deduct coins
                Economy.spendMoney(player, towerType.getCost());

                // Reduce item stack
                item.setAmount(item.getAmount() - 1);

                // Send success message
                player.sendRichMessage("<green><tower_type> Tower placed successfully!",
                        Placeholder.component("tower_type", Component.text(towerType.name()))
);


            } catch (Exception e) {
                player.sendRichMessage("<red>Error placing tower: <e_message>", Placeholder.component("e_message", Component.text(e.getMessage())));
                //e.printStackTrace();

            }
        } else {
            player.sendRichMessage("<red>You need at least <t_cost> coins to place a <t_type> Tower.", Placeholder.component("t_type", Component.text(towerType.name())), Placeholder.component("t_cost", Component.text(towerType.getCost())));

        }
    }
}
