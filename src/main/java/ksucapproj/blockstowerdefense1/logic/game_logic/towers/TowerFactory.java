
// this is the code for setting ownership for a tower:
//towerEntity.setMetadata("owner", new FixedMetadataValue(plugin, owner.getUniqueId().toString()));
//zombie.setMetadata("attacker", new FixedMetadataValue(plugin, owner.getUniqueId().toString()));


package ksucapproj.blockstowerdefense1.logic.game_logic.towers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

public class TowerFactory {
    public enum TowerType {
        // All the different towers, their cost and class
        BASIC(BasicTower.class),
        FAST(FastTower.class),
        SNIPER(SniperTower.class),
        SPLASH(SplashTower.class),
        SLOW(SlowTower.class);


        private final Class<? extends Tower> towerClass;

        TowerType(Class<? extends Tower> towerClass) {
            this.towerClass = towerClass;
        }

        public Class<? extends Tower> getTowerClass() {
            return towerClass;
        }
    }

    // Used to make sure you cant spam towers all on one block
    public static boolean nearbyTower(Location placementLocation, double radius) {
        if(placementLocation == null) {return false;} // Handle null
        Collection<Villager> entities = placementLocation.getNearbyEntitiesByType(Villager.class, radius);
        try {return !entities.isEmpty();} // If there's a nearby tower return false
        catch (Exception e) {
            return false;
        }

    }


    public static void placeTower(TowerType towerType, Player player, Location placementLocation, String mapId, JavaPlugin plugin, ItemStack item) {
            boolean towerNearby = nearbyTower(placementLocation, .5);

             // Make sure you cant stack towers
            if (towerNearby) {
                player.sendRichMessage("<red>Invalid Tower location");
                return;
            }

            try {
                // Use reflection to create the tower instance
                towerType.getTowerClass().getConstructor(Location.class, Player.class, String.class, JavaPlugin.class).newInstance(placementLocation, player, mapId, plugin);

                // Remove egg used
                item.setAmount(item.getAmount() - 1);

                // Send success message
                player.sendRichMessage("<green><tower_type> Tower placed successfully!",
                        Placeholder.component("tower_type", Component.text(towerType.name())));


            } catch (Exception e) {
                player.sendRichMessage("<red>Invalid Tower location exception");
                //e.printStackTrace(); - just clogs the server logs
            }

    }
}
