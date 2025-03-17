package ksucapproj.blockstowerdefense1.maps;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;

public class MapData {
    public static Location getStartLocation(World world) {
        return new Location(world, 69.5, 64, -585.5); // Map1 coordinates
    }

    public static List<Location> getWaypoints(World world) {
        Location start = getStartLocation(world);
        return List.of(
                start.clone().add(0, 0, 23),  // +23 Z
                start.clone().add(8, 0, 23),   // +8 X
                start.clone().add(8, 0, 14),   // -9 Z
                start.clone().add(-8, 0, 14),  // -16 X
                start.clone().add(-8, 0, 8),   // -6 Z
                start.clone().add(-14, 0, 8)   // -6 X
        );
    }

    public static Location getEndLocation(World world) {
        return getWaypoints(world).get(getWaypoints(world).size() - 1);
    }
}