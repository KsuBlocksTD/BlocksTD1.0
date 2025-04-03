package ksucapproj.blockstowerdefense1.maps;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapData {
    public MapData(Location startLocation, List<Location> waypoints) {
        this.startLocation = startLocation;
        this.waypoints = waypoints;
    }

    public static boolean mapExists(String mapId) {
        return maps.containsKey(mapId);
    }
    // Store maps with their associated waypoints
    private static final Map<String, MapDetails> maps = new HashMap<>();
    private static String defaultMap; // Default map to use
    private static File configFile;
    private static JavaPlugin plugin;

    // Map details class to store start location and waypoints

    private final Location startLocation;
    private final List<Location> waypoints;

    public static class MapDetails {
        private static Location startLocation = null;
        private final List<Location> waypoints;

        public MapDetails(Location startLocation, List<Location> waypoints) {
            this.startLocation = startLocation;
            this.waypoints = waypoints;
        }

        public static Location getStartLocationinternal() {
            return startLocation.clone();
        }

        public List<Location> getWaypoints() {
            // Return a deep copy of waypoints to prevent modifications
            List<Location> waypointsCopy = new ArrayList<>();
            for (Location waypoint : waypoints) {
                waypointsCopy.add(waypoint.clone());
            }
            return waypointsCopy;
        }

        public Location getEndLocation() {
            if (waypoints.isEmpty()) {
                return startLocation.clone();
            }
            return waypoints.get(waypoints.size() - 1).clone();
        }

        // Add a waypoint to this map
        public void addWaypoint(Location waypoint) {
            waypoints.add(waypoint.clone());
        }

        // Clear all waypoints
        public void clearWaypoints() {
            waypoints.clear();
        }
    }

    // Load maps from configuration file
    public static void loadMaps(JavaPlugin pluginInstance) {
        // Store plugin reference
        plugin = pluginInstance;

        // Clear existing maps
        maps.clear();

        // Create config if it doesn't exist
        configFile = new File(plugin.getDataFolder(), "maps.yml");
        if (!configFile.exists()) {
            plugin.saveResource("maps.yml", false);
        }

        // Load config
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Get default map
        defaultMap = config.getString("default-map", "map1");

        // Load all maps
        if (config.contains("maps")) {
            for (String mapId : config.getConfigurationSection("maps").getKeys(false)) {
                String mapPath = "maps." + mapId;

                // Process this map only if it has start section
                if (config.contains(mapPath + ".start")) {
                    // Get world for this map
                    String worldName = config.getString(mapPath + ".world", "world");
                    World world = plugin.getServer().getWorld(worldName);

                    if (world != null) {
                        // Load start location
                        double startX = config.getDouble(mapPath + ".start.x");
                        double startY = config.getDouble(mapPath + ".start.y");
                        double startZ = config.getDouble(mapPath + ".start.z");
                        float yaw = (float) config.getDouble(mapPath + ".start.yaw", 0);
                        float pitch = (float) config.getDouble(mapPath + ".start.pitch", 0);
                        Location startLoc = new Location(world, startX, startY, startZ, yaw, pitch);

                        // Load waypoints
                        List<Location> waypoints = new ArrayList<>();

                        // Check if waypoints section exists
                        if (config.contains(mapPath + ".waypoints")) {
                            for (String waypointKey : config.getConfigurationSection(mapPath + ".waypoints").getKeys(false)) {
                                String wpPath = mapPath + ".waypoints." + waypointKey;

                                // Load waypoint from config (absolute coordinates)
                                if (config.contains(wpPath + ".x") && config.contains(wpPath + ".y") && config.contains(wpPath + ".z")) {
                                    double wpX = config.getDouble(wpPath + ".x");
                                    double wpY = config.getDouble(wpPath + ".y");
                                    double wpZ = config.getDouble(wpPath + ".z");
                                    float wpYaw = (float) config.getDouble(wpPath + ".yaw", 0);
                                    float wpPitch = (float) config.getDouble(wpPath + ".pitch", 0);
                                    waypoints.add(new Location(world, wpX, wpY, wpZ, wpYaw, wpPitch));
                                }
                                // Load waypoint as relative offset to start
                                else if (config.contains(wpPath + ".offsetX") && config.contains(wpPath + ".offsetY") && config.contains(wpPath + ".offsetZ")) {
                                    double offsetX = config.getDouble(wpPath + ".offsetX");
                                    double offsetY = config.getDouble(wpPath + ".offsetY");
                                    double offsetZ = config.getDouble(wpPath + ".offsetZ");
                                    waypoints.add(startLoc.clone().add(offsetX, offsetY, offsetZ));
                                }
                            }
                        }

                        // Add map to our collection
                        maps.put(mapId, new MapDetails(startLoc, waypoints));
                    }
                }
            }
        }

    }

    // Save maps to configuration file
    public static void saveMaps() {
        if (configFile == null || plugin == null) {
            return;
        }

        // Create new configuration
        FileConfiguration config = new YamlConfiguration();

        // Set default map
        config.set("default-map", defaultMap);

        // Save all maps
        for (Map.Entry<String, MapDetails> entry : maps.entrySet()) {
            String mapId = entry.getKey();
            MapDetails mapDetails = entry.getValue();

            // Save start location
            Location start = mapDetails.getStartLocationinternal();
            config.set("maps." + mapId + ".world", start.getWorld().getName());
            config.set("maps." + mapId + ".start.x", start.getX());
            config.set("maps." + mapId + ".start.y", start.getY());
            config.set("maps." + mapId + ".start.z", start.getZ());
            config.set("maps." + mapId + ".start.yaw", start.getYaw());
            config.set("maps." + mapId + ".start.pitch", start.getPitch());

            // Save waypoints
            List<Location> waypoints = mapDetails.getWaypoints();
            for (int i = 0; i < waypoints.size(); i++) {
                Location wp = waypoints.get(i);
                String wpKey = "wp" + (i + 1);

                config.set("maps." + mapId + ".waypoints." + wpKey + ".x", wp.getX());
                config.set("maps." + mapId + ".waypoints." + wpKey + ".y", wp.getY());
                config.set("maps." + mapId + ".waypoints." + wpKey + ".z", wp.getZ());
                config.set("maps." + mapId + ".waypoints." + wpKey + ".yaw", wp.getYaw());
                config.set("maps." + mapId + ".waypoints." + wpKey + ".pitch", wp.getPitch());
            }
        }

        // Save configuration to file
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save maps configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Create a new map with the given ID and start location
    public static boolean createMap(String mapId, Location startLocation) {
        if (maps.containsKey(mapId)) {
            return false; // Map already exists
        }

        maps.put(mapId, new MapDetails(startLocation.clone(), new ArrayList<>()));
        saveMaps();
        return true;
    }

    // Delete a map by ID
    public static boolean deleteMap(String mapId) {
        if (!maps.containsKey(mapId) || mapId.equals(defaultMap)) {
            return false; // Map doesn't exist or is the default map
        }

        maps.remove(mapId);
        saveMaps();
        return true;
    }

    // Add a waypoint to a map
    public static boolean addWaypoint(String mapId, Location waypoint) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            return false; // Map doesn't exist
        }

        map.addWaypoint(waypoint.clone());
        saveMaps();
        return true;
    }

    // Clear all waypoints from a map
    public static boolean clearWaypoints(String mapId) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            return false; // Map doesn't exist
        }

        map.clearWaypoints();
        saveMaps();
        return true;
    }

    // Get available map IDs
    public static List<String> getAvailableMaps() {
        return new ArrayList<>(maps.keySet());
    }

    // Set the default map
    public static boolean setDefaultMap(String mapId) {
        if (!maps.containsKey(mapId)) {
            return false; // Map doesn't exist
        }

        defaultMap = mapId;
        saveMaps();
        return true;
    }

    // Get map details by ID
    public static MapDetails getMap(String mapId) {
        return maps.getOrDefault(mapId, maps.get(defaultMap));
    }

    // Get default map details
    public static MapDetails getDefaultMap() {
        return maps.get(defaultMap);
    }

    // Get the number of waypoints in a map
    public static int getWaypointCount(String mapId) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            return 0;
        }
        return map.getWaypoints().size();
    }


    public static Location getStartLocation(World world, String mapId) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            // If requested map doesn't exist, use default
            return MapDetails.getStartLocationinternal();
        }
        // Update the world of the location
        Location loc = MapDetails.getStartLocationinternal();
        loc.setWorld(world);
        return loc;
    }


    public static List<Location> getWaypoints(World world, String mapId) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            // If requested map doesn't exist, use default
            return getDefaultMap().getWaypoints();
        }

        // Update all waypoint worlds
        List<Location> waypoints = map.getWaypoints();
        for (Location waypoint : waypoints) {
            waypoint.setWorld(world);
        }

        return waypoints;
    }


    public static Location getEndLocation(World world, String mapId) {
        MapDetails map = maps.get(mapId);
        if (map == null) {
            // If requested map doesn't exist, use default
            return getDefaultMap().getEndLocation();
        }

        // Get the end location and update its world
        Location endLoc = map.getEndLocation();
        endLoc.setWorld(world);
        return endLoc;
    }
}